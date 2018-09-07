package org.fogbeam.neddick.triggers.actions

import org.fogbeam.neddick.Entry

class WorkflowTriggerAction extends BaseTriggerAction
{
	public void doAction( final String entryUuid )
	{
		println "performing quoddy_share action for uuid: ${entryUuid}";
		

		def entryToSend = Entry.findByUuid( entryUuid );
		
		// TODO: call our Activiti initiateProcessInstance
		// REST endpoint, passing the URL of our Entry.
		
		
		
		
	}
}
