package com.gstasklist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.gstasklist.entities.*;
import com.gstasklist.layout.*;;

public class AddTask extends Activity {

    private DatePicker deadlinePicker;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_add_edit_task);
        
        deadlinePicker = (DatePicker) findViewById(R.id.datePicker1);
        
        Switch switchCalendarView = (Switch) findViewById(R.id.switchCalendarView);
        
        deadlinePicker.setCalendarViewShown(true);
        deadlinePicker.setSpinnersShown(false);
        deadlinePicker.getCalendarView().setFirstDayOfWeek(Calendar.MONDAY);
        deadlinePicker.setLongClickable(true);
        
        switchCalendarView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				DatePicker deadlinePicker = AddTask.this.deadlinePicker;
				deadlinePicker.setSpinnersShown(isChecked);
				deadlinePicker.setCalendarViewShown(!isChecked);
				
			}
		});
        
        Spinner spinner = (Spinner) findViewById(R.id.spinnerTag);
        
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        
        Set<String> tagStrings = prefs.getStringSet("tags", new HashSet<String>());
        
        ArrayList<Tag> tags = Tag.parseTagSet(tagStrings);
        ArrayList<String> tagNames = new ArrayList<String>();
        for(Tag tag: tags){
        	tagNames.add(tag.getName());
        }
        
        tagNames.add(0, "<No tag>");

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tagNames);
        spinner.setAdapter(spinnerArrayAdapter);
    	
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_add_edit_task, menu);
        return true;
    }
    
    public boolean saveTask(MenuItem item){
    	DatePicker datePicker = ((DatePicker)this.findViewById(R.id.datePicker1));
    	int year = datePicker.getYear();
    	int month = datePicker.getMonth();
    	int day = datePicker.getDayOfMonth();
    	
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(Calendar.YEAR, year);
    	calendar.set(Calendar.MONTH, month);
    	calendar.set(Calendar.DAY_OF_MONTH, day);
    	Date date = calendar.getTime();
    	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    	
    	String tagName = ((Spinner)this.findViewById(R.id.spinnerTag)).getSelectedItem().toString();
    	if (tagName.equals("<No tag>")){
    		tagName = "";
    	}
    	
    	
    	String description = ((EditText)this.findViewById(R.id.editTextTask)).getText().toString();
    	String deadline = dateFormat.format(date);
    	String comments = ((EditText)this.findViewById(R.id.editTextComments)).getText().toString();
    	
    	
    	Intent intent=new Intent();
    	intent.putExtra("description", description);
        intent.putExtra("deadline", deadline);
        intent.putExtra("comments", comments);
        intent.putExtra("tagName", tagName);
        
        // only there is a task as a serializable element to give back
        if (getIntent().getExtras() != null && getIntent().getExtras().getSerializable("task")!=null){
        	// return it back for editing
        	Task task = (Task) getIntent().getExtras().getSerializable("task");
        	intent.putExtra("task", task);
        }
    	setResult(RESULT_OK, intent);
        finish();
    	
        return true;
    }
}
