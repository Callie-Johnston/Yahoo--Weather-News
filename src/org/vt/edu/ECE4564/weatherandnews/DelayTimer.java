package org.vt.edu.ECE4564.weatherandnews;

import java.util.TimerTask;

public class DelayTimer extends TimerTask {
	private String location_;    //used for determining the WOEID for the weather
	MainActivity myActivity_;	 // reference to the main activity so that the tasks can be started
	
	// Function: DelayTimer
	// Description: constructs a delay timer with the passed in
	//		main activity and location
	// Inputs: MainActivity myActivity, String location
	// Outputs: DelayTimer
	public DelayTimer (MainActivity myActivity, String location) {
		myActivity_ = myActivity;
		location_ = location;
	}

	// Function: run
	// Description: Starts the tasks on the main thread using
	//		the stored main activity
	// Inputs: none
	// Outputs: none
	@Override
	public void run() {
		myActivity_.startTasks(location_);
	}
	
	public void setLocationString(String newString) {
		location_ = newString;
	}
}
