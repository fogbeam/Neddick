package org.fogbeam.neddick

import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.oauth.OAuthService
import org.springframework.beans.factory.InitializingBean


class DataSourceController implements InitializingBean
{	
	def grailsApplication;
	OAuthService service;
	
	public void afterPropertiesSet() throws Exception
	{
		String twitterDatasourceCallbackUrl = grailsApplication.config.urls.twitter.datasource.callback;
		println "using twitterDatasourceCallbackUrl: ${twitterDatasourceCallbackUrl}";
		service = new ServiceBuilder()
		.provider(TwitterApi.SSL.class )
		// .apiKey("bwUbU865CNQtt2Xdb62FpQ")
		// .apiSecret("opkW7kQEqJP1YMHE0xYXhxXOD5XOfkVeaw2hTQPY")
		.apiKey( "orGS7crqDqjS76B5RS2w" )
		.apiSecret( "GdtSdh6YzrlqusCOJaFUDvelJtZHzUTELi0pn9DHqA" )
		.callback( twitterDatasourceCallbackUrl )
		.build();
	} 
	
	
	def index =
	{
		List<DataSource> allDataSources = DataSource.findAll();
		
		[allDataSources:allDataSources];
		
	}
	
	/* create wizard */

	def finishTwitterFlow =
	{
		start {
			action {
				
				println "finishTwitterFlow: ${params}";
				
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
					accountToCreate.errors.allErrors.each {println it;}
				}
			}
			on("success").to("finishTwitterOne")
		  }
		
		finishTwitterOne {
			redirect( controller: "dataSource", action:"index");
		}
		
	}
		
	def createWizardFlow =
	{
		start {
			action {
				[];
			}
			on("success").to("createWizardOne")
		  }
		
		/* a view state to bring up our GSP */
		createWizardOne {
			on("stage2") {
				
			}.to("createWizardTemp")
		}
		
		createWizardTemp {
			
		 action {
			 String subscriptionType = params.datasourceType;
			 
			 if( subscriptionType.equals( "rssFeed" ) )
			 {
				 println "rssFeed";
				 createRssFeed();
			 }
			 else if( subscriptionType.equals( "imapAccount" ) )
			 {
				 println "imapAccount";
				 createImapAccount();
			 }
			 else if( subscriptionType.equals( "twitterAccount" ) )
			 {
				 println "twitterAccount";
				 createTwitterAccount();
			 }
		   }
		   on( "createRssFeed" ).to("createRssFeedWizardOne")
		   on( "createImapAccount" ).to("createImapAccountWizardOne")
		   on( "createTwitterAccount" ).to("startTwitterFlow")
		}
		
		
		startTwitterFlow
		{
			action {
			}
			on("success").to("createTwitterAccountWizardOne")	
		}
		
		createTwitterAccountWizardOne
		{
			on("twitterStage2")
			{
				TwitterAccount accountToCreate = new TwitterAccount();
				accountToCreate.description = params.description;
				
				session.accountToCreate = accountToCreate;
				
				Token requestToken = service.getRequestToken();
				session.requestToken = requestToken;
				
				String authUrl = service.getAuthorizationUrl(requestToken);
				
				[authUrl: authUrl];
			}.to( "createTwitterAccountWizardTwo")
		}
		
		createTwitterAccountWizardTwo
		{
			
		}
		
		
		createRssFeedWizardOne {
			
			on("finishWizard") {
				println "finishing wizard with params ${params}";
							
			   [];
			}.to("finishRssFeed")
		}
		
		/* an action state to do the final save/update on the object */
		finishRssFeed {
			action {
				println "create using params: ${params}"
				
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
						newFeed.errors.allErrors.each { println it };
					}
				}
				
			}
			on("success").to("exitWizard");
	   }
		
		createImapAccountWizardOne {
			on("finishWizard") {
				println "finishing wizard with params ${params}";
							
			   [];
			}.to("finishImapAccount")
			
		}
		
		finishImapAccount {
			action {
				println "create using params: ${params}"
				
				
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
						newAccount.errors.allErrors.each { println it };
					}
				}
			}
			on("success").to("exitWizard");
		}
		
		
	   exitWizard {
		   println "exiting Wizard!";
		   redirect(controller:"dataSource", action:"index");
	   }		
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
