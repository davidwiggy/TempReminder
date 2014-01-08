package com.tempreminder;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import dbmanager.AlertTempDatabaseProvider;

//*********************************************************************************************
//** Class:      StartMyServiceAtBootReceiver                                                **
//** Programmer: Timothy David Wiggins                                                       **
//** PURPOSE:    This class extends BroadcastReceiver. It queries the database for a value.  **
//** PURPOSE:    Then that value is place in a varible. That variable is placed in a         **
//** PURPOSE:    condition, if that condition is meet it then starts the repeating alarm     **
//** PURPOSE:    again. The whole purpose of this class is because alarmManagers are lost    **
//*********************************************************************************************
public class StartMyServiceAtBootReceiver extends BroadcastReceiver 
{
	private String whereClause = dbmanager.Constants._id + " = " + "'1'";
	private int alertSet;
	private int hour, minute;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		alertSet = -1;
		if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction()))
		{
			
			Cursor cursor = context.getContentResolver().query(AlertTempDatabaseProvider.TABLE_URI,
					null, whereClause, null, null);
			
			String tempAlertSet = null;

			String tempHour = null;
			String tempMinute = null;
			
			if(cursor.moveToFirst())
			{
				do
				{
					int alertSetIndex = cursor.getColumnIndex("AlertSet");
					tempAlertSet = cursor.getString(alertSetIndex);
					int hourIndex = cursor.getColumnIndex("Hour");
					tempHour = cursor.getString(hourIndex);
					int minIndex = cursor.getColumnIndex("Minute");
					tempMinute = cursor.getString(minIndex);
				}while(cursor.moveToNext());
			}			
			
			try 
			{
				alertSet = Integer.valueOf(tempAlertSet);
				hour = Integer.valueOf(tempHour);
				minute = Integer.valueOf(tempMinute);
			}
			catch (NumberFormatException e) 
			{
			    e.printStackTrace();
			}
						
			if(alertSet == 1)
			{
	    		int setHourTime;
				if(hour <= 2)
	    			setHourTime = 0;
	    		else
	    			setHourTime = hour - 2;
				
				//Start the new repeating alarm!!!
				Calendar cal = Calendar.getInstance();
	    		cal.set(Calendar.HOUR_OF_DAY, setHourTime);
	    		cal.set(Calendar.MINUTE, minute);
	    		cal.set(Calendar.SECOND, 00);
	    		Intent intentService = new Intent(context, MyService.class);
	    		PendingIntent pi = PendingIntent.getService(context, 0, intentService, 0);
	    		AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	    		alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
	    	
			}
	    	
		}
	}	
}
