package com.cgthornt.avnet.activity;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;
import android.widget.TextView;

import com.cgthornt.avnet.R;
import com.cgthornt.avnet.fragment.*;
import com.cgthornt.avnet.lib.App;
import com.cgthornt.avnet.lib.BaseActivity;
import com.cgthornt.avnet.lib.BaseFragment;
import com.cgthornt.avnet.lib.Db;
import com.cgthornt.avnet.model.LogEntry;

public class WelcomeActivity extends BaseActivity implements LeftMenu.OnLeftMenuSelectedListener {

	public int lastClickedPosition = -1;
	
	
	
	protected void showImportScreen() {
		startActivityForResult(new Intent(this, ImportActivity.class), 1);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		App.closeDatabase();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		try {
			App.start(getApplicationContext());
			App.cnx.newEntity(LogEntry.class);
			
		} catch(Exception e) {
			Log.wtf("WelcomeActivity", "Exception initializing application", e);
			finish();
			return;
		}
		
		if(Db.tableCount() == 0) {
			setContentView(R.layout.blank);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("No data has been imported; import data?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						showImportScreen();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				}).create().show();
			
		} else {
		
			Integer savedClickedIndex = (Integer) getLastNonConfigurationInstance();
			int initialClick = (savedClickedIndex != null && savedClickedIndex >= 0) ? savedClickedIndex : 0;
			onLeftMenuSelected(initialClick, WelcomeFragment.class);
			
			setContentView(R.layout.main_layout);
		}
	}
	
	// Restart activity upon result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent i = getIntent();
		finish();
		startActivity(i);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.welcome_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menu_welcome_load_data:
				showImportScreen();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	// Remember last clicked index
	@Override
	public Object onRetainNonConfigurationInstance() {
		return new Integer(lastClickedPosition);
	}

	
	// Remember position in case of orientation change
	public void onLeftMenuSelected(int index, Class<?> fragment) {
		if(index == lastClickedPosition)
			return;
		lastClickedPosition = index;
		if(fragment == null) {
			Log.i("WelcomeActivity", "Clicked new position '" + index + "' with null class");
		} else {
			FragmentTransaction transaction = getFragmentManager().beginTransaction();
			try {
				
				
				
				
				Fragment f = (Fragment) fragment.newInstance();
				transaction.replace(R.id.main_content, f);
				transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				
				if(f instanceof BaseFragment) {
					BaseFragment bf = (BaseFragment) f;
					if(bf.shouldPreloadData()) {
						doPreloadData(bf, transaction);
						return;
					}
				}
				
				
				transaction.commit();
			} catch (Exception e) {
				Log.wtf("WelcomeActivity", "Unexpected exception caught when loading new fragment", e);
				return;
			}
		}
	}
	
	protected void doPreloadData(final BaseFragment fragment, final FragmentTransaction trans) {
		final ProgressDialog dialog = ProgressDialog.show(this, "", 
                "Loading. Please wait...", true);
		dialog.setCancelable(false);
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected void onPreExecute() {
				dialog.show();
			}
			
			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();
				trans.commit();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				fragment.preloadData();
				return null;
			}
			
		}.execute();
	}
	
}
