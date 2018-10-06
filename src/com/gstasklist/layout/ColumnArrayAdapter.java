package com.gstasklist.layout;

import java.util.ArrayList;
import java.util.List;

import com.gstasklist.entities.Column;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

public class ColumnArrayAdapter extends ArrayAdapter<Column> {

	private ArrayList<Column> columns;
	private int layoutResourceId;
	private Context context;

	public ColumnArrayAdapter(Context context, int layoutResourceId, ArrayList<Column> columns) {
		super(context, layoutResourceId, columns);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.columns = columns;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		ColumnHolder holder = null;
		
		if (row == null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, null);
			holder = new ColumnHolder();
			
			holder.field  = (TextView)row.findViewById(android.R.id.text1);
            
            row.setTag(holder);
		}
		else{
			holder = (ColumnHolder)row.getTag();
		}
		
		Column column = columns.get(position);
        holder.field.setText(column.getType() + ": " + column.getName());

        return row;
	
	}
	
	private static class ColumnHolder{
		TextView field;
	}

}
