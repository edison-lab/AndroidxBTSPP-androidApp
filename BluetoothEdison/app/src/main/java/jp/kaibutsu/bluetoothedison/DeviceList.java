package jp.kaibutsu.bluetoothedison;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceList extends Activity
        implements AdapterView.OnItemClickListener {

    private final static String BR = System.getProperty("line.separator");
    private final static int WC = LinearLayout.LayoutParams.WRAP_CONTENT;
    private final static int MP = LinearLayout.LayoutParams.MATCH_PARENT;

    private BluetoothAdapter btAdapter;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setResult(Activity.RESULT_CANCELED);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        adapter = new ArrayAdapter<String>(this, R.layout.list_items);

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(MP, WC));
        listView.setAdapter(adapter);
        layout.addView(listView);
        listView.setOnItemClickListener(this);

        IntentFilter filter;
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();//(1)
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device.getName() + BR + device.getAddress());
            }
        }
        if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (btAdapter != null) btAdapter.cancelDiscovery();
        this.unregisterReceiver(receiver);

    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        btAdapter.cancelDiscovery();

        String info = ((TextView) view).getText().toString();
        String address = info.substring(info.length() - 17);
        Intent intent = new Intent();
        intent.putExtra("device_address", address);
        setResult(Activity.RESULT_OK, intent);
        finish();

    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.
                        getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapter.add(device.getName() + BR + device.getAddress());
                }
            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            }
        }

    };
}