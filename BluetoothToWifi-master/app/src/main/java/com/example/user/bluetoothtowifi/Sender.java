package com.example.user.bluetoothtowifi;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * Atraya Mukherjee 1001144456
 * Raveena Jadhav 1000833967
 */

public class Sender extends AsyncTask<Void,Void,Void> {
    private static final String TAG = "Sender";
    private static final int PORT = 5004;
    private Context context;

    private String destinationAddress;
    private String[] selectedFiles;
    private Socket socket;

    private boolean xceptionFlag = false;

    // the sender takes context, all the selected files and the destination address of the socket
    Sender(Context context, String[] selectedFiles, String destinationAddress) {
        this.context = context;
        this.selectedFiles = selectedFiles;
        this.destinationAddress = destinationAddress;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ArrayList<File> files = new ArrayList<>();

        for (String f: selectedFiles) {
           files.add(new File(f));
           Log.d(TAG, "File added: " + f);
        }

        try {
            // Creates a stream socket and connects it to the specified port number at the specified IP address.
            Log.d(TAG, "Opened socket to " + destinationAddress + " on port " + PORT);
            socket = new Socket(InetAddress.getByName(destinationAddress), PORT);

            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            // Number of files
            dos.writeInt(files.size());
            dos.flush();

            // File size
            for(int i = 0;i< files.size();i++){
                int file_size = Integer.parseInt(String.valueOf(files.get(i).length()));
                dos.writeLong(file_size);
                dos.flush();
            }

            // File name
            for(int i = 0 ; i < files.size();i++){
                dos.writeUTF(files.get(i).getName());
                dos.flush();
            }

            // Buffer for file writing
            int n = 0;
            byte[]buf = new byte[4092];

            for(int i = 0; i < files.size(); i++) {

                System.out.println(files.get(i).getName());
                //create new fileinputstream for each file
                FileInputStream fis = new FileInputStream(files.get(i));

                //write file to dos
                while((n =fis.read(buf)) != -1){
                    dos.write(buf,0,n);
                    dos.flush();

                }
            }
            dos.close();
        } catch (IOException e) {
            xceptionFlag = true;
            e.printStackTrace();
        }

        finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        System.out.println("Socket closed");
                        socket.close();
                    } catch (IOException e) {
                        xceptionFlag = true;
                    }
                }
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if(xceptionFlag){
            Toast.makeText(context,"Something went wrong.",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context,"Files sent to " + destinationAddress,Toast.LENGTH_LONG).show();
        }
    }


    protected void onPostExecuteagain() {

            Toast.makeText(context,"Files has been modified on " + destinationAddress,Toast.LENGTH_LONG).show();

    }
}
