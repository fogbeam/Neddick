package org.fogbeam.neddick.jms.listeners;

import javax.jms.MapMessage

public class NewEntryListenerService {

	static int count = 0;
	static expose = ['jms'];
	static destination = "entryQueue"
	def sessionFactory;

	def onMessage( MapMessage msg )
	{
		count++;
		log.info( "NewEntryListenerService.onMessage: received message number: ${count}" );
		
		String msgType = msg.getString('msgType');
		def entryId = msg.getString( 'id' );
		
		log.info( "got msg as ${msg}" );
		log.info( "entryId: ${entryId}" );
		
		if( msgType.equals("NEW_ENTRY"))
		{
			log.info( "processing NEW_ENTRY message" );
			
			// a new Entry was added to the system.  Populate the user_entry_score_link
			// table with initial default values
			def hibSession = sessionFactory.openSession();
			def tx = null;
			try
			{
				tx = hibSession.beginTransaction();
			}
			catch( Exception e ) 
			{
				e.printStackTrace();	
			}
			def query =
			hibSession.createSQLQuery( "insert into user_entry_score_link (id, version, entry_id, entry_base_score, entry_controversy, entry_hotness, user_id ) " 
				+ "select nextval('hibernate_sequence'), 1, ${entryId}, 0, 0, 0, id from uzer" );
			try
			{
				query.executeUpdate();
			}
			catch( Exception e ) 
			{
				log.error( "error inserting into user_entry_score_link", e );
				e.printStackTrace();	
			}
			finally
			{
				try
				{
					tx.commit();
				}
				catch( Exception e ) 
				{
					log.error( "error committing transaction", e );
					e.printStackTrace();	
				}
			}
		}
 	
		return null;	
	}	
}