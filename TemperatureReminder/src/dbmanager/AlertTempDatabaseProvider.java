package dbmanager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

//*********************************************************************************************
//** Class:      AlertTempDatabaseProvider                                                   **
//** Programmer: Timothy David Wiggins                                                       **
//** PURPOSE:    This class is the AlertTempDatabaseProvider, it extends ContentProvider.    **
//** PURPOSE:    This class contians the CRUD methods for access to the database information.**                                           
//*********************************************************************************************
public class AlertTempDatabaseProvider extends ContentProvider 
{
	private static String AUTHORITY = "com.AlertTempDatabaseProvider";
	public static final Uri TABLE_URI = Uri.parse("content://" + AUTHORITY +
													"/" + Constants.TABLE_NAME);
	public static final Uri ONE_REC_URI = Uri.parse("content://" + AUTHORITY +
													 "/" + Constants.TABLE_NAME + "/row");
	
	private DataBaseHelper myHelper;
	private SQLiteDatabase myDB;
	
	private static final int ALERT_LIST = 0;
	private static final int ALERT_RECORD = 1;
	private static final UriMatcher myURIMatcher = buildUriMatcher();
	
	static UriMatcher buildUriMatcher()
	{
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		matcher.addURI(AUTHORITY, Constants.TABLE_NAME, ALERT_LIST);
		matcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/row", ALERT_RECORD);
		
		return matcher;
	}//End of buildUriMatcher

	@Override
	public boolean onCreate() 
	{ 
		return true;
	}//End of onCreate

	@Override
	public String getType(Uri uri)
	{
		return null;
	}//End of getType
	
	//*******************************************************
	//** This method is the query method for access to the **
	//** information in the database.                      **
	//*******************************************************
	@Override
	public Cursor query(Uri uri, String[] projectionIn, String Selection,
						String[] selectionArgs, String sortOrder)
	{
				Log.d("IN QUERY", "");
		if(myHelper == null)
			initializeDB();

		Cursor myCursor = null;
		switch (myURIMatcher.match(uri))
		{
			case ALERT_LIST:
				myCursor = myDB.query(Constants.TABLE_NAME, projectionIn, Selection, selectionArgs, null, null, null);
				break;
			case ALERT_RECORD:
				myCursor = myDB.query(Constants.TABLE_NAME,
						              projectionIn, 
						              Constants._id, 
						              selectionArgs, 
						              null, null, null, null);
				if(myCursor != null && myCursor.getCount() > 0)
					myCursor.moveToFirst();
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri:   " + uri);
		}//End of Switch Statement
		
		//Update the cursor and report changes
		myCursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return myCursor;
	}//End of query
	
	//*******************************************************
	//** This method is the delete method to delete any    **
	//** information in the database.                      **
	//*******************************************************
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		if(myHelper == null)
			initializeDB();
		
		int count = myDB.delete(Constants.TABLE_NAME, selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}//End of Delete 
	
	//*******************************************************
	//** This method is the insert method to insert        **
	//** information into the database.                    **
	//*******************************************************
	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		if (myHelper == null)
			initializeDB();
		
		long id = 0;
		values.remove(Constants._id);

		try
		{
			id = myDB.insertOrThrow(Constants.TABLE_NAME, null, values);
		}
		catch (SQLException ex)
		{
			Log.d("AlertDatabase", "Unable to insert record. "+ ex.getStackTrace());
		}

		if (id >= 0)
		{
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return ContentUris.withAppendedId(uri, id);
	}//END insert
	
	//*******************************************************
	//** This method is the update method to update the    **
	//** information in the database.                      **
	//*******************************************************
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
	{
		if (myHelper == null)
			initializeDB();
		
		int count = myDB.update(Constants.TABLE_NAME, values, 
								selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
	
		return count;
	}//END update
	
	//*******************************************************
	//** This method is to initialize the database.        **
	//*******************************************************
	public void initializeDB()
	{
		//First, instantiate the DB helper, then create the DB.
		myHelper = new DataBaseHelper(getContext());
		try
		{
			myDB = myHelper.createDatabase();
		}
		catch (Exception e)
		{
			Log.d("In ContentProvider", "Call to createDatabase failed");
			e.printStackTrace();
		}
	}//END DB initialization


}
