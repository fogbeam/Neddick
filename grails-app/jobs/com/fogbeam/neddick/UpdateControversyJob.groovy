package com.fogbeam.neddick

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class UpdateControversyJob 
{
	
	private static final long BEGINNING_OF_TIME = 1230786000000; // 01 01 2009 00:00:00
	
	def dataSource;
	
	def group = "MyGroup";
	def volatility = false;
	
	static triggers =
	{
		
	}

	
	def execute(context)
	{
		println "Executing UpdateControversyJob";	
		
		// Connection conn = DriverManager.getConnection(url, username, password);
		Connection conn = dataSource.getConnection();
		
		try
		{
			// get all the votes (note: will we want to retrieve only the ones after the
			// last update? Or somehow mark the ones that have been processed, so we
			// don't process them again?
			PreparedStatement getUESLinksSt = conn.prepareStatement( "select * from user_entry_score_link" );
			PreparedStatement getEntrySt = conn.prepareStatement( "select * from entry where id = ?" );
			PreparedStatement getVotesSt = conn.prepareStatement( "select * from vote where entry_id = ?" );
			PreparedStatement updateControversySt = conn.prepareStatement( "update user_entry_score_link set entry_controversy = ? where id = ?" );
			
			ResultSet getUESLinksRs = getUESLinksSt.executeQuery();
			
			while( getUESLinksRs.next() )
			{
				int entryId = getUESLinksRs.getInt( "entry_id" );
				int userId = getUESLinksRs.getInt( "user_id" );
				int uelId = getUESLinksRs.getInt( "id" );
				
				getVotesSt.setInt( 1, entryId );
				ResultSet getVotesRs = getVotesSt.executeQuery();
				// int score = 0;
				int totalVotes = 0;
				int upVotes = 0;
				int downVotes = 0;
				// for( vote in votes )
				while( getVotesRs.next() )
				{
					boolean voteEnabled = getVotesRs.getBoolean( "enabled" );
					int voteWeight = getVotesRs.getInt( "weight" );
					
					
					if( voteEnabled )
					{
						// score = score + vote.weight;
						totalVotes++;
					
						if( voteWeight > 0 )
						{
							upVotes++;
						}
						else if( voteWeight < 0 )
						{
							downVotes++;
						}
						else
						{
							// a vote with a weight of 0 is nonsensical
						}
					}
				}
				
				// println "\n\nCalculated score for entry ${entry.id} as ${score}";
				// entry.score = score;
				Date dNow = new Date();
				long now = dNow.getTime();
				
				getEntrySt.setInt( 1, entryId );
				ResultSet getEntryRs = getEntrySt.executeQuery();
				Date dateCreated = null;
				if( getEntryRs.next())
				{
					dateCreated = getEntryRs.getDate( "date_created" );
				}
				
				long age = ( now - BEGINNING_OF_TIME ) - (dateCreated.time - BEGINNING_OF_TIME );
				age = age / (1000*60);
				
				// println "age in milliseconds: ${age}";
				
				// entry.age = age;
				
				def decayForAge = Math.log( age );
				
				double ratio = 0.0;
				if( upVotes != 0 && downVotes != 0 )
				{
					Math.min( upVotes, downVotes ) / Math.max(upVotes, downVotes)
				}
				
				// println "totalVotes: ${totalVotes}, upVotes: ${upVotes}, downVotes: ${downVotes}, ratio: ${ratio}, decayForAge: ${decayForAge}";
				
				double finalControversy = ( totalVotes * ratio ) - decayForAge;
				// println "final \"controversy\" score: ${finalControversy}";
				
				updateControversySt.setDouble( 1, finalControversy );
				updateControversySt.setInt( 2, uelId );
				updateControversySt.executeUpdate();
			}
		}
		finally
		{
			conn.close();	
		}
	}
}
