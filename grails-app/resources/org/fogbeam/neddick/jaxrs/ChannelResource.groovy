package org.fogbeam.neddick.jaxrs

import static org.grails.jaxrs.response.Responses.*
import groovy.json.JsonSlurper

import javax.ws.rs.Consumes
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.Response

import org.fogbeam.neddick.Channel
import org.fogbeam.neddick.jaxrs.collection.ChannelCollection

@Path('/api/channel')
@Consumes('application/json')
class ChannelResource 
{
	def channelService;
	def userService;
	
	
	@GET
	@Produces('application/json')
	public ChannelCollection getChannels()
	{
		ChannelCollection collection = new ChannelCollection();
	
		List<Channel> channels = channelService.getAllChannels();
			
		collection.addAll(channels);
		
		return collection;
	}
	
	
	@GET
	@Path('/{id}')
	@Produces('application/json')
	public Channel getChannel( @PathParam("id") long id )
	{
		Channel channel = channelService.findById( id );
		return channel;
	}
	
	
	@PUT
	@Produces('text/plain')
	@Consumes('application/json')
	public Response createChannel( final String inputData )
	{
		log.info("received data: \n" + inputData );		
		println "inputData: \n ${inputData}";

		JsonSlurper jsonSlurper = new JsonSlurper();
		def jsonObject = jsonSlurper.parseText(inputData);
		
		
		Channel newChannel = new Channel();
		
		newChannel.uuid = jsonObject.uuid;
		newChannel.name = jsonObject.name;
		newChannel.description = jsonObject.description;
		newChannel.dateCreated = Date.parse(jsonObject.dateCreated);
		newChannel.privateChannel = Boolean.parseBoolean(jsonObject.privateChannel);
		newChannel.owner = userService.findUserByUserId( jsonObject.userId);
			
		
		channelService.save( newChannel );
		
		ok( "OK" );
	}
	
	@POST
	@Produces('text/plain')
	@Consumes('application/json')
	public Response updateChannel( String inputData )
	{
		ok( "OK" );
	}
	
}
