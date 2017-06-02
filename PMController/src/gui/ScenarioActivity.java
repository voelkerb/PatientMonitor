/**
 *
 * @package Patient Monitor - Controller
 * @copyright (c) 2014 University of Freiburg
 * @author Johannes Scherle, Benjamin Voelker
 * @email {scherlej, voelkerb}@informatik.uni-freiburg.de
 * @date 17.11.2014
 * @summary Scenario view with all UI elements for setting playing back scenarios. 
 * 
 */


package gui;

// Import stuff goes here
import java.util.List;

import Scenario.Event;
import Scenario.Event.TimerState;
import Scenario.EventAdapter;
import Scenario.Scenario;
import Scenario.ScenarioHelper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.pmcontroller1.R;
//import Scenario.ScenarioHelper;

public class ScenarioActivity extends Activity {

	//@SuppressWarnings("unused")
	// Debug mode shows stickies 
	private static final boolean DEBUG = true;
	// Auto hide the Action bar after this time
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	// Auto hide active or not
	private static final boolean AUTO_HIDE_ACTIVE = false;
	// If hiding was initialized don't hide again
	private boolean WILL_HIDE = false;	
	public static ScenarioHelper _scenarioHelper;
	public static EventAdapter _eventAdapter;
	public static List<Scenario> _currentScenarios;
	public static List<Event> _currentEvents;
	public static Scenario _currentScenario;
	public static Event _currentEvent;
	public static Integer _currentPositionEvent = 0;
	public static ListView listViewScenario;
	public static ListView listViewEvents;
	private static boolean _scenarioRunning = false;
	private static boolean _scenarioPaused = false;
	private Handler _scenarioHandler;
	private int _timeInSec = 0;

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * Constructor of the Scenario class
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		// Overlay action bar and content, so that the content does not get shrinked if the action bar is visible
		this.getWindow();
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		// Set up the actionbar correct		
		setupActionBar();


		setContentView(R.layout.activity_scenario);

		// Make the statusbar disappear at init
		if (AUTO_HIDE_ACTIVE) delayedHide(AUTO_HIDE_DELAY_MILLIS);

