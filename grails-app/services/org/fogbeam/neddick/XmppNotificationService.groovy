package org.fogbeam.neddick

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.Roster
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.Message

class XmppNotificationService {

    boolean transactional = false

    def sendChat(String to, String msg) {

        log.debug( "Sending notification to: [${to}] )");
        ConnectionConfiguration cc = new ConnectionConfiguration(
                ConfigurationHolder.config.chat.host,
                ConfigurationHolder.config.chat.port,
                ConfigurationHolder.config.chat.serviceName)
		
		cc.setSASLAuthenticationEnabled( false );
        XMPPConnection connection = new XMPPConnection(cc)


        try {

            connection.connect()
            connection.login(ConfigurationHolder.config.chat.username,
                    ConfigurationHolder.config.chat.password)

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