package org.modelcatalogue.core.security

class UserAdminController extends grails.plugin.springsecurity.ui.UserController {

    def create(){
        super.create()
    }
    def save(){
		//update name(which is inherited from CatalogueElement) in User Class and fill it with username
		params['name'] = params?.username;
        super.save()
    }

	def update() {
		//update name(which is inherited from CatalogueElement) in User Class and fill it with username
		params['name'] = params?.username;
		super.update()
	}
}
