package org.modelcatalogue.core.testapp

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


		String url = generateLink('verifyRegistration', [t: registrationCode.token])

		def conf = SpringSecurityUtils.securityConfig
		def body = conf.ui.register.emailBody
		if (body.contains('$')) {
			body = evaluate(body, [user: user, url: url])
		}
		mailService.sendMail {
			to command.email
			from conf.ui.register.emailFrom
			subject conf.ui.register.emailSubject
			html body.toString()
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
		password blank: false, nullable: false, validator: org.modelcatalogue.core.testapp.RegisterController.betterPasswordValidator
		password2 validator: org.modelcatalogue.core.testapp.RegisterController.password2Validator
		password2 validator: org.modelcatalogue.core.testapp.RegisterController.password2Validator
	}
}

class ResetPasswordCommand {
	String username
	String password
	String password2

	static constraints = {
		username nullable: false
		password blank: false, nullable: false, validator: org.modelcatalogue.core.testapp.RegisterController.betterPasswordValidator
		password2 validator: org.modelcatalogue.core.testapp.RegisterController.password2Validator
	}
}