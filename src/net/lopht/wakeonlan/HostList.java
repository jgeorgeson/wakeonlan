package net.lopht.wakeonlan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class HostList extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT = 1;
	
	private static final int MENU_WAKE = Menu.FIRST;
	private static final int MENU_ADD = Menu.FIRST + 1;
	private static final int MENU_EDIT = Menu.FIRST + 2;
	private static final int MENU_DELETE = Menu.FIRST + 3;
	
	private HostDbAdapter mDbAdapter;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDbAdapter = new HostDbAdapter(this);
        mDbAdapter.open();
        updateList();
        registerForContextMenu(getListView());
    }
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0,MENU_WAKE,0,R.string.menu_wake);
		menu.add(0,MENU_EDIT,0,R.string.menu_edit);
		menu.add(0,MENU_DELETE,0,R.string.menu_delete);
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch(item.getItemId()) {
		case MENU_WAKE:
			wakeHost(info.id);
			return true;
		case MENU_EDIT:
	        Intent i = new Intent(this, HostEdit.class);
	        i.putExtra(HostDbAdapter.KEY_ROWID, info.id);
	        startActivityForResult(i, ACTIVITY_EDIT);
	        return true;
		case MENU_DELETE:
			mDbAdapter.deleteHost(info.id);
			updateList();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0,MENU_ADD,0,R.string.menu_add);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case MENU_ADD:
			Intent i = new Intent(this,HostEdit.class);
			startActivityForResult(i,ACTIVITY_CREATE);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		wakeHost(id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		updateList();
	}

	private void updateList () {
        // Get all of the rows from the database and create the item list
        Cursor hostsCursor = mDbAdapter.fetchAllHosts();
        startManagingCursor(hostsCursor);
        
        // Create an array to specify the fields we want to display in the list (only hostname)
        String[] from = new String[]{HostDbAdapter.KEY_HOSTNAME};
        
        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.host_row};
        
        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter hosts = new SimpleCursorAdapter(this, R.layout.host_row, hostsCursor, from, to);
        setListAdapter(hosts);
 	}
	
	private void wakeHost(long rowID) {
		// Retrieve the host info
        Cursor hostCursor = mDbAdapter.fetchHost(rowID);
        startManagingCursor(hostCursor);
		String ipStr = hostCursor.getString(
				hostCursor.getColumnIndexOrThrow(HostDbAdapter.KEY_IP));
		String macStr = hostCursor.getString(
				hostCursor.getColumnIndexOrThrow(HostDbAdapter.KEY_MAC));
		int port = Integer.parseInt(hostCursor.getString(
				hostCursor.getColumnIndexOrThrow(HostDbAdapter.KEY_PORT)));
		
		// Convert to two's complement byte array's
		// Convert the MAC address into a byte array
		byte[] mac = toByteArray(macStr,":",16);

		// convert the IP address into a byte array
		byte[] ip = toByteArray(ipStr,"[.]",10);

		// Construct the magic packet
		byte[] magicPacket = new byte[102];
		magicPacket[0] = -1;
		magicPacket[1] = -1;
		magicPacket[2] = -1;
		magicPacket[3] = -1;
		magicPacket[4] = -1;
		magicPacket[5] = -1;
		System.arraycopy(mac, 0, magicPacket, 6, 6);
		System.arraycopy(mac, 0, magicPacket, 12, 6);		
		System.arraycopy(mac, 0, magicPacket, 18, 6);		
		System.arraycopy(mac, 0, magicPacket, 24, 6);		
		System.arraycopy(mac, 0, magicPacket, 30, 6);		
		System.arraycopy(mac, 0, magicPacket, 36, 6);		
		System.arraycopy(mac, 0, magicPacket, 42, 6);		
		System.arraycopy(mac, 0, magicPacket, 48, 6);		
		System.arraycopy(mac, 0, magicPacket, 54, 6);		
		System.arraycopy(mac, 0, magicPacket, 60, 6);		
		System.arraycopy(mac, 0, magicPacket, 66, 6);		
		System.arraycopy(mac, 0, magicPacket, 72, 6);		
		System.arraycopy(mac, 0, magicPacket, 78, 6);		
		System.arraycopy(mac, 0, magicPacket, 84, 6);		
		System.arraycopy(mac, 0, magicPacket, 90, 6);		
		System.arraycopy(mac, 0, magicPacket, 96, 6);		

		// Send the packet
		try {
			InetAddress network = InetAddress.getByAddress(ip);
			DatagramPacket packet = new DatagramPacket(magicPacket, magicPacket.length, network, port);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);			
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}

	private static byte[] toByteArray (String source, String exp, int radix){
		String[] str = source.split(exp);
		byte[] bytes = new byte[str.length];
		for (int i = 0; i < str.length; i++) {
			int x = Integer.parseInt(str[i],radix);
			bytes[i] = x > 127 ? (byte) (x - 256) : (byte) x;
		}
		return bytes;
	}
}