package com.gstasklist.layout;

import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.TextWatcher;

public class ColumnConfigurationTextWatcher implements TextWatcher {

	private Editor editor;
	private String columnType;

	public ColumnConfigurationTextWatcher(Editor editor, String columnType){
		this.editor = editor;
		this.columnType = columnType;
	}
	
	@Override
	public void afterTextChanged(Editable s) {
		//editor.putString(columnType, s.toString());
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
		
	}

}
