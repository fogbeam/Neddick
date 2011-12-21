package org.fogbeam.neddick

import java.util.List

import org.codehaus.groovy.grails.commons.ArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsClass
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.quartz.Trigger

class ScheduleController {

	def jobManagerService;
	
	def index = {
		
		// get all the "Job" Artefacts
		GrailsClass[] artefacts = grailsApplication.getArtefacts( "Job" );
		
		
		ArtefactHandler[] handlers = grailsApplication.getArtefactHandlers();
		/* for( ArtefactHandler handler : handlers )
		{
			println "Handler: ${handler.pluginName} - ${handler.type}";
		}
		*/
		
		
		
		Class[] artefacts2 = grailsApplication.getAllArtefacts();
		// println "Here we are2";
		
		for( Class clazz : artefacts2 )
		{
			// println "Artefact: ${clazz.toString()}";
			ArtefactHandler h = grailsApplication.getArtefactType( clazz );
			// println h;
		}
		
		/*
		for( GrailsClass clazz : artefacts )
		{
			println "GrailsClass: Name: ${clazz.name} - ${clazz.shortName} - ${clazz.fullName}";
		}
		*/
		
		
		[artefacts:artefacts];
			
	}

	def editSchedule =
	{
		println "received id: ${params.id}";
	
		List<String> jobGroups = jobManagerService.quartzScheduler.getJobGroupNames();
		def triggers = null;
		def jobGroup = null;
		def jobName = null;
		def jobFullName = null;
		for( String aJobGroup : jobGroups )
		{
			// println "jobGroup: ${jobGroup}";
			for(String aJobName : jobManagerService.quartzScheduler.getJobNames(aJobGroup))
			{
				// System.out.println("Found job identified by: " + aJobName);

				JobDetail detail = jobManagerService.quartzScheduler.getJobDetail(aJobName, aJobGroup);
				/* println "The whole thing: ${detail}";
				println "full name: ${detail.fullName}";
				println "Class: ${detail.jobClass.name}";
				println "\n"; */
				
				if( detail.fullName.contains( params.id ))
				{
					// println "found jobDetail for job class: ${params.id}";
					// println "get any associated triggers...";
						
					triggers = jobManagerService.quartzScheduler.getTriggersOfJob(aJobName, aJobGroup);
					jobName = aJobName;
					jobGroup = aJobGroup;
					jobFullName = detail.fullName;
				}
				
			}
		}
				
		[existingTriggers:triggers, jobGroup: jobGroup, jobName: jobName, jobFullName: jobFullName];
	}
	
	def createTrigger =
	{
		// println "createTrigger:";
		// println "params.id: ${params.id}";
		
		List<String> jobGroups = jobManagerService.quartzScheduler.getJobGroupNames();
		def triggers = null;
		def jobGroup = null;
		def jobName = null;
		def jobFullName = null;
		for( String aJobGroup : jobGroups )
		{
			// println "jobGroup: ${jobGroup}";
			for(String aJobName : jobManagerService.quartzScheduler.getJobNames(aJobGroup))
			{
				// System.out.println("jobName: " + aJobName);

				JobDetail detail = jobManagerService.quartzScheduler.getJobDetail(aJobName, aJobGroup);
				/* println "The whole thing: ${detail}";
				println "full name: ${detail.fullName}";
				println "Class: ${detail.jobClass.name}";
				println "\n";
				*/
				
				if( detail?.fullName?.contains( params.id ))
				{
					// println "found jobDetail for job class: ${params.id}";
					// println "get any associated triggers...";
						
					triggers = jobManagerService.quartzScheduler.getTriggersOfJob(aJobName, aJobGroup);
					jobName = aJobName;
					jobGroup = aJobGroup;
					jobFullName = detail.fullName;
				}
				else {
					// println "no job detail or no fullname found!";
				}
			}
		}
				
		[existingTriggers:triggers, jobGroup: jobGroup, jobName: jobName, jobFullName:jobFullName];
			
	}
	
	def addTrigger =
	{
		String jobGroup = params.jobGroup;
		String jobName = params.jobName;
		String recurrenceInterval = params.recurrenceInterval;
		
		// println "adding Trigger for jobName: ${jobName}";
		// println "recurrenceInterval: ${recurrenceInterval}";
		
		GrailsClass jobClass = grailsApplication.getArtefact( "Job", jobName );
		
		if( jobClass == null )
		{
			println "Could not load GrailsClass for ${jobName}";
		}
		else
		{
			// SimpleTrigger(String name, String group, int repeatCount, long repeatInterval)
			Trigger trigger = new SimpleTrigger( params.triggerName, params.triggerGroup, Integer.parseInt(params.repeatCount), Long.parseLong(params.recurrenceInterval));
			// jobClass.newInstance().schedule( Long.parseLong( recurrenceInterval ), SimpleTrigger.REPEAT_INDEFINITELY, sparams );
			jobClass.newInstance().schedule( trigger );
		}

		
		redirect(action:"index");
	}
	
	def executeJobNow =
	{
		GrailsClass jobClass = grailsApplication.getArtefact( "Job", params.jobName );
		jobClass.newInstance().triggerNow();
		
		redirect(action:"index");
	}
	
	def editTrigger =
	{
		println "Edit Trigger, params: ${params}";
		
		Trigger theTrigger = jobManagerService.quartzScheduler.getTrigger(params.triggerName, params.triggerGroup);
		[trigger: theTrigger];
	}

	
	def deleteTrigger =
	{
		jobManagerService.quartzScheduler.unscheduleJob(params.triggerName, params.triggerGroup);
		redirect(action:"index");
	}
		
	def saveTrigger =
	{

		Trigger theTrigger = jobManagerService.quartzScheduler.getTrigger(params.oldTriggerName, params.oldTriggerGroup);
		Trigger newTrigger = theTrigger.clone();
		newTrigger.name = params.triggerName;
		newTrigger.group = params.triggerGroup;
		newTrigger.repeatInterval = Long.parseLong( params.recurrenceInterval );
		jobManagerService.quartzScheduler.rescheduleJob(params.oldTriggerName, params.oldTriggerGroup, newTrigger);
			
		redirect(action:"index");
	}
}
