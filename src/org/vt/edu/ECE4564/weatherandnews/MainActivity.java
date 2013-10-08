package org.vt.edu.ECE4564.weatherandnews;

import java.util.Timer;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Button goButton_;			//button used for triggering http requests
	private EditText locationText_;		//user input box for location
	TextView updateText_;				//text view for update date/time
	TextView currentTempText_;			//text view for current temperature in F
	TextView conditionsText_;			//text view for current conditions
	TextView windText_;					//text view for wind chill in F and wind speed in mph
	TextView humidityText_;				//text view for humidity as a percent
	TextView sunriseText_;				//text view for sunrise time
	TextView sunsetText_;				//text view for sunset time
	TextView news0_;					//text view for news story 0
	TextView news1_;					//text view for news story 1
	TextView news2_;					//text view for news story 2
	TextView news3_;					//text view for news story 3
	TextView news4_;					//text view for news story 4
	TextView news5_;					//text view for news story 5
	TextView endLocationText_;			//text view for retrieved location information
	TextView timeText_;					//text view for last time server was called
	TextView forecast0_;				//text view for forecast day 0
	TextView forecast1_;				//text view for forecast day 1
	TextView forecast2_;				//text view for forecast day 2
	TextView forecast3_;				//text view for forecast day 3
	TextView forecast4_;				//text view for forecast day 4
	
	private String updateString_ = null;				//string for update text view
	private String currentTempString_ = null;			//string for current temp text view
	private String conditionsString_ = null;			//string for condition text view
	private String windString_ = null;					//string for wind text view
	private String humidityString_ = null;				//string for humidity text view
	private String sunriseString_ = null;				//string for sunrise text view
	private String sunsetString_ = null;				//string for sunset text view
	private String[] newsString_ = new String[20];		//string array for news text views
	private String endLocationString_ = null;			//string for end location text view
	private String timeString_ = null;					//string for time text view
	private String[] forecastString_ = new String[5];	//string array for forecast text views
	
	// Function: onCreate
	// Description: This function creates the GUI and sets up the listener for the button
	// 		by finding the objects by ID in the view. It also creates and starts a timer
	//		used for polling the server.
	// Inputs: Bundle savedInstanceState
	// Outputs: None
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		goButton_ = (Button) findViewById(R.id.startThreads);
		locationText_ = (EditText) findViewById(R.id.location);
		updateText_ = (TextView) findViewById(R.id.updateText);
		currentTempText_ = (TextView) findViewById(R.id.currentTemp);
		conditionsText_ = (TextView) findViewById(R.id.conditions);
		windText_ = (TextView) findViewById(R.id.wind);
		humidityText_ = (TextView) findViewById(R.id.humidity);
		sunriseText_ = (TextView) findViewById(R.id.sunrise);
		sunsetText_ = (TextView) findViewById(R.id.sunset);
		news0_ = (TextView) findViewById(R.id.news0);
		news1_ = (TextView) findViewById(R.id.news1);
		news2_ = (TextView) findViewById(R.id.news2);
		news3_ = (TextView) findViewById(R.id.news3);
		news4_ = (TextView) findViewById(R.id.news4);
		news5_ = (TextView) findViewById(R.id.news5);
		endLocationText_ = (TextView) findViewById(R.id.locText);
		timeText_ = (TextView) findViewById(R.id.timeText);
		forecast0_ = (TextView) findViewById(R.id.forecast0);
		forecast1_ = (TextView) findViewById(R.id.forecast1);
		forecast2_ = (TextView) findViewById(R.id.forecast2);
		forecast3_ = (TextView) findViewById(R.id.forecast3);
		forecast4_ = (TextView) findViewById(R.id.forecast4);
		
		//Create a new timer, must only be done once
		final Timer timer = new Timer();
		final DelayTimer delay = new DelayTimer(MainActivity.this, null);
		
		//schedule a timer to fire every 10 minutes, irrelevant to whether location has changed or not
		timer.schedule(delay, 600000, 600000);
				
		goButton_.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				//get user input string
				String locationString = locationText_.getText().toString();
				
				//start the tasks with that input
				startTasks(locationString);
				
				//update location
				delay.setLocationString(locationString);
			}
		
		});
	}

	// Function: onCreateOptionsMenu
	// Description: Inflate the menu: this adds items to the action bar if it is present
	// Inputs: Menu menu
	// Outputs: boolean
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	// Function: startTasks
	// Description: Creates and executes the news task and 
	//		the weather task with the passed in location string
	// Inputs: String locationString
	// Outputs: none
	public void startTasks(String locationString) {
		
		final NewsTask currentNews = new NewsTask(MainActivity.this);
		
		currentNews.execute("start");
		
		final WeatherTask myTask = new WeatherTask(MainActivity.this, currentNews);
		
		myTask.execute(locationString);	
	}
	
	// Function: updateGUI
	// Description: Places the stored string values into their
	//		respective text views. This is called once both
	//		tasks have completed.
	// Inputs: none
	// Outputs: none
	public void updateGUI() {
		updateText_.setText(updateString_);
		currentTempText_.setText(currentTempString_);
		conditionsText_.setText(conditionsString_);
		windText_.setText(windString_);
		humidityText_.setText(humidityString_);
		sunriseText_.setText(sunriseString_);
		sunsetText_.setText(sunsetString_);
		endLocationText_.setText(endLocationString_);
		timeText_.setText(timeString_);
		
		int counter = 0;
		for (int i = 0; i < 20; i++) {
			if (newsString_[i] != null) {
				switch (counter) {
					case 0:
						news0_.setText(Html.fromHtml(newsString_[i]));
						news0_.setMovementMethod(new LinkMovementMethod());
						break;
					case 1:
						news1_.setText(Html.fromHtml(newsString_[i]));
						news1_.setMovementMethod(new LinkMovementMethod());
						break;
					case 2:
						news2_.setText(Html.fromHtml(newsString_[i]));
						news2_.setMovementMethod(new LinkMovementMethod());
						break;
					case 3:
						news3_.setText(Html.fromHtml(newsString_[i]));
						news3_.setMovementMethod(new LinkMovementMethod());
						break;
					case 4:
						news4_.setText(Html.fromHtml(newsString_[i]));
						news4_.setMovementMethod(new LinkMovementMethod());
						break;
					case 5:
						news5_.setText(Html.fromHtml(newsString_[i]));
						news5_.setMovementMethod(new LinkMovementMethod());
						break;
				}
				counter++;
				if (counter == 6) {
					break;
				}
			}
		}
		
		counter = 0;
		for (int i = 0; i < 5; i++) {
			if (forecastString_[i] != null) {
				switch (counter) {
					case 0:
						forecast0_.setText(forecastString_[i]);
						break;
					case 1:
						forecast1_.setText(forecastString_[i]);
						break;
					case 2:
						forecast2_.setText(forecastString_[i]);
						break;
					case 3:
						forecast3_.setText(forecastString_[i]);
						break;
					case 4:
						forecast4_.setText(forecastString_[i]);
						break;
				}
				counter++;
			}
		}
	}


	// Function: setUpdateString
	// Description: Sets the updateString with the passed in string
	// Inputs: String updateString
	// Outputs: none
	public void setUpdateString(String updateString_) {
		this.updateString_ = updateString_;
	}

	// Function: setCurrentTempString
	// Description: sets the currentTempString with the passed in string
	// Inputs: String currentTempString
	// Outputs: none
	public void setCurrentTempString(String currentTempString_) {
		this.currentTempString_ = currentTempString_;
	}


	// Function: setConditionsString
	// Description: sets the conditionsString with the passed in string
	// Inputs: String conditionsString
	// Outputs: none
	public void setConditionsString(String conditionsString_) {
		this.conditionsString_ = conditionsString_;
	}

	// Function: setWindString
	// Description: sets the windString with the passed in string
	// Inputs: String windString
	// Outputs: none
	public void setWindString(String windString_) {
		this.windString_ = windString_;
	}

	// Function: setHumidityString
	// Description: sets the humidityString with the passed in string
	// Inputs: String humidityString
	// Outputs: none
	public void setHumidityString_(String humidityString_) {
		this.humidityString_ = humidityString_;
	}

	// Function: setSunriseString
	// Description: sets the sunriseString with the passed in string
	// Inputs: String sunriseString
	// Outputs: none
	public void setSunriseString(String sunriseString_) {
		this.sunriseString_ = sunriseString_;
	}

	// Function: setSunsetString
	// Description: sets the sunsetString with the passed in string
	// Inputs: String sunsetString
	// Outputs: none
	public void setSunsetString_(String sunsetString_) {
		this.sunsetString_ = sunsetString_;
	}

	// Function: setNewsString
	// Description: sets the newsString array with the passed in string array
	// Inputs: String[] newsString
	// Outputs: none
	public void setNewsString(String[] newsString_) {
		this.newsString_ = newsString_;
	}

	// Function: setEndLocationString
	// Description: sets the endLocationString with the passed in string
	// Inputs: String endLocationString
	// Outputs: none
	public void setEndLocationString(String endLocationString_) {
		this.endLocationString_ = endLocationString_;
	}

	// Function: setTimeString
	// Description: sets the timeString with the passed in string
	// Inputs: String timeString
	// Outputs: none
	public void setTimeString(String timeString_) {
		this.timeString_ = timeString_;
	}

	// Function: setForecastString
	// Description: sets the forecastString array with the passed in string array
	// Inputs: String[] forecastString
	// Outputs: none
	public void setForecastString(String[] forecastString) {
		this.forecastString_ = forecastString;
	}
}
