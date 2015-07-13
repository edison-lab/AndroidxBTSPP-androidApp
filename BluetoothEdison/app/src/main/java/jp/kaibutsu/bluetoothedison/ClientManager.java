package jp.kaibutsu.bluetoothedison;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ClientManager {

    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private BluetoothMain parent;
    private Handler handler;
    private BluetoothAdapter btAdapter;
    private int state;
    private ConnectThread connectT;
    private ConnectedThread connectedT;

    public ClientManager(BluetoothMain parent) {
        this.parent = parent;
        this.handler = new Handler();
        this.btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.state = STATE_NONE;
    }

    private synchronized void setState(final int state) {
        this.state = state;
        handler.post(new Runnable() {
            public void run() {
                if (state == STATE_CONNECTED) {
                    parent.setTitle("BluetoothEdison - 接続完了");
                } else if (state == STATE_CONNECTING) {
                    parent.setTitle("BluetoothEdison - 接続中");
                } else if (state == STATE_LISTEN) {
                    parent.setTitle("BluetoothEdison - 接続待ち");
                } else if (state == STATE_NONE) {
                    parent.setTitle("BluetoothEdison - 未接続");
                }
            }
        });
    }

    public synchronized int getState() {
        return state;
    }

    public synchronized void connect(BluetoothDevice device) {

        if (connectT != null) {
            connectT.cancel();
            connectT = null;
        }
        if (connectT == null) {
            connectT = new ConnectThread(device);
            connectT.start();
            setState(STATE_CONNECTING);
        }
    }

    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (connectT != null) {
            connectT.cancel();
            connectT = null;
        }
        if (connectedT == null) {
            connectedT = new ConnectedThread(socket);
            connectedT.start();
            setState(STATE_CONNECTED);
        }
    }

    public synchronized void stop() {
        if (connectT != null) {
            connectT.cancel();
            connectT = null;
        }
        if (connectedT != null) {
            connectedT.cancel();
            connectedT = null;
        }
        setState(STATE_NONE);
    }

    public synchronized void write(byte[] out) {
        if (state != STATE_CONNECTED) return;
        connectedT.write(out);
    }

    private class ConnectThread extends Thread {

        private BluetoothDevice device;
        private BluetoothSocket socket;
        private boolean cancel;

        public ConnectThread(BluetoothDevice device) {
            try {
                this.device = device;
                this.socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (Exception e) {
                android.util.Log.e("debug", "ConnectThread>>" + e.getMessage());
                setState(STATE_NONE);
            }
        }

        public void run() {

            btAdapter.cancelDiscovery();
            try {
                socket.connect();
                connectT = null;
                connected(socket, device);
            } catch (Exception e) {
                try {
                    socket.close();
                } catch (Exception e2) {
                }
                setState(STATE_NONE);
            }
        }

        public void cancel() {

            cancel = true;
            try {
                socket.close();
            } catch (Exception e) {
            }

        }
    }

    private class ConnectedThread extends Thread {

        private BluetoothSocket socket;
        private OutputStream out;
        private InputStream in; //read
        private boolean cancel;

        public ConnectedThread(BluetoothSocket socket) {
            try {
                this.socket = socket;
                this.out = socket.getOutputStream();
                this.in     = socket.getInputStream(); //read

            } catch (Exception e) {
                android.util.Log.e("debug", "ConnectedThread>>" + e.getMessage());
                setState(STATE_NONE);
            }
        }

//       //read
//        public void run() {
//            byte[] buf = new byte[1024];
//            while (true) {
//                try {
//                    int size = in.read(buf); //read data
//                    final String str = new String(buf, 0, size);
//                    android.util.Log.e("",str);
//
//                } catch (Exception e) {
//                    //stop
//                    try {
//                        socket.close();
//                    } catch (Exception e2) {
//                    }
//
//                }
//            }
//        }


        public void write(byte[] buf) {

            try {
                out.write(buf);
            } catch (Exception e) {
            }

        }

        public void cancel() {

            cancel = true;
            try {
                socket.close();
            } catch (Exception e) {
            }

        }
    }
}