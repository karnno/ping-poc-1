package org.ping.services.report;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReportObserver implements Observer {

	private static final Logger LOGGER = Logger.getLogger(ReportObserver.class.getName());
	
	String reportObserverName;
	String urlServer;
	String USER_AGENT ="";
	boolean active = false; 
	public ReportObserver(String name, String urlserver, boolean active) {
		this.reportObserverName = name;
		this.urlServer = urlserver;
		this.active = active;
	}
	
	String jsonFromObservable="";
	@Override
	public void update(Observable o, Object arg) {
//		System.out.println(this.reportObserverName + " =  Report observer is updated with json : " + (String) arg);
		this.jsonFromObservable = (String)arg ;
		
		// logging ! 
		LOGGER.warning("> " + this.jsonFromObservable);
		
		//send the report by POST 
		if (active) {
			try {
			sendingPostRequest();
			} catch (Exception excp) {
				LOGGER.log(Level.SEVERE, " Failed to send JSON report ", excp);
			}
		}
	}

	public String getJsonReport() {
		return this.jsonFromObservable;
	}
	
	private void sendingPostRequest() throws Exception {
		 
		  URL obj = new URL(this.urlServer);
		  HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		 
		        // Setting basic post request
		  con.setRequestMethod("POST");
		  con.setRequestProperty("User-Agent", USER_AGENT);
		  con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		  con.setRequestProperty("Content-Type","application/json");
		 
		  
		  // Send post request
		  con.setDoOutput(true);
		  DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		  wr.writeBytes(this.jsonFromObservable);
		  wr.flush();
		  wr.close();
		 
		  int responseCode = con.getResponseCode();
		  System.out.println("nSending 'POST' request to URL : " + this.urlServer);
		  System.out.println("Post Data : " + this.jsonFromObservable);
		  System.out.println("Response Code : " + responseCode);
		 
		  BufferedReader in = new BufferedReader(
		          new InputStreamReader(con.getInputStream()));
		  String output;
		  StringBuffer response = new StringBuffer();
		 
		  while ((output = in.readLine()) != null) {
		   response.append(output);
		  }
		  in.close();
		  
		  //printing result from response
		  System.out.println("Server answer back : " + response.toString());
		 }
	
}
