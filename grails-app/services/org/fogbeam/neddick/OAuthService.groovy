package org.fogbeam.neddick

import org.apache.commons.lang3.StringUtils
import org.apache.http.HttpEntity
import org.apache.http.HttpHeaders
import org.apache.http.HttpResponse
import org.apache.http.NameValuePair
import org.apache.http.client.HttpClient
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.fogbeam.common.oauth.client.Insecure

public class OAuthService 
{
	
	def grailsApplication;
	
	public String getQuoddyOAuthToken()
	{
		
		HttpClient httpClient = null;
		
		String insecureHttpClientFlag = grailsApplication.config.oauth.httpclient.insecure;
		if( Boolean.parseBoolean( insecureHttpClientFlag ) )
		{
			httpClient = Insecure.getInsecureHttpClient();
		}
		else
		{
			httpClient = HttpClients.createDefault();
		}

		// String clientId = "test_client";
		// String clientSecret = "clientSecret";
		
		String clientId = grailsApplication.config.oauth.clientId.quoddy;
		String clientSecret = grailsApplication.config.oauth.clientSecret.quoddy;
		
		
		String authorizationHeader = "Basic " + new String( Base64.getEncoder().encode( ( clientId + ":"+clientSecret).getBytes() ) );
		
		// String accessTokenUrlQuoddy = "https://localhost:8443/cas/oauth2.0/accessToken";
		String accessTokenUrlQuoddy = grailsApplication.config.oauth.client.accessTokenUrl.quoddy;
		
		
		HttpPost httpPost = new HttpPost( accessTokenUrlQuoddy );
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add( new BasicNameValuePair( "grant_type", "client_credentials" ));
		params.add( new BasicNameValuePair( "client_id", clientId ));
		
		httpPost.setEntity( new UrlEncodedFormEntity(params));
		
		HttpResponse response = httpClient.execute( httpPost );
		
		String oauthToken = "";
		try
		{
			System.out.println(response.getStatusLine());
			HttpEntity entity1 = response.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			String strResponse = EntityUtils.toString(entity1);

			System.out.println( "Response: " + strResponse );
			
			oauthToken = StringUtils.substringAfter(strResponse, "access_token=");
			oauthToken = StringUtils.substringBefore(oauthToken, "&");
			
			System.out.println( "token: " + oauthToken );

		}
		finally
		{}	
		
		return oauthToken;	
	}
	
	public void introspectToken()
	{
		
		// introspect our token to see if it's good
		
		/* 
		httpPost = new HttpPost( "https://localhost:8443/cas/oidc/introspect" );
		httpPost.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeader);
		
		params = new ArrayList<NameValuePair>();
		params.add( new BasicNameValuePair( "token", oauthToken ));
		params.add( new BasicNameValuePair( "client_id", "test_client" ));
		
		httpPost.setEntity( new UrlEncodedFormEntity(params));
		
		response = httpClient.execute( httpPost );
		
		try
		{
			System.out.println(response.getStatusLine());
			HttpEntity entity1 = response.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			String strResponse = EntityUtils.toString(entity1);
		
			System.out.println( "Response: " + strResponse );
		}
		finally
		{}
		*/
		
	}
}
