/**
 *
 * @package Patient Monitor - Controller
 * @copyright (c) 2014 University of Freiburg
 * @author Benjamin Völker
 * @email voelkerb@informatik.uni-freiburg.de
 * @date 17.11.2014
 * @summary Root view of the patient monitor controller application. 
 * 
 */

package gui;

// Import stuff goes here
import com.example.pmcontroller1.R;

import Server.Server;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Main Activity, displaying the available devices and a Start/Connect button to start controlling the monitor
 */
@SuppressLint("InlinedApi") public class MainActivity extends Activity {
	// The server object
	static Server server = null;
	
	// The context needed for the NSD Manager
	private static Context context;

	// Debug messages for main activity
	private final boolean DEBUG = false;
	
	// If app should check if in wifi or not
	static final boolean CHECK_WIFI = true;
	
	// If the user should be able to set the service name by his own
	private final boolean USER_INPUT_SERVICE_NAME = true;
	// Default service name
	private final String DEFAULT_SERVICE_NAME = "PaM_Session";
	private String service_name = DEFAULT_SERVICE_NAME;

	// Timer handler for Wifi check
	private Handler timer_handler = null;
	Runnable updateTimerMethod = null;
	// The time in milliseconds when the timer gets updated again
	private final int TIMER_UPDATE_RATE = 1000;
	// The start button to get to controller view
	private ImageButton start_button = null;
	// The change session name button
	private ImageButton change_service_name_button = null;
	// The TextView to display information about the connection state
	private TextView connection_text = null;
	private TextView service_text = null;
	private boolean showed_alert = false;
	private boolean show_connected = true;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * Constructor of Main class
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get context and set View
        MainActivity.context = getApplicationContext();
        setContentView(R.layout.activity_main);
        
