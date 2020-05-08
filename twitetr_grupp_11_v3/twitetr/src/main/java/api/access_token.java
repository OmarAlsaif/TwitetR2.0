package api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *  Klass som skickar anskaffad token och verifier från användarens login-försök
 *   till twitter för att få tillbaka ett access token
 */
public class access_token {

	public String[] generateAccessToken(String token, String verifier) throws IOException {
		
		 //Vi kommer att få 4 värden från Twitter, vi skapar en array för att lagra dessa
		String[] tokenInformation = new String[4];
		
		//Sparar svaret från vår request till Twitter i en tillfällig StringBuilder
		StringBuilder response = new StringBuilder();
		
		//Skickar token och verifier till Twitter för att få vår access token
		URL getToken = new URL("https://api.twitter.com/oauth/access_token?oauth_token=" + token + "&oauth_verifier=" + verifier);
		
		//Skapar en connection och ström för att läsa svaret från Twitter
		URLConnection connection = getToken.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine = "";
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
			}
		
		//Stänger strömmen när vi läst färdigt informationen
		in.close();

		/* Svaret från twitter är formatterat enligt "oauth_token=xx&oauth_token=xx&user_id=xx&screen_name=xx"
		*  Vi separerar dessa fyra värden med String.split("&")
		*/
		String responseString = response.toString();
		tokenInformation = responseString.split("&");

		return tokenInformation;

	}

}
