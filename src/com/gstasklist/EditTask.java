package com.gstasklist;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.gstasklist.entities.Tag;
import com.gstasklist.entities.Task;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.AdapterView.OnItemLongClickListener;

// only preload the stuff, the rest is the same
public class EditTask extends AddTask {

	DatePicker deadlinePicker;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Task task = (Task) getIntent().getExtras().getSerializable("task");
        
        Spinner spinner = (Spinner) findViewById(R.id.spinnerTag);
        EditText editViewTask = (EditText) findViewById(R.id.editTextTask);
        EditText editViewComments = (EditText) findViewById(R.id.editTextComments);
        
        deadlinePicker = (DatePicker) findViewById(R.id.datePicker1);
        deadlinePicker.getCalendarView().setFirstDayOfWeek(Calendar.MONDAY);
        
        Switch switchCalendarView = (Switch) findViewById(R.id.switchCalendarView);
        
        boolean isTaskSoonish = task.isDueToSoonish();
        
    	deadlinePicker.setCalendarViewShown(!isTaskSoonish);
    	deadlinePicker.setSpinnersShown(isTaskSoonish);
    	switchCalendarView.setChecked(isTaskSoonish);
    
        
        switchCalendarView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DatePicker deadlinePicker = EditTask.this.deadlinePicker;
				deadlinePicker.setSpinnersShown(isChecked);
				deadlinePicker.setCalendarViewShown(!isChecked);
				
			}
		});
        
        editViewTask.setText(task.getDescription());
        editViewComments.setText(task.getComment());
        

    	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    	Date date = null;
		try {
			date = dateFormat.parse(task.getDeadline());
		} catch (ParseException e) {
			if (task.hasInfiniteDeadline()){
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.YEAR, 1);
				date = cal.getTime();						
			}
			else{
				date = Calendar.getInstance().getTime();
			}
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
        deadlinePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        
        if (task.getTagNames().size() > 0){
	        
        	String tagName = task.getTagNames().get(0);
        	
        	SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
	        Set<String> tagStrings = prefs.getStringSet("tags", new HashSet<String>());
	        ArrayList<Tag> tags = Tag.parseTagSet(tagStrings);
	        
	    	int index = -1;
	    	for (int i = 0; i < tags.size(); i++){
	    		if (tagName.equalsIgnoreCase(tags.get(i).getName())){
	    			index = i;
	    			break;
	    		}
	    	}
	    	
	    	// +1 because <no tag> is added in the AddTask
	    	spinner.setSelection(index + 1);
        }
    	
    }
    
}
