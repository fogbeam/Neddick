package org.fogbeam.neddick

import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException

import org.jivesoftware.smack.AbstractXMPPConnection
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smack.util.TLSUtils
import org.jxmpp.jid.DomainBareJid
import org.jxmpp.jid.EntityJid
import org.jxmpp.jid.impl.JidCreate

public class XmppNotificationService 
{

    boolean transactional = false
	
	def grailsApplication;
	
    def sendChat(String to, String msg) 
	{
        log.info( "Sending notification to: [${to}] )");
		log.info( "Sending message: ${msg}");
		
		String xmppHost = grailsApplication.config.chat.host;
		log.info( "xmppHost: [${xmppHost}]");
		
		int xmppPort = Integer.parseInt( grailsApplication.config.chat.port );
		log.info( "xmppPort: [${xmppPort}]");
		
		String xmppServiceName = grailsApplication.config.chat.serviceName;
		log.info( "xmppServiceName: [${xmppServiceName}]");

		String xmppUser = grailsApplication.config.chat.username;
		String xmppPassword = grailsApplication.config.chat.password;
		log.info( "xmppUser: ${xmppUser}, xmppPassword: ${xmppPassword}" );
		
		XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
		configBuilder.setUsernameAndPassword( xmppUser, xmppPassword );
		configBuilder.setResource("SomeResource");
		DomainBareJid serviceName = JidCreate.domainBareFrom("fogbeam.org");
		configBuilder.setXmppDomain(serviceName);
		configBuilder.setHost( "fogbeam.org" );
		configBuilder.setSecurityMode(XMPPTCPConnectionConfiguration.SecurityMode.disabled);
		
		// accept all certificate - just for testing
		try
		{
			TLSUtils.acceptAllCertificates(configBuilder);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (KeyManagementException e)
		{
			e.printStackTrace();
		}

		AbstractXMPPConnection connection = new XMPPTCPConnection(configBuilder.build());		
		
        try 
		{
			log.info( "calling connect() for XMPP" );
			connection.connect();
	
			log.info( "Connect() called for XMPP" );
			connection.login("prhodes", "3nothing");
			
			log.info( "Logged in to XMPP server" );	
						
            ChatManager manager = ChatManager.getInstanceFor(connection);
			EntityJid entity = JidCreate.entityBareFrom(to);
			Chat chat = manager.chatWith(entity);
			
			// google bounces back the default message types, you must use chat
            Message msgObj = new Message(entity, Message.Type.chat );
            msgObj.setBody(msg);
            chat.send(msgObj);
			
			log.info( "message sent!");
        } 
		catch (Exception e) 
		{
            log.error("Failed to send XMPP message", e );
        }
		finally
		{
			// and close down
			if( connection != null )
			{
				connection.disconnect()
			}
		}
    }	
}