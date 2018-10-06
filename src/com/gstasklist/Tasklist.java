package com.gstasklist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.SimpleTimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.gstasklist.auth.AndroidAuthenticator;
import com.gstasklist.entities.Task;
import com.gstasklist.entities.TaskServerAction;
import com.gstasklist.entities.TaskServerStatus;
import com.gstasklist.io.TaskListFileSerializer;
import com.gstasklist.layout.TasklistSectionFragment;
import com.gstasklist.utils.TaskDeadlineComparator;
import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;
import com.pras.auth.BasicAuthenticatorImpl;

public class Tasklist extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

	private int spreadsheetId;
	private int worksheetId;

	public ArrayList<Task> tasks = new ArrayList<Task>();

	private String worksheetName;

	private String spreadsheetName;
	
	private TaskListFileSerializer tasklistSerializer = new TaskListFileSerializer();

	private SharedPreferences prefs;
	
	private final int ADD_CODE = 101;
	private final int UPDATE_CODE = 102;

	
	private WorkSheet currentWorksheet;

	public Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			String text = (String) msg.obj;
			if (text.equals("updateView")){
				notifyNewTaskSet();
			}
		}
	};
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
        spreadsheetName = prefs.getString("spreadsheetName", "Name unavailable");
		worksheetName = prefs.getString("worksheetName", "Name unavailable");
		worksheetId = prefs.getInt("worksheetId", -1);
		spreadsheetId = prefs.getInt("spreadsheetId", -1);
		
		if(worksheetId == -1 || spreadsheetId == -1){
			configure(null);
		}
		

		setContentView(R.layout.activity_tasklist);
        
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
		
        tasklistSerializer.loadTasks(this, tasks);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tasklist, menu);
        return true;
    }
    
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
        	ArrayList<Task> tasks = Tasklist.this.tasks;
			
			String status = (String) getPageTitle(i);
			
			TasklistSectionFragment fragment = new TasklistSectionFragment();
			fragment.setFragmentArguments(Tasklist.this, tasks, status, mHandler, i);
			
            return fragment;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.scheduled_section).toUpperCase();
                case 1: return getString(R.string.in_progress_section).toUpperCase();
                case 2: return getString(R.string.ready_section).toUpperCase();
                case 3: return getString(R.string.done_section).toUpperCase();
            }
            return null;
        }
        
        @Override
        public void notifyDataSetChanged(){
        	super.notifyDataSetChanged();
        	
        	for (int i = 0; i<getCount(); i++){
        		TasklistSectionFragment fragment = (TasklistSectionFragment)        				
        				Tasklist.this.getSupportFragmentManager().findFragmentByTag(
        	                       "android:switcher:"+R.id.pager+":"+i);
        		if (fragment!=null) fragment.refresh(tasks);
        	}
        	
        	super.notifyDataSetChanged();
        }
    }
    
	private class LoadSpreadsheetDataCommand extends AsyncTask{

		
		 String[] currentColumnNameIds = new String[]{
				"currentDeadlineColumnName",
				"currentTaskColumnName",
				"currentStatusColumnName",
				"currentCommentsColumnName"
			};
		
		Dialog dialog;
		//private String[] cols;
		private ArrayList<WorkSheetRow> rows;

		private SpreadSheet sp;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(Tasklist.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(Tasklist.this.getApplicationContext());
			tv.setText("Retrieving the tasks.");
			dialog.setContentView(tv);
			dialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			// Read Spread Sheet list from the server.
			
			SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE); 
			String email = prefs.getString("spreadsheetAccessEmail", "");
			String password = prefs.getString("spreadsheetAccessPassword", "");
			SpreadSheetFactory factory = SpreadSheetFactory.getInstance(new BasicAuthenticatorImpl(email, password));
			// Read from local Cache
			SpreadSheet sp = getSpreadSheet(factory);
			
			currentWorksheet = getWorksheet(sp);
			//cols = wk.getColumns();
			
			int timePeriod = prefs.getInt("timePeriod", 15);
			String deadlineColumn = prefs.getString(currentColumnNameIds[0], "dl").toLowerCase();
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -timePeriod);

	    	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String targetTime = dateFormat.format(calendar.getTime());
			
			rows = currentWorksheet.getData(false, deadlineColumn + ">" + targetTime);
			return null;
		}
		
		private SpreadSheet getSpreadSheet(SpreadSheetFactory factory){
			return factory.getSpreadSheet(spreadsheetName, true).get(0);
		}
		
		
		private WorkSheet getWorksheet(SpreadSheet sp){
			return sp.getWorkSheet(worksheetName, true).get(0);
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(dialog.isShowing())
				dialog.cancel();
			
			if(rows == null || rows.size() == 0){
				return;
			}
			
			tasks.clear();
			
			for(int i=0; i<rows.size(); i++){
				WorkSheetRow row = rows.get(i);
				
				ArrayList<WorkSheetCell> cells = row.getCells();
				
				Task task;
				String deadline = "";
				String description = "";
				String status = "";
				String comments = "";
				
				for(int j=0; j<cells.size(); j++){
					WorkSheetCell cell = cells.get(j);
					String columnName = cell.getName();
					
					if (columnName.equalsIgnoreCase(prefs.getString(currentColumnNameIds[0], "dl"))){
						deadline = cell.getValue();
					}
					else if (columnName.equalsIgnoreCase(prefs.getString(currentColumnNameIds[1], "task"))){
						description = cell.getValue();
					}
					else if (columnName.equalsIgnoreCase(prefs.getString(currentColumnNameIds[2], "status"))){
						status = cell.getValue();
					}
					else if (columnName.equalsIgnoreCase(prefs.getString(currentColumnNameIds[3], "comments"))){
						comments = cell.getValue();
					}
				}
				
				task = new Task(description, deadline, status, comments);
				task.setDescriptionAndTags(description);
				
//				HashMap<String, String> records = new HashMap<String, String>();
//				records.put("status", "test");
//				currentWorksheet.updateListRow(sp.getKey(), row, records);
//				
				task.setRowIndex(row.getRowIndex());
				insertTaskIntoTheList(task);
			}
			
			notifyNewTaskSet();
		}
	}

	public void notifyNewTaskSet() {
		tasklistSerializer.saveTasks(this, tasks);
		mSectionsPagerAdapter.notifyDataSetChanged();
		
	}
	
	public boolean reloadTasklist(MenuItem item){
		boolean pendingTasksPresent = false;
		for (Task task : tasks){
			if(!task.getServerStatus().isCommitted()){
				pendingTasksPresent = true;
				break;
			}
		}
		
		if (pendingTasksPresent){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("There are pending tasks that will be lost. Proceed?");
			builder.setPositiveButton("Yes", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					new LoadSpreadsheetDataCommand().execute();
				}
			});
			builder.setNegativeButton("No", new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
					
				}
			});
			builder.show();
		}
		else{
			new LoadSpreadsheetDataCommand().execute();
		}
		
		
		return true;
		
	}
	
	public boolean addTask(MenuItem item){
		Intent intent = new Intent(this, AddTask.class);
		startActivityForResult(intent, ADD_CODE);
		return true;
	}
	
	public boolean showHowTo(MenuItem item){
		Intent intent = new Intent(this, HowTo.class);
		startActivity(intent);
		return true;
	}
	
	public boolean configure(MenuItem item){
		Intent intent = new Intent(this, TasklistConfiguration.class);
		startActivity(intent);
		return true;
	}
	
	public boolean commitToSpreadsheet(MenuItem item){
		ArrayList<Task> pendingTasks = new ArrayList<Task>();
		
		for (Task task : tasks){
			if (!task.getServerStatus().isCommitted()){
				pendingTasks.add(task);
			}
		}
		
		SendDataToServer sendDataToServer = new SendDataToServer(pendingTasks, currentWorksheet);
		sendDataToServer.execute();
		
		return true;
	}
	
	public boolean showInfo(MenuItem item){
		
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode != RESULT_OK){
			return;
		}
		
		TaskServerAction action = TaskServerAction.NO_ACTION;
		Task task = (Task) data.getExtras().getSerializable("task");
		requestCode = task==null?ADD_CODE : UPDATE_CODE;

		Bundle bundle = data.getExtras();
		
		String description = bundle.getString("description");
		String comments = bundle.getString("comments");
		String tag = bundle.getString("tagName");
		String deadline = bundle.getString("deadline");		
		
		switch (requestCode) {
		case ADD_CODE:
			{
				action = TaskServerAction.ADD;
				
				task = new Task(description, deadline, getString(R.string.scheduled_section), comments);
				if (tag.length() > 0)
				{
					task.getTagNames().add(tag);
				}
				break;
			}
		case UPDATE_CODE:
			{
				// if it's not yet adde to the source, then we just update the data localy
				// but the action that needs to be added w.r.t the server is till ADD.
				if (task.getServerStatus().getAction() == TaskServerAction.ADD){
					action = TaskServerAction.ADD;
				}
				else{
					action = TaskServerAction.UPDATE;
				}
		        //task = (Task) getIntent().getExtras().getSerializable("task");

				task.setComment(comments);
				
				task.setDeadline(deadline);
				
				ArrayList<String> tagNames = new ArrayList<String>();
				if (tag.length() > 0)
				{
					tagNames.add(tag);
				}
				task.setDescription(description);
				
				task.setTagNames(tagNames);
				
				
				removeTaskByRowId(task);
				
				break;
			}
		default:
			break;
		}
		
		TaskServerStatus serverStatus = new TaskServerStatus(false, action);
		task.setServerStatus(serverStatus);
		
		// add it in both cases to update the position in the list
		insertTaskIntoTheList(task);
		
		notifyNewTaskSet();
		
		return;
	}
	
	private void removeTaskByRowId(Task task) {
		for (int i = 0 ; i < tasks.size(); i++){
			if (tasks.get(i).getRowIndex().equals(task.getRowIndex())){
				tasks.remove(i);
			}
		}
	}

	private void insertTaskIntoTheList(Task task){
		TaskDeadlineComparator comparator = new TaskDeadlineComparator();
		
		int i = tasks.size()-1;
		for (; i>=0; i--){
			if (comparator.compare(tasks.get(i), task) <= 0){
				break;
			}	
		}
		tasks.add(i+1, task);
	}
	
	private class SendDataToServer extends AsyncTask{

		private ArrayList<Task> pendingTasks = new ArrayList<Task>();
		private WorkSheet worksheet;
		
		public SendDataToServer(Task pendingTasks, WorkSheet worksheet){
			this.pendingTasks.add(pendingTasks);
			this.worksheet = worksheet;
		}
		
		public SendDataToServer(ArrayList<Task> pendingTasks, WorkSheet worksheet){
			this.pendingTasks = pendingTasks;
			this.worksheet = worksheet;
		}
		
		@Override
		protected Object doInBackground(Object... params) {
			
			TaskServerSynchronizer serverSynchronizer = new TaskServerSynchronizer(Tasklist.this);
			
			for (Task task : pendingTasks){
				if (task.getServerStatus().isCommitted()){
					continue;
				}
				
				switch (task.getServerStatus().getAction()) {
				case ADD:
					serverSynchronizer.addTask(task);
					break;
					
				case UPDATE:
					serverSynchronizer.updateTask(task);
					break;
					
				case DELETE:
					serverSynchronizer.deleteTask(task);
					break;

				default:
					break;
				}
			}
			
			int stillPendingTasksCount = 0;
			
			for (Task task : pendingTasks){
				if (!task.getServerStatus().isCommitted()){
					stillPendingTasksCount++;
				}
				else{
					
					if(task.getServerStatus().getAction() == TaskServerAction.DELETE){
						Tasklist.this.tasks.remove(task);
					}
					else{
						task.getServerStatus().setAction(TaskServerAction.NO_ACTION);
					}
				}
			}
			
			Message msg = new Message();
			msg.obj = "updateView";
			mHandler.sendMessage(msg);
			
			return stillPendingTasksCount;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if ((Integer)result == 0){
				Toast toast = Toast.makeText(getApplicationContext(), "Synchronization with the server finished", Toast.LENGTH_SHORT);
				toast.show();
			}
			else{
				Toast toast = Toast.makeText(getApplicationContext(), "Synchronized with the server, but still "+result.toString()+" tasks pending. Please try again later.", Toast.LENGTH_SHORT);
				toast.show();
			}
		}		
	}
}
