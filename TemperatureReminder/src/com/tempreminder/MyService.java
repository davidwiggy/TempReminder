package com.tempreminder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.IBinder;
import dbmanager.AlertTempDatabaseProvider;

//*********************************************************************************************
//** Class:      MyService                                                                   **
//** Programmer: Timothy David Wiggins                                                       **
//** PURPOSE:    This class/activity provides a user interface when the alarm is firing. It  **
//** PURPOSE:    uses the same view as the main activity. The only difference is it has a    ** 
//** PURPOSE:    dialog box displayed on it. This class uses a WAKE Lock to wake the device  **
//** PURPOSE:    and lock it until the user responds to the alarm. It also uses a media      **
//** PURPOSE:    player to play the alert sound when the alarm is firing.                    **
//*********************************************************************************************
public class MyService extends Service 
{
	private final int ID = 1;
	private String minTemp;
	private int zipCode; 
	private int alertTemp;
	private int projectedLow;
	private String whereClause = dbmanager.Constants._id + " = " + "'1'";
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return null;
	}

	@Override
	public void onCreate() 
	{
		super.onCreate();
		//Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onDestroy() 
	{
		
      NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		
		//Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	//*******************************************************
	//** This method queries the database to get some      **
	//** values. It then test the values and calls either  **
	//** the web api or another method. It also calls a    **
	//** method to check the connection and if it fails it **
	//** builds a notification letting the user know that  **
	//** a internet connection is needed for the app.      **
	//*******************************************************
	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		boolean network;
		
		network = CheckNetwork();
		
		if(network == true)
		{
			//Querying to get the alert Temp and projected low if available			
			Cursor cursor = getContentResolver().query(AlertTempDatabaseProvider.TABLE_URI,
					null, whereClause, null, null);
			
			String tempAlertTemp = null;
			String tempProjectedLow = null;
			String tempZipCode = null;
			
			if(cursor.moveToFirst())
			{
				do
				{
					int zipCodeIndex = cursor.getColumnIndex("Zip");
					tempZipCode = cursor.getString(zipCodeIndex);
					int alertTempIndex = cursor.getColumnIndex("AlertTemp");
					tempAlertTemp = cursor.getString(alertTempIndex);
					int projectedTempIndex = cursor.getColumnIndex("ProjectedLow");
					tempProjectedLow = cursor.getString(projectedTempIndex);
				}while(cursor.moveToNext());
			}
			
			try { 
			  alertTemp = Integer.valueOf(tempAlertTemp);
			  projectedLow = Integer.valueOf(tempProjectedLow);
			  zipCode = Integer.valueOf(tempZipCode);
			}
			catch (NumberFormatException e) {
			  e.printStackTrace();
			}
			
			if(projectedLow == 0)
			{
				new GetData().execute("http://api.worldweatheronline.com/free/v1/weather.ashx?q=" + zipCode + "&format=json&num_of_days=1&cc=cc&includelocation=no&show_comments=no&key=tvhecebgqy5szyz89qe4ps77");
			}
			else
			{
				ContentValues value = new ContentValues();
				value.put(dbmanager.Constants.COLUMN_PROJECTED_LOW, 0);
				getContentResolver().update(AlertTempDatabaseProvider.TABLE_URI,
						value, whereClause, null);
				checkAndSetService();
			}
		}
		else
		{
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			Intent notificationIntent = new Intent(this, MainActivity.class);
			int icon = R.drawable.notifylauncher;
			String tickerText = "Temperature Reminder Failed to Update";
			long when = System.currentTimeMillis();
			  
			Notification notification = new Notification(icon, tickerText, when);
			  
			PendingIntent pending = PendingIntent.getActivity(this, 0, notificationIntent, 0);

			String contentTitle = "Temperature Reminder";
			String contentText = "No Internet Connection Detected!";
			notification.setLatestEventInfo(this, contentTitle, contentText, pending);
		
			notificationManager.notify(ID, notification); 
			//Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
			//this.stopSelf();
		}
		
		return super.onStartCommand(intent, flags, startId);
		
	}//End of onStartCommand
	
	//*******************************************************
	//** This method checks the internet connection and     **
	//** returns a boolean.                                 **
	//*******************************************************
	private boolean CheckNetwork()
	{
	    ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (conMgr.getActiveNetworkInfo() != null
	            && conMgr.getActiveNetworkInfo().isAvailable()
	            && conMgr.getActiveNetworkInfo().isConnected()) 
	        return true;
	    else 
	        return false;
	}
	
	//*******************************************************
	//** This method checks a condition and if it is meet  **
	//** it queries the db from some values. Then sets up  **
	//** a new alarm manager to alert the user of the      **
	//** projected low.                                    **
	//*******************************************************
	@SuppressWarnings("deprecation")
	private void checkAndSetService()
	{
		int hour = 0;
		int minute = 10;
		String hourStr = null;
		String minStr = null;
		
		if(projectedLow < alertTemp)
		{
			
			Cursor cursor1 = getContentResolver().query(AlertTempDatabaseProvider.TABLE_URI,
					null, whereClause, null, null);
			
			if(cursor1.moveToFirst())
			{
				do
				{
					int hourIndex = cursor1.getColumnIndex("Hour");
					hourStr = cursor1.getString(hourIndex);
					int minIndex = cursor1.getColumnIndex("Minute");
					minStr = cursor1.getString(minIndex);
				}while(cursor1.moveToNext());
			}
			
			try { 
				  hour = Integer.valueOf(hourStr);
				  minute = Integer.valueOf(minStr);
				}
				catch (NumberFormatException e) {
				  e.printStackTrace();
				}

			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.SECOND, 0);
			
			Intent stopIntent = new Intent(this, AlarmReceiverActivty.class);
			PendingIntent piStop = PendingIntent.getActivity(this, 0, stopIntent, 0);
			AlarmManager alarmStop = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			alarmStop.cancel(piStop);	
			
			AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
			Intent alarmIntent = new Intent(this, AlarmReceiverActivty.class);
			PendingIntent pi = PendingIntent.getActivity(this, 0, alarmIntent, 0);
			alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
		}
		
		//Setting up the Notification 
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(this, MainActivity.class);
		int icon = R.drawable.notifylauncher;
		String tickerText = "Temperature Reminder Updating";
		long when = System.currentTimeMillis();
		  
		Notification notification = new Notification(icon, tickerText, when);
		  
		PendingIntent pending = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		String contentTitle = "Temperature Reminder";
		String contentText = "Click to Launch";
		notification.setLatestEventInfo(this, contentTitle, contentText, pending);
	
		notificationManager.notify(ID, notification); 
		//Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();
		this.stopSelf();
	}
	
	//*********************************************************************************************
	//** Class:      SubClass=GetData                                                            **
	//** Programmer: Timothy David Wiggins                                                       **
	//** PURPOSE:    This class is a AsyncTask that runs on a separate thread from the main      **
	//** PURPOSE:    class. This class access a web api and pulls data from the site and then    **
	//** PURPOSE:    parses the JSON data to get the relevant information out of the file. Once  **
	//** PURPOSE:    the information has been parsed it uses a post execute method to display if **
	//** PURPOSE:    the zip code was found or not. If the zip was found it updates the db with  **
	//** PURPOSE:    the new zip and then calls the startService method.                         **
	//*********************************************************************************************
	 public class GetData extends AsyncTask<String, Void, String>
	 {
	    @Override
	    protected String doInBackground(String...params)
  	{
			
			BufferedReader reader = null ;
			
			try{
				HttpClient client = new DefaultHttpClient();
				//Storing the address
				URI uri = new URI(params[0]);
				//Retrieve info
				HttpGet get = new HttpGet(uri);
				//Executing the request
				HttpResponse response = client.execute(get);
				//Entity - sent or received with an HTTP message
				InputStream stream = response.getEntity().getContent();
				reader = new BufferedReader(new InputStreamReader(stream));
				
				StringBuilder builder = new StringBuilder();
				String line = "";
				
				while((line = reader.readLine()) != null)
				{
					builder.append(line);
				}
				reader.close();
				
				String jsonData = builder.toString();
				
				
				JSONObject json = new JSONObject(jsonData);
				JSONObject data = json.getJSONObject("data");
				JSONArray weather = data.getJSONArray("weather");
				
				for(int  i = 0; i < weather.length(); i++)
				{
					JSONObject temperature = weather.getJSONObject(i);
					minTemp = temperature.getString("tempMinF");
				}
	
				return minTemp;
			
			
			}catch (URISyntaxException e){
				e.printStackTrace();
				
			}catch (ClientProtocolException e){
				e.printStackTrace();
				
			}catch (IOException e){
				e.printStackTrace();
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			finally {
				if (reader != null) {
					try {
						reader.close();
					}catch (Exception e){
						
					}
				}
			}
			//If null is returned means; Finally block is not executed
			return null;
		}
	    
  	@Override
  	protected void onPostExecute(String result)
  	{  		
  		try
  		{
  			projectedLow = Integer.valueOf(minTemp);
  		}
  		catch(Exception e)
  		{
  			e.printStackTrace();
  		}
  		
  		super.onPostExecute(result);
  		if(result != null)
  		{
  			ContentValues value = new ContentValues();
				value.put(dbmanager.Constants.COLUMN_NEW_LOW, projectedLow);
				getContentResolver().update(AlertTempDatabaseProvider.TABLE_URI,
						value, whereClause, null);
      		checkAndSetService();
  		}

  	}	
	    
  }//End of GetData	
}//End of MyService
