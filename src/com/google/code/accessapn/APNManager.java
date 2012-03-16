package com.google.code.accessapn;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

/**
 * Manager for operating APN data on the phone.
 * 
 * @author Chris. Z inspired by {@link http://blogs.msdn.com/b/zhengpei/archive/2009/10/13/managing-apn-data-in-google-android.aspx}
 * 
 */
public class APNManager
{
    private static final String TAG = APNManager.class.getSimpleName();
    private Context             mContext;
 

    /*
     * Information of all APNs Details can be found in com.android.providers.telephony.TelephonyProvider
     */
    public static final Uri   APN_TABLE_URI     =
                                                        Uri.parse( "content://telephony/carriers" );
    /*
     * Information of the preferred APN
     */
    public static final Uri   PREFERRED_APN_URI =
                                                        Uri.parse( "content://telephony/carriers/preferapn" );
    private static APNManager sInstance;

    /**
     * Singleton method
     * 
     * @param _context
     * @return
     */
    public static APNManager getInstance( Context _context )
    {
        if( sInstance == null )
        {
            synchronized( APNManager.class )
            {
                if( sInstance == null )
                {
                    sInstance = new APNManager( _context );
                }
            }
        }
        return sInstance;
    }

    private APNManager( Context _context )
    {
        mContext = _context;
    }

    /**
     * Set an apn to be the default apn for web traffic Require an input of the apn id to be set
     * 
     * @param _id
     *            APN id
     */
    public boolean setAPNDefault( int _id )
    {
        boolean res = false;
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();

        // See /etc/apns-conf.xml. The TelephonyProvider uses this file to provide
        // content://telephony/carriers/preferapn URI mapping
        values.put( "apn_id", _id );
        try
        {
            resolver.update( PREFERRED_APN_URI, values, null, null );
            Cursor c = resolver.query(
                    PREFERRED_APN_URI,
                    new String[]
                    { "name", "apn" },
                    "_id=" + _id,
                    null,
                    null );
            if( c != null )
            {
                res = true;
                c.close();
            }
        }
        catch( SQLException e )
        {
            Log.d( TAG, e.getMessage() );
        }
        return res;
    }
 
    
    

    /**
     * Insert a new APN entry into the system APN table Require an apn name, and the apn address. More can be added. Return an id (_id) that is automatically generated for the new apn entry.
     * 
     * @param _name
     *            Name of APN
     * @param _accessAddress
     *            Address of APN to access Internet
     */
    public int insertAPN( String _name, String _accessAddress )
    {
        int id = -1;
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put( "name", _name );
        values.put( "apn", _accessAddress );

        /*
         * The following three field values are for testing in Android emulator only The APN setting page UI will ONLY display APNs whose 'numeric' 
         * filed is TelephonyProperties.PROPERTY_SIM_OPERATOR_NUMERIC. On Android emulator, this value is 310260, where
         * 310 is mcc, and 260 mnc. With these field values, the newly added apn will appear in system UI.
         */
        values.put( "mcc", "310" );
        values.put( "mnc", "260" );
        values.put( "numeric", "310260" );

        Cursor c = null;
        try
        {
            Uri newRow = resolver.insert( APN_TABLE_URI, values );
            if( newRow != null )
            {
                c = resolver.query( newRow, null, null, null, null );
                Log.d( TAG, "Newly added APN:" );
                printAllData( c ); // Print the entire result set

                // Obtain the apn id
                int idindex = c.getColumnIndex( "_id" );
                c.moveToFirst();
                id = c.getShort( idindex );
                Log.d( TAG, "New ID: " + id + ": Inserting new APN succeeded!" );
            }
        }
        catch( SQLException e )
        {
            Log.d( TAG, e.getMessage() );
        }

        if( c != null )
            c.close();
        return id;
    }
    
    /**
     * Update APN
     * @param _name
     * @param _newAddress
     * @return
     */
    public int updateAPN( String _name, String _newAddress )
    {
        int id = -1;
        ContentResolver resolver = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put( "name", _name );
        values.put( "apn", _newAddress );

        /*
         * The following three field values are for testing in Android emulator only The APN setting page UI will ONLY display APNs whose 'numeric' 
         * filed is TelephonyProperties.PROPERTY_SIM_OPERATOR_NUMERIC. On Android emulator, this value is 310260, where
         * 310 is mcc, and 260 mnc. With these field values, the newly added apn will appear in system UI.
         */
//        values.put( "mcc", "310" );
//        values.put( "mnc", "260" );
//        values.put( "numeric", "310260" );
 
        try
        { 
            int updatedRows = resolver.update( APN_TABLE_URI, values, "name='" + _name + "'", null );
            if( updatedRows > 0 )
            { 
                Log.d( TAG, "Updated APN:" + _name + ", New Address:" + _newAddress );
            }
        }
        catch( SQLException e )
        {
            Log.d( TAG, e.getMessage() );
        }
 
        return id;
    }

    /**
     * Return all column names stored in the string array
     * 
     * @param _columnNames
     *            colums of APN table
     */
    private String getAllColumnNames( String[] _columnNames )
    {
        String s = "Column Names:\n";
        for( String t : _columnNames )
        {
            s += t + ":\t";
        }
        return s + "\n";
    }   

    /**
     * Print all data records associated with Cursor c. Return a string that contains all record data. For some weird reason, Android SDK Log class cannot print very long string message. Thus we have to log record-by-record.
     * 
     * @param _cursor
     *            Cursor on the APN table
     */
    private String printAllData( Cursor _cursor )
    {
        if( _cursor == null )
            return null;
        String s = "";
        int record_cnt = _cursor.getColumnCount();
        Log.d( TAG, "Total # of records: " + record_cnt );

        if( _cursor.moveToFirst() )
        {
            String[] columnNames = _cursor.getColumnNames();
            Log.d( TAG, getAllColumnNames( columnNames ) );
            s += getAllColumnNames( columnNames );
            do
            {
                String row = "";
                for( String columnIndex : columnNames )
                {
                    int i = _cursor.getColumnIndex( columnIndex );
                    row += _cursor.getString( i ) + ":\t";
                }
                row += "\n";
                Log.d( TAG, row );
                s += row;
            }
            while( _cursor.moveToNext() );
            Log.d( TAG, "End Of Records" );
        }
        return s;
    }
}
