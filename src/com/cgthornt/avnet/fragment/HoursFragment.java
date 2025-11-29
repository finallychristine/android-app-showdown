package com.cgthornt.avnet.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.cgthornt.avnet.R;
import com.cgthornt.avnet.lib.BaseFragment;
import com.cgthornt.avnet.lib.Db;
import com.cgthornt.avnet.lib.ListItem;




public class HoursFragment extends BaseFragment {

	private static Report report;
	
	@Override
	public boolean shouldPreloadData() {
		return report == null;
	}
	
	@Override
	public void preloadData() {
		Log.i("HoursFragment", "Loading Data...");
		report = new Report();
		Log.i("HoursFragment", "Done!");
	}
	
	
	public String formatTime(double time) {
		int hour = (int) time;
		boolean am = true;
		if(hour > 12) {
			hour -= 12;
			am = false;
		}
		if(hour == 12)
			am = false;
		
		return String.format("%2d", hour) + (am ? "am" : "pm");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.hours_report, container, false);
		
		TextView summary = (TextView) v.findViewById(R.id.hours_subtext);
		ListView list = (ListView) v.findViewById(R.id.hours_list);
		
		int parsed = report.numRecordsParsed;
		double percent = (double) parsed / Db.tableCount();
		
		// summary.setText("Displaying data from a total of " + report.numRecordsParsed + " records");
		
		summary.setText(summary.getText() + " Reports contain data from " + parsed + " records (" + percentFormat.format(percent) + " of total records).");
		
		ArrayList<ListItem> items = new ArrayList<ListItem>();
		for(double[] d : report.data) {
			ListItem l = new ListItem(
					"<b>" + formatTime(d[0]) + "</b> " + percentFormat.format(d[2]),
					"<i>In " + ((int) d[1]) + " entries</i>",
					R.drawable.image_generic);
			
			//l.setIconText(formatTime(d[0]));
			
			items.add(l);
		}
		NiceListAdapter adapter = new NiceListAdapter(v.getContext(), R.id.hours_list, items);
		list.setAdapter(adapter);
		return v;
	}
	
	protected class Report {
		
		public double[][] data;
		
		public int numRecordsParsed = 0;
		
		public Report() {
			data = Db.getHourCountsAndPercent();
			for(int i = 0; i < data.length; i++)
				numRecordsParsed += data[i][1];
		}
	}
	
}
