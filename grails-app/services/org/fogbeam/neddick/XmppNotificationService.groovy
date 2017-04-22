package org.fogbeam.neddick

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.Roster
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.Message

class XmppNotificationService {

    def grailsApplication;

    boolean transactional = false

    def sendChat(String to, String msg) {

        log.debug( "Sending notification to: [${to}] )");

		String chatHost = grailsApplication.config.chat.host;
		String chatPort = grailsApplication.config.chat.port;
		String chatServiceName = grailsApplication.config.chat.serviceName;
		        
		log.debug( "chat configuration. Host: ${chatHost}, port: ${chatPort}, serviceName: ${chatServiceName}.");
		
		
		ConnectionConfiguration cc = new ConnectionConfiguration(
                chatHost,
                chatPort,
                chatServiceName)
		
		cc.setSASLAuthenticationEnabled( false );
        XMPPConnection connection = new XMPPConnection(cc)


        try {

            connection.connect()
            String chatUserName = grailsApplication.config.chat.username; 
			connection.login( chatUserName,
                    grailsApplication.config.chat.password)

            def chatmanager = connection.getChatManager()

            // we talk, but don't listen, how rude
            Chat chat = chatmanager.createChat(to, null)

            // google bounces back the default message types, you must use chat
            def msgObj = new Message(to, Message.Type.chat)
            msgObj.setBody(msg)
            chat.sendMessage(msgObj)
        } 
		catch (Exception e) 
		{
            log.error("Failed to send XMPP message");
			log.error( e );
        }

        // and close down
        connection.disconnect()

    }	
}