		// Set up the back view for onclicklistener
		final View backView = findViewById(R.id.scenario_background_view);
		// If the background is clicked, the status bar should appear
		if (AUTO_HIDE_ACTIVE) {
			backView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					// Show the action bar
					getActionBar().show();
					// And hide it again after delay if not yet scheduled
					if (!WILL_HIDE)	delayedHide(AUTO_HIDE_DELAY_MILLIS);
				}
			});	
		}


		TimingLogger timings = new TimingLogger("TopicLogTag", "read scenarios");
		// Set the list adapter and read the scenario list.
		readScenariosToList();
		timings.dumpToLog();


		// Set the on item click listener for the scenario list.
		listViewScenario = (ListView)findViewById(R.id.listViewScenario);
		listViewScenario.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long arg3) {
				//v.setSelected(true);
				Scenario chosenScenario = (Scenario)adapter.getItemAtPosition(position);
				_currentScenario = chosenScenario;
				_currentEvents = chosenScenario.getEventList();
				readEventsToList(chosenScenario);
			}
		});

		// Set the on item click listener for the event list.
		listViewEvents = (ListView)findViewById(R.id.listViewEvents);
		listViewEvents.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long arg3) {
				listViewEvents.setItemChecked(position, true);
				Event chosenEvent = (Event)adapter.getItemAtPosition(position);
				_currentPositionEvent = position;
				_currentEvent = chosenEvent;	
				_timeInSec = _currentEvent._timeStamp;
				// Sync the timer.
				_currentEvent._syncTimer = true;
				MainActivity.server.out(_currentEvent.toJson());					
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 * If dialog boxes or other notification appear, the navigation and status bar will get visible -> hide it again if done
	 */
	@SuppressLint("InlinedApi") @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// make the activity fulscreen without navigation bar
		if (hasFocus) {
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
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 * If the application resumes from other applications, make it full screen again
	 */
	@SuppressLint("InlinedApi") @Override
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scenario, menu);
		if (ControllerActivity.PREFERENCE_WINDOW_ACTIVE) {
			menu.add(Menu.NONE, R.id.action_settings, 100, "")
			.setIcon(R.drawable.ic_action_settings)
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		getActionBar().setDisplayShowHomeEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (_scenarioRunning)
			stopScenario();
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (id == R.id.action_settings) {
			startActivity (new Intent (this, PreferenceActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Read all the stored scenarios and visualise them in the listview.
	 * @author Johannes
	 */
	public void readScenariosToList() {
		_currentScenarios = _scenarioHelper.getAllScenarios();
		try {
			ListAdapter adapter2 = new ArrayAdapter<Scenario>(getApplicationContext(),
					R.layout.my_simple_list_item_1, _currentScenarios);
			ListView listViewScenario = (ListView)findViewById(R.id.listViewScenario);
			listViewScenario.setAdapter(adapter2);
		} finally {}		
	}

	/**
	 * Read all events from the current scenario to a listview
	 * @param chosenScenario : the Scenario from which the events should be
	 * displayed.
	 * @author Johannes
	 */
	public void readEventsToList(Scenario chosenScenario) {
		// Get the events from the chosen scenario.
		Event[] eventList = (Event[])chosenScenario.getEventList().toArray(new Event[0]);
		_eventAdapter = new EventAdapter(this, R.layout.event_item, eventList);
		ListView listViewEvents = (ListView)findViewById(R.id.listViewEvents);
		listViewEvents.setAdapter(_eventAdapter);		
	}

	/**
	 * Clear the events in the listview. Used when a scenario is deleted.
	 * @author Johannes
	 */
	public void clearEventList() {
		ListView listViewEvents = (ListView)findViewById(R.id.listViewEvents);
		listViewEvents.setAdapter(null);
	}

	/**
	 * Delete a scenario from the database. This is the callback function
	 * for the delete button.
	 * @param view
	 * @author Johannes
	 */
	public void deleteScenario(View view) {
		if (_scenarioRunning)
			stopScenario();
		_scenarioHelper.deleteScenario(_currentScenario);
		readScenariosToList();
		clearEventList();
	}

	/**
	 * Callback function for the apply button, not used in the current
	 * implementation
	 * @param view
	 * @author Johannes
	 */
	public void applyPressedScenario(View view) {
		// Send to Server
		MainActivity.server.out(_currentEvent.toJson().toString());
	}

	/**
	 * Select the next event from the event list. Only used in the protocoll-
	 * mode at the moment.
	 * @param view
	 * @author Johannes
	 */
	public void nextEvent(View view) {		
		if (_currentEvents != null && _currentPositionEvent != null) {
			if (_currentPositionEvent >= _currentEvents.size() - 1)
				_currentPositionEvent = 0;
			else
				_currentPositionEvent++;
			listViewEvents.setItemChecked(_currentPositionEvent, true);
			_currentEvent = (Event) listViewEvents
					.getItemAtPosition(_currentPositionEvent);
		}			
	}

	/**
	 * Start the automatic playback of the scenario. Callback function for
	 * the play button.
	 * @param view
	 * @author Johannes
	 */
	public void startScenario(View view) {
		if (_currentEvents != null && _currentPositionEvent != null
				&& !_scenarioRunning) {		
			_currentEvent = (Event) listViewEvents
					.getItemAtPosition(_currentPositionEvent);
			/*
			 * Get the first events from the current scenario and send them to
			 * the monitor.
			 * Used a while loop for the case that the scenario starts with
			* several timestamp == 0 Events in a row.
			*/
			int firstTimestamp = _currentEvent._timeStamp;
			while (_currentEvent._timeStamp == firstTimestamp) {
				// Start the timer on the Monitor again
				if (_scenarioPaused) {
					_currentEvent._timerState = TimerState.START;
					_scenarioPaused = false;
				}
				// Send the event only if it is not a flag.
				if (!_currentEvent._flag)
					MainActivity.server.out(_currentEvent.toJson().toString());				
				listViewEvents.setItemChecked(_currentPositionEvent, true);
				_currentPositionEvent++;
				if (_currentPositionEvent < _currentEvents.size() - 1) {
					_currentEvent = (Event) listViewEvents
							.getItemAtPosition(_currentPositionEvent);			
				} else {
					listViewEvents.setItemChecked(_currentPositionEvent, true);
					_currentPositionEvent = 0;					
					return;					
				}
			}
			_scenarioRunning = true;
			// Set up a timer which controls the stopwatch and the sending
			// of events.
			_scenarioHandler = new Handler();
			_scenarioHandler.postDelayed(new Runnable() {
				public void run() {
					_timeInSec++; // Increase the counter every second.
					if (_scenarioRunning)
						updateTime(); // Update the GUI.
					if (_currentEvents != null
							&& _currentPositionEvent != null) {
						// If the timer equals the timestamp of the next event
						// and the event is not a flag, send it to the monitor.
						if (_timeInSec == _currentEvent._timeStamp) {
							// If we reached the last event.
							if (_currentPositionEvent >= _currentEvents
									.size() - 1) {
								if (!_currentEvent._flag)
									listViewEvents.setItemChecked(
											_currentPositionEvent, true);
								MainActivity.server.out(_currentEvent
										.toJson().toString());
								if (DEBUG)
									System.out.println("Sent Event");
								// Stop the timer and reset the variables.								
								stopScenario();
								if (DEBUG)
									System.out.println("Stopped Scenario");
							} else {
								// For the case that several events with the
								// same time stamp occur.
								int currentTimestamp = _currentEvent._timeStamp;
								while (currentTimestamp == _currentEvent._timeStamp) {
									MainActivity.server.out(_currentEvent
											.toJson().toString());
									if (DEBUG)
										System.out.println("Sent Event");
									if (!_currentEvent._flag)
										listViewEvents.
										setItemChecked(_currentPositionEvent, true);
									currentTimestamp = _currentEvent._timeStamp;
									_currentPositionEvent++;
									_currentEvent = (Event) listViewEvents
											.getItemAtPosition(_currentPositionEvent);
								}
							}
						}
					}
					// If the scenario is running set the next callback,
					// else stop the timer.
					if (_scenarioRunning)
						_scenarioHandler.postDelayed(this, 1000);
					else if (!_scenarioRunning)
						_scenarioHandler.removeCallbacks(this);
				}
			}, 1000);

		}
	}

	/**
	 * The callback function for the stopScenario-Button.
	 * @param view
	 * @author Johannes
	 */
	public void stopScenarioCallback(View view) {
		stopScenario();
	}

	/**
	 * Stops the scenario. The timer will be stopeped, the next time
	 * the timer is called.
	 * @author Johannes
	 */
	public void stopScenario() {
		_currentPositionEvent = 0;
		_timeInSec = 0;
		updateTime();
		_scenarioRunning = false;
	}	

	/**
	 * Pause the scenario
	 */
	public void pauseScenario(View view) {
		_scenarioRunning = false;		
		_currentPositionEvent--;
		// Send last Event to pause the timer on the monitor.
		_currentEvent = (Event) listViewEvents
				.getItemAtPosition(_currentPositionEvent);
		_currentEvent._timerState = TimerState.PAUSE;
		MainActivity.server.out(_currentEvent.toJson());
		_scenarioPaused = true;
	}

	/**
	 * Writes the current time since the start of the scenario in the status-bar. The format
	 * is mm:ss.
	 * Taken from Marc Pfeifer
	 */
	private void updateTime() {
		// Schedule the update of the timer in main GUI-thread.
		runOnUiThread(new Runnable() {
			public void run() {
				TextView statusBarTitle = (TextView) findViewById(R.id.scenarioTimer);
				// Calculate the minute and second value out of the second-counter.
				int sec = _timeInSec % 60;
				int min = _timeInSec / 60;
				// If necessary add leading zeros to the well that it looks nice.
				if (sec < 10 && min < 10) {
					statusBarTitle.setText("Timer:   0" + min + ":0" + sec);
				} else if (sec < 10) {
					statusBarTitle.setText("Timer:   " + min + ":0" + sec);
				} else if (min < 10) {
					statusBarTitle.setText("Timer:   0" + min + ":" + sec);
				} else {
					statusBarTitle.setText("Timer:   " + min + ":" + sec);
				}
			}
		});
	}

	/**
	 *  Select the previous event from the event list. Only used in the
	 *  protocoll mode in the current implementation.
	 * @param view
	 * @author Johannes
	 */
	public void previousEvent(View view) {		
		if (_currentEvents != null && _currentPositionEvent != null) {
			if (_currentPositionEvent == 0)
				_currentPositionEvent = _currentEvents.size() - 1;
			else
				_currentPositionEvent--;
			listViewEvents.setItemChecked(_currentPositionEvent, true);
			_currentEvent = (Event) listViewEvents
					.getItemAtPosition(_currentPositionEvent);
		}

	}

	/**
	 * Make the scenario a protocol, which is only change the runnable value.
	 * @param view
	 * @author Johannes
	 */
	public void changeToProtocol(View view) {
		if (_currentScenario != null) {
			_currentScenario.setRunnable(!_currentScenario.isRunnable());
			_scenarioHelper.setRunnableInDatabase(_currentScenario);
			readScenariosToList();
			clearEventList();
		}
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
}
