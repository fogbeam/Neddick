package org.fogbeam.neddick

import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import static org.scribe.builder.api.TwitterApi.*
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.oauth.OAuthService
import org.springframework.beans.factory.InitializingBean

import grails.plugin.springsecurity.annotation.Secured

class DataSourceController implements InitializingBean
{	
	OAuthService service;
	
	public void afterPropertiesSet() throws Exception
	{
		String twitterDatasourceCallbackUrl = grailsApplication.config.urls.twitter.datasource.callback;
		log.info( "using twitterDatasourceCallbackUrl: ${twitterDatasourceCallbackUrl}");
		service = new ServiceBuilder()
		.provider(TwitterApi.SSL.class )
		// .apiKey("bwUbU865CNQtt2Xdb62FpQ")
		// .apiSecret("opkW7kQEqJP1YMHE0xYXhxXOD5XOfkVeaw2hTQPY")
		.apiKey( "orGS7crqDqjS76B5RS2w" )
		.apiSecret( "GdtSdh6YzrlqusCOJaFUDvelJtZHzUTELi0pn9DHqA" )
		.callback( twitterDatasourceCallbackUrl )
		.build();
	} 
	
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def index()
	{
		List<DataSource> allDataSources = DataSource.findAll();
		
		[allDataSources:allDataSources];
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createWizardOne()
	{
		[:];	
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createWizardTwo()
	{
		String subscriptionType = params.datasourceType;

		String nextAction = "";
				
		if( subscriptionType.equals( "rssFeed" ) )
		{
			log.debug "rssFeed";
			nextAction = "createRssFeedOne";
		}
		else if( subscriptionType.equals( "imapAccount" ) )
		{
			log.debug "imapAccount";
			nextAction = "createImapAccountOne";
		}
		else if( subscriptionType.equals( "twitterAccount" ) )
		{
			log.debug "twitterAccount";
			nextAction = "createTwitterAccountOne"
		}
		
		redirect( controller:"dataSource", action:nextAction );
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createRssFeedOne()
	{
		[:];
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createRssFeedTwo()
	{
		log.debug "create using params: ${params}"
		
		// create using params: [feedUrl:https://news.ycombinator.com/bigrss,
		// _eventId_finishWizard:Save, feedDescription:Hacker News (BigRSS),
		// execution:[e1s2, e1s2], action:createWizard, controller:dataSource]
		RssFeed newFeed = new RssFeed();
		newFeed.feedUrl = params.feedUrl;
		newFeed.description = params.feedDescription;
		RssFeed.withTransaction { status ->
			
			if( !newFeed.save(flush:true, validate:true) )
			{
				log.error( "Error saving Feed" );
				newFeed.errors.allErrors.each { log.debug it };
			}
		}

		redirect( controller:"dataSource", action:"index");
		
	}
		
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createImapAccountOne()
	{
		[:];
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createImapAccountTwo()
	{
		log.debug "create using params: ${params}"
		
		
		// create using params: [imapPassword:7400seriesIC,
		// _eventId_finishWizard:Save, imapUsername:motley.crue.fan@gmail.com,
		// imapServerPort:993, imapServer:imap.gmail.com, execution:[e1s2, e1s2],
		// action:createWizard, controller:dataSource]
		IMAPAccount newAccount = new IMAPAccount();
		newAccount.server = params.imapServer;
		newAccount.description = params.description;
		newAccount.port = params.imapServerPort;
		newAccount.username = params.imapUsername;
		newAccount.password = params.imapPassword;
		newAccount.folder = params.imapFolder;
		
		IMAPAccount.withTransaction { status ->
			
			if( !newAccount.save(flush:true, validate:true) )
			{
				log.error( "Error saving IMAPAccount" );
				newAccount.errors.allErrors.each { log.debug it };
			}
		}
		
		
		redirect( controller:"dataSource", action:"index");	
	}
		
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createTwitterAccountOne()
	{
		[:];
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def createTwitterAccountTwo()
	{
		TwitterAccount accountToCreate = new TwitterAccount();
		accountToCreate.description = params.description;
		
		session.accountToCreate = accountToCreate;
		
		Token requestToken = service.getRequestToken();
		session.requestToken = requestToken;
		
		String authUrl = service.getAuthorizationUrl(requestToken);
		
		[authUrl: authUrl];
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def finishTwitter()
	{
				
		log.debug "finishTwitterFlow: ${params}";
		
		/*
		 finishTwitterFlow: [oauth_token:ygeG7wFvolBlPnIHrCL5VeFtj0xjYbud0NDVDhks, oauth_verifier:4dHR4yCWbnIPLm3R0i7YwOPceq74YfDRAlijdGhHKVQ, action:finishTwitter, controller:dataSource]

		 */
		
		Verifier v = new Verifier(params.oauth_verifier);
		Token accessToken = service.getAccessToken(session.requestToken, v); // the requestToken you had from step 2
		
		TwitterAccount accountToCreate = session.accountToCreate;
		accountToCreate.accessToken = accessToken.token;
		accountToCreate.tokenSecret = accessToken.secret;
		
		if( !accountToCreate.save(flush:true))
		{
			accountToCreate.errors.allErrors.each {log.error it.toString()}
		}
		
		redirect( controller:"dataSource", action:"index");		
	}

	
/*
 action {
			 String subscriptionType = params.subscriptionType;
			 
			 if( subscriptionType.equals( "activitiUserTask" ) )
			 {
				 activitiUserTask();
			 }
			 else if( subscriptionType.equals( "businessEvent" ) )
			 {
				 businessEventSubscription();
			 }
			 else if( subscriptionType.equals( "calendarFeed" ) )
			 {
				 calendarFeed();
			 }
			 else if( subscriptionType.equals( "rssFeed" ) )
			 {
				 rssFeed();
			 }
			 
		   }	
		   on( "activitiUserTask" ).to("createActivitiUserTaskSubscriptionWizardOne")
		   on( "businessEventSubscription" ).to("createBusinessEventSubscriptionWizardOne")
		   on( "calendarFeed" ).to("createCalendarFeedSubscriptionWizardOne")
		   on( "rssFeed" ).to("createRssFeedSubscriptionWizardOne")	
 */	
	
	/* edit wizard */
	
	// TODO: create this wizard...
	
	
}
