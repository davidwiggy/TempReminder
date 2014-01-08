package com.tempreminder;

import java.io.IOException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import dbmanager.AlertTempDatabaseProvider;

//*********************************************************************************************
//** Class:      AlarmReceiverActivity                                                       **
//** Programmer: Timothy David Wiggins                                                       **
//** PURPOSE:    This class/activity provides a user interface when the alarm is firing. It  **
//** PURPOSE:    uses the same view as the main activity. The only difference is it has a    ** 
//** PURPOSE:    dialog box displayed on it. This class uses a WAKE Lock to wake the device  **
//** PURPOSE:    and lock it until the user responds to the alarm. It also uses a media      **
//** PURPOSE:    player to play the alert sound when the alarm is firing.                    **
//*********************************************************************************************
public class AlarmReceiverActivty extends Activity
{
	private MediaPlayer mMediaPlayer;
	private PowerManager.WakeLock mWakeLock;
	private Button stop;
	final int ID = 1;
	private int low, playing;
	private TextView tvTemp;
	private String temp = null;
	private String whereClause = dbmanager.Constants._id + " = " + "'1'";
	private int delayTime;
	private Handler myHandler;
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Wake Lock");
		mWakeLock.acquire();
	
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
						WindowManager.LayoutParams.FLAG_FULLSCREEN |
						WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
						WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		setContentView(R.layout.alertwindow);	
		queryTemp();
		
		delayTime = ((60 * 2) * 1000);
		myHandler = new Handler();
		
		tvTemp = (TextView)findViewById(R.id.tvExpectedTemp);
		tvTemp.setText(temp + ((char)0x00B0) + " EXPECTED!");
		
		
		stop = (Button)findViewById(R.id.btnStopAlarm);
		stop.setOnClickListener(new View.OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
			      switch(v.getId())
			      {
			        case R.id.btnStopAlarm:
			        	ContentValues value = new ContentValues();
						value.put(dbmanager.Constants.COLUMN_ALERT_FIRING, 0);
						getContentResolver().update(AlertTempDatabaseProvider.TABLE_URI,
								value, whereClause, null);
			        	stopAlert();
			        	myHandler.removeCallbacks(stopAlertRunnable);
						finish();
						

						
			        break;
			      }
			}
		});
		
		myHandler.postDelayed(stopAlertRunnable, delayTime);
		if(playing == 0)
			playSound(this, getAlarmUri());				
		
		
	}//End of onCreate
		
	//*******************************************************
	//** This method stops the alarm by stopping and       **
	//** releasing both the mediaplayer and wake lock.     **
	//*******************************************************
	private void stopAlert()
	{
		if(mMediaPlayer.isPlaying())
		{
			mMediaPlayer.stop();
			mMediaPlayer.release();
		}
		
		if(mWakeLock.isHeld())
			mWakeLock.release();

		finish();
	}
	
	//*******************************************************
	//** This method is a runnable that only happens if the**
	//** users doesn't stop the alarm after 2 minutes. It  **
	//** also builds a notification letting the user know  **
	//** they have missed the alarm.                       **
	//*******************************************************
	private Runnable stopAlertRunnable = new Runnable() 
	{	
		@SuppressWarnings("deprecation")
		@Override
		public void run() 
		{
			
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			Intent notificationIntent = new Intent(AlarmReceiverActivty.this, MainActivity.class);
			int icon = R.drawable.notifylauncher;
			String tickerText = "Temperature Reminder Alert";
			long when = System.currentTimeMillis();
			  
			Notification notification = new Notification(icon, tickerText, when);
			  
			PendingIntent pending = PendingIntent.getActivity(AlarmReceiverActivty.this, 0, notificationIntent, 0);

			String contentTitle = "Temperature Reminder";
			String contentText = "WARNING ALERT MISSED!";
			notification.setLatestEventInfo(AlarmReceiverActivty.this, contentTitle, contentText, pending);
		
			notificationManager.notify(ID, notification); 

      	ContentValues value = new ContentValues();
			value.put(dbmanager.Constants.COLUMN_ALERT_FIRING, 0);
			getContentResolver().update(AlertTempDatabaseProvider.TABLE_URI,
					value, whereClause, null);
			
			if(mMediaPlayer.isPlaying())
			{
				mMediaPlayer.stop();
				mMediaPlayer.release();
			}
			if(mWakeLock.isHeld())
				mWakeLock.release();
			finish();
		}
	};
	
	//*******************************************************
	//** This method queries the database for the expected **
	//** low temperature.                                  **
	//*******************************************************
	private void queryTemp()
	{
		
		Cursor cursor = getContentResolver().query(AlertTempDatabaseProvider.TABLE_URI,
				null, whereClause, null, null);
		String tempMedia = null;
		
		if(cursor.moveToFirst())
		{
			do
			{
				int ct = 0;
				int lowIndex = cursor.getColumnIndex("NewLow");
				temp = cursor.getString(lowIndex);
				int mediaIndex = cursor.getColumnIndex("AlertFiring");
				tempMedia = cursor.getString(mediaIndex);
				ct++;
			}while(cursor.moveToNext());
		}
		try 
		{ 
			low = Integer.valueOf(temp);
			playing = Integer.valueOf(tempMedia);
		}
		catch (NumberFormatException e) 
		{
		  e.printStackTrace();
		}
	}
	
	//*******************************************************
	//** This method instanciated the media player and gets **
	//** the audio manager to start the alert sound.       **
	//*******************************************************
	private void playSound(Context context, Uri alert)
	{
		mMediaPlayer = new MediaPlayer();
		try
		{
			mMediaPlayer.setDataSource(context, alert);
			final AudioManager audioManager =
					(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
			if(audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0)
			{
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
				
  			ContentValues value = new ContentValues();
				value.put(dbmanager.Constants.COLUMN_ALERT_FIRING, 1);
				getContentResolver().update(AlertTempDatabaseProvider.TABLE_URI,
						value, whereClause, null);
			}	
		} catch(IOException e)
		{
			Log.i("AlarmReceiver", "No audio files are found!");
		}
	}
	
	//*******************************************************
	//** This method gets the sound from the device.       **
	//*******************************************************
	private Uri getAlarmUri()
	{
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
		if(alert == null)
		{
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			if(alert == null)
			{
				alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			}
		}
		return alert;
	}
	
	protected void onStop() 
	{
		if(mWakeLock.isHeld())
			mWakeLock.release();				
	
		super.onStop();
	}
	
	protected void onPause()
	{
		if(mWakeLock.isHeld())
			mWakeLock.release();
		super.onPause();
	}
}
