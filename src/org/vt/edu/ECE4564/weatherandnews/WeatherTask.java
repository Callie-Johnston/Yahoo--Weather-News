package org.vt.edu.ECE4564.weatherandnews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class WeatherTask extends AsyncTask<String, Void, String> {
	public String curLocation_;						//string for the passed in current location
	private MainActivity storeActivity_;			//copy of main activity
	private NewsTask storedNews_;					//copy of news task
	private String city_;							//string for user input city
	private String region_;							//string for user input region/state/country
	private String guiUpdate_;						//string for update date/time
	private String guiCurrentTemp_;					//string for current temperature (F)
	private String guiCondition_;					//string for current condition
	private String guiWind_;						//string for wind chill (F) and wind speed (mph)
	private String guiHumidity_;					//string for humidity in %
	private String guiSunrise_;						//string for sunrise time
	private String guiSunset_;						//string for sunset time
	private String guiEndLocation_;					//string for location according to yahoo
	private String[] guiForecast_ = new String[5];	//string array for forecast days
	
	// Function: WeatherTask
	// Description: constructs a weather task object with the passed
	//		in main activity and news task
	// Inputs: MainActivity myActivity, NewsTask currentNews
	// Outputs: WeatherTask
	public WeatherTask (MainActivity myActivity, NewsTask currentNews) {
		curLocation_ = null;
		storeActivity_ = myActivity;
		storedNews_ = currentNews;
		city_ = null;
		region_ = null;
	}

	// Function: doInBackground
	// Description: Starts the initial http request to find the WOEID.
	//		then continues functions calls to complete parsing and requests
	// Inputs:  String... params
	// Outputs: String
	@Override
	protected String doInBackground(String... params) {
		//split apart the user input into a city and region
		String delims = "[,]+";
		curLocation_ = params[0];
		String[] tokens = curLocation_.split(delims);
		
		//replace spaces with %20 for URL encoding
		city_ = tokens[0].replaceAll(" ", "%20");
		
		region_ = tokens[1].trim();
		
		//Create URL with parameters. App id is hard-coded and provided by Yahoo
		String url = "http://where.yahooapis.com/v1/places.q('" + city_ + "','" + region_ + "')";
		url = url + "?appid=[WjYjGdbV34F0KgWNay6Oi052UV8yGerRmya6Dih9nvWDwkeQjbBa4LWgxyWXI3iyFoXR7ZqCEoWLg6vLyWP15l6eCSyJnVA-]";
		
		//Send GET request to server
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			response = client.execute(request);
		}
		catch (Exception e){
			Log.i("ERROR", e.toString());
		}
		
		//Read response from server and put into readable format
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
		
		String httpResponseVal = sb.toString();
		
		//parse for WOEID (where on earth id)
		String woeid = null;
		if (httpResponseVal != null) {
			woeid = parseForWOEID(httpResponseVal);
		}
		
		//do another request for weather with woeid if one was returned
		String guiInfo = null;
		if (woeid != null) {
			guiInfo = requestWeather(woeid);
		
			//parse for all needed info if weather returned
			if (guiInfo != null) {
				parseForGui(guiInfo);
			}
			else {
				guiEndLocation_ = "No weather available";
			}
		}
		
		//make sure news task has finished
		while (!storedNews_.getFinished());
				
		return "complete!";
	}
	
	// Function: onPostExecute
	// Description: Send all data to main activity
	//		and execute GUI update on main thread
	// Inputs: String incomingString
	// Outputs: none
	protected void onPostExecute(String incomingString) {
		
		//Set up time for last server call
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		storeActivity_.setTimeString(dateFormat.format(date));
		
		//if nothing is available, clear the GUI
		if (guiEndLocation_ == "No weather available") {
			storeActivity_.setUpdateString("");
			storeActivity_.setCurrentTempString("");
			storeActivity_.setConditionsString("");
			storeActivity_.setWindString("");
			storeActivity_.setHumidityString_("");
			storeActivity_.setSunriseString("");
			storeActivity_.setSunsetString_("");
			storeActivity_.setEndLocationString(guiEndLocation_);
			for (int i = 0; i < 5; i++) {
				guiForecast_[i] ="";
			}
			storeActivity_.setForecastString(guiForecast_);
		}
		//else update it with the parsed variables
		else {
			storeActivity_.setUpdateString(guiUpdate_);
			storeActivity_.setCurrentTempString(guiCurrentTemp_);
			storeActivity_.setConditionsString(guiCondition_);
			storeActivity_.setWindString(guiWind_);
			storeActivity_.setHumidityString_(guiHumidity_);
			storeActivity_.setSunriseString(guiSunrise_);
			storeActivity_.setSunsetString_(guiSunset_);
			storeActivity_.setEndLocationString(guiEndLocation_);
			storeActivity_.setForecastString(guiForecast_);
		}
		
		//update the GUI
		storeActivity_.updateGUI();
	}
	
	// Function: parseForWOEID
	// Description: find the woeid in the http response
	// Inputs: String responseString
	// Outputs: String
	private String parseForWOEID(String responseString) {
		String Woeid = null;
		
		//Split by tages
		String[] tokens = responseString.split("><");
		
		//Find the tag that defines the WOEID
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i].contains("woeid")) {
				Woeid = tokens[i];
				break;
			}
		}
		
		//check if null because if so, no data available
		if (Woeid != null) {
			//just retrieve the numerical portion of the line
			Woeid = Woeid.replaceAll("[^0-9]", "");
		}
		else {
			guiEndLocation_ = "No weather available";
		}
		
		return Woeid;
	}
	
	// Function: requestWeather
	// Description: send an http get request with WOEID and return response
	// Inputs: String woeid
	// Outputs: String 
	private String requestWeather(String woeid) {
		String httpResponseVal = null;
		
		//Set up URL with woeid and Farenheit parameter
		String url = "http://weather.yahooapis.com/forecastrss?w=" + woeid + "&u=f";
		
		//Send http GET request
		HttpResponse response = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			response = client.execute(request);
		}
		catch (Exception e){
			Log.i("ERROR", e.toString());
		}
		
		//Read http response
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
		
		httpResponseVal = sb.toString();
		
		return httpResponseVal;
	}
	
	// Function: parseForGui
	// Description: retrieve the info for the GUI by string parsing
	//		the http response
	// Inputs: String inputString
	// Outputs: none
	private void parseForGui(String inputString) {
		int forecastCounter = 0;
		
		//split by new lines
		String[] tokens = inputString.split("\n");
		
		for (int i = 0; i < tokens.length; i++) {
			//if the line contains wind, split and string manipulate
			if (tokens[i].contains("wind")) {
				String[] windTokens = tokens[i].split(" ");
				for (int j = 0; j < windTokens.length; j++) {
					if (windTokens[j].contains("chill")) {
						guiWind_ = windTokens[j].replaceAll("[^0-9]", "");
						guiWind_ = guiWind_ + " F/ ";
					}
					else if (windTokens[j].contains("speed")) {
						guiWind_ = guiWind_ + windTokens[j].replaceAll("[^0-9]", "");
						guiWind_ = guiWind_ + " mph";
					}
				}
			}
			//if the line contains humidity, split and string manipulate
			else if (tokens[i].contains("humidity")) {
				String[] atmosphereTokens = tokens[i].split(" ");
				for (int j = 0; j < atmosphereTokens.length; j++) {
					if (atmosphereTokens[j].contains("humidity")) {
						guiHumidity_ = atmosphereTokens[j].replaceAll("[^0-9]", "");
						guiHumidity_ = guiHumidity_ + "%";
					}
				}
			}
			//if the line contains astronomy, split and string manipulate
			else if (tokens[i].contains("astronomy")) {
				String[] astronomyTokens = tokens[i].split(" ");
				for (int j = 0; j < astronomyTokens.length; j++) {
					if (astronomyTokens[j].contains("sunrise")) {
						guiSunrise_ = astronomyTokens[j].replaceAll("[a-z]", "") + " " + astronomyTokens[j+1];
						guiSunrise_ = guiSunrise_.substring(2, guiSunrise_.length() - 1);
					}
					else if (astronomyTokens[j].contains("sunset")) {
						guiSunset_ = astronomyTokens[j].replaceAll("[a-z]", "") + " " + astronomyTokens[j+1];
						guiSunset_ = guiSunset_.substring(2, guiSunset_.length() - 3);
					}
				}
			}
			//if the line contains pubdate, split and string manipulate
			else if (tokens[i].contains("pubDate")) {
				guiUpdate_ = tokens[i].substring(9, tokens[i].length() - 10);
			}
			//if the line contains condition, split and string manipulate
			else if (tokens[i].contains("condition")) {
				String[] conditionTokens = tokens[i].split(" ");
				for (int j = 0; j < conditionTokens.length; j++) {
					if (conditionTokens[j].contains("temp")) {
						guiCurrentTemp_ = conditionTokens[j].replaceAll("[^0-9]", "");
					}
					else if (conditionTokens[j].contains("code")) {
						int code = Integer.parseInt(conditionTokens[j].replaceAll("[^0-9]", ""));
						guiCondition_ = determineCode(code);
					}
				}
			}
			//if the line contains location, split and string manipulate
			else if (tokens[i].contains("location")) {
				String[] locationTokens = tokens[i].split(" ");
				String cityText = null;
				String regionText = null;
				String countryText = null;
				for (int j = 0; j < locationTokens.length; j++) {
					if (locationTokens[j].contains("city")) {
						if (!locationTokens[j+1].contains("region")) {
							cityText = locationTokens[j].substring(6) + " " + 
									locationTokens[j+1].substring(0, locationTokens[j+1].length() - 1);
						}
						else {
							cityText = locationTokens[j].substring(6, locationTokens[j].length() - 1);
						}
					}
					else if (locationTokens[j].contains("region")) {
						regionText = locationTokens[j].substring(8, locationTokens[j].length() - 1);
					}
					else if (locationTokens[j].contains("country")) {
						if (j+1 < locationTokens.length) {
							countryText = locationTokens[j].substring(9) + " " + 
									locationTokens[j+1].substring(0, locationTokens[j+1].length() - 3);
						}
						else {
							countryText = locationTokens[j].substring(9, locationTokens[j].length() - 3);
						}
					}
				}
				
				guiEndLocation_= cityText + " " + regionText + " " + countryText;
			}
			//if the line is a forecast line
			else if (tokens[i].contains("yweather:forecast")) {
				String[] forecastTokens = tokens[i].split(" ");
				String day = null;
				String date = null;
				String low = null;
				String high = null;
				String condition = null;
				for (int j = 0; j < forecastTokens.length; j++) {
					if (forecastTokens[j].contains("day=")) {
						day = forecastTokens[j].substring(5, forecastTokens[j].length() - 1);
					}
					else if (forecastTokens[j].contains("date=")) {
						date = forecastTokens[j].substring(6) + " " +
								forecastTokens[j+1] + " " + 
								forecastTokens[j+2].substring(0, forecastTokens[j+2].length() - 1);
					}
					else if (forecastTokens[j].contains("low=")) {
						low = forecastTokens[j].substring(5, forecastTokens[j].length() - 1);
					}
					else if (forecastTokens[j].contains("high=")) {
						high = forecastTokens[j].substring(6, forecastTokens[j].length() - 1);
					}
					else if (forecastTokens[j].contains("code=")) {
						int code = Integer.parseInt(forecastTokens[j].substring(6, forecastTokens[j].length() - 1));
						condition = determineCode(code);
					}
				}
				
				//set up the forecast string
				guiForecast_[forecastCounter] = day + ", " + date + ", " + high + " F/" + low + " F, " + condition;
				forecastCounter++;
			}
		}
	}
	
	// Function: determineCode
	// Description: yahoo weather returns condition with a code, this
	//		translates code to condition string
	// Inputs: int code
	// Outputs: String
	private String determineCode(int code) {
		String condition = null;
		
		switch (code) {
			case 0:
				condition = "tornado";
				break;
			case 1:
				condition = "tropical storm";
				break;
			case 2:
				condition = "hurricane";
				break;
			case 3:
				condition = "severe thunderstorms";
				break;
			case 4:
				condition = "thunderstorms";
				break;
			case 5:
				condition = "mixed rain and snow";
				break;
			case 6:
				condition = "mixed rain and sleet";
				break;
			case 7:
				condition = "mixed snow and sleet";
				break;
			case 8:
				condition = "freezing drizzle";
				break;
			case 9:
				condition = "drizzle";
				break;
			case 10:
				condition = "freezing rain";
				break;
			case 11:
				condition = "showers";
				break;
			case 12:
				condition = "showers";
				break;
			case 13:
				condition = "snow flurries";
				break;
			case 14:
				condition = "light snow showers";
				break;
			case 15:
				condition = "blowing snow";
				break;
			case 16:
				condition = "snow";
				break;
			case 17:
				condition = "hail";
				break;
			case 18:
				condition = "sleet";
				break;
			case 19:
				condition = "dust";
				break;
			case 20:
				condition = "foggy";
				break;
			case 21:
				condition = "haze";
				break;
			case 22:
				condition = "smoky";
				break;
			case 23:
				condition = "blustery";
				break;
			case 24:
				condition = "windy";
				break;
			case 25:
				condition = "cold";
				break;
			case 26:
				condition = "cloudy";
				break;
			case 27:
				condition = "mostly cloudy";
				break;
			case 28:
				condition = "mostly cloudy";
				break;
			case 29:
				condition = "partly cloudy";
				break;
			case 30:
				condition = "partly cloudy";
				break;
			case 31: 
				condition = "clear";
				break;
			case 32:
				condition = "sunny";
				break;
			case 33:
				condition = "fair";
				break;
			case 34:
				condition = "fair";
				break;
			case 35:
				condition = "mixed rain and hail";
				break;
			case 36:
				condition = "hot";
				break;
			case 37:
				condition = "isolated thunderstorms";
				break;
			case 38:
				condition = "scattered thunderstorms";
				break;
			case 39:
				condition = "scattered thunderstorms";
				break;
			case 40:
				condition = "scattered showers";
				break;
			case 41:
				condition = "heavy snow";
				break;
			case 42:
				condition = "scattered snow showers";
				break;
			case 43:
				condition = "heavy snow";
				break;
			case 44:
				condition = "partly cloudy";
				break;
			case 45:
				condition = "thundershowers";
				break;
			case 46:
				condition = "snow showers";
				break;
			case 47: 
				condition = "isolated thundershowers";
				break;
			default:
				condition = "not available";
				break;
		}
		
		return condition;
	}
}
