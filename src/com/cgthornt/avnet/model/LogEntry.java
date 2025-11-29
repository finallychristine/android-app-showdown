package com.cgthornt.avnet.model;

import org.kroz.activerecord.*;
import java.sql.Timestamp;

/**
 * Note to self: convert time zones to UTC!
 * @author Christine
 *
 */
public class LogEntry extends ActiveRecordBase {

	public String host;

	public String employee_id;

	public String destination;


	// :)
	public int excel, pdf, xml, csv;

	public Timestamp timestamp;

	public LogEntry() { }

	public LogEntry(Database db) {
		super(db);
	}

}
