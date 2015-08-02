package com.fogbeam.neddick

import java.sql.Connection
import java.sql.PreparedStatement

class CreateInitialUserEntryLinksJob 
{
	def dataSource;
	
	def group = "MyGroup";
	def volatility = false;
	
	static triggers = 
	{
		
	}
	
	/* Note: This should basically never be used.  It's a "one shot" thing used to seed a database that
	 * is missing entries in the UserEntryScoreLink table.  That situation would probably only arise in
	 * development and this will probably be deleted at some point.
	 */
	def execute(context)
	{
		log.debug( "Executing CreateInitialUserEntryLinksJob" );
		
		// Connection conn = DriverManager.getConnection(url, username, password);
		Connection conn = dataSource.getConnection();
		
		PreparedStatement st = conn.prepareStatement( "insert into user_entry_score_link (id, version, entry_id, entry_base_score, entry_controversy, entry_hotness, user_id ) select nextval('hibernate_sequence'), '1', entry.id, '1', '1', '1', uzer.id from entry, uzer;" );
		
		
		st.executeUpdate();
		
	}
}