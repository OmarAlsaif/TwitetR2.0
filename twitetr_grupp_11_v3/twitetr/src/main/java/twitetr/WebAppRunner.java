package twitetr;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import api.APIRunner;
import api.TwitterLogin;
import api.access_token;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;

public final class WebAppRunner {

//	public static String render(Map<String, Object> model, String templatePath) {
//		return new VelocityTemplateEngine().render(new ModelAndView(model, templatePath));
//	}


	public static void main(final String[] args) {
		access_token accesstoken = new access_token();
		staticFileLocation("/public"); // CSS och bilder. Eventuellt JS-filer
		
		APIRunner api = new APIRunner();
		api.api();
		
		//get route för startsidan, d.v.s. "localhost:4567/"

		get("/", (request, response) -> {
			Map<String, Object> model = new HashMap<>();
			String loggedIn = request.session().attribute("loggedIn");
			String screen_name = request.session().attribute("screen_name");
			
			//Information för att hantera inloggs- och utloggsknapp
			if (loggedIn != null) {
				model.put("loggedIn", "true");
			}
			
			//Skickar med användarens login namn till klienten
			if (screen_name != null) {
				String temp[] = screen_name.split("=");
				screen_name = temp[1];
				model.put("screen_name", screen_name);
			}

			return new ModelAndView(model, "templates/index.html");
		}, new VelocityTemplateEngine());

		//get-route som tar emot användarens tweet-meddelande och bearbetar detta 
		get("/tweetcheck", (request, response) -> {
			Map<String, Object> model = new HashMap<>();
			
			String userTweet = request.queryParams("userTweet"); //hämtar och sparar användarens tweet-text
			String loggedIn = request.session().attribute("loggedIn");
			String screen_name = request.session().attribute("screen_name");
			//Information för att hantera inloggs- och utloggsknapp
			if (loggedIn != null) {
				model.put("loggedIn", "true");
			}
			
			//Skickar med användarens login namn till klienten
			if (screen_name != null) {
				String temp[] = screen_name.split("=");
				screen_name = temp[1];
				model.put("screen_name", screen_name);
			}

			if (userTweet != null && userTweet.length() <= 280) { //koden körs endast om användaren skickat med en sträng, samt att denne inte överstiger 280 karaktärer, då detta är max storlek tillåtet för tweets

				CloseableHttpClient httpClient = HttpClients.createDefault();
				HttpPost post = new HttpPost("http://localhost:4567/tweetcheck?usertweet="+URLEncoder.encode(userTweet, "UTF-8"));
				HttpResponse resp = httpClient.execute(post);
				HttpEntity entity = resp.getEntity();
				
				//Sparar svaret
				String json = EntityUtils.toString(entity, "UTF-8");
				//går igenom svaret
				Object object = new JSONParser().parse(json);
		        JSONObject jsonobject = (JSONObject) object; 
		        JSONArray jsonarray = (JSONArray) jsonobject.get("flagged_words");
		        
		        String query = (String) jsonobject.get("query"); 
		        String suggestedSentence = (String) jsonobject.get("suggested_sentence"); 
		        ArrayList<Object> flaggedWords = (ArrayList<Object>) jsonobject.get("flagged_words"); 
		        ArrayList<Word> words = new ArrayList<Word>();
		        
		        for (int i = 0; i < jsonarray.size(); i++) {
		            JSONObject jsn = (JSONObject) jsonarray.get(i);
		            long index = (long) jsn.get("index");
		            String word = (String) jsn.get("word");
		            Word test = new Word(index, word);
		            words.add(test);
		        }
		        

				model.put("list", query); //skickar med det ursprungliga meddelandet till klienten
				
				
				if(suggestedSentence.length() < 1) { //om det vi skickade till libris inte ger något svar så har något gått fel
					model.put("librisError", "true");
				}	
				
				model.put("query", userTweet);
				model.put("flaggedWords", flaggedWords);
				model.put("suggestedSentence", suggestedSentence); 
				model.put("words", words); //arraylistan med rättstavning skickas till klitenten
				model.put("sentenceChecked", "true"); //endast när denna är true så visas correction-boxen
			}

			return new ModelAndView(model, "templates/index.html");
		}, new VelocityTemplateEngine());
		
		get("/tweet", (request, response) -> {
			Map<String, Object> model = new HashMap<>();

			//Skall ändast kunna tweeta om användaren är inloggad. Om ej inloggad så stoppas användarens request.
			if (request.session().attribute("oauth_token_secret") == null) {
				halt(401, "Authentication required");
			}
			
			model.put("sentenceChecked", "false"); // initialiserar sentenceChecked till false så att correction-box på html-sidan inte visas förrän en mening skickats till Libris-API
			model.put("librisError", "false"); 
			
			String spellCheckedTweet = request.queryParams("userTweet");
			System.out.println("kom till webapprunner " + spellCheckedTweet);
			
			//Lägger till token och secret till sessionen så att dessa kan användas senare (vid tweetande)
			String token = request.session().attribute("oauth_token");
			String secret = request.session().attribute("oauth_token_secret");

			if (spellCheckedTweet != null && spellCheckedTweet.length() <= 280 && secret != null) { //koden körs endast om användaren skickat med en sträng, samt att denne inte överstiger 280 karaktärer, då detta är max storlek tillåtet för tweets
				if (token!=null) {
					String[] tempArray = token.split("=");
					token = tempArray[1];
					String[] tempArray2 = secret.split("=");
					secret = tempArray2[1];
					}
				CloseableHttpClient httpClient = HttpClients.createDefault();
				HttpPost post = new HttpPost("http://localhost:4567/tweet");
				//Lägger till våra headers
				post.addHeader("Content-Type", "application/x-www-form-urlencoded");
				post.addHeader("tweet", spellCheckedTweet);
				JSONObject tweetJSON = new JSONObject();
		        JSONObject oauth = new JSONObject();
		        oauth.put("access_token", token);
		        oauth.put("secret_key", secret);
		        tweetJSON.put("oauth", oauth);
		        tweetJSON.put("tweet", spellCheckedTweet);
		        System.out.println(tweetJSON.toString());
		        StringEntity entity = new StringEntity(tweetJSON.toString());
		        post.setEntity(entity);

				//Skickar vår POST-request
				httpClient.execute(post);
				model.put("tweetSent", "true");

			}
			
			return new ModelAndView(model, "templates/index.html");
		}, new VelocityTemplateEngine());
		
		//route för api:ets dokumentation
		get("/documentation.html", (request, response) -> {
			Map<String, Object> model = new HashMap<>();
			return new ModelAndView(model, "templates/documentation.html");
		}, new VelocityTemplateEngine());
		
		//ändpunkt för login-funktion
		get("/login", (request, response) -> {
			Map<String, Object> model = new HashMap<>();
			String oauth_token = "";
			
			TwitterLogin login = new TwitterLogin();
			String array[] = login.loginHandler(); //Returnerar oauth_token

			oauth_token = array[0]; //Sparar oauth_token
			
			/*
			 * Omdirigerar användaren för att logga in på vår hemsida med Twitter
			 * Skickar med oauth_token i URL:et 
			 * Vid lyckad inloggning så återkommer användaren till vår callback_url "localhost:4567/Success" som hanteras senare
			 */
			response.redirect("https://api.twitter.com/oauth/authenticate?" + oauth_token); 
			
			return new ModelAndView(model, "templates/index.html");
		}, new VelocityTemplateEngine());
		
		//ändpunkt för utloggning. Dödar nuvarande sessionen genom att ta bort alla session-värden
		get("/logout", (request, response) -> {
			
			Map<String, Object> model = new HashMap<>();
			if (request.session().attribute("loggedIn") != null) { 
				request.session().removeAttribute("loggedIn");
				request.session().removeAttribute("oauth_token");
				request.session().removeAttribute("oauth_token_secret");
				request.session().removeAttribute("user_id");
				request.session().removeAttribute("screen_name");
			} else {
				return new ModelAndView(model, "templates/index.html");
			}

			return new ModelAndView(model, "templates/index.html");
		}, new VelocityTemplateEngine());

		get("/Success", (request, response) -> {
			
			Map<String, Object> model = new HashMap<>();
			String token = request.queryParams("oauth_token");
			String verifier = request.queryParams("oauth_verifier");
			request.session().attribute("loggedIn", "true");
			String[] tokenInfo = null;
			if (token != null) {
				tokenInfo = accesstoken.generateAccessToken(token, verifier);
				}
			
			if (tokenInfo != null) { 
				System.out.println("oauth_token: " + tokenInfo[0]);
				System.out.println("oauth_secret: " + tokenInfo[1]);
				request.session().attribute("oauth_token", tokenInfo[0]);
				request.session().attribute("oauth_token_secret", tokenInfo[1]);
				request.session().attribute("user_id", tokenInfo[2]);
				request.session().attribute("screen_name", tokenInfo[3]);
				} else {
					halt(401, "Inloggning misslyckades");
				}
			response.redirect("/");
			return new ModelAndView(model, "templates/index.html");
		}, new VelocityTemplateEngine());

		
		
	}
}