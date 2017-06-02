/**
 * Class to store all available protocols / scenarios.
 * @author Johannes Scherle
 * November 2014
 * johannes.scherle@googlemail.com
 * University of Freiburg
 */

package Scenario;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScenarioDatabaseHelper extends SQLiteOpenHelper  {

	// The column names.
	private static final String KEY_SCENARIONAME = "NAME";
	private static final String KEY_RUNNABLE = "RUNNABLE";
	public static final int DATABASE_VERSION = 1;
	private static String DATABASE_NAME = "SCENARIODATABASE";
	private static final String KEY_INDEX = "_id";

	/**
	 * Constructor for the database helper.
	 * @param context
	 */
	public ScenarioDatabaseHelper(Context context) {      
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	/**
	 * On Upgrade method.
	 */
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
		onCreate(db);
	}

	/**
	 * On create method.
	 */
	public void onCreate(SQLiteDatabase db) {
		String CREATE_SCENARIO_TABLE = "CREATE TABLE " + DATABASE_NAME + " ( " +
				KEY_INDEX + " INTEGER PRIMARY KEY , " + KEY_SCENARIONAME + " TEXT, " +
				KEY_RUNNABLE + " INTEGER" + ");";
		db.execSQL(CREATE_SCENARIO_TABLE);
	}


	/**
	 * On Downgrade method.
	 */
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}  
	
	/**
	 * Returns a list with all the stored scenarios.
	 * @param scenarioHelper
	 * @param isRunnable
	 * @return
	 * @author Johannes
	 */
	public List<Scenario> getAllScenarios(ScenarioHelper scenarioHelper, boolean isRunnable) {
		List<Scenario> scenarioList = new ArrayList<Scenario>();

		// Select All Query
		String selectQuery = "SELECT  * FROM " + DATABASE_NAME;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				String name = cursor.getString(1);
				boolean runnable = (cursor.getInt(2) != 0);
				if (runnable == isRunnable) {
					Scenario scenario = new Scenario(name, runnable);
					Scenario tmpScenario = scenarioHelper.loadScenario(scenario
							.getName());
					// Get the event list to the scenario and set it.
					if (tmpScenario != null) {
						List<Event> eventList = scenarioHelper.loadScenario(
								tmpScenario.getName()).getEventList();
						scenario.setEventList(eventList);
						scenarioList.add(scenario);
					}
				}
			} while (cursor.moveToNext());
		}
		return scenarioList;    
	}

	/**
	 * Add a scenario to the scenario database. Before that, check if the
	 * scenario already exists to not create doubles. 
	 * @param scenario
	 * @return 1 if successful -1 of scenario already exists.
	 * @author Johannes
	 */
	public int addScenario(Scenario scenario) {
		// Check if scenario name already exists.
		String query = KEY_SCENARIONAME + " = " + "'" + scenario.getName() + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		String[] columns = {KEY_SCENARIONAME};
		Cursor cursor = db.query(DATABASE_NAME, columns, query, null, null, null, null);
		// If the cursor is not empty the scenarioname already exists.
		if (cursor.moveToFirst())
			return -1; 
		// Add the scenario to the database.
		ContentValues values = new ContentValues();
		values.put(KEY_SCENARIONAME, scenario.getName());
		values.put(KEY_RUNNABLE, (scenario.isRunnable() ? 1 : 0));
		db.insert(DATABASE_NAME, null, values);
		return 1;
	}

	/**
	 * Delete a scenario from the database.
	 * @param scenario the scenario which should be deleted
	 * @return
	 */
	public int deleteScenario(Scenario scenario) {
		SQLiteDatabase db = this.getWritableDatabase();
		return db.delete(ScenarioDatabaseHelper.DATABASE_NAME,
				KEY_SCENARIONAME + " =?", new String[] {scenario.getName()});
	}

	// Get the runnable value of a scenario.
	public boolean getRunnable(Scenario scenario) {
		SQLiteDatabase db = this.getWritableDatabase();
		// Query for the scenario name.
		String whereClause = KEY_SCENARIONAME + " = '" + scenario.getName() + "'";
		Cursor cursor = db.query(DATABASE_NAME, null, whereClause,
				null, null, null, null, null);		
		if(cursor.moveToFirst() && cursor != null) {
			int runnableInt = (int) cursor.getInt(1);
			boolean runnable = (runnableInt == 1) ? true : false; 
			return runnable;
		}    
		return true;
	}

	// Set the runnable of a scenario.
	public void setRunnableInDatabase(Scenario scenario) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_RUNNABLE, (scenario.isRunnable() ? 1 : 0));
		String whereclause = KEY_SCENARIONAME + " = '" + scenario.getName() + "'";
		db.update(DATABASE_NAME, values, whereclause, null);
	}

	// Delete all scenarios meaning drop the database table.
	public void deleteAllScenarios() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
	}
}
