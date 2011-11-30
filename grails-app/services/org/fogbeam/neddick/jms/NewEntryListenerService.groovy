package org.fogbeam.neddick.jms;

import org.fogbeam.neddick.Entry

public class NewEntryListenerService {

	static int count = 0;
	static expose = ['jms'];
	static destination = "entryQueue"
	def sessionFactory;

	def onMessage(msg)
	{
		count++;
		println "NewEntryListenerService.onMessage: received message number: ${count}";
		
		String msgType = msg['msgType'];
		def entryId = msg['id'];
		
		println "got msg as ${msg}";
		println "entryId: ${entryId}";
		
		if( msgType.equals("NEW_ENTRY"))
		{
			println "processing NEW_ENTRY message";
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
					e.printStackTrace();	
				}
			}
		}
 	
		return null;	
	}
	
}
