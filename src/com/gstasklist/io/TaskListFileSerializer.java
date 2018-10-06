package com.gstasklist.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;

import com.gstasklist.entities.Task;
import com.gstasklist.entities.TaskServerAction;
import com.gstasklist.entities.TaskServerStatus;
import com.pras.Log;

public class TaskListFileSerializer {
	private String path = "tasklist-storage.txt";
	private final String fieldDelimiter = ";;;::;;;";
	private final String taskDelimiter = "::;::";
	
	public void saveTasks(Context context, ArrayList<Task> tasks){
		
		StringBuffer sb = new StringBuffer();
		for (Task task : tasks){
			
			sb.append(task.getRowIndex() + fieldDelimiter);
			sb.append(task.getServerStatus().getAction()+fieldDelimiter);
			sb.append(task.getServerStatus().isCommitted()+fieldDelimiter);
			sb.append(task.getDescription()+fieldDelimiter);
			sb.append(task.getDeadline()+fieldDelimiter);
			sb.append(task.getStatus() + fieldDelimiter);
			sb.append(task.getComment()+ fieldDelimiter);
			
			
			for (String tag : task.getTagNames()){
				sb.append(tag + fieldDelimiter);
			}
			sb.append(taskDelimiter);
		}
		
		try{
			FileOutputStream fos = context.getApplicationContext().openFileOutput(path, Context.MODE_PRIVATE);
			fos.write(sb.toString().getBytes());
			fos.close();
		}
		catch (Exception e) {
			Log.p("Saving tasklist", "Exception occured while storing the spreadsheet data");
		}
		
	}
	
	public void loadTasks(Context context, ArrayList<Task> tasks){
	
		try{
			
			StringBuffer sb = new StringBuffer();
			
			InputStream inputStream = context.openFileInput(path);
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			String line = null;
			while((line=bufferedReader.readLine())!=null){
				sb.append(line);
				sb.append("\\n");
			}
			
			extractTasksFromString(sb.toString(), tasks);
			
		}
		catch (Exception e) {
			Log.p("Loading tasklist", "Exception occured while storing the spreadsheet");
		}
	}
	
	private ArrayList<Task> extractTasksFromString(String fileContent, ArrayList<Task> tasks) {
		String[] taskStrings = fileContent.split(taskDelimiter);
		if (taskStrings == null){
			return new ArrayList<Task>();
		}
		
		tasks.clear();
		
		for (int i = 0; i< taskStrings.length; i++){
			
			String[] taskFields = taskStrings[i].split(fieldDelimiter);
			
			String rowIndex = taskFields[0];
			String actionString = taskFields[1];
			String isCommittedString = taskFields[2];
			
			boolean isCommitted = Boolean.parseBoolean(isCommittedString);
			
			TaskServerAction action = TaskServerAction.valueOf(actionString);
			
			Task task = new Task(
					taskFields[3], 
					taskFields[4], 
					taskFields[5], 
					taskFields.length > 6 ? taskFields[6] : "");
			TaskServerStatus serverStatus = new TaskServerStatus(isCommitted, action);
			task.setServerStatus(serverStatus);
			task.setRowIndex(rowIndex);
			
			ArrayList<String> tagNames = task.getTagNames();
			
			for (int j = 7; j < taskFields.length; j++){
				tagNames.add(taskFields[j]);
			}
			
			tasks.add(task);
		}
		
		return tasks;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
}
