package org.modelcatalogue.core.security

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import grails.plugin.springsecurity.ui.RegistrationCode

class RegisterController extends grails.plugin.springsecurity.ui.RegisterController {


	def messageSource


	/**
	 * Overridden index because we need to use the RegisterCommand object from this class.
	 */
	def index = {
		def copy = [:] + (flash.chainedParams ?: [:])
		copy.remove 'controller'
		copy.remove 'action'
		[command: new RegisterCommand(copy)]
	}

	/**
	 * Overridden register action to provide firstname/lastname support
	 */
	def register = { RegisterCommand command ->

		def msg
		if (command.hasErrors()) {
			command.errors?.allErrors?.each{
				//get the error message from the validation code
				msg =  messageSource.getMessage(it, null)
			};
			//has error, so return the error message as JSON
			render([success: false, error:msg] as JSON)
			return
		}

		String salt = saltSource instanceof NullSaltSource ? null : command.username
		def user = lookupUserClass().newInstance(
				email: command.email,
				username: command.username,
				firstName: command.firstName,
				name : command.username,
				lastName: command.lastName,
				accountLocked: true,
				enabled: true
		)

		RegistrationCode registrationCode = springSecurityUiService.register(user, command.password, salt)
		if (registrationCode == null || registrationCode.hasErrors()) {
			// null means problem creating the user
			msg = message(code: 'spring.security.ui.register.miscError')
			//has error, so return the error message as JSON
			render([success: false, error:msg] as JSON)
			return
		}

//		def conf = SpringSecurityUtils.securityConfig
//		String url = generateLink('verifyRegistration', [t: registrationCode.token])
//		def body = conf.ui.register.emailBody
//		if (body.contains('$')) {
//			body = evaluate(body, [user: user, url: url])
//		}
//		mailService.sendMail {
//			to command.email
//			from conf.ui.register.emailFrom
//			subject conf.ui.register.emailSubject
//			html body.toString()
//		}

		def siteUrl = createLink(uri:"/",absolute : 'true');


		def body = g.render(template: "userRegisteredEmail", model: [user: user, mcURL: siteUrl])

		mailService.sendMail {
			to "${command.firstName} ${command.lastName}<${command.email}>"
			// FIXME needs to be refactored into a messages class - i18n support
			subject "New Account"
			html(body)
		}

		render([success: true] as JSON)
		return
	}

	static final betterPasswordValidator = { String password, command ->
		// Username cannot be password
		if (command.username && command.username.equals(password)) {
			return 'command.password.error.username'
		}

		if (!checkPasswordMinLength(password, command) ||
				!checkPasswordMaxLength(password, command)){
			return 'command.password.error.length'
		}

		if (!checkPasswordMinLength(password, command) ||
				!checkPasswordMaxLength(password, command) ||
				!checkPasswordRegex(password, command)) {
			return 'command.password.error.strength'
		}
	}

	/**
	 * changePassword
	 */
	def changePassword (ResetPasswordCommand command){

		def user = springSecurityService.getCurrentUser()
		if(!user){
			flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return
		}

		def msg
		command.username = user.username
		command.validate()
		if (command.hasErrors()) {
			command.errors?.allErrors?.each{
				//get the error message from the validation code
				msg =  messageSource.getMessage(it, null)
			};

			//has error, so return the error message as JSON
			render([success: false, error:msg] as JSON)
			return
		}


		String salt = saltSource instanceof NullSaltSource ? null : user.username
		user.password = springSecurityUiService.encodePassword(command.password, salt)
		user.save()

		springSecurityService.reauthenticate user.username

		//successfully changed so return
		command.password = null
		command.password2 = null
		render([success: true] as JSON)
		return
	}



