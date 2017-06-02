/**
 *
 * @package Patient Monitor - Controller
 * @copyright (c) 2014 University of Freiburg
 * @author Benjamin Völker
 * @email voelkerb@informatik.uni-freiburg.de
 * @date 17.11.2014
 * @summary Image array adapter to handle images in spinner objects right 
 * 
 */

package gui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class ImageArrayAdapter extends ArrayAdapter<Integer> {
	private Integer[] images;
	
	/*
	 * Constructor
	 */
	public ImageArrayAdapter(Context context, Integer[] images) {
	    super(context, android.R.layout.simple_spinner_item, images);
	    this.images = images;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getDropDownView(int, android.view.View, android.view.ViewGroup)
	 * returns the correct image for the given position
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		View item = getImageForPosition(position);
	    return item;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * returns the correct view for the image
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View item = getImageForPosition(position);
		   
	    return item;
	}
	
	
	/*
	 * handling the image of a position in the Integer array
	 */
	private View getImageForPosition(int position) {
	        ImageView imageView = new ImageView(getContext());
	        imageView.setId(images[position]);
	        imageView.setBackgroundResource(images[position]);
	        imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	        return imageView;
	}
}