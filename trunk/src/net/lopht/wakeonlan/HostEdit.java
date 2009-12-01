package net.lopht.wakeonlan;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class HostEdit extends Activity {
	private EditText mHostname, mIP, mMAC;
	private Long mRowID;
	private HostDbAdapter mDbAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_edit);
		
		/* Get the database */
		mDbAdapter = new HostDbAdapter(this);
		mDbAdapter.open();
		
		/* Get the form fields */
		mHostname = (EditText) findViewById(R.id.hostname);
		mIP = (EditText) findViewById(R.id.ip);
		mMAC = (EditText) findViewById(R.id.mac);
		
		/* Saved state? */
		mRowID = savedInstanceState != null ?
				savedInstanceState.getLong(HostDbAdapter.KEY_ROWID):
				null;

		/* No saved state */
        if (mRowID == null) {
			Bundle extras = getIntent().getExtras();            
			mRowID = extras != null ? extras.getLong(HostDbAdapter.KEY_ROWID) : null;
		}
        
        /* Populate the fields with existing data, if any */
        updateFields();
		        
		/* Get the button and setup a listener for it */
		Button confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
                setResult(RESULT_OK);
                finish();				
			}
		});
	}
	
	private void updateFields () {
		if (mRowID != null) {
			Cursor host = mDbAdapter.fetchHost(mRowID);
			startManagingCursor(host);
			mHostname.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_HOSTNAME)));
			mIP.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_IP)));
			mMAC.setText(host.getString(host.getColumnIndexOrThrow(HostDbAdapter.KEY_MAC)));
		}
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
    
    private void saveState() {
        String hostname = mHostname.getText().toString();
        String ip = mIP.getText().toString();
        String mac = mMAC.getText().toString();

        if (mRowID == null) {
            long id = mDbAdapter.createHost(hostname, ip, mac);
            if (id > 0) {
                mRowID = id;
            }
        } else {
            mDbAdapter.updateHost(mRowID, hostname, ip, mac);
        }
    }
}
