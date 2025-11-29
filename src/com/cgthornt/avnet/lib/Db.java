package com.cgthornt.avnet.lib;

import java.util.HashMap;

import com.cgthornt.avnet.R;
import android.database.Cursor;

/**
 * SQL Queries for report info
 * @author Christine Thornton
 *
 */
public class Db {

	// Doc Types
	public static final int EXCEL = 0, PDF = 1, CSV = 2, XML = 3;
	public static final int[]    	DOC_TYPES = {EXCEL, PDF, CSV, XML};
	public static final String[]	DOC_TYPES_STR = {"Excel", "PDF", "CSV", "XML"};
	public static final int[]		DOC_TYPES_IMG = {R.drawable.spreadsheet, R.drawable.pdf, R.drawable.csv, R.drawable.xml };

	// Dest types
	public static final int EMAIL = 4, REQUEST_SYSTEM = 5, BROWSER = 6;
	public static final int[]  		DEST_TYPES = {EMAIL, REQUEST_SYSTEM, BROWSER};
	public static final String[] 	DEST_TYPES_STR = {"Email", "Request System", "Browser"};
	public static final String[]    DEST_TYPES_SYSTEM_STR = {"EMAIL", "REQUEST_SYSTEM", "BROWSER"};
	public static final int[]		DEST_TYPES_IMG = {R.drawable.email, R.drawable.system, R.drawable.internet };


	/**
	 * Gets the number of rows in the table
	 */
	public static int tableCount() {
		App.openDatabase();
		Cursor c = App.db.rawQuery("SELECT COUNT(*) FROM `LOG_ENTRY` LIMIT 1");
		c.moveToFirst();
		int count = c.getInt(0);
		c.close();
		return count;
	}


	/**
	 * Gets the hours in count and percent
	 * Format:
	 * 	[
	 * 		[ hour_of_day, count, percent ],
	 * 		[ hour_of_day, count, percent ],
	 * 		...
	 *  ]
	 */
	public static double[][] getHourCountsAndPercent() {
		App.openDatabase();
		// The requirements specify that the timestamps are in milliseconds, so we need to make sure to only select dates with
		// times in the range of 8 AM inclusive (28800000 milliseconds) and 12 PM exclusive (43199999 milliseconds)
		String datesToParseQuery =
				"SELECT " +
				"	DISTINCT strftime('%Y-%m-%d', `timestamp`) AS `date` " +
				"FROM `LOG_ENTRY` WHERE " +
				"	CAST ((" +
							// Converts the stored date into the number of seconds since the beginning of the day
				"			(strftime('%s', `timestamp`) - strftime('%s', datetime(`timestamp`, 'start of day')) " +

							// Subtracts the number of seconds in the timestamp and adds back the seconds and milliseconds
							// as a floating point number.
				"			- strftime('%S', `timestamp`) + strftime('%f', `timestamp`)" +

						// Multiply by 1000 to convert only into milliseconds
				"		) * 1000)" +

					// Cast back into an integer instead of a float
				"	AS INTEGER) " +

				// Only select dates between 8 AM and 12PM, now with millisecond perscision :D
				"BETWEEN 28800000 AND 43199999";


		// Now we might as well just make it a nested query and we can get all of our results in a single result.
		// Our query will be nicely formatted and it will look like:
		//	 hour | count
		//	 hour | count
		//	 ...
		// Yeah for simplicity!!!

		String query =
				"SELECT" +
				"	CAST(strftime('%H', `timestamp`) AS INTEGER) AS `hour`, " +
				"	COUNT(*) as `cnt` " +
				"FROM `LOG_ENTRY` " +
				"WHERE " +
				"	`hour` BETWEEN 8 AND 17 " +
				" AND " +
				"	strftime('%Y-%m-%d', `timestamp`) IN (" + datesToParseQuery + ") " +
				"GROUP BY `hour`";

		Cursor c = App.db.rawQuery(query);
		c.moveToFirst();

		// Make the result!
		double result[][] = new double[c.getCount()][3];
		int i = 0;
		int totalCount = 0;
		while(!c.isAfterLast()) {
			result[i] = new double[] { c.getInt(0), c.getInt(1), 0 };
			totalCount += result[i][1];
			i++;
			c.moveToNext();
		}
		c.close();

		// Now get the percentages
		for(int j = 0; j < result.length; j++)
			result[j][2] = result[j][1] / totalCount;

		return result;
	}



	/**
	 * Gets counts per destination type
	 * In format of
	 * 	[ count, count, ... ] where each coresponds to [ EMAIL, REQUEST_SYSTEM, ... ]
	 * QUERY:
	 * SELECT destination, count(*) FROM `LOG_ENTRY` GROUP BY `destination`
	 */
	public static int[] getDestTypeCounts() {
		App.openDatabase();
		int result[] = new int [DEST_TYPES.length];
		Cursor c = App.db.rawQuery("SELECT destination, count(*) FROM `LOG_ENTRY` GROUP BY `destination`");
		c.moveToFirst();
		while(!c.isAfterLast()) {
			String type = c.getString(0);
			int count = c.getInt(1);
			for(int i = 0; i < DEST_TYPES_SYSTEM_STR.length; i++) {
				if(DEST_TYPES_SYSTEM_STR[i].equals(type))
					result[i] = count;
			}
			c.moveToNext();
		}
		return result;
	}

