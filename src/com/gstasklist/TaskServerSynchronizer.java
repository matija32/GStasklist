package com.gstasklist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.gstasklist.auth.AndroidAuthenticator;
import com.gstasklist.entities.Task;
import com.gstasklist.entities.TaskServerAction;
import com.pras.Log;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetRow;
import com.pras.auth.Authenticator;
import com.pras.auth.BasicAuthenticatorImpl;
import com.pras.conn.HttpConHandler;
import com.pras.conn.Response;
import com.pras.sp.Entry;
import com.pras.sp.Feed;
import com.pras.sp.ParseFeed;

public class TaskServerSynchronizer {
	
	private String[] currentColumnNames;
	private String workSheetURL;
	private String spreadsheetKey;
	private String workSheetStringID;
	private String authToken;

	private final static String DOCUMENT_LIST_API_SERVICE_NAME = "writely";
	private final static String SPREADSHEET_API_SERVICE_NAME = "wise";
	
	private final String[] currentColumnNameIds = new String[]{
			"currentDeadlineColumnName",
			"currentTaskColumnName",
			"currentStatusColumnName",
			"currentCommentsColumnName"
		};
	
	public TaskServerSynchronizer(Activity activity){
		
		SharedPreferences prefs = activity.getSharedPreferences("prefs", activity.MODE_PRIVATE);

		currentColumnNames = new String[]{
				prefs.getString(currentColumnNameIds[0], "1"),
				prefs.getString(currentColumnNameIds[1], "2"),
				prefs.getString(currentColumnNameIds[2], "3"),
				prefs.getString(currentColumnNameIds[3], "4")
		};
		
		workSheetURL = prefs.getString("worksheetUrl", "");
		spreadsheetKey = prefs.getString("spreadsheetKey", "");
		workSheetStringID = prefs.getString("worksheetStringId", "");
		
		//authToken = prefs.getString("authToken", "none");
		//if (authToken.equals("none")){
		
		 
			String email = prefs.getString("spreadsheetAccessEmail", "");
			String password = prefs.getString("spreadsheetAccessPassword", "");
			
			Authenticator authenticator = new BasicAuthenticatorImpl(email, password);
			
			authToken = authenticator.getAuthToken(SPREADSHEET_API_SERVICE_NAME);
			prefs.edit().putString("authToken", authToken).commit();
		//}
	}
	
	private WorkSheetRow addListRowInternal(HashMap<String, String> records){
		/*
		 * It will send request to WorkSheet URL:
		 * https://spreadsheets.google.com/feeds/list/key/worksheetId/private/full
		 * Unlike Tables, there can be only one List Feed associated with a given WorkSheet
		 */
		
		StringBuffer listRecordXML = new StringBuffer();
		listRecordXML.append("<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:gsx=\"http://schemas.google.com/spreadsheets/2006/extended\">");
		
		Iterator<String> ks = records.keySet().iterator();
		while(ks.hasNext()){
			String colName = ks.next();
			String value = records.get(colName);
			listRecordXML.append(" <gsx:"+ colName +">"+ value +"</gsx:"+ colName +">");
		}
		listRecordXML.append("</entry>");
		
		// Do server transaction
		// Add Headers
		HashMap<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put(HttpConHandler.AUTHORIZATION_HTTP_HEADER, "GoogleLogin auth="+ authToken);
		httpHeaders.put(HttpConHandler.GDATA_VERSION_HTTP_HEADER, "3.0");
		httpHeaders.put(HttpConHandler.CONTENT_TYPE_HTTP_HEADER, "application/atom+xml");
		
		HttpConHandler http = new HttpConHandler();
		Response res = http.doConnect(workSheetURL, HttpConHandler.HTTP_POST, httpHeaders, listRecordXML.toString());
		
		// Add the into local Record cache
		// Parse response and create new Row instance
		if(res.isError()){
			//Log.p(TAG, "Error in updating List Row...");
			return null;
		}
		
		String xmlOut = res.getOutput();
		
		// XML Parsing
		ParseFeed pf = new ParseFeed();
		Feed f = pf.parse(xmlOut.getBytes());
		ArrayList<Entry> entries = f.getEntries();
		
		if(entries == null || entries.size() == 0){
			//Log.p(TAG, "Error in parsing...");
			return null;
		}
		
		Entry e = entries.get(0);
		WorkSheetRow row = new WorkSheetRow();
		row.setId(e.getId());
		row.setCells(e.getCells());
	
		return row;
	}
	
