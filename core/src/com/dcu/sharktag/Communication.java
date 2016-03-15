package com.dcu.sharktag;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.dcu.sharktag.ServerRequests.ImageRequest;
import com.dcu.sharktag.ServerRequests.LoginRequest;
import com.dcu.sharktag.ServerRequests.RecoveryRequest;
import com.dcu.sharktag.ServerRequests.RegisterRequest;
import com.dcu.sharktag.ServerRequests.ServerRequestBuilder;
import com.dcu.sharktag.ServerRequests.TagRequest;
import com.dcu.sharktag.ServerRequests.SessionRequest;

public class Communication {
	
	private String serverURL = "http://povilas.ovh:8080";

	private boolean firstTimer = false;
	private String sessionToken = "";
	
	public String jsonValue = "";
	
	private String imageId = "";
	
	//A temporary variable for storing server's messages
	private String tmpString = "";
	
	public void setServerURL(String url){
		serverURL = url;
	}
	
	public String getServerURL(){
		return serverURL;
	}
	
	public String getSessionToken(){
		return sessionToken;
	}
	
	public boolean isFirstTimer(){
		return firstTimer;
	}
	
	public String getTmpString(){
		return tmpString;
	}
	
	// Builds a HttpRequest object from a route and object data
	private HttpRequest buildRequest(String route, Object data) {
		ServerRequestBuilder reqBuilder = new ServerRequestBuilder();
		reqBuilder.newRequest();
		reqBuilder.url(serverURL + route);
		reqBuilder.method(HttpMethods.POST);
		reqBuilder.jsonContent(data);
		return reqBuilder.build();
	}
	
	public String logIn(String username, String password){
		
		String status = "ERROR";
		
		HttpRequest request = buildRequest("/login", new LoginRequest(username, password));
		
		MyHttpResponseListener customListener = new MyHttpResponseListener();
		
		Gdx.net.sendHttpRequest(request, customListener);
		
		while(!customListener.isResponseReceived());
		
		int serverResponse;
		String serverMessage;
		
		if(customListener.getHttpCode() == 200){
			serverResponse = customListener.getInt("success");
			serverMessage = customListener.getString("message");
			
			if(serverResponse == 1){
				status = "";
				sessionToken = customListener.getString("token");
				firstTimer = !customListener.getBoolean("tutorialFinished");
			}
			else{
				status = serverMessage;
			}
		}
		else{
			status = "Server could not be reached";
		}
		
		return status;
	}
	
	public String register(String username, String email, String password){
		
		String status = "ERROR";
		
		HttpRequest request = buildRequest("/register", new RegisterRequest(username, email, password));
		
		MyHttpResponseListener customListener = new MyHttpResponseListener();
		
		Gdx.net.sendHttpRequest(request, customListener);
		
		while(!customListener.isResponseReceived());
		
		int serverResponse;
		String serverMessage;
		
		if(customListener.getHttpCode() == 200){
			serverResponse = customListener.getInt("success");
			serverMessage = customListener.getString("message");
			
			if(serverResponse == 1){
				status = "";
			}
			else{
				status = serverMessage;
			}
		}
		else{
			status = "Server could not be reached";
		}
		
		return status;
	}
	
	// This method returns a URL string to the image provided by the server
	public String requestImage(){
		
		String url = "";
		
		HttpRequest request = buildRequest("/reqimage", new ImageRequest(sessionToken));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int serverStatus = response.getInt("success");
		
		if(serverStatus == 1){
			url = response.getString("URL");
			imageId = response.getString("imageId");
		}
		else{
			String serverMessage = response.getString("message");
		}
		return url;
	}
	
	public Texture fetchImage(String url){
	
		Gdx.app.log("debug", url);
		
		Texture bucket = null;
		byte[] imageData;
		
		HttpRequest request = new HttpRequest(HttpMethods.GET);
		request.setUrl(url);
		request.setContent(null);
		
		MyHttpResponseListener customListener = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, customListener);
		
		while(!customListener.isResponseReceived());
		
		imageData = customListener.getData();
		
		Gdx.app.log("debug", Integer.toString(imageData.length));

		Pixmap pixMap = new Pixmap(imageData, 0, imageData.length);

		bucket = new Texture(pixMap);
		pixMap.dispose();

		return bucket;
	}

	public boolean uploadTags(Array<Tag> tags){
		HttpRequest request = buildRequest("/submittags", new TagRequest(sessionToken, imageId, tags));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int success = response.getInt("success");
		String message = response.getString("message");
		
		return success == 1;
	}
	
	// Set a flag on the server, so that we know the player has gone through the tutorial
	public boolean finishTutorial(){
		HttpRequest request = buildRequest("/finishtutorial", new SessionRequest(sessionToken));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int success = response.getInt("success");
		Gdx.app.log("debug", response.getString("message"));
		
		if(success == 1){
			firstTimer = false;
		}
		
		return success == 1;
	}
	
	// Auto log in using the session token
	public boolean autoLogin(String token){
		HttpRequest request = buildRequest("/autologin", new SessionRequest(token));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int success = response.getInt("success");
		Gdx.app.log("debug", response.getString("message"));
		
		if(success == 1){
			sessionToken = token;
		}
		
		return success == 1;
	}
	
	// Log out and remove the session token from the database
	public boolean logOut(){
		HttpRequest request = buildRequest("/logout", new SessionRequest(sessionToken));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int success = response.getInt("success");
		Gdx.app.log("debug", response.getString("message"));
		
		if(success == 1){
			sessionToken = "";
		}
		
		return success == 1;
	}

	// Request a recovery code from the server
	public String recoverPassword(String username){
		HttpRequest request = buildRequest("/recoverpassword", new RecoveryRequest(username));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int success;
		String message;
		
		if(response.getHttpCode() == 200){
			success = response.getInt("success");
			message = response.getString("message");
			if(success == 1){
				tmpString = response.getString("username");
			}
		}
		else{
			message = "Server could not be reached";
		}
		return message;
	}
	
	// Use the code to change the password
	public String recoverPasswordChange(String username, String pass, String code){
		HttpRequest request = buildRequest("/recoverpasswordchange",
				new RecoveryRequest(username, code, pass));
		
		MyHttpResponseListener response = new MyHttpResponseListener();
		Gdx.net.sendHttpRequest(request, response);
		
		while(!response.isResponseReceived());
		
		int success;
		String message;
		
		if(response.getHttpCode() == 200){
			success = response.getInt("success");
			message = response.getString("message");
		}
		else{
			message = "Server could not be reached";
		}
		return message;
	}
}