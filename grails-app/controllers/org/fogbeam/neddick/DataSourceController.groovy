package org.fogbeam.neddick

class DataSourceController
{
	def index =
	{
		List<DataSource> allDataSources = DataSource.findAll();
		
		[allDataSources:allDataSources];
		
	}
	
	/* create wizard */
	
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
			 
		   }
		   on( "createRssFeed" ).to("createRssFeedWizardOne")
		   on( "createImapAccount" ).to("createImapAccountWizardOne")
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
