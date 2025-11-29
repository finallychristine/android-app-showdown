package com.cgthornt.avnet.fragment;

import java.util.ArrayList;

import com.cgthornt.avnet.R;
import com.cgthornt.avnet.lib.BaseFragment;
import com.cgthornt.avnet.lib.BaseFragment.NiceListAdapter;
import com.cgthornt.avnet.lib.ListItem;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class LeftMenu extends BaseFragment  {

	private OnLeftMenuSelectedListener clickListener;
	
	private Class<?> klasses[];

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.left_menu, container, false);
		
		// TextView summary = (TextView) v.findViewById(R.id.hours_summary);
		ListView list = (ListView) v.findViewById(R.id.left_menu);

		ArrayList<ListItem> items = new ArrayList<ListItem>();
		String[] labels = getResources().getStringArray(R.array.left_menu_titles);
		String[] icons = getResources().getStringArray(R.array.left_menu_icons);
		
		for(int i = 0; i < labels.length; i++) {
			ListItem l = new ListItem(labels[i]);
			if(i < icons.length) {
				String ic = icons[i];
				if(!ic.isEmpty()) {
					int icId = getResources().getIdentifier(ic, "drawable", "com.cgthornt.avnet");
					l.image = icId;
				}
			}
			items.add(l);
		}
		
		NiceListAdapter adapter = new NiceListAdapter(v.getContext(), R.id.host_list, items);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				// Set BG color
				/*
				ListView list = (ListView) getView().findViewById(R.id.left_menu);
				for(int i = 0; i < list.getChildCount(); i++) {
					LinearLayout item = (LinearLayout) list.getChildAt(i);
					int color = position == i ? 0xFFDDDDDD : 0x0;
					item.setBackgroundColor(color);
				} */
				
				
				clickListener.onLeftMenuSelected(position, (position < getKlasses().length) ? getKlasses()[position] : null);
				
			}

		});
		
		return v;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			clickListener = (OnLeftMenuSelectedListener) activity;
		} catch(ClassCastException e) {
			Log.e("LeftMenu", "Passed activity does not implement the OnLeftMenuSelectedListener", e);
			throw e;
		}
	}
	
	public Class<?>[] getKlasses() {
		if(klasses == null) {
			String[] strClasses = getResources().getStringArray(R.array.left_menu_classes);
			klasses = new Class<?>[strClasses.length];
			for(int i = 0; i < strClasses.length; i++) {
				try {
					klasses[i] = Class.forName("com.cgthornt.avnet.fragment." + strClasses[i]);
				} catch (Exception e) {
					Log.e("LeftMenu", "Could not parse fragment classname " + strClasses[i], e);
				}
			}
		}
		return klasses;
	}
	
	public interface OnLeftMenuSelectedListener {
		public void onLeftMenuSelected(int index, Class<?> fragment);
	}





}
