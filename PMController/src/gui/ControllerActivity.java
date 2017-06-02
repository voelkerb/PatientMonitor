/**
 *
 * @package Patient Monitor - Controller
 * @copyright (c) 2014 University of Freiburg
 * @author Benjamin Völker
 * @email voelkerb@informatik.uni-freiburg.de
 * @date 17.11.2014
 * @summary Controller view with all UI elements for setting the correct health parameters. 
 * 
 */


package gui;

// Import stuff goes here
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Scenario.Event;
import Scenario.Event.TimerState;
import Scenario.Scenario;
import Scenario.ScenarioHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pmcontroller1.R;

@SuppressLint({ "InflateParams", "InlinedApi" }) 
/*
 * Controller Class that handles every user interaction like button presses or slider changes
 * Displaying all values correct
 */
public class ControllerActivity extends Activity {
	// Debug mode shows debug messages all over the application
	private static final boolean DEBUG = false;

	// Set this to true if you want to shortly show which patterns are set
	private static final boolean SHOW_PATTERN_TOAST = false;
	// Set this to true, id you want to show a message if a protocoll gets stored
	private static final boolean SHOW_STORE_TOAST = true;
	// If you need a Preference window, set this to true
	public static final boolean PREFERENCE_WINDOW_ACTIVE = false;
	// Auto hide the Action bar after this time
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	// If hiding was initialized don't hide again
	private boolean WILL_HIDE = false;
	
	// If curves should be activated without an apply button press
	private static final boolean ACTIVATE_CURVES_WITHOUT_APPLY = true;

	// If a new scenario is being recorded
	private boolean new_scenario = false;
	// If protocoll is recorded
	private boolean new_protocoll = false;
	// If the timer is currently not running
	private boolean timer_paused = true;
	// If one popup is active on screen
	
	// All UI-Buttons on the view, which text get changed
	private ImageButton apply_button, dismiss_button = null;
	private Button scenario_button, reset_timer_button, pause_timer_button = null;
	private Button heart_rate_active_button, o2_rate_active_button, co2_rate_active_button,
					blood_pressure_active_button, raspiration_rate_active_button = null;
	private CheckBox cuff_attached = null;
	
	// The horizontal Seekbar for the time schedule
	private SeekBar scheduler_slider = null;
	// All vertical Seekbars for the health information values
	private VerticalSeekBar heart_rate_slider, blood_pressure_systolic_slider,
					blood_pressure_diastolic_slider, o2_rate_slider, co2_rate_slider, raspiration_rate_slider = null;
	// The Text views showing the current value of the sliders
	private TextView scheduler_slider_value, heart_rate_slider_value, blood_pressure_slider_value,
					o2_rate_slider_value, co2_rate_slider_value, raspiration_rate_slider_value = null;
	// The Text view displaying the timer value
	private TextView timer_value = null;

	// The spinner items for selecting the curve
	private Spinner spinner_heart_rate, spinner_o2_rate, spinner_co2_rate, spinner_blood_pressure, spinner_raspiration_rate = null;
	// The chosen pattern value for each spinner
	private int heart_rate_pattern, o2_rate_pattern, co2_rate_pattern, blood_pressure_pattern, raspiration_rate_pattern = 0; 
	// The image array adapter handling the spinner items
	private ImageArrayAdapter adapter_heart_rate, adapter_o2_rate, adapter_co2_rate, adapter_blood_pressure, adapter_raspiration_rate = null;
	
	// Integer values holding the SEEKBAR value (range 0 to maxvalue - minvalue .. e.g. heart rate range (20-250) -> heart rate value (0-230))
	// To get the correct value, add the minimum value
	private Integer scheduler_value, heart_rate_value, o2_rate_value, co2_rate_value, blood_pressure_systolic_value, 
					blood_pressure_diastolic_value, raspiration_rate_value = 0;
	
	// If health information should be visible at the monitor
	private boolean heart_rate_value_active, o2_rate_value_active, co2_rate_value_active, blood_pressure_value_active, cuff_active,
					raspiration_rate_value_active = false;
	private boolean heart_rate_value_last_active, o2_rate_value_last_active, co2_rate_value_last_active, blood_pressure_value_last_active,
					cuff_last_active, raspiration_rate_value_last_active = false;
	
	// Handling the display of the timer
	// The time in milliseconds when the timer gets updated again
	private final int TIMER_UPDATE_RATE = 1000;
	private Handler timer_handler = new Handler();
	Runnable updateTimerMethod = null;
	// Holds the time in milliseconds since the timer is started (hours, minutes and seconds can be calculated from this)
	long time_millies = 0L;
	// Swap due to pause and resume
	long time_swap = 0L;
	// Holds the time in milliseconds of the timer (with swap, hours, minutes and seconds can be calculated from this)
	long final_time = 0L;
	// The system time when the timer gets started
	long start_time = 0L;
	
	// DATABASE for scenario and protocoll
	List<Event> eventList_scenario = null;
	List<Event> eventList_protocoll = null;
	ScenarioHelper scenarioHelper = null;
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * On create of the Controller activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		// Overlay action bar and content, so that the content does not get shrinked if the action bar is visible
		this.getWindow();
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		// Set up the actionbar correct
		setupActionBar();
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// Set the view
		setContentView(R.layout.activity_controller);
		
		// Make the statusbar disappear after initial delay
		delayedHide(AUTO_HIDE_DELAY_MILLIS);
		
