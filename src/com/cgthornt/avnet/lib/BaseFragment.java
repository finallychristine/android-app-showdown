package com.cgthornt.avnet.lib;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.cgthornt.avnet.R;

import android.app.Fragment;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseFragment extends Fragment {

	/**
	 * If true, causes a loading dialog and calls preloadData. If false, does not call preload data
	 * @return
	 */
	public boolean shouldPreloadData() { return false; }
	
	public void preloadData() { }
	
	public static NumberFormat percentFormat = NumberFormat.getPercentInstance();
	
	
	public class NiceListAdapter extends ArrayAdapter<ListItem> {

		private ArrayList<ListItem> items;
		
	    public NiceListAdapter(Context context, int textViewResourceId, ArrayList<ListItem> items) {
	    	super(context, textViewResourceId, items);
	    	this.items = items;
	    }
	    
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.generic_list_item, null);
	        }
	        ListItem item = items.get(position);
	        if(item != null) {
	        	ImageView img = (ImageView) v.findViewById(R.id.list_icon);
	        	TextView title = (TextView) v.findViewById(R.id.list_title),
	        			subtext = (TextView) v.findViewById(R.id.list_subtext),
	        			iconText = (TextView) v.findViewById(R.id.list_icon_text);
	        	
	        	if(item.iconText != null) {
	        		iconText.setText(Html.fromHtml(item.iconText));
	        		iconText.setVisibility(View.VISIBLE);
	        	}
	        	
	        	
	        	if(item.image != -1)
	        		img.setImageResource(item.image);
	        	if(item.title != null)
	        		title.setText(Html.fromHtml(item.title));
	        	if(item.subtext != null) {
	        		subtext.setText(Html.fromHtml(item.subtext));
	        		subtext.setVisibility(View.VISIBLE);
	        	} else
	        		subtext.setVisibility(View.GONE);
	        }
	        return v;
	    }
		
	}
	
}
