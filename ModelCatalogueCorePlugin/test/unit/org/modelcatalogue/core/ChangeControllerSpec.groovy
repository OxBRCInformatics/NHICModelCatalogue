package org.modelcatalogue.core

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.modelcatalogue.core.audit.Change
import spock.lang.Specification
import spock.lang.Unroll
import static org.springframework.http.HttpStatus.UNAUTHORIZED

/**
 *
 */
@TestFor(ChangeController)
@Mock(Change)
class ChangeControllerSpec extends Specification {

	def setup(){
		controller.modelCatalogueSecurityService = Mock(ModelCatalogueSecurityService)
	}
	@Unroll
	void "Controller action (#actionName) is accessible just to Admin role"() {

		when:
		controller."${actionName}"()

		then:
		1 * controller.modelCatalogueSecurityService.hasRole("ADMIN") >> {return false}
		response.status == UNAUTHORIZED.value()

		where:
		actionName << ["undo","changes","global","classificationActivity","userActivity"]
	}
}