		// Set up the back view for onclicklistener
		final View backView = findViewById(R.id.background_view);
		// If the background is clicked, the status bar should appear
		backView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getActionBar().show();
				// Hide it again after delay if not yet scheduled
				if (!WILL_HIDE)	delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
		});

		

		// Init UI elements...
		
		// ... Timer
		timer_value = (TextView) findViewById(R.id.timer_text_view);
		initTimer();

		// ... Buttons
    	initButtons();
		// ... Sliders
		initSliders();
		// ... Spinners
		initSpinners();

		// Disable apply and dismiss buttons at first
		disableButtons();
		
		// Open scenario data base
		scenarioHelper = new ScenarioHelper(getBaseContext());
	}

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 * If application is closed, the NSD manager must be also closed to avoid reinitialisation of NSD service with the same name
	 */
	@Override
	public void onDestroy() {
		if (DEBUG) Toast.makeText(getApplicationContext(),"Closed Controller Activity", Toast.LENGTH_SHORT).show();
		super.onDestroy();	    
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 * If dialog boxes or other notification appear, the navigation and status bar will get visible -> hide it again if done
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// make the activity fulscreen without navigation bar
		if (Build.VERSION.SDK_INT >= 16) { // Jelly Bean
		    if (hasFocus) {
		    	this.findViewById(android.R.id.content).setSystemUiVisibility(
		                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
		                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		    }
		} else if (Build.VERSION.SDK_INT >= 19) { //KITKAT
		    if (hasFocus) {
		    	this.findViewById(android.R.id.content).setSystemUiVisibility(
		                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
		                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		    }
		}
	}
	
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 * If the application resumes from other applications, make it full screen again
	 */
	@Override
	protected void onResume() {
		super.onResume();

		// make the activity fulscreen without navigation bar
		if (Build.VERSION.SDK_INT >= 16) { //Jelly Bean
		    this.findViewById(android.R.id.content).setSystemUiVisibility(
		                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
		                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		} else if (Build.VERSION.SDK_INT >= 19) { //KITKAT
		    	this.findViewById(android.R.id.content).setSystemUiVisibility(
		                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_FULLSCREEN
		                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.controller, menu);

		// Hide Preference Window if not needed
		if (PREFERENCE_WINDOW_ACTIVE) {
			menu.add(Menu.NONE, R.id.action_settings, 100, "")
	    	.setIcon(R.drawable.ic_action_settings)
	    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
	    
        getActionBar().setDisplayShowHomeEnabled(false);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 * Handling pressed on the action bar here
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		// If up/home pressed, go hirachically up
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		// If settings icon pressed, open settings activity
		} else if (id == R.id.action_settings) {
			startActivity (new Intent (this, PreferenceActivity.class));
			return true;
			// If protocol icon pressed, open protocol activity
		} else if (id == R.id.action_protocoll) {
			ProtocollActivity._scenarioHelper = scenarioHelper;
			startActivity (new Intent (this, ProtocollActivity.class));
			return true;
		// If scenario pressed, open scenario activity
		} else if (id == R.id.action_scenario) {
			ScenarioActivity._scenarioHelper = scenarioHelper;
			startActivity (new Intent (this, ScenarioActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	/*
	 * Set up the action bar, if it is available
	 */
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Show the Up button in the action bar.
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
	
	/*
	 * Schedules a call to hide() the action bar in milliseconds,
	 */
	private void delayedHide(int delayMillis) {
		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			  @Override
			  public void run() {
				  // Hide the action bar
				  getActionBar().hide();
				  // Reset flag
				  WILL_HIDE = false;
			  }
			}, delayMillis);
		// Flag that hiding is in progress
		WILL_HIDE = true;
	}	
	
	
	/*
	 * Called if the curves should be activated right on button click
	 */
    void handleCurveButtonPressed() {
    	// Store current active values
		heart_rate_value_last_active = heart_rate_value_active;
		o2_rate_value_last_active = o2_rate_value_active;
		co2_rate_value_last_active = co2_rate_value_active;
		raspiration_rate_value_last_active = raspiration_rate_value_active;
		blood_pressure_value_last_active = blood_pressure_value_active;
		cuff_last_active = cuff_active;
		// Get new schedule value
		scheduler_value = Integer.valueOf(scheduler_slider.getProgress());
		
		// Create and send the event with old slider values leave apply and dismiss buttons as they are
		createAndSendEvent(0, false);
    }
    
	
	/*
	 *  Called when the user clicks the Apply button 
	 */
	public void applyPressed(View view) {
       	// Store current slider values
		blood_pressure_systolic_value = Integer.valueOf(blood_pressure_systolic_slider.getProgress());
		blood_pressure_diastolic_value = Integer.valueOf(blood_pressure_diastolic_slider.getProgress());
		heart_rate_value = Integer.valueOf(heart_rate_slider.getProgress());
		o2_rate_value = Integer.valueOf(o2_rate_slider.getProgress());
		co2_rate_value = Integer.valueOf(co2_rate_slider.getProgress());
		raspiration_rate_value = Integer.valueOf(raspiration_rate_slider.getProgress());
		scheduler_value = Integer.valueOf(scheduler_slider.getProgress());

    	// Store current patterns
		heart_rate_pattern = spinner_heart_rate.getSelectedItemPosition();
		o2_rate_pattern = spinner_o2_rate.getSelectedItemPosition();
		co2_rate_pattern = spinner_co2_rate.getSelectedItemPosition();
		raspiration_rate_pattern = spinner_raspiration_rate.getSelectedItemPosition();
		blood_pressure_pattern = spinner_blood_pressure.getSelectedItemPosition();

    	// Store current active values
		heart_rate_value_last_active = heart_rate_value_active;
		o2_rate_value_last_active = o2_rate_value_active;
		co2_rate_value_last_active = co2_rate_value_active;
		raspiration_rate_value_last_active = raspiration_rate_value_active;
		blood_pressure_value_last_active = blood_pressure_value_active;
		cuff_last_active = cuff_active;
			
		// Create and send the event
		createAndSendEvent(0, false);
		
		// Disable apply and dismiss button till next slider or pattern change
		disableButtons();	
    }
    
	/*
	 * create and event and Send it if needed. Pass if the timer needs to be synced or the flag needs to be set in the protocoll
	 */
	void createAndSendEvent(int timerSync, boolean flag) {
		Calendar c = Calendar.getInstance(); 
		Integer seconds = c.get(Calendar.SECOND);
		Integer minutes = c.get(Calendar.MINUTE);
		Integer hours = c.get(Calendar.HOUR_OF_DAY);
		Integer day = c.get(Calendar.DAY_OF_MONTH);
		Integer month = c.get(Calendar.MONTH);
		Integer year = c.get(Calendar.YEAR);
		Integer timer = (int) (final_time / 1000);
		
    	boolean syncTimer = false;
		if (timerSync != 0) syncTimer = true;
		TimerState timerState = Scenario.intToTimerState(timerSync);
		
		Event event = new Event(
				/* Scheduler value */
				scheduler_value + getResources().getInteger(R.integer.min_scheduler_slider),
				/* Heart rate value and pattern */
				heart_rate_value + getResources().getInteger(R.integer.min_heart_rate_slider),
				Scenario.intToHeartPattern(heart_rate_pattern),
				/* Blood pressure systolic, diastolic and pattern */
				blood_pressure_systolic_value + getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider),
				blood_pressure_diastolic_value + getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider),
				Scenario.intToBpPattern(blood_pressure_pattern),
				/* Oxygen value and pattern */
				o2_rate_value + getResources().getInteger(R.integer.min_o2_rate_slider),
				Scenario.intToO2Pattern(o2_rate_pattern),
				/* Raspiration value and pattern */
				raspiration_rate_value + getResources().getInteger(R.integer.min_raspiration_rate_slider),
				Scenario.intToRespPattern(raspiration_rate_pattern),
				/* CO2 value */
				co2_rate_value + getResources().getInteger(R.integer.min_co2_rate_slider),
				Scenario.intToCarbPattern(co2_rate_pattern),
				/* Timestamp */
				timer,
				/* Active buttons */
				heart_rate_value_active, blood_pressure_value_active, cuff_active, o2_rate_value_active, co2_rate_value_active , raspiration_rate_value_active,
				/* Sync Timer */
				syncTimer,
				/* Flag */
				flag,
				/* Timer state */
				timerState);
    
		
		// If a new scenario is being recorded, create event object and store it
		if (new_scenario) {
			// Add event to list
	    	eventList_scenario.add(event);
		    if (DEBUG) {
		    	System.out.println("Scenario: new Event added:");
		    	System.out.println(event.toJson().toString());
		    }
		}
		
		// If protocoll is activated create event object and store it
		if (new_protocoll) {
			// Add event to list
	    	eventList_protocoll.add(event);
		    if (DEBUG) {
		    	System.out.println("Protocoll: new Event added:");
		    	System.out.println(event.toJson().toString());
		    }
		}
		
		// Display toasti
    	if (DEBUG) {
    		new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
		    .setTitle("(DEBUG) Event generated:")
	    	.setMessage("Date: " + hours + ":" + minutes + ":" + seconds 
    			+ " " + day + "/" + month + "/" + year + "\n" + event.toString())
		    // If Save button is pressed on the alert dialog
		    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int whichButton) {
				        	// Get the typed name
				        }
		            }).show();
    	}
		
		
		// Send to Server if it was not just a flag
		if (!flag) {
			if (MainActivity.server != null) {
				MainActivity.server.out(event.toJson().toString());
			} else {
				if (MainActivity.CHECK_WIFI) {
					// Show connection alert
		        	new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
				    .setTitle(R.string.connection_alert_title)
			    	.setMessage(R.string.connection_alert_text)
				    // If Save button is pressed on the alert dialog
				    .setPositiveButton(R.string.connection_alert_button, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
						        }
				            }).show();
				}
			}
		}
    	
    }	
    
	/*
	 *  Called when the user clicks the Dismissed button
	 */
    public void dismissPressed(View view) {
    	// Display toasti
    	if (DEBUG) {
	    	Toast toast = Toast.makeText(this, "Dismiss Button pressed", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
			toast.show();
    	}
    	// Revert the slider values to their last applied position
		blood_pressure_systolic_slider.setProgress(blood_pressure_systolic_value.intValue());
		blood_pressure_diastolic_slider.setProgress(blood_pressure_diastolic_value.intValue());
		// Again the value of the systolic slider because the constraints of the slider maximum and minimum values
		// not calling this again causes dependency problems if the systolic slider needs to be setted to a lower 
		// value than the current value of the diastolic slider
		blood_pressure_systolic_slider.setProgress(blood_pressure_systolic_value.intValue());
		heart_rate_slider.setProgress(heart_rate_value.intValue());
		o2_rate_slider.setProgress(o2_rate_value);
		co2_rate_slider.setProgress(co2_rate_value);
		raspiration_rate_slider.setProgress(raspiration_rate_value);
		scheduler_slider.setProgress(scheduler_value);

    	// Revert the spinner patterns to their last applied position
		spinner_heart_rate.setSelection(heart_rate_pattern, true);
		spinner_o2_rate.setSelection(o2_rate_pattern, true);
		spinner_co2_rate.setSelection(co2_rate_pattern, true);
		spinner_raspiration_rate.setSelection(raspiration_rate_pattern, true);
		spinner_blood_pressure.setSelection(blood_pressure_pattern, true);
		
		// Revert the activate buttons/checkboxes back
		if (heart_rate_value_last_active){
			heart_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
	   	} else {
	   		heart_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
	   	}
		if (o2_rate_value_last_active){
			o2_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
	   	} else {
	   		o2_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
	   	}
		if (co2_rate_value_last_active){
			co2_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
	   	} else {
	   		co2_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
	   	}
		if (raspiration_rate_value_last_active){
			raspiration_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
	   	} else {
	   		raspiration_rate_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
	   	}
	   	if (blood_pressure_value_last_active){
	       	 blood_pressure_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
	   	} else {
	   		 blood_pressure_active_button.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
	   	}
	   	if (cuff_last_active){
			cuff_attached.setChecked(true);
	   	} else {
	   		cuff_attached.setChecked(false);
	   	}
	   	// Clear current active values
	 	heart_rate_value_active = heart_rate_value_last_active;
	 	o2_rate_value_active = o2_rate_value_last_active;
	 	co2_rate_value_active = co2_rate_value_last_active;
	 	raspiration_rate_value_active = raspiration_rate_value_last_active;
	 	blood_pressure_value_active = blood_pressure_value_last_active;
	 	cuff_active = cuff_last_active;
	 		
		// Disable apply and dismiss button till a slider change
		disableButtons();
    }
	    

	/*
	 *  Called when the user wants to reset the timer 
	 */
    public void resetTimer(View view) {
    	if (new_protocoll) {
		    // Display toasti	
	    	if (DEBUG) {
		    	Toast toast = Toast.makeText(this, "Timer resetted", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
				toast.show();
	    	}
	    	// Store current protocoll
	    	storeProtocol();
	    	// Stop protocoll
	    	new_protocoll = false;
	    	// The timer is paused on a reset
	    	timer_paused = true;
	    	// Set the text of the pause button to display "Start" 
	    	pause_timer_button.setText( getResources().getString(R.string.start_timer_button));
	    	reset_timer_button.setBackground( getResources().getDrawable(R.drawable.gray_button));
	    	reset_timer_button.setEnabled(false); 
	    	// Remove the runnable methode to update the timer
	    	timer_handler.removeCallbacks(updateTimerMethod);
	    	// Clear the swap
	    	time_swap = 0;
	    	// Set timer text view to show 0
			timer_value.setText(getResources().getString(R.string.timer_init_text));
			// Reset Final Time
			final_time = 0;
			// Create an event to sync the timer
			createAndSendEvent(4, false);
    	}
    }
    
    
	/*
	 *  Called when the user wants to pause the timer
	 */
    public void pauseTimer(View view) {
    	// If the timer is running
    	if (!timer_paused) {
        	// Display toasti
	    	if (DEBUG) {
		    	Toast toast = Toast.makeText(this, "Timer paused", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
				toast.show();
	    	}
	    	// Add to the swap
	    	time_swap += time_millies;
	    	// Stop the update timer methode
	    	timer_handler.removeCallbacks(updateTimerMethod);
	    	// Set text of button to "Resume"
			pause_timer_button.setText( getResources().getString(R.string.resume_timer_button));

			// Create an event to sync the timer
			createAndSendEvent(2, false);
		// If the timer is paused
    	} else {
        	// Display toasti
	    	if (DEBUG) {
		    	Toast toast = Toast.makeText(this, "Timer started", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
				toast.show();
	    	}
	    	// Start protocoll
	    	new_protocoll = true;
	    	// Init arraylist carrying the events
			eventList_protocoll = new ArrayList<Event>();
			
	    	// Start time is set to the current system clock
			start_time = SystemClock.uptimeMillis();
			// Begin to update the timer value 
			timer_handler.postDelayed(updateTimerMethod, 0);
			// Set the text of the pause button to "Pause"
			pause_timer_button.setText( getResources().getString(R.string.pause_timer_button));
	    	reset_timer_button.setBackground( getResources().getDrawable(R.drawable.timer_button));
	    	reset_timer_button.setEnabled(true); 
			// Create an event to sync the timer
			createAndSendEvent(1, false);
    	}
    	// Toggle timer paused
    	timer_paused = !timer_paused;
    }
    

   
    /*
	 *  Called when the user clicks the New scenario button 
	 */
    public void startStopScenario(View view) {
    	// If a new scenario is being recorded
    	if (!new_scenario) {
    		new_scenario = true;
    		// Display taosti
        	if (DEBUG) {
		    	Toast toast = Toast.makeText(this, "Scenario started", Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
				toast.show();
        	}
        	// Change button appearance to be orange and display stop message
			scenario_button.setBackground( getResources().getDrawable(R.drawable.orange_button));
			scenario_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_save, 0, 0, 0);
			scenario_button.setText( getResources().getString(R.string.scenario_stop_button));
			
			// start Scenario
			eventList_scenario = new ArrayList<Event>();

	    // If a new scenario is finished and should get stored
    	} else {
    		new_scenario = false;

        	// Change button appearance to be yellow and display new scenario message
			scenario_button.setText( getResources().getString(R.string.scenario_start_button));
			scenario_button.setBackground( getResources().getDrawable(R.drawable.yellow_button));
			scenario_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_new, 0, 0, 0);

			// Open an alert dialog where the name of the scenario should be entered
			final EditText input = new EditText(this);
			new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
			    .setTitle(getResources().getString(R.string.scenario_alert_title))
			    .setView(input)
			    // If Save button is pressed on the alert dialog
			    .setPositiveButton(getResources().getString(R.string.scenario_alert_positive_button), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			        	// Get the typed name
			        	Editable value = input.getText();
			        	// Store all progress so far
			        	storeScenario(value.toString(), true);
			        }
			    // If cancel button is pressed on the alert dialog
			    }).setNeutralButton(getResources().getString(R.string.scenario_alert_neutral_button),  new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
			        	// Get the typed name
			        	Editable value = input.getText();
			        	// Store all progress so far
			        	storeScenario(value.toString(), false);
	                }
	            }).setNegativeButton(getResources().getString(R.string.scenario_alert_negative_button), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int whichButton) {
			            // Delete all progress
		        		eventList_scenario = null;
			        }
			    }).show();
    		}
    }
    

    /*
     * Store the Scenario
     */
    private void storeScenario(String value, boolean runnable) {
    	// Display toasti
    	if (SHOW_STORE_TOAST) {
        	String message = "Saved Scenario under name: ";
        	message += value;
	    	Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
			toast.show();
    	}
    	
    	// Store eventlist under scenario
    	Scenario scenario = new Scenario(value.toString(), runnable);
    	
    	// Add the event list to the scenario.
    	scenario.setEventList(eventList_scenario);
    	
    	// Write in Database
    	int error = scenarioHelper.addScenario(scenario);
    	
    	// If name already exists, open a new prompt
    	if (error == -1) {
    		final boolean runble = runnable;
    		// Open an alert dialog where the new name of the scenario should be entered
    		final EditText input = new EditText(this);
    		new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
    			    .setTitle(getResources().getString(R.string.scenario_alert_title_name_already_exists1) + value.toString() 
    			    		+ getResources().getString(R.string.scenario_alert_title_name_already_exists2))
    			    .setView(input)
    			    // If Save button is pressed on the alert dialog
    			    .setPositiveButton(getResources().getString(R.string.scenario_alert_positive_button2), new DialogInterface.OnClickListener() {
    			        public void onClick(DialogInterface dialog, int whichButton) {
    					        	// Get the typed name
    					        	Editable value = input.getText();
    					        	// Store all progress so far
    					        	storeScenario(value.toString(), runble);
    					        }
    			            }).setNegativeButton(getResources().getString(R.string.scenario_alert_negative_button), new DialogInterface.OnClickListener() {
    					        public void onClick(DialogInterface dialog, int whichButton) {
    					            // Delete all progress
    				        		eventList_scenario = null;
    					        }
    					    }).show();
    	}
    }
    
    /*
     * Store the Protocoll
     * Could be put together with store scenario, but for better understanding we distinguish the two functions
     */
    private void storeProtocol() {
		Calendar c = Calendar.getInstance(); 
		Integer seconds = c.get(Calendar.SECOND);
		Integer minutes = c.get(Calendar.MINUTE);
		Integer hours = c.get(Calendar.HOUR_OF_DAY);
		Integer day = c.get(Calendar.DAY_OF_MONTH);
		Integer month = 1 + c.get(Calendar.MONTH);
		Integer year = c.get(Calendar.YEAR);
		
		String value = "[" + day + "/" + month + "/" + year + "_" + hours + ":"
		+ minutes + ":" + seconds + "]";
    	// Display toasti
    	if (SHOW_STORE_TOAST) {
        	String message = "Saved Protocoll under name: ";
        	message += value;
	    	Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
			toast.show();
    	}
  
    	
    	// Store eventlist under scenario
    	Scenario scenario = new Scenario(value.toString(), false);
    	
    	// Add the event list to the scenario.
    	scenario.setEventList(eventList_protocoll);
    	
    	// Write in Database
    	scenarioHelper.addScenario(scenario);
    }
    
  

	/*
	 *  Called when the user clicks the Flag button 
	 */
    public void flagPressed(View view) {
    	// Show toasti
        if (DEBUG) {
		   	Toast toast = Toast.makeText(this, "Flag pressed", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
			toast.show();
        }

		// Create an event to show the flag without sending the event
        createAndSendEvent(0, true);
    }
    
    /*
     *  Called if the button over the slider is pressed revert button background color and activate the slider
     */
    public void sliderButtonClicked(View view) {
    	 switch(view.getId()) {
    	 // If the heart rate slider was clicked
         case R.id.heart_rate_slider_text:
        	 heart_rate_value_active = !heart_rate_value_active;
        	 if (heart_rate_value_active){
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
        	 } else {
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
        	 }
          break;
      	 // If the o2 rate slider was clicked
         case R.id.o2_rate_slider_text:
        	 o2_rate_value_active = !o2_rate_value_active;
        	 if (o2_rate_value_active){
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
        	 } else {
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
        	 }
           break;
      	 // If the co2 rate slider was clicked
         case R.id.co2_rate_slider_text:
        	 co2_rate_value_active = !co2_rate_value_active;
        	 if (co2_rate_value_active){
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
        	 } else {
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
        	 }
           break;
         // If the co2 rate slider was clicked
         case R.id.raspiration_rate_slider_text:
        	 raspiration_rate_value_active = !raspiration_rate_value_active;
          	 if (raspiration_rate_value_active){
              	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
          	 } else {
              	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
          	 }
           break;
      	 // If the blood pressure slider was clicked
         case R.id.blood_pressure_systolic_slider_text:
        	 blood_pressure_value_active = !blood_pressure_value_active;
        	 if (blood_pressure_value_active){
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_active_button));
        	 } else {
            	 view.setBackground( getResources().getDrawable(R.drawable.ic_slider_deactive_button));
        	 }
           break;
         // If the blood pressure slider was clicked
         case R.id.cuff_checkBox:
        	 cuff_active = !cuff_active;
           break;
    	 }
    	 
    	 // Look if something has changed
    	 boolean change = (blood_pressure_value_active != blood_pressure_value_last_active) 
    			 		|| (co2_rate_value_active != co2_rate_value_last_active)
    			 		|| (o2_rate_value_active != o2_rate_value_last_active)
    			 		|| (raspiration_rate_value_active != raspiration_rate_value_last_active)
    			 		|| (heart_rate_value_active != heart_rate_value_last_active)
    			 		|| (cuff_active != cuff_last_active);

    	 // If yes, activate the apply and dismiss buttons or activate it right away
    	 if (change) {
    		 if (ACTIVATE_CURVES_WITHOUT_APPLY) {
    			 handleCurveButtonPressed();
    		 } else {
    			 enableButtons();
    		 }
    	 } else {
    		 if (!ACTIVATE_CURVES_WITHOUT_APPLY) {
    			 disableButtons();
    		 }
    	 }
    }
  
    
    /*
     * Set the runnable for updating the timer value
     */
    private void initTimer() {
    	updateTimerMethod = new Runnable() {
			public void run() {
				// Calculate time since timer start
				time_millies = SystemClock.uptimeMillis() - start_time;
				// Add the time the user pauses the timer
				final_time = time_swap + time_millies;
				// Calculate seconds minutes hours from it
				int seconds = (int) (final_time / 1000);
				int minutes = seconds / 60;
				// Seconds range 0-60
				seconds = seconds % 60;
				// Display timer
				timer_value.setText("" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds));
				// Call this function again with update rate of approximately 1s (not faster needed)
				timer_handler.postDelayed(this, TIMER_UPDATE_RATE);
			}
		};
    }

    
    /*
     * get Buttons and set them to their initial states
     */
    private void initButtons() {
    	// Get button views
    	apply_button = (ImageButton) findViewById(R.id.apply_button);
		dismiss_button = (ImageButton) findViewById(R.id.dismiss_button);
		scenario_button = (Button) findViewById(R.id.scenario_button);
		reset_timer_button = (Button) findViewById(R.id.reset_timer_button);
		pause_timer_button = (Button) findViewById(R.id.pause_timer_button);
		heart_rate_active_button = (Button) findViewById(R.id.heart_rate_slider_text);
		o2_rate_active_button = (Button) findViewById(R.id.o2_rate_slider_text);
		co2_rate_active_button = (Button) findViewById(R.id.co2_rate_slider_text);
		blood_pressure_active_button = (Button) findViewById(R.id.blood_pressure_systolic_slider_text);
		raspiration_rate_active_button = (Button) findViewById(R.id.raspiration_rate_slider_text);
		cuff_attached = (CheckBox) findViewById(R.id.cuff_checkBox);

		// disable timer button
    	reset_timer_button.setBackground( getResources().getDrawable(R.drawable.gray_button));
    	reset_timer_button.setEnabled(false); 	
    }
    
    
	/*
	 *  Set initial values for sliders on activity start
	 */
    private void initSliders() {
    	// Init SeekBars
    	scheduler_slider = (SeekBar) findViewById(R.id.scheduler_slider);
		heart_rate_slider = (VerticalSeekBar) findViewById(R.id.heart_rate_slider);
		blood_pressure_systolic_slider = (VerticalSeekBar) findViewById(R.id.blood_pressure_systolic_slider);
		blood_pressure_diastolic_slider = (VerticalSeekBar) findViewById(R.id.blood_pressure_diastolic_slider);
		o2_rate_slider = (VerticalSeekBar) findViewById(R.id.o2_rate_slider);
		co2_rate_slider = (VerticalSeekBar) findViewById(R.id.co2_rate_slider);
		raspiration_rate_slider = (VerticalSeekBar) findViewById(R.id.raspiration_rate_slider);
		
		// Init text views
		scheduler_slider_value = (TextView) findViewById(R.id.scheduler_slider_value);
		heart_rate_slider_value = (TextView) findViewById(R.id.heart_rate_slider_value);
		blood_pressure_slider_value = (TextView) findViewById(R.id.blood_pressure_slider_value);
		o2_rate_slider_value = (TextView) findViewById(R.id.o2_rate_slider_value);
		co2_rate_slider_value = (TextView) findViewById(R.id.co2_rate_slider_value);
		raspiration_rate_slider_value = (TextView) findViewById(R.id.raspiration_rate_slider_value);
		
		// Make this class react to seekbar changes
		scheduler_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
		heart_rate_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
		blood_pressure_systolic_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
		blood_pressure_diastolic_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
		o2_rate_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
		co2_rate_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
		raspiration_rate_slider.setOnSeekBarChangeListener(new SeekBarChangeListener());
    	
    	// Set initial values to the sliders
    	blood_pressure_systolic_slider.setProgress( getResources().getInteger(R.integer.init_blood_pressure_systolic_slider));
		blood_pressure_diastolic_slider.setProgress( getResources().getInteger(R.integer.init_blood_pressure_diastolic_slider));
		heart_rate_slider.setProgress( getResources().getInteger(R.integer.init_heart_rate_slider));
		o2_rate_slider.setProgress( getResources().getInteger(R.integer.init_o2_rate_slider));
		co2_rate_slider.setProgress( getResources().getInteger(R.integer.init_co2_rate_slider));
		raspiration_rate_slider.setProgress( getResources().getInteger(R.integer.init_raspiration_rate_slider));
		scheduler_slider.setProgress( getResources().getInteger(R.integer.init_scheduler_slider));

		// And to the variables
		blood_pressure_systolic_value = Integer.valueOf(blood_pressure_systolic_slider.getProgress());
		blood_pressure_diastolic_value = Integer.valueOf(blood_pressure_diastolic_slider.getProgress());
		heart_rate_value = Integer.valueOf(heart_rate_slider.getProgress());
		o2_rate_value = Integer.valueOf(o2_rate_slider.getProgress());
		co2_rate_value = Integer.valueOf(co2_rate_slider.getProgress());
		raspiration_rate_value = Integer.valueOf(raspiration_rate_slider.getProgress());
		scheduler_value = Integer.valueOf(scheduler_slider.getProgress());
    }
    

    /*
     *  Init the Spinner views
     */
    private void initSpinners() {
    	// Init the image array adapters to contain the images with correct patterns
    	// These image must be added to the array in the correct order.
    	// If you want to add patterns, add them here to the end of the array and change the event class as well as the scenario class
    	/*
    	0:  HeartPattern.SINE;
  	  	1:	HeartPattern.ARRYTHMIC;
  	  	2:	HeartPattern.AVBLOCK;
  	  	3:	HeartPattern.LEFTBLOCK;
  	  	4:	HeartPattern.LEFTBLOCKAA;
  	  	5:	HeartPattern.STEMI;
  	  	6:	HeartPattern.PACE;
  	  	7:	HeartPattern.VENTFLUTTER;
  	  	8:	HeartPattern.VENTFIBRI;
  	  	9: 	HeartPattern.CPR;
  	  	10: HeartPattern.ASYSTOLE;
  	    */
    	adapter_heart_rate = new ImageArrayAdapter(this, 
    		new Integer[]{R.drawable.hr_sine, R.drawable.hr_absolute_arrhythmie, R.drawable.hr_avblock, R.drawable.hr_leftblock,
    			R.drawable.hr_leftblock_aa, R.drawable.hr_stemi, R.drawable.hr_pacer,
    			R.drawable.hr_ventriflutter, R.drawable.hr_ventifibri, R.drawable.hr_cpr, R.drawable.hr_asystoly});
    	adapter_o2_rate = new ImageArrayAdapter(this, 
                new Integer[]{R.drawable.o2_normal, R.drawable.o2_coldfingers});
    	adapter_co2_rate = new ImageArrayAdapter(this, 
                new Integer[]{R.drawable.co2_normal});
    	adapter_blood_pressure = new ImageArrayAdapter(this, 
                new Integer[]{R.drawable.bp_normal});
    	adapter_raspiration_rate = new ImageArrayAdapter(this, 
                new Integer[]{R.drawable.raspiration_normal});

    	
    	// Init the spinner views
    	spinner_heart_rate = (Spinner) findViewById(R.id.heart_rate_spinner);
    	spinner_o2_rate = (Spinner) findViewById(R.id.o2_rate_spinner);
    	spinner_co2_rate = (Spinner) findViewById(R.id.co2_rate_spinner);
    	spinner_raspiration_rate = (Spinner) findViewById(R.id.raspiration_rate_spinner);
    	spinner_blood_pressure = (Spinner) findViewById(R.id.blood_pressure_spinner);
    	// Set the arrays of the spinners
        spinner_heart_rate.setAdapter(adapter_heart_rate);
        spinner_o2_rate.setAdapter(adapter_o2_rate);
        spinner_co2_rate.setAdapter(adapter_co2_rate);
        spinner_raspiration_rate.setAdapter(adapter_raspiration_rate);
        spinner_blood_pressure.setAdapter(adapter_blood_pressure);

        // Set the Listener of the Spinners
        spinner_heart_rate.setOnItemSelectedListener(new SpinnerListener());
        spinner_o2_rate.setOnItemSelectedListener(new SpinnerListener());
        spinner_co2_rate.setOnItemSelectedListener(new SpinnerListener());
        spinner_raspiration_rate.setOnItemSelectedListener(new SpinnerListener());
        spinner_blood_pressure.setOnItemSelectedListener(new SpinnerListener());
        
        // Deactivate other spinners, this can be later deleted for future use
        spinner_co2_rate.setClickable(false);
        spinner_raspiration_rate.setClickable(false);
        spinner_blood_pressure.setClickable(false);

        spinner_heart_rate.setDropDownWidth((int)getResources().getDimension(R.dimen.slider_value_max));
        spinner_o2_rate.setDropDownWidth((int)getResources().getDimension(R.dimen.slider_value_max));
        spinner_co2_rate.setDropDownWidth((int)getResources().getDimension(R.dimen.slider_value_max));
        spinner_raspiration_rate.setDropDownWidth((int)getResources().getDimension(R.dimen.slider_value_max));
        spinner_blood_pressure.setDropDownWidth((int)getResources().getDimension(R.dimen.slider_value_max));
        
    }
    
	/*
	 *  Gray out the Dismiss and Apply Button, since nothing is changed
	 */
	private void disableButtons() {
		apply_button.setBackground( getResources().getDrawable(R.drawable.gray_button));
		apply_button.setEnabled(false); 
		dismiss_button.setBackground( getResources().getDrawable(R.drawable.gray_button));
		dismiss_button.setEnabled(false); 
	}			
	
	/*
	 * Makes all buttons pressable and in the right color
	 */
    private void enableButtons() {
		apply_button.setBackground( getResources().getDrawable(R.drawable.green_button));
		apply_button.setEnabled(true); 
		dismiss_button.setBackground( getResources().getDrawable(R.drawable.red_button));
		dismiss_button.setEnabled(true); 
	}
    
	/**
	 * Class for the SeekBar. Methods get called when changes to the sliders are applied
	 */
	class SeekBarChangeListener implements OnSeekBarChangeListener {

		   
		/*
		 * (non-Javadoc)
		 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
		 * If the user changes the Slider value, update the value below the slider and enable the buttons
		 */
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			boolean slider_change = false;
			// Look what seekbar was changed
			switch (seekBar.getId()) {
				// If it was the scheduler slider
			    case R.id.scheduler_slider:
			    	scheduler_slider_value.setText(Integer.valueOf(progress + getResources().getInteger(R.integer.min_scheduler_slider)).toString() + " " + getResources().getString(R.string.scheduler_slider_value));
			        slider_change = true;
			    	break;
			    // If it was the heart rate slider
			    case R.id.heart_rate_slider:
			    	heart_rate_slider_value.setText(Integer.valueOf(progress + getResources().getInteger(R.integer.min_heart_rate_slider)).toString() + " " + getResources().getString(R.string.heart_rate_slider_value));
			        break;
			    // If it was the o2 rate slider
			    case R.id.o2_rate_slider:
			    	o2_rate_slider_value.setText(Integer.valueOf(progress + getResources().getInteger(R.integer.min_o2_rate_slider)).toString() + " " + getResources().getString(R.string.o2_rate_slider_value));
			        break;
			    // If it was the co2 rate slider
			    case R.id.co2_rate_slider:
			    	co2_rate_slider_value.setText(Integer.valueOf(progress + getResources().getInteger(R.integer.min_co2_rate_slider)).toString() + " " + getResources().getString(R.string.co2_rate_slider_value));
			        break;
			    // If it was the co2 rate slider
			    case R.id.raspiration_rate_slider:
			    	raspiration_rate_slider_value.setText(Integer.valueOf(progress + getResources().getInteger(R.integer.min_raspiration_rate_slider)).toString() + " " + getResources().getString(R.string.raspiration_rate_slider_value));
			        break;
			    // If it was the systolic blood pressure slider
			    case R.id.blood_pressure_systolic_slider:
			    	// The slider value needs to be greater than the diastolic slider value
			    	if (progress < blood_pressure_diastolic_slider.getProgress()) {
			    		blood_pressure_systolic_slider.setProgress(blood_pressure_diastolic_slider.getProgress());
			    	// And larger than the minimum value
			    	} else if (progress < getResources().getInteger(R.integer.min_blood_pressure_systolic_slider) - getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider)) {
			    		blood_pressure_systolic_slider.setProgress(getResources().getInteger(R.integer.min_blood_pressure_systolic_slider) - getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider));
			    	// Else simple update the slider string to the slider value
			    	} else {
				    	StringBuilder stringBuilder = new StringBuilder();
				    	stringBuilder.append(Integer.valueOf(progress + getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider)).toString());
				    	stringBuilder.append("/");
				    	stringBuilder.append(Integer.valueOf(blood_pressure_diastolic_slider.getProgress() + getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider)).toString());
				    	stringBuilder.append(" ");
				    	stringBuilder.append(getResources().getString(R.string.blood_pressure_slider_value));
				    	blood_pressure_slider_value.setText(stringBuilder.toString());
				    }
			        break;
				// If it was the systolic blood pressure slider
			    case R.id.blood_pressure_diastolic_slider:
			    	// The diastolic slider is always lower than the systolic slider
			    	if (progress > blood_pressure_systolic_slider.getProgress()) {
			    		blood_pressure_diastolic_slider.setProgress(blood_pressure_systolic_slider.getProgress());
			    	// And not greater than a maximum value
			    	} else if (progress > getResources().getInteger(R.integer.range_blood_pressure_diastolic_slider)) {
			    		blood_pressure_diastolic_slider.setProgress(getResources().getInteger(R.integer.range_blood_pressure_diastolic_slider));
			    	// Else simply update the value
			    	} else {
				    	StringBuilder stringBuilder2 = new StringBuilder();	    			
				    	stringBuilder2.append(Integer.valueOf(blood_pressure_systolic_slider.getProgress() + getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider)).toString());
				    	stringBuilder2.append("/");
				    	stringBuilder2.append(Integer.valueOf(progress + getResources().getInteger(R.integer.min_blood_pressure_diastolic_slider)).toString());
				    	stringBuilder2.append(" ");
				    	stringBuilder2.append(getResources().getString(R.string.blood_pressure_slider_value));
				    	blood_pressure_slider_value.setText(stringBuilder2.toString());
			    	}
			    	break;
			}
			// Enable the buttons if it was not only the scheduler slider, because something changed
			if (!slider_change) {
				enableButtons();
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}
	
	
	
	/**
	 * The Listener for the Spinners representing the curve style. Methods are called if the spinner objects are changed
	 */
	class SpinnerListener implements OnItemSelectedListener {
	
		/*
		 * (non-Javadoc)
		 * @see android.widget.AdapterView.OnItemSelectedListener#onNothingSelected(android.widget.AdapterView)
		 * If a spinner is opened but nothing is selected
		 */
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			
		}
		/*
		 * (non-Javadoc)
		 * @see android.widget.AdapterView.OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
		 * If a spinner item is selected
		 */
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position,
				long id) {
			
			// Display the patterns name if needed
			if (SHOW_PATTERN_TOAST) {
				if (parent.getAdapter().equals(adapter_heart_rate)) {
					Toast.makeText(parent.getContext(), "" + Scenario.intToO2Pattern(position), Toast.LENGTH_SHORT).show();
				} else if (parent.getAdapter().equals(adapter_o2_rate)) {
					Toast.makeText(parent.getContext(), "" + Scenario.intToO2Pattern(position), Toast.LENGTH_SHORT).show();
				} else if (parent.getAdapter().equals(adapter_co2_rate)) {
					Toast.makeText(parent.getContext(), "pattern: " + Scenario.intToCarbPattern(position), Toast.LENGTH_SHORT).show();
				} else if (parent.getAdapter().equals(adapter_raspiration_rate)) {
					Toast.makeText(parent.getContext(), "" + Scenario.intToRespPattern(position), Toast.LENGTH_SHORT).show();
				} else if (parent.getAdapter().equals(adapter_blood_pressure)) {
					Toast.makeText(parent.getContext(), "" + Scenario.intToBpPattern(position), Toast.LENGTH_SHORT).show();
				}
			}
			
			// If something has changed (and not changed back), enable the buttons
			if (heart_rate_pattern != spinner_heart_rate.getSelectedItemPosition() 
					|| o2_rate_pattern != spinner_o2_rate.getSelectedItemPosition()
					|| raspiration_rate_pattern != spinner_raspiration_rate.getSelectedItemPosition() 
					|| co2_rate_pattern != spinner_co2_rate.getSelectedItemPosition() 
					|| blood_pressure_pattern != spinner_blood_pressure.getSelectedItemPosition()) {
				enableButtons();
			}
		}
	}
}
