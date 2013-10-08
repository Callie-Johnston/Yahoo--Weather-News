package org.vt.edu.ECE4564.weatherandnews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class NewsTask extends AsyncTask<String, Void, String>{
	private Boolean finished_ = false;
	private MainActivity storedActivity_ = null;
	private String[] finalNews_ = new String[20];
	
	
	// Function: NewsTask
	// Description: constructs a news task with give main activity
	// Inputs: MainActivity myActivity
	// Outputs: NewTask
	public NewsTask(MainActivity myActivity) {
		storedActivity_ = myActivity;
	}

	// Function: doInBackground
	// Description: Retrieves the http get response from the server.
	//		parses for needed titles and links for news stories
	// Inputs: String... params
	// Outputs: String
	@Override
	protected String doInBackground(String... params) {
		
		//Create URL for news stories
		String url = "http://rss.news.yahoo.com/rss/";
		
		//Retrieve http response from server
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			response = client.execute(request);
		}
		catch (Exception e){
			Log.i("ERROR", e.toString());
		}
		
		//Read in the http response to form a readable string
		String line = null;
		StringBuilder sb = new StringBuilder();
		try {
			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		}
		catch (IOException e) {
			Log.i("ERROR", e.toString());
		}
		
		//Allocate memory for variables
		String httpResponseVal = sb.toString();
		String[] titles = new String[20];
		String[] addresses = new String[20];
		int counter = 0;
		
		//Split the http response into items
		String[] tokens = httpResponseVal.split("item");
		
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains("<title>")) {
				//split by headers
				String[] newsTokens = tokens[i].split("><");
				for (int j = 0; j < newsTokens.length; j++) {
					//found a title, now grab it exactly by string manipulation
					if (newsTokens[j].contains("title>") && newsTokens[j].contains("</title")) {
						if (!newsTokens[j].contains("version=") &&
								!newsTokens[j].contains("channel>") &&
								!newsTokens[j].contains("description>")) {
							String title = newsTokens[j].substring(6, newsTokens[j].length() - 7);
							if (counter < 20) {
								titles[counter] = title;
							}
						}
					}
					//found a link, now grab it exactly by string manipulation
					else if (newsTokens[j].contains("link>") && newsTokens[j].contains("</link")) {
						String address = newsTokens[j].substring(5, newsTokens[j].length() - 6);
						if (counter < 20) {
							addresses[counter] = address;
						}
					}
				}
				counter++;
			}
		}
		
		//form html strings
		String[] outputString = new String[20];
		for (int i =0; i < 20; i++) {
			if (titles[i] != null && addresses[i] != null) {
				outputString[i] = "<a href='" + addresses[i] + "'>" + titles[i] + "</a>";
			}
		}
		
		//set them in object
		finalNews_ = outputString;
		
		//tell the weather task that we are finished
		setFinished(true);
		
		return null;
	}
	
	// Function: onPostExecute
	// Description: Stores the news into the GUI on the main thread
	// Inputs: String incomingString
	// Outputs: none
	protected void onPostExecute(String incomingString) {
		storedActivity_.setNewsString(finalNews_);
	}
	
	// Function: getFinished
	// Description: returns whether the task has finished 
	// Inputs: none
	// Outputs: Boolean
	public Boolean getFinished() {
		return finished_;
	}

	// Function: setFinished
	// Description: sets the finished flag
	// Inputs: Boolean finished
	// Outputs: none
	public void setFinished(Boolean finished) {
		this.finished_ = finished;
	}

}
