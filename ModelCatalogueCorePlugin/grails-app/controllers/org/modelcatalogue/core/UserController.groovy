package org.modelcatalogue.core

import grails.converters.JSON
import org.modelcatalogue.core.security.User
import org.modelcatalogue.core.util.ClassificationFilter
import org.modelcatalogue.core.util.OrderedMap
import org.modelcatalogue.core.util.RelationshipDirection
import org.modelcatalogue.core.util.marshalling.CatalogueElementMarshaller
import org.modelcatalogue.core.util.marshalling.RelationshipsMarshaller

import javax.servlet.http.HttpServletResponse

class UserController extends AbstractCatalogueElementController<User> {

    UserController() {
        super(User, false)
    }

    @Override
    protected String getRoleForSaveAndEdit() { "ADMIN" }

    def classifications() {
        if (!modelCatalogueSecurityService.isUserLoggedIn()) {
            notAuthorized()
            return
        }

        ClassificationFilter.from(request.JSON).to(modelCatalogueSecurityService.currentUser)

        redirect controller: 'user', action: 'current'
    }

    def current() {
        if (!modelCatalogueSecurityService.currentUser) {
            render([success: false, username: null, roles: [], id: null, classifications: []] as JSON)
            return
        }

        ClassificationFilter filter = ClassificationFilter.from(modelCatalogueSecurityService.currentUser)

        render([
                success: true,
                username: modelCatalogueSecurityService.currentUser.username,
                roles: modelCatalogueSecurityService.currentUser.authorities*.authority,
                id: modelCatalogueSecurityService.currentUser.hasProperty('id') ? modelCatalogueSecurityService.currentUser.id : null,
                classifications: filter.toMap()
        ] as JSON)
    }

    def lastSeen() {
        if (!modelCatalogueSecurityService.hasRole('ADMIN')) {
            notFound()
            return
        }
        respond modelCatalogueSecurityService.usersLastSeen.sort { it.value }.collect { [username: it.key, lastSeen: new Date(it.value)] }.reverse()
    }


	@Override
	def addOutgoing(Long id, String type) {
		if (type == "favourite" && modelCatalogueSecurityService.hasRole('VIEWER'))
			addRelationForFavourite(id,type,true)
		else
			addRelation(id, type, true)
	}

	@Override
	def removeOutgoing(Long id, String type) {
		if (type == "favourite" && modelCatalogueSecurityService.hasRole('VIEWER'))
			removeRelationForFavourite(id,type,true)
		else
			removeRelation(id, type, true)
	}


	private void removeRelationForFavourite(Long id, String type, boolean outgoing) {
		withRetryingTransaction {
			if (!modelCatalogueSecurityService.hasRole('VIEWER')) {
				notAuthorized()
				return
			}

			def otherSide = parseOtherSide()

			CatalogueElement source = resource.get(id)
			if (!source) {
				notFound()
				return
			}
			RelationshipType relationshipType = RelationshipType.readByName(otherSide.type ? otherSide.type.name : type)
			if (!relationshipType) {
				notFound()
				return

			}
			Class otherSideType
			try {
				otherSideType = Class.forName (otherSide.relation ? otherSide.relation.elementType : otherSide.elementType)
			} catch (ClassNotFoundException ignored) {
				notFound()
				return
			}

			CatalogueElement destination = otherSideType.get(otherSide.relation ? otherSide.relation.id : otherSide.id )
			if (!destination) {
				notFound()
				return
			}

			Classification classification = otherSide.classification ? Classification.get(otherSide.classification.id) : null

			Relationship old = outgoing ?  relationshipService.unlink(source, destination, relationshipType, classification) :  relationshipService.unlink(destination, source, relationshipType, classification)
			if (!old) {
				notFound()
				return
			}

			if (old.hasErrors()) {
				respond old.errors
				return
			}

			response.status = HttpServletResponse.SC_NO_CONTENT
			render "DELETED"
		}
	}

	private void addRelationForFavourite(Long id, String type, boolean outgoing) {
		withRetryingTransaction {
			if (!modelCatalogueSecurityService.hasRole('VIEWER')) {
				notAuthorized()
				return
			}

			def otherSide = parseOtherSide()

			CatalogueElement source = resource.get(id)
			if (!source) {
				notFound()
				return
			}
			RelationshipType relationshipType = RelationshipType.readByName(type)
			if (!relationshipType) {
				notFound()
				return

			}

			def newClassification = objectToBind['__classification']
			Long classificationId = newClassification instanceof Map ? newClassification.id : null

			Classification classification = classificationId ? Classification.get(classificationId) : null


			def oldClassification = objectToBind['__oldClassification']
			Long oldClassificationId = oldClassification instanceof Map ? oldClassification.id : null

			Classification oldClassificationInstance = oldClassificationId ? Classification.get(oldClassificationId) : null

			if (classificationId && !classification) {
				notFound()
				return
			}

			Class otherSideType
			try {
				otherSideType = Class.forName otherSide.elementType
			} catch (ClassNotFoundException ignored) {
				notFound()
				return
			}

			CatalogueElement destination = otherSideType.get(otherSide.id)
			if (!destination) {
				notFound()
				return
			}

			if (oldClassificationInstance != classification) {
				if (outgoing) {
					relationshipService.unlink(source, destination, relationshipType, oldClassificationInstance)
				} else {
					relationshipService.unlink(destination, source, relationshipType, oldClassificationInstance)
				}
			}

			RelationshipDefinitionBuilder definition = outgoing ? RelationshipDefinition.create(source, destination, relationshipType) : RelationshipDefinition.create(destination, source, relationshipType)

			definition.withClassification(classification).withMetadata(OrderedMap.fromJsonMap(objectToBind.metadata ?: [:]))

			Relationship rel = relationshipService.link(definition.definition)

			if (rel.hasErrors()) {
				respond rel.errors
				return
			}

			response.status = HttpServletResponse.SC_CREATED
			RelationshipDirection direction = outgoing ? RelationshipDirection.OUTGOING : RelationshipDirection.INCOMING

			respond(id: rel.id, type: rel.relationshipType, ext: OrderedMap.toJsonMap(rel.ext), element: CatalogueElementMarshaller.minimalCatalogueElementJSON(direction.getElement(source, rel)), relation: direction.getRelation(source, rel), direction: direction.getDirection(source, rel), removeLink: RelationshipsMarshaller.getDeleteLink(source, rel), archived: rel.archived, elementType: Relationship.name, classification: rel.classification)
		}
	}


}
