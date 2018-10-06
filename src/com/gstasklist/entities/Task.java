package com.gstasklist.entities;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.gstasklist.R;
import com.gstasklist.utils.StringUtils;

import android.content.Context;

public class Task implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8410317753141601251L;
	private String description;
	private String deadline;
	private String status;
	private String comment = "";
	
	private String rowIndex = "unknownIndex";
	
	private TaskServerStatus serverStatus = new TaskServerStatus();
	
	// an array for backwards compatibility
	private ArrayList<String> tagNames = new ArrayList<String>();
	
	private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private Date dueDate;
	
	public Task(String description, String deadline, String status){
		this.description = description;
		this.deadline = deadline;
		this.status = status;
		
		try {
			dueDate = dateFormat.parse(deadline);
		} catch (ParseException e) {
			dueDate = null;
		}
	}
	
	public Task(String description, String deadline, String status, String comment){
		this(description, deadline, status);
		this.comment = comment;
	}
	
	public Task(String description, String deadline, String status, String comment, ArrayList<String> tagNames){
		this(description, deadline, status, comment);
		this.tagNames = tagNames;
	}
	
	public Task(String description, String deadline, String status, String comment, String tagName){
		this(description, deadline, status, comment);
		this.tagNames.add(tagName);
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setDescriptionAndTags(String description) {
		extractTagsAndSetDescription(description);
	}
	public String getDeadline() {
		return deadline;
	}
	public void setDeadline(String deadline) {
		this.deadline = deadline;
		try {
			dueDate = dateFormat.parse(deadline);
		} catch (ParseException e) {
			dueDate = null;
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ArrayList<String> getTagNames() {
		return tagNames;
	}

	public void setTagNames(ArrayList<String> tagNames) {
		this.tagNames = tagNames;
	}

	public boolean isDueToToday() {
		
		if (hasInfiniteDeadline()) {
			return false;
		}
		
		try{
			Date todayDate = getTodayDate();
			return dueDate.compareTo(todayDate) == 0;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isDueToTomorrow(){
		
		if (hasInfiniteDeadline()) {
			return false;
		}
		
		try{
			Date tomorrowDate = getDateInANumberOfDays(1);
			return dueDate.compareTo(tomorrowDate) == 0;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean isDueToSoonish() {
		
		if (hasInfiniteDeadline()) {
			return false;
		}
		
		try{
			Date targetDate = getDateInANumberOfDays(10);
			return dueDate.compareTo(targetDate) != 1;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	private Date getTodayDate(){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		return cal.getTime();
	}
	
	private Date getDateInANumberOfDays(int numberOfDays){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, numberOfDays);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		return cal.getTime();
	}
	
	public boolean isOverdue(){
		
		if (hasInfiniteDeadline()) {
			return false;
		}
		
		try{
			Date dueDate = dateFormat.parse(deadline);
			Date todayDate = getTodayDate();
			
			return dueDate.before(todayDate);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
		
	}
	
	private void extractTagsAndSetDescription(String originalDescription){
		tagNames.clear();
		String description = originalDescription;
		while (description.contains("[") && description.contains("]") && description.indexOf('[') < description.indexOf(']')){
			String tagName = StringUtils.extractValueBetweenTags(description, "[", "]");
			if (!tagNames.contains(tagName)){
				tagNames.add(tagName);
			}
			description = StringUtils.substringUntilEnd(description, "]");
		}
		
		//this.description = originalDescription;
		this.description = description.trim();
	}

	public TaskServerStatus getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(TaskServerStatus serverStatus) {
		this.serverStatus = serverStatus;
	}

	public String getRowIndex() {
		return rowIndex;
	}

	public void setRowIndex(String rowIndex) {
		this.rowIndex = rowIndex;
	}

	public String getDeadlineDayOfWeek() {
		try{
			Date dueDate = dateFormat.parse(deadline);
			return new SimpleDateFormat("EEE").format(dueDate);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return "ERR";
	}

	public boolean hasInfiniteDeadline() {
		return deadline.equals("?");
	}
	
}