	public void addTask(Task task){
		HashMap<String, String> records = makeCellMap(task);
		WorkSheetRow row = addListRowInternal(records);
		
		if (row != null){
			task.setRowIndex(row.getRowIndex());
			markAsCommitted(task);
		}
	}
	
	private void markAsCommitted(Task task) {
		task.getServerStatus().setCommitted(true);
	}

	private WorkSheetRow updateListRowInternal(String rowIndex, HashMap<String, String> records)
	{
		
		// Sample PUT URL- https://spreadsheets.google.com/feeds/list/tmG5DprMeR-l2j91JQBB1TQ/odp/private/full/cokwr
		String listRowURL = "https://spreadsheets.google.com/feeds/list/"+ spreadsheetKey +"/"+ workSheetStringID + "/private/full/"+ rowIndex;
		
		StringBuffer rowUpdateXML = new StringBuffer("<entry xmlns=\"http://www.w3.org/2005/Atom\" xmlns:gsx=\"http://schemas.google.com/spreadsheets/2006/extended\">");
		
		Iterator<String> ks = records.keySet().iterator();
		while(ks.hasNext()){
			String colName = ks.next();
			String value = records.get(colName);
			rowUpdateXML.append(" <gsx:"+ colName +">"+ value +"</gsx:"+ colName +">");
		}
		rowUpdateXML.append("</entry>");
		
		// Do server transaction- PUT
		// Add Headers
		HashMap<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put(HttpConHandler.AUTHORIZATION_HTTP_HEADER, "GoogleLogin auth="+ authToken);
		httpHeaders.put(HttpConHandler.GDATA_VERSION_HTTP_HEADER, "3.0");
		httpHeaders.put(HttpConHandler.CONTENT_TYPE_HTTP_HEADER, "application/atom+xml");
		//If you want to delete the row regardless of whether someone else has updated it since you retrieved it
		httpHeaders.put("If-Match", "*");
		
		HttpConHandler http = new HttpConHandler();
		Response res = http.doConnect(listRowURL, HttpConHandler.HTTP_PUT, httpHeaders, rowUpdateXML.toString());
		
		// Parse response and create new Row instance
		if(res.isError()){
			return null;
		}
		
		String xmlOut = res.getOutput();
		
		// XML Parsing
		ParseFeed pf = new ParseFeed();
		Feed f = pf.parse(xmlOut.getBytes());
		ArrayList<Entry> entries = f.getEntries();
		
		if(entries == null || entries.size() == 0){
			return null;
		}
		
		Entry e = entries.get(0);
		WorkSheetRow row = new WorkSheetRow();
		row.setId(e.getId());
		row.setCells(e.getCells());

		return row;
	}
	
	public void updateTask(Task task){
		WorkSheetRow row = updateListRowInternal(task.getRowIndex(), makeCellMap(task));
		if (row != null){
			task.setRowIndex(row.getRowIndex());
			markAsCommitted(task);
		}
	}
	
	private void deleteListRowInternal(String rowIndex)
	{
		
		// Sample DELETE URL- https://spreadsheets.google.com/feeds/list/tmG5DprMeR-l2j91JQBB1TQ/odp/private/full/cokwr
		String listRowURL = "https://spreadsheets.google.com/feeds/list/"+ spreadsheetKey +"/"+ workSheetStringID + "/private/full/"+ rowIndex;
		
		// Do server transaction
		// Add Headers
		HashMap<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put(HttpConHandler.AUTHORIZATION_HTTP_HEADER, "GoogleLogin auth="+ authToken);
		httpHeaders.put(HttpConHandler.GDATA_VERSION_HTTP_HEADER, "3.0");
		//If you want to delete the row regardless of whether someone else has updated it since you retrieved it
		httpHeaders.put("If-Match", "*");
		
		HttpConHandler http = new HttpConHandler();
		http.doConnect(listRowURL, HttpConHandler.HTTP_DELETE, httpHeaders, null);
		
	}
	
	public void deleteTask(Task task){
		deleteListRowInternal(task.getRowIndex());
		markAsCommitted(task);
	}
	
	private HashMap<String,String> makeCellMap(Task task){
		HashMap<String, String> records = new HashMap<String, String>();
		
		String description = "";
		for (String tagName : task.getTagNames()){
			if (tagName.length()>0)
			{
				description += "["+tagName+"] ";
			}
		}
		
		description += task.getDescription();
		
		records.put(currentColumnNames[0].toLowerCase(), task.getDeadline());
		records.put(currentColumnNames[1].toLowerCase(), description);
		records.put(currentColumnNames[2].toLowerCase(), task.getStatus().toLowerCase());
		records.put(currentColumnNames[3].toLowerCase(), task.getComment());
		return records;
	}

}