        // Get Buttons and TextViews members
        start_button = (ImageButton) findViewById(R.id.start_button);
    	change_service_name_button = (ImageButton) findViewById(R.id.change_service_name_button);
        connection_text = (TextView) findViewById(R.id.connection_textview);
        service_text = (TextView) findViewById(R.id.service_name_text);
    }
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 * If application is closed, the NSD manager must be also closed to avoid reinitialisation of NSD service with the same name
	 */
	@Override
	public void onDestroy() {
	    if (DEBUG) Toast.makeText(getApplicationContext(),"deattaching NSD service", Toast.LENGTH_SHORT).show();
	    // Teardown the service
	    if(server != null){
	    	destroyServer();
	    }
        if (DEBUG) Toast.makeText(getApplicationContext(),"destroid Main", Toast.LENGTH_SHORT).show();
    	timer_handler.removeCallbacks(updateTimerMethod);
		show_connected = false;
		super.onDestroy();	
	}
	
	
	/*
	 * Returns the context to the Server needed for the NSD manager
	 */
    public static Context getAppContext() {
        return MainActivity.context;
    }
    
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 * do what to do if we resume to this app from another
	 */
    @Override
	protected void onResume() {
	    if (DEBUG) Toast.makeText(getApplicationContext(),"on resume main", Toast.LENGTH_SHORT).show();
	    
	    // Stop wifi check timer if not yet done
	    if (timer_handler != null) timer_handler.removeCallbacks(updateTimerMethod);
		// make the activity fulycreen without navigation nor titlebar

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

		if (CHECK_WIFI) {
	        // Start a new timer to look for wifi connection
	        timer_handler = new Handler();
	        updateTimerMethod = new Runnable() {
				public void run() {
					handleWifi();
					timer_handler.postDelayed(this, TIMER_UPDATE_RATE);
				}
			};
			timer_handler.postDelayed(updateTimerMethod, 0);
		}
		super.onResume();
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
     * This methode is called, if the user clicks the start/connect button and it opens the Controller Activity
     */
    public void startPressed(View view) {
    	// start controller activity to control the monitor    	
    	Intent intent = new Intent(this, ControllerActivity.class);
        startActivity(intent);
    }
    
    
    /*
     * This methode is called, if the user clicks the change service name button, a alert window will pop up
     * where the user can enter the new wished session name
     */
    public void changeServiceName(View view) {
    	// Open an alert dialog where the name of the session should be entered
    	final EditText input = new EditText(this);
    	new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
    			.setTitle(getResources().getString(R.string.change_service_alert_title))
    			.setView(input)
    			// If Save button is pressed on the alert dialog
    			.setPositiveButton(getResources().getString(R.string.change_service_alert_positive_button), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    					
	    			   	// Get the typed name
    					service_name = "" + input.getText();
    					
    					// Handle problem with empty session name
    					if (TextUtils.isEmpty(service_name)) {
    						Toast input = Toast.makeText(getApplicationContext(), "Session name must not be empty", Toast.LENGTH_LONG);
    						input.show();
    					} else {
    	    				// Display toast
    	    				if(DEBUG) Toast.makeText(getApplicationContext(),"New service name: " + service_name, Toast.LENGTH_SHORT).show();
    	    				        	
    	    				// Destroy Server
    	    				if(server != null){
    	    					destroyServer();
    	    				}
    	    				    	    
    	    				// Update the textfiel beyond the start button
    	    				show_connected = false;
    	    				// Start new service
    	    				server = new Server(service_name);
    	    			}
    				}			
    			}).setNegativeButton(getResources().getString(R.string.change_service_alert_negative_button), new DialogInterface.OnClickListener() {
    				public void onClick(DialogInterface dialog, int whichButton) {
    				}
    			}).show();
    }
    
    
    void handleWifi() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (DEBUG) System.out.println("Connected: " + mWifi.isConnected() + "\n " );
		
        // If server is not ruinning
    	if (server == null) {
    		// Check if wifi connection active, if yes...
    		if (mWifi.isConnected()) {
    			// Start server
                server = new Server(service_name);
                // Display Green start button
                start_button.setImageResource(R.drawable.onbutton_green);
        		start_button.setEnabled(true); 
        		if (DEBUG) Toast.makeText(getApplicationContext(),"Start Server, enabled button", Toast.LENGTH_SHORT).show();
        		setConnectionText(0);
		    	show_connected = true;
		    	
        	// if no wifi connection
    		} else {
    			if (!showed_alert) {
    				// Show connection alert
    	        	new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
    			    .setTitle(R.string.connection_alert_title)
    		    	.setMessage(R.string.connection_alert_text)
    			    // If Save button is pressed on the alert dialog
    			    .setPositiveButton(R.string.connection_alert_button, new DialogInterface.OnClickListener() {
    			        public void onClick(DialogInterface dialog, int whichButton) {
    					        }
    			            }).show();
                    // Display Gray start button
                    start_button.setImageResource(R.drawable.onbutton_gray);
            		start_button.setEnabled(false); 
            		showed_alert = true;
            		// Display no connection warning
            		setConnectionText(1);
    			}
    		}
    	// If server is running and wifi died, detach server
    	} else {
    		if (!mWifi.isConnected()) {
    			// Teardown service
    			if (server != null) {
    				destroyServer();
                    // Display Gray start button
                    start_button.setImageResource(R.drawable.onbutton_gray);
               		start_button.setEnabled(false); 
            		if (DEBUG) Toast.makeText(getApplicationContext(),"Detached Server, disabled button", Toast.LENGTH_SHORT).show();    
            		showed_alert = false;    		
    	        }
    		} else {
    			if (!show_connected) {
                    // Display Green start button
                    start_button.setImageResource(R.drawable.onbutton_green);
            		start_button.setEnabled(true); 
            		if (DEBUG) Toast.makeText(getApplicationContext(),"Start Server, enabled button", Toast.LENGTH_SHORT).show();
            		// Show IP adress of device for older monitor device support
            		setConnectionText(0);
            		show_connected = true;
    			}
    		}
    	}
 
    }

    /*
     * Set the textviews to show the correct texts in order to display successfull or unsuccesfull server status
     */
	void setConnectionText(int state) {
		switch (state) {
			case 0:
				WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        		@SuppressWarnings("deprecation")
				String ipAddr = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        		WifiInfo wifiInfo = wm.getConnectionInfo();
		    	connection_text.setText(getResources().getString(R.string.check_connection_ip1)  + " "  + wifiInfo.getSSID().toString()
		    			+ getResources().getString(R.string.check_connection_ip3) + " " + ipAddr);
		    	service_text.setText(getResources().getString(R.string.check_connection_ip2)  + " \"" + service_name + "\"");
	    		service_text.setVisibility(View.VISIBLE);
		    	if (USER_INPUT_SERVICE_NAME) {
		    		change_service_name_button.setVisibility(View.VISIBLE);
		    	}
				break;
			case 1:
		    	connection_text.setText(getResources().getString(R.string.check_connection_no_wifi) + "\n");
		        change_service_name_button.setVisibility(View.GONE);
	    		service_text.setVisibility(View.GONE);
		    	break;
		}
	}	
	
	
	/*
	 * Destroy the whole server object and unregister Servcies right
	 */
	void destroyServer() {
		if (server != null) {
	    	if(server.getmNsdManager() != null){
	    		server.tearDownNSD();
	    		server.tearDownServer();
	    	}
       		server = null;
		}
	}
}