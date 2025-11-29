package com.cgthornt.avnet.activity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.cgthornt.avnet.R;
import com.cgthornt.avnet.lib.App;
import com.cgthornt.avnet.model.LogEntry;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ImportActivity extends Activity {

	protected static ProgressBar progressBar;
	
	protected static Button importButton, cancelButton;
	protected static TextView importPercent, importStatus;
	protected static SimpleDateFormat dateFormatter, dateDebugger;
	protected static ImportWorker importer;
	
	
	protected void notifyImportComplete() {
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher_tiny;
		CharSequence text = "Import Complete";
		long when = System.currentTimeMillis();
		
		Notification notification = new Notification(icon, text, when);
		PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, WelcomeActivity.class), 0);
		notification.setLatestEventInfo(getApplicationContext(), "Import Complete", "The data importer has completed", intent);
		nm.notify(1, notification);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.import_data);
		// notifyImportComplete();
	}
	
	public void onResume() {
		super.onResume();
		
		App.openDatabase();
		final Spinner spinner = (Spinner) findViewById(R.id.import_spinner);
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		try {
			path.mkdirs();
			File[] files = path.listFiles();
			
			if(files.length > 0) {
				ArrayAdapter<File> adapter = new ArrayAdapter<File>(this, android.R.layout.simple_spinner_item, files);
				spinner.setAdapter(adapter);
			} else {
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, new String[] { "No files found under downloads folder" });
				spinner.setAdapter(adapter);
				spinner.setEnabled(false);
				importButton.setEnabled(false);
			}
		} catch (Exception e ) {
			
		}
		
		importButton = (Button) findViewById(R.id.import_submit);
		cancelButton = (Button) findViewById(R.id.import_cancel);
		importPercent = (TextView) findViewById(R.id.import_percent);
		importStatus = (TextView) findViewById(R.id.import_status);
		
				
		progressBar = (ProgressBar) findViewById(R.id.import_progress);
		
		// Make pressing the back button and cancel button do the same thing
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		
		if(importer != null && importer.getStatus() == AsyncTask.Status.RUNNING) {
			importButton.setEnabled(false);
			importButton.setText("Importing");
		}
		
	
		importButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				App.confirmDialog(ImportActivity.this, "Are you sure you want to import this log file?\nThis will overwrite ALL existing data!",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							
							// Delete all content from the existing table
							App.db.execute("DELETE FROM LOG_ENTRY");
							
							dialog.dismiss();
							importer = new ImportWorker((File) spinner.getSelectedItem());
							importer.execute();
						}
				});
				
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
		Log.i("ImportActivity", "Back button pressed");
		// If the importer is not working as normally, cancel it!
		if(importer == null || importer.getStatus() != AsyncTask.Status.RUNNING) {
			super.onBackPressed();
			return;
		}
		
		App.confirmDialog(this, "Are you sure you want to cancel the import?", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				importer.cancel(true);
				ImportActivity.super.onBackPressed();
			}
		});
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		importButton = cancelButton = null;
		importPercent = importStatus = null;
	}
	
	
	/**
	 * Helper function to convert a line of text
	 * @param line
	 * @throws Exception
	 */
	protected void importLine(String line) throws Exception {
		LogEntry entry = App.cnx.newEntity(LogEntry.class);
		//Log.i("ImportActivity", "Importing line: \"" + line + "\"");
		String[] parts = line.split(",");
		Date date;
		for(String p : parts) {
			
			String[] s = p.split(":", 2);
			//Log.i("ImportActivity", "\t[" + parts.length + " parts]: " + p + " -> (1) " + s[0] + " (2) ");
			String key = s[0].trim();
			String val = s[1].trim();
			
			//Log.i("ImportActivity", "Importing key \"" + key + "\" with value \"" + val + "\"");
			
			// Host
			if(key.equals("host"))
				entry.host = val;
			
			// Employee ID
			if(key.equals("empl_id"))
				entry.employee_id = val;
			
			// Dest
			if(key.equals("dest"))
				entry.destination = val;
			
			// Doc Type
			if(key.equals("doc_types")) {
				val = val.toLowerCase();
				entry.excel = val.indexOf("excel") == -1 ? 0 : 1;
				entry.csv   = val.indexOf("csv") == -1 ? 0 : 1;
				entry.xml   = val.indexOf("xml") == -1 ? 0 : 1;
				entry.pdf   = val.indexOf("pdf") == -1 ? 0 : 1;
			}
			
			// Date time
			if(key.equals("date_time")) {
				
				// Remove Brackets
				val = val.replace('[', ' ');
				val = val.replace(']', ' ');
				val = val.trim();
				date = dateFormatter.parse(val);
				
				// Log.i("ImportActivity", "Parsed date literal '" + val + "' as '" + dateDebugger.format(date) + "'");
				
				// Convert to a timestamp
				entry.timestamp = new java.sql.Timestamp(date.getTime());
			}
		}
		
		entry.save();
	}
	
	
	protected class ImportWorker extends AsyncTask<Void, Integer, Void> {

		protected NumberFormat percentFormat;
		
		protected Exception exception;
		
		protected File file;
		
		protected int numLines;
		
		protected int linesParsed = 0;
		
		public ImportWorker(File f) {
			file = f;
		}
		
		
		/**
		 * Called before execution. Creates a date formatter and determine the number of lines in the file
		 * that is to be imported.
		 */
		@Override
		protected void onPreExecute() {
			// Make a date formatter
			dateFormatter = new SimpleDateFormat("MM/dd/yy HH:mm:ss:SS z");
			dateDebugger = new SimpleDateFormat("EEE, MMM d, yyyy");
			
			percentFormat = NumberFormat.getPercentInstance();
			percentFormat.setMaximumFractionDigits(0);
			
			// Count the number of lines in the file
			// http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
		    InputStream is = null;
		    try {
		    	is = new BufferedInputStream(new FileInputStream(file));
		        byte[] c = new byte[1024];
		        int count = 0;
		        int readChars = 0;
		        while ((readChars = is.read(c)) != -1) {
		            for (int i = 0; i < readChars; ++i) {
		                if (c[i] == '\n')
		                    ++count;
		            }
		        }
		        numLines = count;
		        is.close();
		    } catch (Exception e) {
		    	// Handle it!
		    }
		    
		    importStatus.setText("Importing " + numLines + " records...");
		    
		    linesParsed = 0;
		    
		    progressBar.setMax(numLines);
		    importButton.setEnabled(false);
		    importButton.setText("Importing...");
		}
		
		@Override
		protected void onProgressUpdate(final Integer...values) {
			linesParsed++;
			if(progressBar == null || importPercent == null)
				return;
			
			
			progressBar.setProgress(linesParsed);
			//importedAmount.setText("" + linesParsed);
			
			// Only update percent every 20 lines
			if(linesParsed % 20 == 0)
				importPercent.setText(percentFormat.format((double) linesParsed / numLines));
		}
		
		
		@Override
		protected void onCancelled(Void result) {
			Log.i("ImportActivity", "Import Cancelled");
		}
		
		@Override
		protected void onPostExecute(final Void result) {
			if(importButton != null) importButton.setEnabled(true);
			if(importButton != null) importButton.setText("Import");
			if(progressBar != null) progressBar.setProgress(0);
			if(exception != null) {
				Log.e("ImportActivity", "Error when importing file", exception);
				App.okDialog(ImportActivity.this, "Error when importing data log file; please ensure the integrity of the file");
			}
			
			if(isCancelled()) {
				if(importStatus != null) importStatus.setText("Import cancelled.");
			} else {
				notifyImportComplete();
				if(importStatus != null) importStatus.setText("Import completed.");
			}
		}
		
		/**
		 * Imports everything!
		 */
		@Override
		protected Void doInBackground(Void... args) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				while((line = br.readLine()) != null) {
					if(isCancelled())
						break;
					
					importLine(line);
					publishProgress();
					// break;
				}
			} catch (Exception e) {
				exception = e;
			}
			return null;
		}
		
	}
	
}
