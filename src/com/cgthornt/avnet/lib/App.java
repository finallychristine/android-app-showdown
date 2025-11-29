package com.cgthornt.avnet.lib;

import org.kroz.activerecord.*;

import com.cgthornt.avnet.model.LogEntry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

public class App {

	
	public static ActiveRecordBase cnx;
	public static Database db;
	
	public static final int DB_VERSION = 2;
	
	public static void start(Context context) throws Exception {
		String dbname = "logs.db";
		DatabaseBuilder builder = new DatabaseBuilder(dbname);
		builder.addClass(LogEntry.class);
		Database.setBuilder(builder);
		
		if(cnx == null)
			cnx = ActiveRecordBase.open(context, dbname, DB_VERSION);
		
		// Get the database
		if(db == null)
			db = Database.createInstance(context, dbname, DB_VERSION);
		openDatabase();
	}
	
	
	
	public static void okDialog(Context context, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
		       .setCancelable(false)
		       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.dismiss();
		           }
		       });

		builder.create().show();
	}
	
	public static void confirmDialog(Context context, String message,  DialogInterface.OnClickListener positive) {
		confirmDialog(context, message, positive, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
	}
	
	public static void confirmDialog(Context context, String message, DialogInterface.OnClickListener positive, 
			DialogInterface.OnClickListener negative) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message)
	       .setCancelable(false)
	       .setPositiveButton("OK", positive)
	       .setNegativeButton("Cancel", negative);
		builder.create().show();
		
	}
	
	public static void closeDatabase() {
		if(db.isOpen())
			db.close();
	}
	
	public static void openDatabase() {
		if(!db.isOpen())
			db.open();
	}
	

}
