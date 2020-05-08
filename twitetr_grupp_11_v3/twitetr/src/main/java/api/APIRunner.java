package api;

import static spark.Spark.post;
import static spark.Spark.staticFileLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Denna klass ansvarar för att köra API:t
 * Hanterar API:ts paths och datan som skickas
 *
 */
public class APIRunner {

	public void api() {
		XMLHandler handler = new XMLHandler();
		TweetHandler tweeter = new TweetHandler();
		staticFileLocation("/public");

		/**
		 * Denna metod tar emot parametern "usertweet", som skickas vidare till libris api för att 
		 * få rättningsförslag. 
		 * Returdatan är i form av JSON
		 */
		post("/tweetcheck", (request, response) -> {
			String usertweet = request.queryParams("usertweet");

			// Skapar datastrukturer att lagra informationen från libris i
			ArrayList<HashMap<Integer, String>> listStorage = new ArrayList<HashMap<Integer, String>>();
			HashMap<Integer, String> map1 = new HashMap<Integer, String>();
			HashMap<Integer, String> map2 = new HashMap<Integer, String>();
			String suggested_sentence_string = ""; //rättningsförslaget från libris. Hela meningen.
			List<String> list = new ArrayList<String>();
			ArrayList<Object> flagged_words = new ArrayList<Object>();
			boolean found_suggestion = false; //variabel som läggs in i returdatan beroende på om libris hittar förslag eller inte 
			if (usertweet != null && usertweet.length() <= 280) { // kollar efter null sträng, och att strängen inte överstiger 280 tecken (maxstorlek på tweets)
				list = Arrays.asList(usertweet.split(" ")); // sätter in varje ord i användarens text i en array

				listStorage = handler.parseXML(usertweet); // här skickas texten till libris-api

				//Vår metod returnerar en hashmap med två listor. Vi hämtar dessa
				if (listStorage.size() > 0) {
					found_suggestion = true;
					map1 = listStorage.get(0);
					map2 = listStorage.get(1);
				} else { // Om listStorage inte är större än 0, hittades inga fel
					found_suggestion = false;
				}

				//Rättade meningen lagras i en hashmap, vi gör om denna till en sträng
				StringBuilder suggested_sentence = new StringBuilder();
				map1.forEach((key, value) -> suggested_sentence.append(value + " "));

				/*
				 * Information om specifika rättade ord lagras i en hashmap (index och ord), vi gör ett JSON-objekt för varje
				 * och lägger i en arraylista
				 */
				for (Map.Entry mapElement : map2.entrySet()) {
					int key = (int) mapElement.getKey();
					String value = (String) mapElement.getValue();
					flagged_words.add(new MyObject(key, list.get(key), value));
				}
				suggested_sentence_string = suggested_sentence.toString();
			}

			//Returnerar JSON med rättningsförslag
			response.type("application/json");
			return new MyMessage(usertweet, found_suggestion, suggested_sentence_string, flagged_words);
		}, new JsonTransformer());

		
		/**
		 * Denna metod ansvarar för att skicka upp användarens tweet till Twitter
		 * Tar emot ett json-objekt med tweettext och oauth-uppgifter
		 * Returdata är i form av JSON med information om Tweetet som datum, id, användarnamn m.m.
		 */
		post("/tweet", (request, response) -> {
			System.out.println(request.body());
			response.type("application/json");
			System.out.println("kom till apirunner");
			String tweetJSON = request.body(); //Sparar requestets body i en sträng 
			System.out.println("detta är vårt request body");
			System.out.println(tweetJSON);
			int responseCode = 400; //Initialiserar responskod till 400, om inget annat sker.
			
			//Om requestets body är null så returerar vi ett felmeddelande
			if (tweetJSON == null) {
				return new ErrorMessage(1);
			}
			

			//Hanterar JSON-objektet som tas emot, tweetet och oauth-uppgifter
			Object object = new JSONParser().parse(tweetJSON);
			JSONObject jsonobject = (JSONObject) object;
			String tweet = (String) jsonobject.get("tweet");
			
			JSONObject jsonobject2 = (JSONObject) jsonobject.get("oauth");
			String access_token = (String) jsonobject2.get("access_token");
			String secret_key = (String) jsonobject2.get("secret_key");


			// Kontrollerar att längden inte överstiger 280, som tweets inte får överstiga
			if (tweet.length() > 280) {
				return new ErrorMessage(186);
			}

			System.out.println(access_token);
			System.out.println(secret_key);
			System.out.println(tweet);

			// Hanterar null- och nollvärden och skickar felmeddelande med relevant information om det inträffar
			if (secret_key == null || access_token == null || tweet == null) {
				return new ErrorMessage(1);
			}

			if (secret_key.length() == 0 || access_token.length() == 0 || tweet.length() == 0) {
				return new ErrorMessage(2);
			}

			/*
			 *  Skickar oauth-uppgfiter och tweettexten till metoden Tweet i TweetHandler som gör ett request mot twitter för att ladda upp tweetet
			 *  Returdatan från denna metod är strängen som returneras av twitter
			 */
			String responseString = tweeter.Tweet(secret_key, access_token, tweet);
			System.out.println(responseString);
			
			// Om svaret från Twitter innehåller "created_at" så innebär det att tweetet lyckades.
			if (responseString.contains("created_at")) {
				
				//Vi bygger vårt JSON-objekt med relevant information
				try {
					responseCode = 200; 
					Object obj = new JSONParser().parse(responseString);
					JSONObject jsonobj = (JSONObject) obj;
					String created_at = (String) jsonobj.get("created_at");
					String id = (String) jsonobj.get("id_str");
					JSONObject jsonobj2 = (JSONObject) jsonobj.get("user");
					String username = (String) jsonobj2.get("screen_name");
					String userID = (String) jsonobj2.get("id_str");
					return new TweetJSON(tweet, username, userID, created_at);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

			// Hanterar fel och returnerar relevant information om det inträffar
			else if (responseString.contains("Status is a duplicate")) {
				responseCode = 187;
			}

			else if (responseString.contains("Invalid or expired token")) {
				responseCode = 89;
			}

			else if (responseString.contains("Bad authentication data")) {
				responseCode = 215;
			}

			else if (responseString.contains("Tweet needs to be a bit shorter")) {
				responseCode = 186;
			}

			else if (responseString.contains("Could not authenticate you")) {
				responseCode = 32;
			}

			else {
				responseCode = 400;
			}

			return new ErrorMessage(responseCode);
		}, new JsonTransformer());

	}

	public static void main(final String[] args) {
		APIRunner api = new APIRunner();
		api.api();
	}
}
