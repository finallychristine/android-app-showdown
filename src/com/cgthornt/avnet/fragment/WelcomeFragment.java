package com.cgthornt.avnet.fragment;

import com.cgthornt.avnet.R;
import com.cgthornt.avnet.lib.Db;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WelcomeFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.welcome, container, false);
	}
}
