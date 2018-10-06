package com.gstasklist;

import java.util.ArrayList;

import com.gstasklist.entities.Column;
import com.gstasklist.layout.ColumnArrayAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DefineColumns extends ListActivity {


	private SharedPreferences prefs;
	private ArrayList<Column> columns;
	private String[] currentColumnNameIds;
	private ColumnArrayAdapter columnArrayAdapter;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_columns);
        
        prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
        
		currentColumnNameIds = new String[]{
			"currentDeadlineColumnName",
			"currentTaskColumnName",
			"currentStatusColumnName",
			"currentCommentsColumnName"
		};

		String[] currentColumnNames = new String[]{
				prefs.getString(currentColumnNameIds[0], "<column name>"),
				prefs.getString(currentColumnNameIds[1], "<column name>"),
				prefs.getString(currentColumnNameIds[2], "<column name>"),
				prefs.getString(currentColumnNameIds[3], "<column name>")
		};
		
		String[] columnTypes = new String[]{
				getString(R.string.column_deadline),
				getString(R.string.column_task),
				getString(R.string.column_status),
				getString(R.string.column_comments)
		};
		
		columns = new ArrayList<Column>();
		for(int i = 0; i < columnTypes.length; i++){
			columns.add(new Column(currentColumnNames[i], columnTypes[i]));
		}
		
		columnArrayAdapter = new ColumnArrayAdapter(this, android.R.layout.simple_list_item_1, columns);
		
		
		ListView list = getListView();
		TextView textView = new TextView(this);
		textView.setText("Enter the names for columns as they are in your spreadsheet");
		list.addHeaderView(textView);
		list.setAdapter(columnArrayAdapter);
		
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id){
    	if (id < 0) return;
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText editText = new EditText(this);
    	final int itemPosition = (int) id;
    	
    	String currentColumnName = columns.get(itemPosition).getName();
    	if (!currentColumnName.equals("<column name>")){
    		editText.setText(currentColumnName);
    	}
    	
    	builder.setView(editText);
    	
    	
    	builder.setTitle("Column name for "+ columns.get(itemPosition).getType());
    	builder.setPositiveButton("Save", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				String newName = editText.getText().toString();
				
				columns.get(itemPosition).setName(newName);
				
				Editor editor = prefs.edit();
				editor.putString(currentColumnNameIds[itemPosition], newName);
				editor.commit();
				
				updateList();
			}
		});
    	
    	builder.show();
    	
    	
    }

	private void updateList() {
		columnArrayAdapter.notifyDataSetChanged();
	}
}
