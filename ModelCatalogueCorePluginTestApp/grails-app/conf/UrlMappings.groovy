class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

		"/register/changePassword"(controller:"register", action: "changePassword")
		"/register/register"(controller:"register", action: "register")
		"/register"(redirect: "/")
		"/register/index"(redirect:"/")
        "/"(view:"/index")
	}
}
