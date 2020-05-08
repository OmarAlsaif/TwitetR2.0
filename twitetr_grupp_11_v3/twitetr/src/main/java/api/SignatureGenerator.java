package api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 
 * Klass som genererar en signature enligt Twitters specifikation från nedan länk
 * https://developer.twitter.com/en/docs/basics/authentication/oauth-1-0a/creating-a-signature
 * Dessa behövs i Authorization-headers (OAuth 1.0)
 */

public class SignatureGenerator {

	//Signatur för Authorization-header i Tweet-requests
	public String generateSignature(String nonce, double time, String token, String customer_secret, String status) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {

		//Bas sträng av alla oauth- och url-parametrar i vår Tweet-request
		String base = "oauth_consumer_key=hHW0UfVeS4rbHtbHYfxAR2i5i&"
				+ "oauth_nonce="+ nonce + "&"
				+ "oauth_signature_method=HMAC-SHA1&"
				+ "oauth_timestamp=" + time + "&"
				+ "oauth_token=" + token + "&"
				+ "oauth_version=1.0&"
				+ "status="+status;
		
		//URL encoding för att hantera mellanrum och speciella karaktärer i URL:et
		base = URLEncoder.encode(base, "UTF-8");
		String signatureBase = "POST&https%3A%2F%2Fapi.twitter.com%2F1.1%2Fstatuses%2Fupdate.json&" + base;
		String signingKey = "mQu0L6HlbwxCBli7NgBVN5VShEZUzTFUaX7aeqhjWltOKEpJnI&"+customer_secret; 

		HMACSHA1 hasher = new HMACSHA1();
		//Hashar vår meddelande med nyckel
		String temp = hasher.generateHash(signatureBase, signingKey);
		
		//URL-encodar igen
		String signature = URLEncoder.encode(temp, "UTF-8");
		//Returnerar url-encodad signatur
		return signature;
	}

	
	//Genererar signature för TwitterLogin 
	public String generateSignature(String nonce, double time) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
		
		//Bas sträng av alla oauth-parametrar i vårt login-request
		String base = "oauth_callback=http%3A%2F%2Flocalhost%3A4567%2FSuccess&"
				+ "oauth_consumer_key=hHW0UfVeS4rbHtbHYfxAR2i5i&"
				+ "oauth_nonce="+ nonce + "&"
				+ "oauth_signature_method=HMAC-SHA1&"
				+ "oauth_timestamp=" + time + "&"
				+ "oauth_version=1.0";
		
		//Encodar vår bas-sträng
		base = URLEncoder.encode(base, "UTF-8");

		String signatureBase = "POST&https%3A%2F%2Fapi.twitter.com%2Foauth%2Frequest_token&"+base;
		String apiSecret = "mQu0L6HlbwxCBli7NgBVN5VShEZUzTFUaX7aeqhjWltOKEpJnI&"; 
	
		//Hashar strängen
		HMACSHA1 hasher = new HMACSHA1();
		String temp = hasher.generateHash(signatureBase, apiSecret);
		
		//Returnerar url-encodad signatur
		String signature = URLEncoder.encode(temp, "UTF-8");
		return signature;
	}

}
