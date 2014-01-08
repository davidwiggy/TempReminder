package dbmanager;

import java.io.File;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//*********************************************************************************************
//** Class:      DataBaseHelper                                                              **
//** Programmer: Timothy David Wiggins                                                       **
//** PURPOSE:    This is the DataBaseHelper class that extends SQLiteOpenHelper.             **
//*********************************************************************************************
public class DataBaseHelper extends SQLiteOpenHelper
{
	private SQLiteDatabase myDataBase;
	private final Context myContext;
	String pathToDB = Constants.DATABASE_PATH + "/" + Constants.DATABASE_NAME;
	
	
	public DataBaseHelper(Context context)
	{
		super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
		this.myContext = context;
	}
			
	
	//*******************************************************
	//** This method creates the database if the database  **
	//** does not exist.                                   **
	//*******************************************************
	public SQLiteDatabase createDatabase() 
	{
		boolean dbExists = false;
		dbExists = checkIfDataBaseExists();
		myDataBase = getWritableDatabase();
		
		if(dbExists == false)
		{
			myDataBase.execSQL(Constants.DATABASE_CREATE);
		}		
		return myDataBase;
	}//End of createDatabase

	//*******************************************************
	//** This method checks to see if the database exists. **
	//*******************************************************
	private boolean checkIfDataBaseExists() 
	{
		Log.d("In checkDatabaseExists", "Checking Database");
		
		//Checking to see if there is a database in the path
		File dbFile = myContext.getDatabasePath(Constants.DATABASE_NAME);
		return dbFile.exists();
	}//End of checkIfDataBaseExists


	//On Create for the DatabaseHelper class
	@Override
	public void onCreate(SQLiteDatabase db)
	{

	}//End of onCreate
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		throw new UnsupportedOperationException();
	}//End of onUpgrade
		
}//End of Class







