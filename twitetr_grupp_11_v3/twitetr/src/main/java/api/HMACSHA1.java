package api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Denna klass tar emot ett meddelande och nyckel som ska hashas
 * Och hashar med HMACSHA1
 * 
 */
public class HMACSHA1 {

	public String generateHash(String message, String key) throws InvalidKeyException, NoSuchAlgorithmException {
		SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(signingKey);
		String hash = Base64.encodeBase64String(mac.doFinal(message.getBytes()));
		return hash;
	}
}