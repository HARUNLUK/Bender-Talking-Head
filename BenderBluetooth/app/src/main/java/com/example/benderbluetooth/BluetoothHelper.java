package com.example.benderbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    private static final String TAG = "BluetoothHelper";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private ArrayList<BluetoothDevice> pairedDevicesList;
    private ArrayAdapter<String> deviceListAdapter;

    public BluetoothHelper(Context context, BluetoothAdapter bluetoothAdapter) {
        this.context = context;
        this.bluetoothAdapter = bluetoothAdapter;
        this.pairedDevicesList = new ArrayList<>();
        this.deviceListAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
//        deviceListView.setAdapter(deviceListAdapter);

    }

    public ArrayList<BluetoothDevice> getPairedDevicesList() {
        return pairedDevicesList;
    }

    public ArrayAdapter<String> getDeviceListAdapter() {
        return deviceListAdapter;
    }

    public void listPairedDevices() {
        pairedDevicesList.clear();
        deviceListAdapter.clear();

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device);
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            Toast.makeText(context, "No paired devices found", Toast.LENGTH_SHORT).show();
        }

        deviceListAdapter.notifyDataSetChanged();
    }

    public void connectToDevice(BluetoothDevice device) {
        try {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                return;
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(context, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "Error connecting to device", e);
            Toast.makeText(context, "Connection failed", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean connectToBender() {
        listPairedDevices();
        for (BluetoothDevice device : pairedDevicesList) {
            if (device.getName().toString().equalsIgnoreCase("Bender ")) {
                connectToDevice(device);
                return true;
            }
        }
        return false;
    }

    public void sendMessage(String message) {
        try {
            if (outputStream != null) {
                outputStream.write(message.getBytes());
                //Toast.makeText(context, "Message sent", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(context, "Output stream is null", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending message", e);
            Toast.makeText(context, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    public void destroy(){
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }
    }
}
