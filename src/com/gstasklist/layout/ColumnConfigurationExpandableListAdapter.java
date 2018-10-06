package com.gstasklist.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gstasklist.TasklistConfiguration;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;

public class ColumnConfigurationExpandableListAdapter extends SimpleExpandableListAdapter {

	private Editor editor;
	private String[] columnConfigIds;
	private ArrayList<View> baseViews = new ArrayList<View>(); 

	public ColumnConfigurationExpandableListAdapter(TasklistConfiguration tasklistConfiguration,
			List<Map<String, String>> groupData, int simpleExpandableListItem1,
			String[] strings, int[] is,
			List<List<Map<String, String>>> childData, int tasklistColumn,
			String[] strings2, int[] is2) {
		super(tasklistConfiguration, groupData, simpleExpandableListItem1, strings, is, childData, tasklistColumn, strings2, is2);
	}	

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView!=null)
			return convertView;
		
		View baseView = super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
		if (baseViews.contains(baseView)){
			return baseView;
		}
		
		LinearLayout linearLayout = (LinearLayout) baseView;
        EditText editText = (EditText) linearLayout.getChildAt(1);
        
        //editText.addTextChangedListener(
        //		new ColumnConfigurationTextWatcher(editor, columnConfigIds[childPosition]));
        
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					return;
				
				EditText editView = (EditText)v;
				String text = editView.getText().toString();
				editor.putString(columnConfigIds[0], text);
				editView.setText(text);
				System.out.println(text);
				
			}
		});
		
		return baseView;
		
	}

	public void setEditor(Editor editor) {
		this.editor = editor;
	}

	public void setColumnConfigIds(String[] columnConfigIds) {
		this.columnConfigIds = columnConfigIds;
	}

	
}
