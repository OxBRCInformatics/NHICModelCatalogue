/** Generated automatically from model. Do not edit this file manually! */
    (function (window) {
        window['fixtures'] = window['fixtures'] || {};
        var fixtures = window['fixtures']
        fixtures['model'] = fixtures['model'] || {};
        var model = fixtures['model']

        window.fixtures.model.incoming5 = {
   "total": 11,
   "previous": "/model/33/incoming/relationship?max=10&offset=0",
   "page": 10,
   "itemType": "org.modelcatalogue.core.Relationship",
   "listType": "org.modelcatalogue.core.util.Relationships",
   "next": "",
   "list": [{
      "id": 4527,
      "direction": "destinationToSource",
      "removeLink": "/model/33/incoming/relationship",
      "relation": {
         "outgoingRelationships": {
            "count": 1,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/model/72/outgoing"
         },
         "status": "DRAFT",
         "link": "/model/72",
         "hasContextOf": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/model/72/incoming/context"
         },
         "childOf": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/model/72/incoming/hierarchy"
         },
         "contains": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/model/72/outgoing/containment"
         },
         "ext": {},
         "elementType": "org.modelcatalogue.core.Model",
         "incomingRelationships": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/model/72/incoming"
         },
         "version": 1,
         "id": 72,
         "description": "this is a second chapter for a book",
         "name": "chapter2",
         "elementTypeName": "Model",
         "parentOf": {
            "count": 0,
            "itemType": "org.modelcatalogue.core.Relationship",
            "link": "/model/72/outgoing/hierarchy"
         },
         "versionNumber": 0.1
      },
      "type": {
         "id": 3,
         "sourceClass": "org.modelcatalogue.core.CatalogueElement",
         "sourceToDestination": "relates to",
         "destinationClass": "org.modelcatalogue.core.CatalogueElement",
         "name": "relationship",
         "link": "/relationshipType/3",
         "elementTypeName": "Relationship Type",
         "elementType": "org.modelcatalogue.core.RelationshipType",
         "destinationToSource": "is relationship of",
         "version": 0
      }
   }],
   "offset": 10,
   "success": true,
   "size": 1
}

    })(window)