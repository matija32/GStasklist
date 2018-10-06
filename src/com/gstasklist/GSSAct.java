/*
 * Copyright (C) 2011 Prasanta Paul, http://prasanta-paul.blogspot.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gstasklist;

import java.util.ArrayList;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.gstasklist.auth.AndroidAuthenticator;
import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.auth.Account;
import com.pras.auth.BasicAuthenticatorImpl;

/**
 * @author Prasanta Paul
 *
 */
public class GSSAct extends Activity {
    
	ArrayList<SpreadSheet> spreadSheets;
	TextView tv;
	ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        list = new ListView(this.getApplicationContext());
        //setContentView(R.layout.main);
        //tv = (TextView)findViewById(R.id.sp_count);
        tv = new TextView(this.getApplicationContext());
        
        // Init and Read SpreadSheet list from Google Server
        new MyTask().execute();
    }
	
	private class MyTask extends AsyncTask{

		Dialog dialog;
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			dialog = new Dialog(GSSAct.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSAct.this.getApplicationContext());
			tv.setText("Retrieving the list of spreadsheets.");
			dialog.setContentView(tv);
			dialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			// Read Spread Sheet list from the server.
//			SpreadSheetFactory factory = SpreadSheetFactory.getInstance(new AndroidAuthenticator(GSSAct.this));
			
			SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE); 
			String email = prefs.getString("spreadsheetAccessEmail", "");
			String password = prefs.getString("spreadsheetAccessPassword", "");
			
			SpreadSheetFactory factory = SpreadSheetFactory.getInstance(new BasicAuthenticatorImpl(email, password));
			spreadSheets = factory.getAllSpreadSheets();
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if(dialog.isShowing())
				dialog.cancel();
			
			if(spreadSheets == null || spreadSheets.size() == 0){
		        tv.setText("No spreadsheet exists in your account.");
		        setContentView(tv);
		    }
		    else{
		        //tv.setText(spreadSheets.size() + "  spreadsheets exists in your account...");
		    	ArrayAdapter<String> arrayAdaper = new ArrayAdapter<String>(GSSAct.this.getApplicationContext(), android.R.layout.simple_list_item_1);
		    	for(int i=0; i<spreadSheets.size(); i++){
		    		SpreadSheet sp = spreadSheets.get(i);
		    		arrayAdaper.add(sp.getTitle());
		    	}
		    	list.addHeaderView(tv);
		    	list.setAdapter(arrayAdaper);
		    	tv.setText("Number of spreadsheets ("+ spreadSheets.size() +")");
		    	
		    	list.setOnItemClickListener(new OnItemClickListener(){

					public void onItemClick(AdapterView<?> adapterView, View view,
							int position, long id) {
						// Show details of the SpreadSheet
						if(position == 0)
							return;
						
						Toast.makeText(GSSAct.this.getApplicationContext(), "Showing spreadsheet details, please wait.", Toast.LENGTH_LONG).show();
						
						// Start SP Details Activity 
						Intent i = new Intent(GSSAct.this, GSSDetails.class);
						i.putExtra("sp_id", position - 1);
						startActivity(i);
						finish();
					}
		    	});
		    	setContentView(list);
		    }
		}

	}
}