	def forgotPassword(ForgotPasswordCommand command) {

		if (!request.post) {
			// show the form
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return
		}

		def msg
		String username = command.username
		String email = command.email
		if (!username) {
			msg = message(code: 'spring.security.ui.forgotPassword.username.missing')
			render([success: false, error:msg] as JSON)
			return
		}

		if (!email) {
			msg = message(code: 'spring.security.ui.forgotPassword.email.missing')
			render([success: false, error:msg] as JSON)
			return
		}

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		def user = lookupUserClass().findWhere((usernameFieldName): username, email:email)
		if (!user) {
			msg = message(code: 'spring.security.ui.forgotPassword.user.notFound')
			render([success: false, error:msg] as JSON)
			return
		}

		def registrationCode = new RegistrationCode(username: user."$usernameFieldName")
		registrationCode.save(flush: true)

		String url = generateLink('resetPassword', [t: registrationCode.token])

		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.forgotPassword.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [user: user, url: url])
		}
		mailService.sendMail {
			to user.email
			from conf.ui.forgotPassword.emailFrom
			subject conf.ui.forgotPassword.emailSubject
			html body.toString()
		}

		render([success: true] as JSON)
		return
	}


	def resetPassword(ResetPasswordCommand command) {

		String token = params.t

		def registrationCode = token ? RegistrationCode.findByToken(token) : null
		if (!registrationCode) {
			flash.error = message(code: 'spring.security.ui.resetPassword.badCode')
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
			return
		}

		if (!request.post) {
			return [token: token, command: new ResetPasswordCommand()]
		}

		command.username = registrationCode.username
		command.validate()
		if (command.hasErrors()) {

			command.errors?.allErrors?.each{
				flash.error =  messageSource.getMessage(it, null)
			};

			return [token: token, command: command]
		}

		String salt = saltSource instanceof NullSaltSource ? null : registrationCode.username
		RegistrationCode.withTransaction { status ->
			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
			def user = lookupUserClass().findWhere((usernameFieldName): registrationCode.username)
			user.password = springSecurityUiService.encodePassword(command.password, salt)
			user.save()
			registrationCode.delete()
		}

		springSecurityService.reauthenticate registrationCode.username

		flash.message = message(code: 'spring.security.ui.resetPassword.success')

		def conf = SpringSecurityUtils.securityConfig
		String postResetUrl = conf.ui.register.postResetUrl ?: conf.successHandler.defaultTargetUrl
		redirect uri: postResetUrl
	}




	static boolean checkPasswordMinLength(String password, command) {
		def conf = SpringSecurityUtils.securityConfig

		int minLength = conf.ui.password.minLength instanceof Number ? conf.ui.password.minLength : 8

		password && password.length() >= minLength
	}

	static boolean checkPasswordMaxLength(String password, command) {
		def conf = SpringSecurityUtils.securityConfig

		int maxLength = conf.ui.password.maxLength instanceof Number ? conf.ui.password.maxLength : 64

		password && password.length() <= maxLength
	}

	static boolean checkPasswordRegex(String password, command) {
		def conf = SpringSecurityUtils.securityConfig

		String passValidationRegex = conf.ui.password.validationRegex ?:
				'^.*(?=.*\\d)(?=.*[a-zA-Z])(?=.*[!@#$%^&]).*$'

		password && password.matches(passValidationRegex)
	}

}


class ForgotPasswordCommand {
	String username
	String email
}

class RegisterCommand {

	String username
	String email
	String firstName
	String lastName
	String password
	String password2

	def grailsApplication

	static constraints = {
		username blank: false, nullable: false, validator: { value, command ->
			if (value) {
				def User = command.grailsApplication.getDomainClass(SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
				if (User.findByUsername(value)) {
					return 'registerCommand.username.unique'
				}
				if(value.length() > 64 || value.length() < 6){
					return 'registerCommand.username.length'
				}
				if(value.contains(' ')){
					return 'registerCommand.username.spaces'
				}
			}
		}
		email blank: false, nullable: false, email: true
		password blank: false, nullable: false, validator: org.modelcatalogue.core.security.RegisterController.betterPasswordValidator
		password2 validator: org.modelcatalogue.core.security.RegisterController.password2Validator
		password2 validator: org.modelcatalogue.core.security.RegisterController.password2Validator
	}
}

class ResetPasswordCommand {
	String username
	String password
	String password2

	static constraints = {
		username nullable: false
		password blank: false, nullable: false, validator: org.modelcatalogue.core.security.RegisterController.betterPasswordValidator
		password2 validator: org.modelcatalogue.core.security.RegisterController.password2Validator
	}
}