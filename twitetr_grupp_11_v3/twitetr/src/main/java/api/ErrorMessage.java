package api;

public class ErrorMessage {

	private int code;
	private String error_message;

	public ErrorMessage(int responseCode) {
		this.code = responseCode;
		if (responseCode == 187) {
			error_message = "Status is a duplicate";
		} else if (responseCode == 89) {
			error_message = "Invalid or expired token";
		} else if (responseCode == 186) {
			error_message = "Tweet needs to be a bit shorter";
		} else if (responseCode == 215) {
			error_message = "Bad authentication data";
		} else if (responseCode == 1) {
			error_message = "Missing parameters";
		} else if (responseCode == 2) {
			error_message = "Invalid parameter data";
		} else if (responseCode == 32) {
			error_message = "Could not authenticate you";
		} else {
			error_message = "Bad request";
		}
		this.code = responseCode;
	}

}
