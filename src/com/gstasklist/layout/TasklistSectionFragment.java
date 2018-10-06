package com.gstasklist.layout;

import java.lang.reflect.Array;
import java.util.ArrayList;

import com.gstasklist.EditTask;
import com.gstasklist.R;
import com.gstasklist.Tasklist;
import com.gstasklist.TasklistConfiguration;
import com.gstasklist.R.layout;
import com.gstasklist.entities.Task;
import com.gstasklist.entities.TaskServerAction;
import com.gstasklist.entities.TaskServerStatus;
import com.gstasklist.utils.TaskDeadlineComparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class TasklistSectionFragment extends Fragment {
	
	
	private ArrayList<Task> tasks;
	private TaskArrayAdapter taskArrayAdapter;
	private ArrayList<Task> filteredTasks = new ArrayList<Task>();
	private String relatedStatus;
	private boolean reversedRows;
	private Handler mHandler;
	private int fragmentId;
	private int fragmentCount;
	private Activity activity;
	private ListView listView;

	private int itemClickedPosition = -1;

	private final int UPDATE_CODE = 102;
	
	private String taskComment;
	private String taskDescription;

	public void setFragmentArguments(Activity activity, ArrayList<Task> tasks, String relatedStatus, Handler mHandler, int fragmentId) {
		this.tasks = tasks;
		this.mHandler = mHandler;
		this.relatedStatus = relatedStatus;
		this.reversedRows = relatedStatus.equalsIgnoreCase(activity.getString(R.string.done_section));
		
		recreateFilteredTasks();
		this.activity = activity;
		this.fragmentId = fragmentId;
		
    }
	
	public TasklistSectionFragment(){
		
	}

    private void recreateFilteredTasks() {
		filteredTasks.clear();
		for (Task task : tasks) {
			if (relatedStatus.equalsIgnoreCase(task.getStatus())){
				insertTaskIntoTheFilteredList(task);
			}
			
		}
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	super.onCreateView(inflater, container, savedInstanceState);
    	listView = new ListView(getActivity());
    	
		taskArrayAdapter = new TaskArrayAdapter(
				getActivity(), 
		  		R.layout.task_entry, 
		  		filteredTasks, 
		  		relatedStatus);
		
		  
        listView.setAdapter(taskArrayAdapter);
        
		listView.setTextFilterEnabled(true);
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> listView, View item,
					int position, long id) {
				
				itemClickedPosition = position;
				getActivity().openContextMenu(listView);
				return true;
			}
		});
		
		
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listView, View item, int position,
					long id) {
				itemClickedPosition = position;
				getActivity().openContextMenu(listView);
			}
		});
		
		
		
		return listView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedState){
    	super.onActivityCreated(savedState);
    	registerForContextMenu(listView);
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater menuInflater = getActivity().getMenuInflater();
    	menuInflater.inflate(R.menu.context_task, menu);
    	Task task = filteredTasks.get(itemClickedPosition);
    	menu.setHeaderTitle("Task: "+task.getDescription());
    	
    	if (fragmentId == 0) {
    		menu.findItem(R.id.moveToPreviousStatus).setEnabled(false);
    	}
    	else  if (fragmentId == 3){
    		menu.findItem(R.id.moveToNextStatus).setEnabled(false);
    		menu.findItem(R.id.markAsDone).setEnabled(false);
    	}
    	
    	if (task.getComment().length() == 0){
    		menu.findItem(R.id.viewComments).setEnabled(false);
    	}
    }
    
    
    private String getPrevousStatus(int fragmentId){
    	switch (fragmentId > 0 ? fragmentId-1 : 0) {
	        case 0: return activity.getString(R.string.scheduled_section).toUpperCase();
	        case 1: return activity.getString(R.string.in_progress_section).toUpperCase();
	        case 2: return activity.getString(R.string.ready_section).toUpperCase();
	        case 3: return activity.getString(R.string.done_section).toUpperCase();
	    }
	    return null;
    }
    
    private String getNextStatus(int fragmentId){
    	switch (fragmentId < 3 ? fragmentId+1 : 3) {
	        case 0: return activity.getString(R.string.scheduled_section).toUpperCase();
	        case 1: return activity.getString(R.string.in_progress_section).toUpperCase();
	        case 2: return activity.getString(R.string.ready_section).toUpperCase();
	        case 3: return activity.getString(R.string.done_section).toUpperCase();
	    }
	    return null;
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	super.onContextItemSelected(item);
    	
    	
    	switch (item.getItemId()) {
	    	case R.id.moveToPreviousStatus:
    		case R.id.moveToNextStatus:
	    	{
	    		
	    		String futureStatus = item.getItemId()==R.id.moveToNextStatus 
	    				? getNextStatus(fragmentId) 
	    				: getPrevousStatus(fragmentId);
	    		Task task = filteredTasks.get(itemClickedPosition);
	    		task.setStatus(futureStatus.toLowerCase());
	    		
	    		TaskServerStatus serverStatus = new TaskServerStatus(false, TaskServerAction.UPDATE);
	    		task.setServerStatus(serverStatus);
	    		
	    		Message msg = new Message();
				msg.obj = "updateView";
				mHandler.sendMessage(msg);
				
				return true;
	    	}
	    	
	    	case R.id.markAsDone:
	    	{
	    		Task task = filteredTasks.get(itemClickedPosition);
	    		task.setStatus(activity.getString(R.string.done_section).toLowerCase());
	    		TaskServerStatus serverStatus = new TaskServerStatus(false, TaskServerAction.UPDATE);
	    		task.setServerStatus(serverStatus);
	    		
	    		Message msg = new Message();
				msg.obj = "updateView";
				mHandler.sendMessage(msg);
				
				return true;
	    	}
	    	
	    	case R.id.edit:
	    	{
	    		Intent intent = new Intent(getActivity(), EditTask.class);
	    		intent.putExtra("task", filteredTasks.get(itemClickedPosition));
	    		startActivityForResult(intent, UPDATE_CODE);
	    		return true;
	    	}
	    	
	    	case R.id.delete:
	    	{
	    		Task task = filteredTasks.get(itemClickedPosition);
	    		
	    		// if we added it only localy, but didn't commit to the google spreadsheet
	    		if (!task.getServerStatus().isCommitted() && task.getServerStatus().getAction() == TaskServerAction.ADD)
	    		{
	    			// remove it from the global task list
	    			tasks.remove(task);
	    			
	    			// later with updating the view, the filtered tasks for this status will also
	    			// kick out this task
	    		}
	    		
	    		TaskServerStatus serverStatus = new TaskServerStatus(false, TaskServerAction.DELETE);
	    		task.setServerStatus(serverStatus);
				
	    		Message msg = new Message();
				msg.obj = "updateView";
				mHandler.sendMessage(msg);
				
	    		return true;
	    	}
	    	
	    	case R.id.viewComments:
	    	{
	        	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        	Task task = filteredTasks.get(itemClickedPosition);
	    		
	        	taskComment = task.getComment();
	        	taskDescription = task.getDescription();
	        	
	        	builder.setTitle("Comments for '"+taskDescription+"'");
	        	
	        	if (taskComment != null && taskComment.length()>0){
	        		builder.setMessage(taskComment);
		        	builder.setNeutralButton("Copy comment", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE); 
							android.content.ClipData clip = android.content.ClipData.newPlainText(taskDescription, taskComment);
							clipboard.setPrimaryClip(clip);					
						}
					});
	        	}
	        	else{
	        		builder.setMessage("No comments are associated with this task");
	        	}
	        	
	        	builder.show();

	    		
	    		return true;
	    	}
	    	 
	    	default:
	    	return super.onContextItemSelected(item);
    	}
    }
    
	private void insertTaskIntoTheFilteredList(Task task){
		TaskDeadlineComparator comparator = new TaskDeadlineComparator();
		
		int i = filteredTasks.size()-1;
		for (; i>=0; i--){
			if (comparator.compare(filteredTasks.get(i), task) <= 0){
				break;
			}	
		}
		
		int index = reversedRows ? filteredTasks.size() - (i+1) : i+1;
		filteredTasks.add(index, task);
	}

	private void refresh() {
		recreateFilteredTasks();
		if (taskArrayAdapter != null){
			taskArrayAdapter.notifyDataSetChanged();
		}
	}
	
	public void refresh(ArrayList<Task> tasks){
		this.tasks = tasks;
		refresh();
	}
    
}