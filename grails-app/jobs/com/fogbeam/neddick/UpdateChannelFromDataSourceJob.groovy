package com.fogbeam.neddick

import java.text.SimpleDateFormat

import org.fogbeam.neddick.Channel
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Scheduler

@DisallowConcurrentExecution
class UpdateChannelFromDataSourceJob 
{
	def jmsService;
	def channelService;
	
	// disable concurrent execution
	static concurrent = false;
	def concurrentExecutionDisallowed = true;
	def volatility = false;
	
	static triggers = {}
		
	def execute(def context)
	{
		log.info( "Updating Channels from DataSources" );
		println "Updating Channels from DataSources";
		
		// as an extra hedge against accidental concurrent execution...
		Scheduler sched = context.getScheduler();
		JobDetail existingJobDetail = context.getJobDetail();

		log.warn( "Beginning execution of UpdateChannelFromDataSourceJob");
		log.info( "existingJobDetail: " + existingJobDetail.toString());
		
		if (existingJobDetail != null)
		{
			List<JobExecutionContext> currentlyExecutingJobs = (List<JobExecutionContext>) sched.getCurrentlyExecutingJobs();
			for (JobExecutionContext jec : currentlyExecutingJobs)
			{
				log.info( "evaluating jec.jobDetail: " + jec.jobDetail.toString());
				
				if(jec.jobDetail.key.equals(existingJobDetail.key) && (!(jec.jobDetail == existingJobDetail)))
				{
					String message = "another instance for " + existingJobDetail.toString() + " is already running.";
					log.warn(message);
					
					// throw new JobExecutionException(message,false);
					return;
				}
				else
				{
					log.info( "not a match, proceed.");
				}
			}
		}
		else
		{
			log.info( "existingJobDetail is NULL");
		}

		
		Date now = new Date();
		SimpleDateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		
		if( jmsService != null )
		{
			log.debug( "found a JMS service..." )
			// get a list of channels that have associated rss feeds
			List<Channel> channelsWithDataSources = channelService.findChannelsWithDatasource();
			if( channelsWithDataSources != null && channelsWithDataSources.size() > 0 )
			{
				log.debug( "Found some channels to update" );
			}
			
			// iterate over that list, sending a message for each channel, to update from RSS
			for( Channel channel in channelsWithDataSources )
			{
				String msg = "UPDATE_CHANNEL:${channel.name}";
			
				log.debug( "TRIGGER: sending update channel message: ${sdf.format( now )}" );

				log.debug( "using JMS Service" );
				jmsService.send( queue: "datasourceQueue", msg, "standard", null );			
			}
		}
		else
		{
			log.debug( "no JMS Service!" );
		}
	}
}