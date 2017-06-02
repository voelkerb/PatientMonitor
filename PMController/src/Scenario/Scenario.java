/**
 * Container-Class for the scenarios.
 * @author Johannes
 * February 2015
 * johannes.scherle@gmail.com
 * University Freiburg
 */
package Scenario;


import java.util.ArrayList;
import java.util.List;

import Scenario.Event.BloodPressPattern;
import Scenario.Event.CarbPattern;
import Scenario.Event.HeartPattern;
import Scenario.Event.O2Pattern;
import Scenario.Event.RespPattern;
import Scenario.Event.TimerState;

public class Scenario {
	private boolean _runnable;
	private String _name;
	private List<Event> _eventList;

	/**
	 * Constructor
	 * @param name
	 * @param runnable
	 */
	public Scenario(String name, boolean runnable) {
		this._name = name;
		this._runnable = runnable;
	}

	/**
	 * Overloaded constructor with event list.
	 * @param name
	 * @param runnable
	 * @param eventList
	 */
	public Scenario(String name, boolean runnable, List<Event> eventList) {
		this._name = name;
		this._runnable = runnable;
		this._eventList = eventList;
	}

	/**
	 * Converts integer to a heart pattern.
	 * @param i position in enum.
	 * @return corresponding heart pattern.
	 */
	public static HeartPattern intToHeartPattern(Integer i) {
		switch(i) {
		case 0:
			return HeartPattern.SINE;
		case 1:
			return HeartPattern.ARRYTHMIC;
		case 2:
			return HeartPattern.AVBLOCK;
		case 3:
			return HeartPattern.LEFTBLOCK;
		case 4:
			return HeartPattern.LEFTBLOCKAA;
		case 5:
			return HeartPattern.STEMI;
		case 6:
			return HeartPattern.PACE;
		case 7:
			return HeartPattern.VENTFLUTTER;
		case 8:
			return HeartPattern.VENTFIBRI;
		case 9:
			return HeartPattern.CPR;
		case 10:
			return HeartPattern.ASYSTOLE;
		default:
			return null;
		}
	}

	/**
	 * Converts integer to a blood pressure pattern.
	 * @param i position in enum.
	 * @return corresponding blood pressure pattern.
	 */	
	public static BloodPressPattern intToBpPattern(int i) {
		switch(i) {
		case 0:
			return BloodPressPattern.NORMAL;
		case 1:
			return BloodPressPattern.BP2;
		default:
			return null;
		}
	}   

	/**
	 * Converts integer to a O2 pattern.
	 * @param i position in enum.
	 * @return corresponding oxygen pattern.
	 */	
	public static O2Pattern intToO2Pattern(int i) {
		switch(i) {
		case 0:
			return O2Pattern.NORMAL;
		case 1:
			return O2Pattern.COLDFINGER;
		default:
			return null;
		}
	}  

	/**
	 * Converts integer to a carb pattern.
	 * @param i position in enum.
	 * @return corresponding carb pattern.
	 */	
	public static CarbPattern intToCarbPattern(int i) {
		switch(i) {
		case 0:
			return CarbPattern.NORMAL;
		case 1:
			return CarbPattern.CARB1;
		default:
			return null;
		}
	}    

	/**
	 * Converts integer to a respiratio pattern.
	 * @param i position in enum.
	 * @return corresponding respiratio pattern.
	 */	
	public static RespPattern intToRespPattern(int i) {
		switch(i) {
		case 0:
			return RespPattern.NORMAL;
		case 1:
			return RespPattern.RESP1;    		
		default:
			return null;
		}
	}    

	/**
	 * Converts integer to a timer state.
	 * @param i position in enum.
	 * @return corresponding timer state.
	 */	
	public static TimerState intToTimerState(int i) {
		switch(i) {
		case 0:
			return TimerState.RUN;
		case 1:
			return TimerState.START;
		case 2:
			return TimerState.PAUSE;
		case 3:
			return TimerState.STOP;
		case 4:
			return TimerState.RESET;
		default:
			return null;
		}
	}

	// Add an event to a scenario
	public void addEvent(Event e) {
		if(this._eventList == null)
			this._eventList = new ArrayList<Event>();
		this._eventList.add(e);
	}

	// Getter for the scenario name
	public String getName() {
		return this._name;
	}

	// Get for the event list
	public List<Event> getEventList() {
		return this._eventList;
	}

	// Set event list
	public void setEventList(List<Event> theEventList) {
		this._eventList = theEventList;
	}

	// Getter for the runnable value.
	public boolean isRunnable() {
		return this._runnable;
	}

	// To string method.
	public String toString() {
		return this._name;
	}

	// Clears the event list.
	public void clearEvents() {
		this._eventList = null;
	}

	// Setter for runnable
	public void setRunnable(boolean runnable) {
		this._runnable = runnable;
	}
}
