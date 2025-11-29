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




public class UsersFragment extends BaseFragment {

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
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.users_report, container, false);
		
		// TextView summary = (TextView) v.findViewById(R.id.hours_summary);
		ListView list = (ListView) v.findViewById(R.id.users_list);

		
		NiceListAdapter adapter = new NiceListAdapter(v.getContext(), R.id.users_list, report.items);
		list.setAdapter(adapter);
		return v;
	}
	
	protected class Report {
		
		public int[][] data;
		
		public ArrayList<ListItem> items = new ArrayList<ListItem>();
		
		
		
		public Report() {
			
			
			data = Db.getTopTenEmployees();
			
			int count = 0;
			for(int[] d : data) {
				count++;
				ListItem l = new ListItem(
						"<b>" + count + "</b> #" + String.format("%06d", d[0]),
						"<i>In " + d[1] + " records</i>",
						R.drawable.address_book);
				
				items.add(l);
			}
		}
	}
	
}
