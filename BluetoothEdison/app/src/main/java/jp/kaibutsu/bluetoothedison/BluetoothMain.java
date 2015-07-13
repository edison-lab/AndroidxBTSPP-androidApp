package jp.kaibutsu.bluetoothedison;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BluetoothMain extends AppCompatActivity {

    private static final int RQ_CONNECT_DEVICE = 1;
    private static final int RQ_ENABLE_BT = 2;

    private BluetoothAdapter btAdapter;
    private ClientManager clientManager;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.activity_bluetooth_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        clientManager = new ClientManager(this);

    }

    @Override
    public void onStart() {
        super.onStart();
        if (!btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, RQ_ENABLE_BT);
        }
    }

    private void ensureDiscoverable() {
        if (btAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK) {
                String address = data.getExtras().getString("device_address");
                clientManager.connect(btAdapter.getRemoteDevice(address));
            }
        }
        else if (requestCode == RQ_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) {
                android.util.Log.e("", "Bluetoothが有効ではありません");
            }
        }
    }

    public void connectDevice(View view) {
        Intent intent = new Intent(this, DeviceList.class);
        startActivityForResult(intent, RQ_CONNECT_DEVICE);
    }

    public void disconnectDevice(View view) {
        clientManager.stop();
    }

    public void sendSound(View view) {
        try {
            switch (view.getId()) {
                case R.id.C:
                    clientManager.write("c".getBytes());
                    break;
                case R.id.D:
                    clientManager.write("d".getBytes());
                    break;
                case R.id.E:
                    clientManager.write("e".getBytes());
                    break;
                case R.id.F:
                    clientManager.write("f".getBytes());
                    break;
                case R.id.G:
                    clientManager.write("g".getBytes());
                    break;
            }
        } catch (Exception e) {
        }
    }

}