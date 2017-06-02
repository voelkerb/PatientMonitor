/**
 * Container-Class for the events
 * @author Johannes
 * February 2015
 * johannes.scherle@gmail.com
 * University Freiburg
 */
package Scenario;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class Event {
	
  // An index to identify the event.
  public Integer _index;
  
  // Variables for the heart rate and pattern.
  public Integer _heartRateTo;
  public Integer _time;
  public enum HeartPattern {
    SINE, ARRYTHMIC, AVBLOCK, LEFTBLOCK, LEFTBLOCKAA, STEMI, PACE, 
    VENTFLUTTER, VENTFIBRI, CPR, ASYSTOLE
  }  
  public HeartPattern _heartPattern;
  public boolean _heartOn;
  
  // Variables for the blood pressure.
  public Integer _bloodPressureSys;
  public Integer _bloodPressureDias;
  public enum BloodPressPattern {
	  NORMAL, BP2
  }
  public BloodPressPattern _bpPattern;
  public boolean _bpOn;
  public boolean _cuffOn;  
  
  // Variables for the oxygen
  public Integer _oxygenTo;
  public enum O2Pattern {
	    NORMAL, COLDFINGER
  }	  
  public O2Pattern _oxyPattern;
  
  // Variables for Respiration  
  public boolean _oxyOn;  
  public Integer _respRate;
  public enum RespPattern{
	  NORMAL, RESP1
  }
  public RespPattern _respPattern;
  public boolean _respOn;
  
  // Variables for the CO2
  public Integer _carbTo;  
  public boolean _carbOn;  
  public enum CarbPattern{
	  NORMAL, CARB1
  }
  public CarbPattern _carbPattern;  
  
  // Variables for the timing.
  public Integer _timeStamp;
  public boolean _syncTimer;
  public boolean _flag;
  public enum TimerState {
	  RUN, START, PAUSE, STOP, RESET
  }
  public TimerState _timerState;
  
  /**
   * Constructor
   * @param time
   * @param heartRateTo
   * @param heartPattern
   * @param bloodPressureSys
   * @param bloodPressureDias
   * @param bloodPressurePattern
   * @param oxygenTo
   * @param oxyPattern
   * @param respRate
   * @param respPattern
   * @param carbTo
   * @param carbPattern
   * @param timeStamp
   * @param heartOn
   * @param bpOn
   * @param cuffOn
   * @param oxyOn
   * @param carbOn
   * @param respOn
   * @param syncTimer
   * @param flag
   * @param timerState
   */
  public Event(Integer time, Integer heartRateTo, 
        HeartPattern heartPattern, Integer bloodPressureSys,
        Integer bloodPressureDias, BloodPressPattern bloodPressurePattern,
        Integer oxygenTo, O2Pattern oxyPattern, Integer respRate, 
        RespPattern respPattern, Integer carbTo, CarbPattern carbPattern,
        Integer timeStamp, boolean heartOn, boolean bpOn, boolean cuffOn,
        boolean oxyOn, boolean carbOn, boolean respOn, boolean syncTimer,
        boolean flag, TimerState timerState) {
	    	  
    this._time = time;
    this._heartRateTo = heartRateTo;
    this._heartPattern = heartPattern;	  
    this._bloodPressureSys = bloodPressureSys;
    this._bloodPressureDias = bloodPressureDias;
    this._bpPattern = bloodPressurePattern;
    this._oxygenTo = oxygenTo;
    this._oxyPattern = oxyPattern;
    this._respRate = respRate;
    this._respPattern = respPattern;
    this._carbTo = carbTo;
    this._carbPattern = carbPattern;    
    this._timeStamp = timeStamp;
    this._heartOn = heartOn;
    this._bpOn = bpOn;
    this._oxyOn = oxyOn;
    this._carbOn = carbOn;
    this._cuffOn = cuffOn;
    this._respOn = respOn;
    this._syncTimer = syncTimer;
    this._flag = flag;
    this._timerState = timerState;
  };
  
  /**
   * Typical toString method.
   */
  @Override
  public String toString() {
    String string = "Scheduler: " + this._time.toString() + "\n" +
    		        "BPSys: " + this._bloodPressureSys.toString() + ",  " + 
    		        "BPDias: " + this._bloodPressureDias.toString() + ",  " +
    		        "Pattern: " + this._bpPattern.toString() + ",  " +
    		        "Curve BP: " + ((this._bpOn) ? "On" : "Off") + "\n" +
    		        "HR: " + this._heartRateTo.toString() + ",  " + 
    		        "Pattern: " + this._heartPattern.toString() + ",  " + 
                    "Curve HR: " + ((this._heartOn) ? "On" : "Off") + "\n" +
    		        "Oxy: " + this._oxygenTo.toString() + ",  " + 
                    "Pattern: " + this._oxyPattern.toString() + ",  " +
                    "Curve O2: " + ((this._oxyOn) ? "On" : "Off") + "\n" +
                    "Resp: " + this._respRate.toString() + ",  " +
                    "Pattern: " + this._respPattern.toString() + "\n" +                    
                    "Curve Resp: " + ((this._respOn) ? "On" : "Off") + "\n" +
                    "Carb: " + this._carbTo.toString() + ",  " +
                    "Curve Carb: " + ((this._carbOn) ? "On" : "Off") + ",  " +  
                    "Pattern: " + this._carbPattern.toString() + "\n" +    
    		        "CuffCorrect: " + ((this._cuffOn) ? "Yes" : "No") + "\n" +                  
                    "TimeStamp: " + this._timeStamp.toString() + "\n" +
                    "SyncTimer: " + ((this._syncTimer) ? "Yes" : "No") + "\n" +
                    "Flag: " + ((this._flag) ? "Yes" : "No" + "\n" +
                    "Timer State: " + this._timerState.toString());
	return string;    
  }
  
  /**
   * Convert an Event to JSon String.
   * @return JSon String
   */
  public String toJson() {
	  GsonBuilder builder = new GsonBuilder();
      builder.setPrettyPrinting().serializeNulls();
      Gson gson = builder.create();      
      System.out.println(gson.toJson(this));	  
	  return gson.toJson(this);
  }
  
  /**
   * Convert a JSon String to an event.
   * @param g JSon String to parse
   * @return The parsed Event.
   */
  static public Event fromJsonEvent(String g) {
	  Gson gson = new Gson();
	  return gson.fromJson(g, Event.class);	  
  }
}
