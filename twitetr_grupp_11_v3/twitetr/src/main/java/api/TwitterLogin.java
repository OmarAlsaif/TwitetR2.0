package api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 
 * Klass som hanterar inloggningen på hemsidan
 * Inloggningen sker med hjälp av Twitter, som använder Oauth 1.0
 *
 */
public class TwitterLogin {

	public String[] loginHandler() throws Exception {
		
		//Skapar vår POST-request
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpPost post = new HttpPost("https://api.twitter.com/oauth/request_token");

		/*
		 * Oauth 1.0 kräver kräver bl.a. nonce-värde (slumpmässigt tal),
		 * Timestamp, nuvarande tid,
		 * Signature, vilket är alla Oauth-värden hashade
		 * Vi genererar dessa här
		 */
		String consumerKey = "hHW0UfVeS4rbHtbHYfxAR2i5i";
		NonceGenerator nonceGen = new NonceGenerator();
		String nonce = nonceGen.nonceGenerator();
		double time = Math.floor((new Date()).getTime() / 1000);
		SignatureGenerator signatureGen = new SignatureGenerator();
		String signature = signatureGen.generateSignature(nonce, time);

		//Lägger till våra headers
		post.addHeader("Content-Type", "application/x-www-form-urlencoded");
		post.addHeader("Host", "api.twitter.com");
		post.addHeader("Accept", "*/*");
		
		
		//Skapar vår Authorization header, Oauth 1.0 används
		//Enligt Oauth 1.0 specifikation måste alla värden vara sorterade i lexikografisk ordning
		post.addHeader("Authorization",
				"OAuth oauth_callback=\"http%3A%2F%2Flocalhost%3A4567%2FSuccess\", "
				+ "oauth_consumer_key=\"" + consumerKey + "\", "
				+ "oauth_nonce=\"" + nonce + "\", "
				+ "oauth_signature=\"" + signature + "\", "
				+ "oauth_signature_method=\"HMAC-SHA1\", "
				+ "oauth_timestamp=\"" + time + "\", "
				+ "oauth_version=\"1.0\"");

		//Skickar vår POST-request
		HttpResponse response = httpClient.execute(post);
		HttpEntity entity = response.getEntity();
		
		//Sparar svaret
		String responseString = EntityUtils.toString(entity, "UTF-8");
		String[] tokenInformation = new String[3];
		tokenInformation = responseString.split("&");
		
		return tokenInformation;


	}

}
