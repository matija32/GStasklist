package com.gstasklist.layout;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gstasklist.entities.Column;
import com.gstasklist.entities.Tag;

public class TagArrayAdapter extends ArrayAdapter<Tag> {
	
	private Context context;
	private ArrayList<Tag> tags;
	private int layoutResourceId;

	public TagArrayAdapter(Context context, int layoutResourceId, ArrayList<Tag> tags) {
		super(context, layoutResourceId, tags);
		this.context = context;
		this.layoutResourceId = layoutResourceId;
		this.tags = tags;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View row = convertView;
		TagHolder holder = null;
		
		if (row == null){
			LayoutInflater inflater = ((Activity)context).getLayoutInflater();
			row = inflater.inflate(layoutResourceId, null);
			holder = new TagHolder();
			
			holder.field  = (TextView)row.findViewById(android.R.id.text1);
            
            row.setTag(holder);
		}
		else{
			holder = (TagHolder)row.getTag();
		}
		
		Tag tag = tags.get(position);
		row.setBackgroundColor(tag.getBackgroundColor());
		
        holder.field.setText(tag.getName());
        holder.field.setTextColor(tag.getForegroundColor());
        
        return row;
	
	}
	
	private static class TagHolder{
		TextView field;
	}

}
