package api;

import java.io.IOException;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Klass som hanterar skickandet av användarens tweets
 */
public class TweetHandler {
	
	public String Tweet(String customer_secret, String token, String status) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException, ClientProtocolException, IOException {
		
		status = URLEncoder.encode(status, "UTF-8").replace("+", "%20");
		System.out.println(status);
		
		/* token är och customer_secret är formatterade enligt "oauth_token=xx". Vi vill endast ha delen efter "="
		/  och separerar därför denna bit med String.split("=")
		*/

		String consumerKey = "hHW0UfVeS4rbHtbHYfxAR2i5i"; //Vårt eget API:s consumer key
	
		//Skapar vår POST-request och lägger till användarens tweet i parameter '?status='
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost("https://api.twitter.com/1.1/statuses/update.json?status="+status);

		/*
		 * Twitter kräver en timestamp, som är nuvarande tid
		 * Ett Nonce-värde, vilket är slumpmässigt genererat tal
		 * En signature, vilket är en hashad värde av informationen i Authorization-headern samt alla parametrar(i vårt fall '?status=xx') 
		 * Vi skapar dessa här
		 */
		NonceGenerator nonceGen = new NonceGenerator();
		String nonce = nonceGen.nonceGenerator();
		double time = Math.floor((new Date()).getTime() / 1000);
		
		//Vi genererar vår signature för vår request enligt twitters specifikation
		//https://developer.twitter.com/en/docs/basics/authentication/oauth-1-0a/creating-a-signature
		SignatureGenerator signatureGen = new SignatureGenerator();
		String signature = signatureGen.generateSignature(nonce, time, token, customer_secret, status);
		
		//Lägger till alla headers
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");
		post.addHeader("Host", "api.twitter.com");
		post.addHeader("Accept", "*/*");
		post.addHeader("Connection", "keep-alive");
		post.addHeader("Authorization",
				"OAuth oauth_consumer_key=\"" + consumerKey + "\", "
				+ "oauth_nonce=\"" + nonce + "\", "
				+ "oauth_signature=\"" + signature + "\", "
				+ "oauth_signature_method=\"HMAC-SHA1\", "
				+ "oauth_timestamp=\"" + time + "\", "
				+ "oauth_token=\"" + token + "\", "
				+ "oauth_version=\"1.0\"");
		
		//Vi skickar vår POST-request
		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		return responseString;
		
	}

}
