package api;

import java.security.SecureRandom;

/**
 * Klass som genererar ett slumpmässigt värde
 *
 */
public class NonceGenerator {

	public String nonceGenerator() {
		SecureRandom secureRandom = new SecureRandom();
		StringBuilder randomNumber = new StringBuilder();
		for (int i = 0; i < 15; i++) {
			randomNumber.append(secureRandom.nextInt(10));
		}
		String randomNumberString = randomNumber.toString();
		return randomNumberString;
	}
}
