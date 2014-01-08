package dbmanager;

//*********************************************************************************************
//** Class:      Constants                                                                   **
//** Programmer: Timothy David Wiggins                                                       **
//** PURPOSE:    This class holds Constants that are used through out the program.           **           
//*********************************************************************************************

public class Constants {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Alertdb";
	public static final String TABLE_NAME = "AlertTable";
	public static final String DATABASE_PATH = "data/data/com.example.tempreminder/databases";
	public static final String _id = "_id";
	public static final String COLUMN_ZIP = "Zip";
	public static final String COLUMN_HOUR = "Hour";
	public static final String COLUMN_MINUTE = "Minute";
	public static final String COLUMN_ALERT_TEMP = "AlertTemp";
	public static final String COLUMN_ALERT_SET = "AlertSet";
	public static final String COLUMN_NEW_LOW = "NewLow";
	public static final String COLUMN_ALERT_FIRING = "AlertFiring";
	public static final String COLUMN_PROJECTED_LOW = "ProjectedLow";
	
	
	public static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
						+ TABLE_NAME           + " (" + _id + " integer primary key autoincrement, "
						+ COLUMN_ZIP           + " integer, " 
						+ COLUMN_HOUR          + " integer, "
						+ COLUMN_MINUTE        + " integer, "
						+ COLUMN_ALERT_TEMP    + " integer, "
						+ COLUMN_ALERT_SET     + " integer, "
						+ COLUMN_NEW_LOW       + " integer, "
						+ COLUMN_ALERT_FIRING  + " integer, "
						+ COLUMN_PROJECTED_LOW + " integer);";
	
}//End of class