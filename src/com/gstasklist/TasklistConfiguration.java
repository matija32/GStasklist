package com.gstasklist;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TasklistConfiguration extends Activity {

    private String sourceUrl;
	private int spreadsheetId;
	private int worksheetId;
	private String workbookName;
	private String spreadsheetName;
	private int timePeriod;
	private String email;
	private String password;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_tasklist);        
        loadConfig();
        
        checkIfSettingSourceShouldBeEnabled();
		
    }
    
    private void checkIfSettingSourceShouldBeEnabled() {
		SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
		
		boolean hasCredentials = prefs.getString("spreadsheetAccessEmail", "").length() != 0 && prefs.getString("spreadsheetAccessPassword", "").length() != 0;
		Button selectSourceButton = (Button) findViewById(R.id.selectSourceButton);
		selectSourceButton.setEnabled(hasCredentials);	
	}

	@Override
    public void onRestart(){
    	super.onRestart();
    	loadConfig();
    }
    
    private void loadConfig() {
		
    	TextView spreadsheetNameTextView = (TextView) findViewById(R.id.currentSourceSpreadsheetNameTextView);
    	TextView workbookNameTextView = (TextView) findViewById(R.id.currentSourceWorkbookNameTextView);
    	
    	SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
    	spreadsheetId = prefs.getInt("spreadsheetId", -1);
    	if (spreadsheetId < 0 ) 
    	{
			return;
		}

    	// set the source spreadsheet 
    	spreadsheetName = prefs.getString("spreadsheetName", "Name unavailable");
		workbookName = prefs.getString("worksheetName", "Name unavailable");
		worksheetId = prefs.getInt("worksheetId", 1);
		timePeriod = prefs.getInt("timePeriod", 15);
		
		spreadsheetNameTextView.setText("Spreadsheet: " + spreadsheetName);
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -timePeriod);
    	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		String deadlineLimit = dateFormat.format(calendar.getTime());
		
		workbookNameTextView.setText("Worksheet: " + workbookName + " [deadline > "+deadlineLimit+"]");
    }

	public void selectSourceSpreadsheet(View view){
    	Intent intent = new Intent(this, GSSAct.class);
    	startActivity(intent);
    }
	
	public void defineColumns(View view){
		Intent intent = new Intent(this, DefineColumns.class);
		startActivity(intent);
	}
	
	public void defineTags(View view){
		Intent intent = new Intent(this, DefineTags.class);
		startActivity(intent);
	}
	
	public void enterCredentials(View view){
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Enter credentials");
		
		TextView tv1 = new TextView(this.getApplicationContext());
		tv1.setText("Please enter the email-adrress and the password for accessing the spreadsheet.");
		
		TextView tv2 = new TextView(this.getApplicationContext());
		tv2.setText("WARNING: This application can potentially be unsafe, so please create a dummy account to give edit access to your private spreadsheet.\n"+
					"\nBe sure to give edit rights to the collaborators by selecting it at Data->Protect sheet), so that your real Google account is not exposed.");
		
		final EditText editTextEmail = new EditText(getApplicationContext());
		editTextEmail.setHint("Email");
		final EditText editTextPassword = new EditText(getApplicationContext());
		editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		editTextPassword.setHint("Password");
		
		SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
		editTextEmail.setText(prefs.getString("spreadsheetAccessEmail", ""));
		editTextPassword.setText(prefs.getString("spreadsheetAccessPassword", ""));
		
		Button button = new Button(getApplicationContext());
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
		    	
				String username = editTextEmail.getText().toString();
				String password = editTextPassword.getText().toString();

	    		prefs.edit()
	    			.putString("spreadsheetAccessEmail", username )
	    			.putString("spreadsheetAccessPassword", password )
	    			.commit();
	    		loadConfig();
	    		dialog.dismiss();
	    	
	    		checkIfSettingSourceShouldBeEnabled();
			}
		});
		
		button.setText("OK");
		
		LinearLayout dialogView = new LinearLayout(getApplicationContext());
		
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(5,5,5,5);
		//dialogView.setLayoutParams(layoutParams);
		
		dialogView.setOrientation(1);
		dialogView.addView(tv1,layoutParams);
		dialogView.addView(editTextEmail, layoutParams);
		dialogView.addView(editTextPassword, layoutParams);
		dialogView.addView(tv2, layoutParams);
		dialogView.addView(button, layoutParams);		

		
		dialog.setContentView(dialogView);
		dialog.show();
		
		editTextEmail.requestFocus();
		
	}
	
	public void setTasklistStartingRow(View view){
    	
		final Dialog dialog = new Dialog(this);
		dialog.setTitle("Tasklist time range");
		
		TextView tv = new TextView(this.getApplicationContext());
		tv.setText("When loading the tasklist, how much do you want to go back in time (in days)?");
		
		final EditText editText = new EditText(getApplicationContext());
		editText.setText("" + timePeriod);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		
		Button button = new Button(getApplicationContext());
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE); 
		    	
				int tP = Integer.parseInt(editText.getText().toString());
		    	if (tP < 1){
		    		Toast toast = Toast.makeText(getApplicationContext(), "The time period has to be greater than 1 day", Toast.LENGTH_SHORT);
					toast.show();
					return;
		    	}
		    	else{
		    		prefs
		    			.edit()
		    			.putInt("timePeriod", tP )
		    			.commit();
		    		loadConfig();
		    		dialog.dismiss();
		    	}
			}
		});
		button.setText("OK");
		
		LinearLayout dialogView = new LinearLayout(getApplicationContext());
		
		dialogView.setOrientation(1);
		dialogView.addView(tv);
		dialogView.addView(editText);
		dialogView.addView(button);
		
		dialog.setContentView(dialogView);
		dialog.show();
    	

	}
}
