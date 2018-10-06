package com.gstasklist;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.gstasklist.entities.Tag;
import com.gstasklist.layout.ColumnArrayAdapter;
import com.gstasklist.layout.TagArrayAdapter;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Canvas.VertexMode;
import android.graphics.Color;
import android.text.Editable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DefineTags extends ListActivity {

	private SharedPreferences prefs;
	private TagArrayAdapter tagArrayAdapter;
	private ArrayList<Tag> tags;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define_tags);
        
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        
        Set<String> tagStrings = prefs.getStringSet("tags", new HashSet<String>());
        
        tags = Tag.parseTagSet(tagStrings);
        
        tagArrayAdapter = new TagArrayAdapter(this, android.R.layout.simple_list_item_1, tags);
        
		ListView list = getListView();
		list.setTextFilterEnabled(true);
		registerForContextMenu(list);

		
		list.setAdapter(tagArrayAdapter);
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id){ 
	    openContextMenu(view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_define_tags, menu);
        return true;
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
    	super.onCreateContextMenu(menu, v, menuInfo);
    	MenuInflater menuInflater = getMenuInflater();
    	menuInflater.inflate(R.menu.context_tag, menu);
    	menu.setHeaderTitle("Tag");
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
    	switch (item.getItemId()) {
	    	case R.id.editItem:
	    	{
	    		editTag(info.position);
	    		return true;
	    	}
	    	
	    	case R.id.deleteItem:
	    	{
	    		deleteTag(info.position);
	    		return true;
	    	}
	    	 
	    	default:
	    	return super.onContextItemSelected(item);
    	}
    }
    


	private void deleteTag(int position) {
		final int pos = position;
		final String tagName = tags.get(position).getName();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Deleting "+tagName+". Sure?");
		builder.setPositiveButton("Yes", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				tags.remove(pos);
				commitTags();
				updateList();
				
				Toast toast = Toast.makeText(getApplicationContext(), "Removed tag: "+tagName, Toast.LENGTH_SHORT);
				toast.show();
				
			}
		});
		
		builder.setNegativeButton("No", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {				
				Toast toast = Toast.makeText(getApplicationContext(), "Phew, that was close. You almost deleted "+tagName+" tag :)", Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		
		builder.show();

	}
	
    private void editTag(int position) {
		final Tag tag = tags.get(position);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        
        final View dialogView = inflater.inflate(R.layout.tag_dialog, null);
        builder.setView(dialogView);
    	
    	final Spinner spinnerForegroundColor = (Spinner) dialogView.findViewById(R.id.spinnerF);
    	final Spinner spinnerBackgroundColor = (Spinner) dialogView.findViewById(R.id.spinnerB);
    	final EditText editText = (EditText) dialogView.findViewById(R.id.editText3);
    	

    	ArrayAdapter<CharSequence> adapterF = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_item);
    	ArrayAdapter<CharSequence> adapterB = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_item);
    	
    	spinnerForegroundColor.setAdapter(adapterF);
    	spinnerBackgroundColor.setAdapter(adapterB);
    	
    	// setting default colors
    	Resources res = getResources();
    	String[] colors = res.getStringArray(R.array.colors);
    	
    	int bIndex = -1;
    	int fIndex = -1;
    	for (int i = 0; i < colors.length; i++){
    		if (tag.getForegroundColor() == Color.parseColor(colors[i])){
    			fIndex = i;
    		}
    		if (tag.getBackgroundColor() == Color.parseColor(colors[i])){
    			bIndex = i;
    		}
    	}
    	
    	spinnerForegroundColor.setSelection(fIndex);
    	
    	spinnerBackgroundColor.setSelection(bIndex);
    	editText.setText(tag.getName());
    	
    	
    	builder.setTitle("Edit tag");
    	
    	builder.setPositiveButton("Done", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				Editable editable = editText.getText();
				if (editable.length() == 0){
					Toast toast = Toast.makeText(getApplicationContext(), "Unable to edit the tag - name cannot be empty", Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				
				String name = editable.toString();
				int foregroundColor = (Integer) Color.parseColor((String) spinnerForegroundColor.getSelectedItem());
				int backgroundColor = (Integer) Color.parseColor((String) spinnerBackgroundColor.getSelectedItem());
				
				tag.setName(name);
				tag.setBackgroundColor(backgroundColor);
				tag.setForegroundColor(foregroundColor);
				
				commitTags();
				updateList();
			}
		});
    	
    	builder.show();
	}

	public boolean addNewTag(MenuItem item){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        
        final View dialogView = inflater.inflate(R.layout.tag_dialog, null);
        builder.setView(dialogView);
    	
    	final Spinner spinnerForegroundColor = (Spinner) dialogView.findViewById(R.id.spinnerF);
    	final Spinner spinnerBackgroundColor = (Spinner) dialogView.findViewById(R.id.spinnerB);
    	
    	ArrayAdapter<CharSequence> adapterF = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_item);
    	ArrayAdapter<CharSequence> adapterB = ArrayAdapter.createFromResource(this, R.array.colors, android.R.layout.simple_spinner_item);
    	
    	spinnerForegroundColor.setAdapter(adapterF);
    	spinnerBackgroundColor.setAdapter(adapterB);
    	
    	spinnerForegroundColor.setSelection(4);
    	spinnerBackgroundColor.setSelection(3);
    	
    	
    	builder.setTitle("New tag");
    	
    	builder.setPositiveButton("Add", new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				
				EditText editText = (EditText) dialogView.findViewById(R.id.editText3);
				Editable editable = editText.getText();
				if (editable.length() == 0){
					Toast toast = Toast.makeText(getApplicationContext(), "No tag created - name cannot be empty", Toast.LENGTH_SHORT);
					toast.show();
					return;
				}
				
				String name = editable.toString();
				int foregroundColor = (Integer) Color.parseColor((String) spinnerForegroundColor.getSelectedItem());
				int backgroundColor = (Integer) Color.parseColor((String) spinnerBackgroundColor.getSelectedItem());
				
				tags.add(new Tag(name, foregroundColor, backgroundColor));
				commitTags();
				updateList();
			}
		});
    	
    	builder.show();
    	return true;
    }
	
	private void commitTags(){
		Editor editor = prefs.edit();
		
		editor.putStringSet("tags", Tag.toStringSet(tags));
		editor.commit();
	}
    
    private void updateList(){
    	tagArrayAdapter.notifyDataSetChanged();
    }
}