	/**
	 * Get host resuts in map format, ordered by percent
	 * 	[ [ host, count, percent ], [ ... ], ... ]
	 * @return
	 */
	public static Object[][] getHostPercents() {
		App.openDatabase();
		int count = tableCount();
		Cursor c = App.db.rawQuery("SELECT `host`, COUNT(*) as `cnt` FROM `LOG_ENTRY` GROUP BY `host` ORDER BY `cnt` DESC");
		Object[][] result = new Object[c.getCount()][3];
		c.moveToFirst();
		int i = 0;
		while(!c.isAfterLast()) {
			result[i][0] = c.getString(0);
			result[i][1] = c.getInt(1);
			result[i][2] = new Double(c.getInt(1) / (double) count);
			c.moveToNext();
			i++;
		}
		c.close();
		return result;
	}


	/**
	 * Get dest types in map format, ordered by percent
	 * @return
	 */
	public static HashMap<Integer,Double> getDestTypePercentsMapped() {
		App.openDatabase();
		HashMap<Integer,Double> map = new HashMap<Integer,Double>();
		int count = tableCount();
		Cursor c = App.db.rawQuery("SELECT `destination`, COUNT(*) as `cnt` FROM `LOG_ENTRY` GROUP BY `destination` ORDER BY `cnt` DESC");
		c.moveToFirst();
		while(!c.isAfterLast()) {
			String type = c.getString(0);
			int type_int = -1;
			int cnt = c.getInt(1);
			for(int i = 0; i < DEST_TYPES_SYSTEM_STR.length; i++) {
				if(DEST_TYPES_SYSTEM_STR[i].equals(type))
					type_int = i;
			}
			map.put(type_int, (double) cnt / count);
			c.moveToNext();
		}
		c.close();
		return map;
	}

	/**
	 * Gets percents of dest type in format of:
	 * 	[ percent, percent, ... ] where each is [ EMAIL, REQUEST, SYSTEM, ... ]
	 */
	public static double[] getDestTypePercents() {
		int count = tableCount();
		int counts[] = getDestTypeCounts();
		double result[] = new double[counts.length];
		for(int i = 0; i < counts.length; i++)
			result[i] = (double) counts[i] / count;
		return result;
	}

	/**
	 * Gets doc types by type. See DOC_TYPES constant for order. In format of:
	 * 	[ num, num, ... ] where each would coresponds to [ EXCEL, PDF, ... ].
	 * Note that these are the number of records where it was EXCEL, PDF, etc, not percent!
	 * Use getDocTypePercents() to get percents!
	 */
	public static int[] getDocTypeCounts() {
		App.openDatabase();
		int[] results = new int[DOC_TYPES.length];
		for(int i = 0; i < DOC_TYPES.length; i++) {
			Cursor c = App.db.rawQuery("SELECT COUNT(*) FROM `LOG_ENTRY` WHERE `" + DOC_TYPES_STR[i] + "` = 1");
			c.moveToFirst();
			results[i] = c.getInt(0);
			c.close();
		}
		return results;
	}


	/**
	 * Gets doc type percents, formatted per requirements. In format of
	 * 	[ percent, percent, ... ] where each coresponds to [ EXCEL, PDF, ... ]
	 */
	public static double[] getDocTypePercents() {

		int[] counts = getDocTypeCounts();
		double[] result = new double[counts.length];
		int type_counts = 0;

		// Get total request counts
		for(int i = 0; i < counts.length; i++)
			type_counts += counts[i];

		// Now get percentages
		for(int i = 0; i < counts.length; i++)
			result[i] =  (double) counts[i] / type_counts;
		return result;
	}


	/**
	 * Gets the top ten employees in the system
	 * FORMAT:
	 * 	[
	 * 	  [employee, count],
	 *    [employee, count],
	 *    ...
	 *  ]
	 * QUERY:
	 * SELECT `employeeid`, COUNT(*) AS `cnt` FROM `LOG_ENTRY` GROUP BY `employeeid` ORDER BY `cnt` DESC LIMIT 10;
	 */
	public static int[][] getTopTenEmployees() {
		App.openDatabase();

		int[][] results = new int[10][2];
		Cursor c = App.db.rawQuery("SELECT `employeeid`, COUNT(*) AS `cnt` FROM `LOG_ENTRY` GROUP BY `employeeid` ORDER BY `cnt` DESC LIMIT 10");
		c.moveToFirst();
		int i = 0;
		while(!c.isAfterLast()) {
			results[i] = new int[]{c.getInt(0), c.getInt(1)};
			i++;
			c.moveToNext();
		}
		c.close();
		return results;
	}



}
