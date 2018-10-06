package com.gstasklist.layout;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.gstasklist.R;
import com.gstasklist.R.id;
import com.gstasklist.entities.Tag;
import com.gstasklist.entities.Task;
import com.gstasklist.entities.TaskServerAction;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings.TextSize;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskArrayAdapter extends ArrayAdapter<Task>{
	
	private final Context context;
	private final int layoutResourceId;
	private final ArrayList<Task> tasks;
	private String relatedStatus;
	
	private String done_string = getContext().getString(R.string.done_section);
	
	public TaskArrayAdapter(Context context, int layoutResourceId, ArrayList<Task> tasks, String relateedStatus) {
		super(context, layoutResourceId, tasks);
		this.tasks = tasks;
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.relatedStatus = relateedStatus;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		TaskHolder holder = null;
		
		if (row == null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, null);
			holder = new TaskHolder();
			
			holder.description  = (TextView)row.findViewById(R.id.labelTaskDescription);
            holder.metaData = (TextView)row.findViewById(R.id.labelTaskDeadline);
            holder.commitImage = (ImageView) row.findViewById(R.id.commitImage);
            holder.commitAction = (TextView) row.findViewById(R.id.labelCommitAction);
            row.setTag(holder);
		}
		else{
			holder = (TaskHolder)row.getTag();
		}
		
		SharedPreferences prefs = getContext().getSharedPreferences("prefs", getContext().MODE_PRIVATE);
        Set<String> definedTagStrings = prefs.getStringSet("tags", new HashSet<String>());
        ArrayList<Tag> definedTags = Tag.parseTagSet(definedTagStrings);
        
		
        Task task = tasks.get(position);
		
        if (!task.getServerStatus().isCommitted()){
        	holder.commitImage.setVisibility(View.VISIBLE);
        	holder.commitAction.setVisibility(View.VISIBLE);
        }
        else {
        	holder.commitImage.setVisibility(View.GONE);
        	holder.commitAction.setVisibility(View.GONE);
        }
        
		holder.description.setText(task.getDescription());
        
		String deadlineText = getDeadlineText(task);
		String tagText = getTagText(task, definedTags);
		
		String metaData = deadlineText;
		
//		if (!task.getServerStatus().isCommitted()){
//			metaData += " | NOT SYNC'D!";
//		}
		
		if (tagText.length() > 0){
			metaData +=" | " + tagText;
		}
		
		String commitAction = "";
		if (task.getServerStatus().getAction() != TaskServerAction.NO_ACTION){
			commitAction = task.getServerStatus().getAction().toString().toLowerCase();
		}

		holder.metaData.setText(metaData);
		holder.commitAction.setText(commitAction);
		
		applyStyles(row, holder, task, definedTags);
		
        return row;
	
	}
	
	private void applyStyles(View row, TaskHolder holder, Task task, ArrayList<Tag> definedTags) {
		
		if (!isTaskDone(task)){
			if (task.isOverdue() || task.isDueToToday()){
				holder.metaData.setTypeface(Typeface.DEFAULT_BOLD);
				holder.description.setTypeface(Typeface.DEFAULT_BOLD);
			}
			else{
				holder.metaData.setTypeface(null);
				holder.description.setTypeface(null);
			}
		}
		else{
			holder.metaData.setTypeface(null);
			holder.description.setTypeface(null);
		}
		
		
		holder.commitAction.setTextColor(Color.LTGRAY);
		holder.commitAction.setTypeface(Typeface.DEFAULT, Typeface.ITALIC);

		for (String tagName : task.getTagNames()){
			for (Tag tag : definedTags){
				if (tag.getName().equals(tagName)){
					row.setBackgroundColor(tag.getBackgroundColor());
					holder.metaData.setTextColor(tag.getForegroundColor());
					holder.description.setTextColor(tag.getForegroundColor());
					
					switch (tag.getBackgroundColor()){	
					case Color.LTGRAY:
					case Color.WHITE: { holder.commitAction.setTextColor(Color.GRAY); break; }
					default: break;
					}
					
					// we apply the first style we run into and return
					return;
				}
			}
		}
		
		
		row.setBackgroundColor(Color.BLACK);
		holder.metaData.setTextColor(Color.WHITE);
		holder.description.setTextColor(Color.LTGRAY);

		
	}

	private String getTagText(Task task, ArrayList<Tag> existingTags) {
		StringBuffer sb = new StringBuffer();
		
		for (String tagName : task.getTagNames()){
			for (Tag existingTag : existingTags){
				if (existingTag.getName().equals(tagName)){
					sb.append(tagName+", ");
					break;
				}
			}
		
		}
		
		String tagText = sb.toString();
		if (tagText.endsWith(", ")){
			tagText = tagText.substring(0, tagText.lastIndexOf(','));
		}
		
		return tagText;
	}

	private String getDeadlineText(Task task) {
		if (isTaskDone(task) || task.hasInfiniteDeadline()){
			return task.getDeadline();
		}
		
		if(task.isOverdue()){
			return "[Overdue] "+task.getDeadline();
		}
		else if (task.isDueToToday()){
			return "Today";
		}
		else if (task.isDueToTomorrow()){
			return "Tomorrow";
		}
		else if (task.isDueToSoonish()){
			return task.getDeadlineDayOfWeek() + ", " + task.getDeadline();
		}
		else {
			return task.getDeadline();
		}
	}

	private boolean isTaskDone(Task task) {
		return task.getStatus().equalsIgnoreCase(done_string);
	}

	static class TaskHolder{
		TextView description;
		TextView metaData;
		ImageView commitImage;
		TextView commitAction;
	}

 
}

