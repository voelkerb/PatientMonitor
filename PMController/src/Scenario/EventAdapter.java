/**
 * Event adapter class for visualising events in the protocoll and scenario mode
 * @author Johannes
 * johannes.scherle@gmail.com
 * February 2015
 * University of Freiburg
 */

/*
 * Inspired by:
 * http://www.ezzylearning.com/tutorial/customizing-android-listview-items-with-custom-arrayadapter
 */

package Scenario;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.example.pmcontroller1.R;


public class EventAdapter extends ArrayAdapter<Event>{

	Context context; 
	int layoutResourceId;    
	Event data[] = null;

	/**
	 * Constructor
	 * @param context
	 * @param layoutResourceId
	 * @param data
	 */
	public EventAdapter(Context context, int layoutResourceId, Event[] data) {
		super(context, layoutResourceId, data);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.data = data;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		EventHolder holder = null;

		if(row == null) {
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, parent, false);
			// Set the UI Elements in the holder class.
			holder = new EventHolder();
			holder.ekgVariable = (TextView)row.findViewById(R.id.ekgVariable);
			holder.rrSys = (TextView)row.findViewById(R.id.rrSys);
			holder.rrDias = (TextView)row.findViewById(R.id.rrDias);
			holder.o2Variable = (TextView)row.findViewById(R.id.o2Variable);
			holder.co2Variable = (TextView)row.findViewById(R.id.co2Variable);
			holder.resp = (TextView)row.findViewById(R.id.respValue);
			holder.time = (TextView)row.findViewById(R.id.time);
			holder.timeStampMinutesTens = (TextView)row.findViewById(R.id.timestampMinutesTens);
			holder.timeStampSecondsTens = (TextView)row.findViewById(R.id.timestampSecondsTens);
			holder.timeStampMinutesOnes = (TextView)row.findViewById(R.id.timestampMinutesOnes);
			holder.timeStampSecondsOnes = (TextView)row.findViewById(R.id.timestampSecondsOnes);
			holder.heartPattern = (ImageView)row.findViewById(R.id.heartPattern);
			holder.o2Pattern = (ImageView)row.findViewById(R.id.o2Pattern);   
			holder.bpPattern =(ImageView)row.findViewById(R.id.bpPattern);
			holder.respPattern =(ImageView)row.findViewById(R.id.respPattern);
			holder.co2Pattern =(ImageView)row.findViewById(R.id.co2Pattern);
			holder.lowerRow = (LinearLayout)row.findViewById(R.id.lowerRow);
			holder.upperRow = (LinearLayout)row.findViewById(R.id.upperRow);
			holder.redLine = (View)row.findViewById(R.id.redLine);
			holder.flag = (TextView)row.findViewById(R.id.flagText);
			holder.flagLayout = (LinearLayout)row.findViewById(R.id.flagLayout);
			holder.cuff = (ImageView)row.findViewById(R.id.cuff);
			row.setTag(holder);
		} else {
			holder = (EventHolder)row.getTag();
		}

		// Set the UI-Elements.
		Event Event = data[position];
		holder.ekgVariable.setText(String.valueOf(Event._heartRateTo));
		holder.rrSys.setText(String.valueOf(Event._bloodPressureSys));
		holder.rrDias.setText(String.valueOf(Event._bloodPressureDias));
		holder.o2Variable.setText(String.valueOf(Event._oxygenTo));
		holder.co2Variable.setText(String.valueOf(Event._carbTo));
		holder.resp.setText(String.valueOf(Event._respRate));
		holder.time.setText(String.valueOf(Event._time));
		int minutesTens = Event._timeStamp / 600;
		int minutesOnes = Event._timeStamp / 60;
		holder.timeStampMinutesTens.setText(String.valueOf(minutesTens));
		holder.timeStampMinutesOnes.setText(String.valueOf(minutesOnes));
		int seconds = (Event._timeStamp % 60);
		int secondsOnes = (Event._timeStamp % 60) % 10;
		int secondsTens = seconds / 10;
		holder.timeStampSecondsTens.setText(String.valueOf(secondsTens));
		holder.timeStampSecondsOnes.setText(String.valueOf(secondsOnes));        
		// Set the icons for curve on/off.
		if (Event._heartOn) {
			holder.heartPattern.setVisibility(View.VISIBLE);
			setHpPicture(Event, holder);
		} else {
			holder.heartPattern.setVisibility(View.INVISIBLE);
		}
		if (Event._oxyOn) {
			holder.o2Pattern.setVisibility(View.VISIBLE);
			seto2Picture(Event, holder);
		} else {
			holder.o2Pattern.setVisibility(View.INVISIBLE);
		}
		if (Event._bpOn)
			holder.bpPattern.setVisibility(View.VISIBLE);
		else
			holder.bpPattern.setVisibility(View.INVISIBLE);
		if (Event._carbOn)
			holder.co2Pattern.setVisibility(View.VISIBLE);
		else
			holder.co2Pattern.setVisibility(View.INVISIBLE);
		if (Event._respOn)
			holder.respPattern.setVisibility(View.VISIBLE);
		else
			holder.respPattern.setVisibility(View.INVISIBLE);
		if (Event._cuffOn)
			holder.cuff.setVisibility(View.VISIBLE);
		else
			holder.cuff.setVisibility(View.INVISIBLE);
		
