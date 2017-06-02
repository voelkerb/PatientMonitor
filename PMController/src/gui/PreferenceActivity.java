/**
 *
 * @package Patient Monitor - Controller
 * @copyright (c) 2014 University of Freiburg
 * @author Benjamin Völker
 * @email voelkerb@informatik.uni-freiburg.de
 * @date 17.11.2014
 * @summary Preference view. 
 * 
 */

package gui;

//Include stuff goes here
import com.example.pmcontroller1.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

/*
 * Preference activity
 */
public class PreferenceActivity extends Activity {

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * Constructor
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Make the action bar overlay
		this.getWindow();
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		// Set content view
		setContentView(R.layout.activity_preference);
		// Setup the action bar
		setupActionBar();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 * If dialog boxes or other notification appear, the navigation and status bar will get visible -> hide it again if done
	 */
	@SuppressLint("InlinedApi") @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		System.out.println("focus change");
		// make the activity fulscreen without navigation bar
		if (Build.VERSION.SDK_INT >= 16) { //KITKAT
		    if (hasFocus) {
		    	this.findViewById(android.R.id.content).setSystemUiVisibility(
		                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
		                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
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
		if (Build.VERSION.SDK_INT >= 16) { //KITKAT	
			this.findViewById(android.R.id.content).setSystemUiVisibility(
	                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
	                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_FULLSCREEN
	                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.preference, menu);
        getActionBar().setDisplayShowHomeEnabled(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
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
}
