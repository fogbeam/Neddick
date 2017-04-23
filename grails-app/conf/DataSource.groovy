
dataSource {
	pooled = true
	// driverClassName = "org.hsqldb.jdbcDriver"
	driverClassName = "org.postgresql.Driver"
	// username = "sa"
	username = "postgres"
	// password = ""
	password = ""
	logSql = false
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='com.opensymphony.oscache.hibernate.OSCacheProvider'
}
// environment specific settings
environments {
	development {
		dataSource {
			dbCreate = "update" // one of 'create', 'create-drop','update'
			// url = "jdbc:hsqldb:mem:devDB"
			url = "jdbc:postgresql:neddick1"
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:mem:neddick_test"
		}
	}
	production {
		dataSource {
			dbCreate = "update"
			url = "jdbc:postgresql:neddick"
		}
	}
}