		// Set the layouts for the flag / no flag case.
		LinearLayout.LayoutParams paramsFlag = (LinearLayout.LayoutParams)
				holder.flagLayout.getLayoutParams();        
		android.widget.LinearLayout.LayoutParams paramsUpper = (android.widget.LinearLayout.LayoutParams) holder.upperRow.
				getLayoutParams();
		android.widget.LinearLayout.LayoutParams paramsLower = (android.widget.LinearLayout.LayoutParams) holder.lowerRow.
				getLayoutParams();

		paramsFlag.weight = 0.0f;
		if (Event._flag) {        
			// Set the red line to visible.
			holder.redLine.setVisibility(View.VISIBLE);        	
			paramsFlag.height = LayoutParams.WRAP_CONTENT;        	
			paramsUpper.height = 0;
			paramsLower.height = 0;
			holder.flag.setVisibility(View.VISIBLE);
			holder.flag.setText("Pressed flag at: " +
					String.valueOf(minutesTens) +
					String.valueOf(minutesOnes) +
					":" +
					String.valueOf(secondsTens) +
					String.valueOf(secondsOnes));
		} else {
			// This has to be set, to not mix up the items.
			holder.redLine.setVisibility(View.INVISIBLE);
			holder.flag.setVisibility(View.INVISIBLE);
			paramsFlag.weight = 0.0f;
			paramsUpper.height = LayoutParams.WRAP_CONTENT;
			paramsLower.height = LayoutParams.WRAP_CONTENT;;        	
		}
		return row;
	}

	/**
	 * Function to set the HeartPattern Picture.
	 * @param e Event which should be used.
	 * @param eh holder, that holds the UI Elements
	 */
	private void setHpPicture(Event e, EventHolder eh) {
		switch(e._heartPattern) {
		case SINE:
			eh.heartPattern.setImageResource(R.drawable.hr_sine);
			break;
		case ARRYTHMIC:
			eh.heartPattern.setImageResource(R.drawable.hr_absolute_arrhythmie);
			break;
		case AVBLOCK:
			eh.heartPattern.setImageResource(R.drawable.hr_avblock);
			break;
		case LEFTBLOCK:
			eh.heartPattern.setImageResource(R.drawable.hr_leftblock);
			break;
		case LEFTBLOCKAA:
			eh.heartPattern.setImageResource(R.drawable.hr_leftblock_aa);
			break;
		case STEMI:
			eh.heartPattern.setImageResource(R.drawable.hr_stemi);
			break;
		case PACE:
			eh.heartPattern.setImageResource(R.drawable.hr_pacer);
			break;
		case VENTFLUTTER:
			eh.heartPattern.setImageResource(R.drawable.hr_ventriflutter);
			break;
		case VENTFIBRI:
			eh.heartPattern.setImageResource(R.drawable.hr_ventifibri);
			break;
		case CPR:
			eh.heartPattern.setImageResource(R.drawable.hr_cpr);
			break;
		case ASYSTOLE:
			eh.heartPattern.setImageResource(R.drawable.hr_asystoly);
			break;
		default:
			break;
		}
	}

	/**
	 * Function to set the O2Pattern Picture.
	 * @param e Event which should be used.
	 * @param eh holder, that holds the UI Elements
	 * @author Johannes
	 */
	private void seto2Picture(Event e, EventHolder eh) {
		switch (e._oxyPattern) {
		case NORMAL:
			eh.o2Pattern.setImageResource(R.drawable.o2_normal);
			break;
		case COLDFINGER:
			eh.o2Pattern.setImageResource(R.drawable.o2_coldfingers);
			break;
		default:
			break;
		}
	}

	/**
	 * Helper class for the items.
	 * @author Johannes
	 *
	 */
	static class EventHolder {
		TextView ekgVariable;
		TextView rrSys;
		TextView rrDias;
		TextView o2Variable;
		TextView co2Variable;
		TextView resp;
		TextView time;
		TextView timeStampMinutesTens;
		TextView timeStampMinutesOnes;
		TextView timeStampSecondsTens;
		TextView timeStampSecondsOnes;
		LinearLayout upperRow;
		LinearLayout lowerRow;
		ImageView heartPattern;
		ImageView o2Pattern;
		ImageView bpPattern;
		ImageView respPattern;
		ImageView co2Pattern;
		View redLine;
		TextView flag;
		ImageView cuff;
		LinearLayout flagLayout;

	}
}