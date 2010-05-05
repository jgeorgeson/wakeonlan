package net.lopht.wakeonlan;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class HostEdit extends Activity {
	private EditText mHostname, mIP, mMAC, mPort;
	private Long mRowID;
	private HostDbAdapter mDbAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_edit);
		
		// Get the database
		mDbAdapter = new HostDbAdapter(this);
		mDbAdapter.open();
		
		// Get the form fields
		mHostname = (EditText) findViewById(R.id.hostname);
		mMAC = (EditText) findViewById(R.id.mac);
		mIP = (EditText) findViewById(R.id.ip);
		mPort = (EditText) findViewById(R.id.port);
		
		// Saved state?
		mRowID = savedInstanceState != null ?
				savedInstanceState.getLong(HostDbAdapter.KEY_ROWID):
				null;

		// No saved state
        if (mRowID == null) {
			Bundle extras = getIntent().getExtras();            
			mRowID = extras != null ? extras.getLong(HostDbAdapter.KEY_ROWID) : null;
		}
        
		// Get the button and setup a listener for it
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
                setResult(RESULT_OK);
                finish();				
			}
		});
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(HostDbAdapter.KEY_ROWID, mRowID);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateFields();
    }
	
    /**
     * Retrieve the values from the database to populate the text fields
     */
	private void updateFields () {
		if (mRowID != null) {
			Cursor host = mDbAdapter.fetchHost(mRowID);
			startManagingCursor(host);
			mHostname.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_HOSTNAME)));
			mMAC.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_MAC)));
			mIP.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_IP)));
			mPort.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_PORT)));
		}
	}
    
	/**
	 * Get the input from the text fields and write them to the database, either updating
	 * and existing row or creating a new one as needed.
	 */
    private void saveState() {
        String hostname = mHostname.getText().toString();
        String mac = mMAC.getText().toString();
        String ip = mIP.getText().toString();
        String port = mPort.getText().toString();

        if (mRowID == null) {
            if (hostname.length() != 0 && mac.length() != 0 && ip.length() != 0 && port.length() != 0) {
	            long id = mDbAdapter.createHost(hostname, mac, ip, port);
	            if (id > 0) {
	                mRowID = id;
	            }
            }
        } else {
            mDbAdapter.updateHost(mRowID, hostname, mac, ip, port);
        }
    }
}
