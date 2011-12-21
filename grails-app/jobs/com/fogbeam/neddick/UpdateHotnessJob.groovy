package com.fogbeam.neddick

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class UpdateHotnessJob 
{
	private static final long BEGINNING_OF_TIME = 1230786000000; // 01 01 2009 00:00:00
	
	def group = "MyGroup";
	def volatility = false;
	
	def dataSource;
	
	static triggers =
	{
		
	}
	
	def execute(context)
	{
		println "Executing UpdateHotnessJob";	
		
		Connection conn = dataSource.getConnection();
		try
		{
			File hotnessLog = new File ("/tmp/hotness.log");
			
			// get all the votes (note: will we want to retrieve only the ones after the
			// last update? Or somehow mark the ones that have been processed, so we
			// don't process them again?
			PreparedStatement getUESLinksSt = conn.prepareStatement( "select * from user_entry_score_link" );
			PreparedStatement getEntrySt = conn.prepareStatement( "select * from entry where id = ?" );
			PreparedStatement updateHotnessSt = conn.prepareStatement( "update user_entry_score_link set entry_hotness = ? where id = ?" );
			
			ResultSet getUESLinksRs = getUESLinksSt.executeQuery();
			
			Date dNow = new Date();
			long now = dNow.getTime();
			
			while( getUESLinksRs.next() )
			{
				int entryId = getUESLinksRs.getInt( "entry_id" );
				int userId = getUESLinksRs.getInt( "user_id" );
				int uelId = getUESLinksRs.getInt( "id" );
				
				double baseScore = getUESLinksRs.getDouble( "entry_base_score" );
				double hotness = baseScore * 10.0;
				// println "set initial \"hotness\" as ${hotness}";
				
				getEntrySt.setInt( 1, entryId );
				ResultSet getEntryRs = getEntrySt.executeQuery();
				Date dateCreated = null;
				if( getEntryRs.next())
				{
					dateCreated = getEntryRs.getTimestamp( "date_created" );
				}
				else
				{
					println "ENTRY LOCATION FAILED!!!!";
					continue;	
				}
				
				hotnessLog.append( "date_created: ${dateCreated.time}\n" );
							
				double age = ( now - BEGINNING_OF_TIME ) - (dateCreated.time - BEGINNING_OF_TIME );
				hotnessLog.append( "age: ${age}\n" );
				age = age / (1000*60);
				hotnessLog.append( "age / (1000*60) = " + age + "\n" );
				
				
				// println "age in milliseconds: ${age}";
				
				double decayForAge = Math.log( age );
				hotnessLog.append( "decayForAge: ${decayForAge}\n" );
				
				double finalHotness =  hotness - decayForAge;
				hotnessLog.append( "final \"hotness\" score: ${finalHotness}\n");
				hotnessLog.append( "entry: ${entryId}\n" );
				
				updateHotnessSt.setDouble( 1, finalHotness );
				updateHotnessSt.setInt( 2, uelId );
				updateHotnessSt.executeUpdate();
					
			}
		}
		finally
		{
			conn.close();	
		}
	}
}
