package neddick_grails3

class UrlMappings {

    static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

		"/tags/$tagName" (controller:"tag", action:"listEntriesByTag")
		"/entry/e/$uuid"( controller:"entry", action:"viewEntry")
		"/user/relate/$targetUserName" (controller:"user", action:"relate")
		"/user/viewDetails/$targetUserName" (controller:"user", action:"viewDetails")
		"/user/viewComments/$targetUserName" (controller:"user", action:"viewComments")
		"/user/viewTags/$targetUserName" (controller:"user", action:"viewTags")
		"/user/viewBookmarks/$targetUserName" (controller:"user", action:"viewBookmarks")
		"/user/viewSubmissions/$targetUserName" (controller:"user", action:"viewDetails")
		"/user/viewFriendsActivity/$targetUserName" (controller:"user", action:"viewFriendsActivity")
		"/r/$channelName/$action?" ( controller:"home")
		"/$controller/$name/rss" (action:"renderRss")
		"/r/$channelName/$name/rss"(controller:"home", action:"renderRss")
		
        "/"(controller:"home", action:"index")
        "500"(view:'/error')
        "404"(view:'/notFound')
    }
}
