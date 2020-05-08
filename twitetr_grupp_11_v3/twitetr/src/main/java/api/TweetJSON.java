package api;

public class TweetJSON {
	
	private String tweet;
	private String screen_name;
	private String user_id;
	private String created_at;
	private String status;
	
	public TweetJSON(String tweet, String screen_name, String user_id, String created_at) {
		this.tweet = tweet;
		this.screen_name = screen_name;
		this.user_id = user_id;
		this.created_at = created_at;
		status = "OK";
	}
}
