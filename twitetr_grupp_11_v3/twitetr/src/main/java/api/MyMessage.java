package api;

import java.util.ArrayList;

public class MyMessage {

	private String query;
	private boolean found_suggestion;
	private String suggested_sentence;
	private ArrayList<Object> flagged_words;

	public MyMessage(String str, boolean b, String s, ArrayList<Object> a) {
		this.query = str;
		this.found_suggestion = b;
		this.suggested_sentence = s;
		this.flagged_words = a;
	}

}
