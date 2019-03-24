package com.example.user.bluetoothtowifi;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import android.os.Environment;
import android.provider.Settings;
import java.util.Timer ;
import java.util.TimerTask ;
/**
 * Atraya Mukherjee 1001144456
 * Raveena Jadhav 1000833967
 */

public class WifiActivity extends Activity{
    private static final String TAG = "WifiActivity";

    Button sendBtn;
    Button receiveBtn;
    ListView listView;
    TextView status;
    Button openWifi;
    Button modification;
    RecursiveFileObserver observer;
    Receiver receiver;
    Sender sender;


    public static final int EXTERNAL_READ_PERMISSION_GRANT=112;

    TextView incomingMessages;
    StringBuilder ipAddresses;

    BluetoothConnectionService mBluetoothConnection;
    String deviceIP;
    String incomingIP;

    InetAddress serverInetAddress = null;

    FilePickerDialog dialog;
    String destinationAddress;
    String[] selectedFiles;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_view);

       listView = (ListView) findViewById(R.id.fileList);
//       status = (TextView) findViewById(R.id.statusTV);
        openWifi = findViewById(R.id.startWifi);

        mBluetoothConnection = BluetoothConnectionService.getInstance();

        //Device IP address
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        deviceIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        Log.d(TAG, "IP: " + deviceIP);

        try {
            serverInetAddress = InetAddress.getByName(deviceIP);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        incomingMessages = (TextView) findViewById(R.id.IPaddress); //declaration
        ipAddresses = new StringBuilder();

        // Connecting device IP
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        // Implement runtime permissions
        if(!checkPermissionForReadExternalStorage()) {
            try {
                requestPermissionForReadExternalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(!checkPermissionForWriteExternalStorage()) {
            try {
                requestPermissionForWriteExternalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        sendBtn = (Button) findViewById(R.id.sendBtn);
        modification = (Button) findViewById(R.id.checkmodification);

        receiveBtn = (Button) findViewById(R.id.receiveBtn);
        receiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Receive Pressed");

                receiver = new Receiver(WifiActivity.this, WifiActivity.this);
                receiver.execute();

            }
        });

        openWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = deviceIP.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });


        //Declare the timer
        Timer t = new Timer();
//Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                      watchPathAndWriteFile();
                                  }

                              },
//Set how long before to start calling the TimerTask (in milliseconds)
                0,
//Set the amount of time between each execution (in milliseconds)
                1000);

        //you can cancel after a while

      modification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFiles(view );




            }
        });



    }

    public void watchPathAndWriteFile() {
        // the following path depends on your Android device. On my system, it is: "/storage/emulated/0/"
      //  String path = Environment.getExternalStorageDirectory().getPath() + "/storage/emulated/0/Mobile Systems";
        String path = Environment.getExternalStorageDirectory().getPath() + "/Mobile Systems";
       observer = new RecursiveFileObserver(path, this);

        // start watching the path
        observer.startWatching();



    }

    public void stopPath() {
        // the following path depends on your Android device. On my system, it is: "/storage/emulated/0/"
       // String path = Environment.getExternalStorageDirectory().getPath() + "/storage/emulated/0/Mobile Systems";
        String path = Environment.getExternalStorageDirectory().getPath() + "/Mobile Systems";
        observer = new RecursiveFileObserver(path, this);

        // start watching the path
        observer.stopWatching();
    }



    public void sendFiles(View view) {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.MULTI_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(DialogConfigs.DEFAULT_DIR);
        properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        properties.extensions = null;

//        status.setText("Shared Files");

        dialog = new FilePickerDialog(this, properties);
        dialog.setTitle("Select files to share");

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                if (null == files || files.length == 0) {
                    Toast.makeText(WifiActivity.this, "Select at least one file to start Share Mode", Toast.LENGTH_SHORT).show();
                    return;
                }

//                ArrayAdapter<String> filesAdapter =
//                        new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, Arrays.asList(files));
//                listView.setAdapter(filesAdapter);
                context=getApplicationContext();
                destinationAddress=incomingIP;
                selectedFiles=files;
                sender = new Sender(getApplicationContext(), files, incomingIP); // this is where the file is determined
                sender.execute();

            }
        });
        dialog.show(); // showing view
    }


    // Display received messages
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            incomingIP = intent.getStringExtra("theMessage");

            ipAddresses.append("Connecting to " + incomingIP + "\n");
            incomingMessages.setText(ipAddresses);

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onReceive: STATE OFF");
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            Log.d(TAG, "mReceiver unregistered");

        } catch (Exception e) {
            Log.d(TAG, "mReceiver not registered");
        }
    }

    // Check storage permissions
    public boolean checkPermissionForReadExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExternalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    EXTERNAL_READ_PERMISSION_GRANT);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkPermissionForWriteExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForWriteExternalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_READ_PERMISSION_GRANT);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
