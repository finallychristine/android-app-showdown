package com.cgthornt.avnet.fragment;

import java.util.ArrayList;
import java.util.Collections;

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


public class HostFragment extends BaseFragment {

	private static Report report;
	
	@Override
	public boolean shouldPreloadData() {
		return report == null;
	}
	
	@Override
	public void preloadData() {
		report = new Report();
	}
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.host_report, container, false);
		
		ListView list = (ListView) v.findViewById(R.id.host_list);

		
		NiceListAdapter adapter = new NiceListAdapter(v.getContext(), R.id.host_list, report.items);
		list.setAdapter(adapter);
		return v;
	}
	
	protected class Report {
		public Object[][] data;
		public ArrayList<ListItem> items = new ArrayList<ListItem>();
		
		public Report() {
			data = Db.getHostPercents();
			
			for(int i = 0; i < data.length; i++) {
				ListItem l = new ListItem(
						"<b>" + percentFormat.format((Double) data[i][2]) + "</b> " + ((String) data[i][0]),
						"<i>In " + ((Integer) data[i][1]) + " records</i>",
						R.drawable.server);
				l.setCompareValue((Double) data[i][2]);
				items.add(l);
			}
			
			// Sort the items
			Collections.sort(items, new ListItem.ListItemComparitor());
		}
	}
	
}
