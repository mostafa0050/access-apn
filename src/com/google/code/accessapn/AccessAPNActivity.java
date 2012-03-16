package com.google.code.accessapn;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class AccessAPNActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.main );
    }
    
    public void insertAPN( View _v )
    {
        APNManager apnMgr = APNManager.getInstance( getApplication() );
        apnMgr.insertAPN( "Cellular", "cellular.de" );
    }
    
    public void updateAPN( View _v )
    {
        APNManager apnMgr = APNManager.getInstance( getApplication() );
        apnMgr.updateAPN( "Cellular", "yahoo.de" );    
    }
}
