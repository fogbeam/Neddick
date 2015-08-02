package com.fogbeam.neddick

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class UpdateRawScoresFromVotesJob 
{
	def group = "MyGroup";
	def volatility = false;
	
	def dataSource;
	
	static triggers = 
	{
		
	}
	
	def execute(context)
	{
		log.debug( "Executing UpdateRawScoresFromVotesJob" );	
		
		Connection conn = dataSource.getConnection();
		
		// get all the votes (note: will we want to retrieve only the ones after the
		// last update? Or somehow mark the ones that have been processed, so we
		// don't process them again?
		PreparedStatement getVotesSt = conn.prepareStatement( "select * from vote where enabled = ?" );
		getVotesSt.setBoolean( 1 , true );
		ResultSet getVotesRs = getVotesSt.executeQuery();
		ArrayList votes = new ArrayList();
		while( getVotesRs.next() )
		{
			Integer voteId = getVotesRs.getInt( "id" );
			Integer entryId = getVotesRs.getInt( "entry_id" );
			Integer weight = getVotesRs.getInt( "weight" );
			Integer submitterId = getVotesRs.getInt( "submitter_id" );
			def map= ['voteId':voteId, 'entryId':entryId, 'weight':weight, 'submitterId':submitterId];
			votes.add( map );
		}
		
		
		
		PreparedStatement updateScoresForEntrySt = conn.prepareStatement( "update user_entry_score_link set entry_base_score = entry_base_score + ? where entry_id = ?" );
		PreparedStatement updateScoresForEntryAndUserSt = conn.prepareStatement( "update user_entry_score_link set entry_base_score = entry_base_score + ? where entry_id = ? and user_id = ?" );
		for( def vote : votes )
		{

			// update all scores for this entry by the default vote amount (+1 or -1)
			// without considering personalization.  Then, ONLY for the users who have
			// personalization for this voter, go back and update their scores for this
			// entry, reflecting the boost amount.
			updateScoresForEntrySt.setInt(1, vote.weight );
			updateScoresForEntrySt.setInt(2, vote.entryId );
			log.debug( "updating entry ${vote.entryId}, adding ${vote.weight} to score" );
			
			updateScoresForEntrySt.executeUpdate();
			
			// is this vote's owner the target of any personalization assignments?
			// if so, get the list of users who have assigned a boost to this user
			PreparedStatement getPersonalizationSt = conn.prepareStatement( "select link.* from user_to_user_link as link, vote where vote.submitter_id = link.target_id and vote.id = ?" );
			getPersonalizationSt.setInt(1, vote.voteId );
			ResultSet getPersonalizationRs = getPersonalizationSt.executeQuery();
			
			Integer owner_id = 0;
			Integer boost = 0;
			while( getPersonalizationRs.next() )
			{
				owner_id = getPersonalizationRs.getInt( "owner_id" );
				boost = getPersonalizationRs.getInt( "boost" );
									
				if( vote.weight < 0 && boost > 0 )
				{
					// if it was a downvote, with positive boost. So *subtract* the boost from the score
					boost = 0 - boost;
				}
				if( vote.weight < 0 && boost < 0 )
				{
					// it was a downvote, but the "boost" is negative, so *add* the boost amount
					boost = Math.abs( boost );
				}
				if( vote.weight > 0  && boost > 0 )
				{
					// upvote, positive boost, leave the boost as is
				}
				if( vote.weight > 0 && boost < 0 )
				{
					// it was an upvote, but the "boost" is negative, so subtract the boost amount from the score
					boost = 0 - boost;
				}
				
				log.debug( "submitter_id: ${vote.submitterId}, boost: ${boost}, vote_id: ${vote.voteId}, entry_id: ${vote.entryId}, owner_id: ${owner_id}" );
				
				
				updateScoresForEntryAndUserSt.setDouble( 1 , boost );
				updateScoresForEntryAndUserSt.setInt( 2, vote.entryId );
				updateScoresForEntryAndUserSt.setInt( 3, owner_id );
				
				updateScoresForEntryAndUserSt.executeUpdate();
			}
			
		}
	}
}
