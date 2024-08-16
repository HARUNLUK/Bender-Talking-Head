package com.example.benderbluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothApp";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> pairedDevicesList;

    private ImageButton updateButton, speakBtn;
    private ImageButton characterImageButton;

    private BluetoothHelper bluetoothHelper;
    private OpenAIClient openAIClient;

    private final ActivityResultLauncher<Intent> voiceInputLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> texts = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (texts != null && !texts.isEmpty()) {
                        String text = texts.get(0);
                        Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();

                        openAIClient.addMessageToHistory("user", text);
                        openAIClient.makePostRequest(new OpenAIClient.ReplyCallback() {
                            @Override
                            public void onReplyFound(String replyCode) {
                                bluetoothHelper.sendMessage(replyCode);
                                openAIClient.addMessageToHistory("assistant", replyCode);
                            }

                            @Override
                            public void onError() {
                                // Handle error
                            }
                        });
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateButton = findViewById(R.id.updateButton);
        characterImageButton = findViewById(R.id.characterImageButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bluetoothHelper = new BluetoothHelper(this, bluetoothAdapter);
        openAIClient = new OpenAIClient();


//        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
//            BluetoothDevice selectedDevice = bluetoothHelper.getPairedDevicesList().get(position);
//            bluetoothHelper.connectToDevice(selectedDevice);
//        });

//        sendButton.setOnClickListener(v -> {
//            String message = editText.getText().toString();
//            bluetoothHelper.sendMessage(message);
//        });

        updateButton.setOnClickListener(v -> {

            boolean isConnected = bluetoothHelper.connectToBender();
            if (isConnected) {
                updateButton.setBackgroundColor(Color.GREEN);
                Toast.makeText(this, " connected", Toast.LENGTH_SHORT).show();

            } else {
                updateButton.setBackgroundColor(Color.RED);
                Toast.makeText(this, " not connected yet", Toast.LENGTH_SHORT).show();

            }
        });


        characterImageButton.setOnClickListener(v -> voiceInput());

        // İzin kontrolü ve isteme
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 1);
        }
    }

    private void voiceInput() {
        String language = "tr"; // Language code, e.g., "en" for English, "ur" for Urdu, "hi" for Hindi, etc.

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");

        try {
            voiceInputLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//    private void sendMessage(String message) {
//        try {
//            if (outputStream != null) {
//                outputStream.write(message.getBytes());
//                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Output stream is null", Toast.LENGTH_SHORT).show();
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "Error sending message", e);
//            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothHelper.destroy();
    }
}
