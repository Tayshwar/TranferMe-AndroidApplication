package com.example.user.bluetoothtowifi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Atraya Mukherjee 1001144456
 * Raveena Jadhav 1000833967
 */

public class Receiver extends AsyncTask<Void,Void,Void> {
    private ServerSocket serverSocket;
    private boolean xceptionFlag = false;

    private Context context;
    private Activity activity;

    ProgressDialog mProgressDialog;

    Receiver(Context context, Activity activity){
        //initprogress dialog
        mProgressDialog = ProgressDialog.show(context,"Receiver"
                ,"Waiting for connection...",true);
        this.context = context;
        this.activity = activity;

    }

    @Override
    protected Void doInBackground(Void... voids) { //executed AsyncTask in background thread

        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(5004));


            System.out.println("waiting");

            Socket socket = serverSocket.accept();

            System.out.println("CONNECTED");
            mProgressDialog.dismiss();

            DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

            // Number of files
            int numFiles = dis.readInt();

            ArrayList<File> files = new ArrayList<>(numFiles);
            System.out.println("Number of Files to be received: " + numFiles);

            ArrayList<Long> fileSize = new ArrayList<>(numFiles);


            for(int i = 0; i < numFiles ;i++){
                long size = dis.readLong();
                System.out.println(size);
                fileSize.add(size);
            }

            //read file names, add files to arraylist
            for(int i = 0; i< numFiles;i++){
                File file = new File(dis.readUTF());
                files.add(file);
            }
            int n = 0;
            byte[]buf = new byte[4092];

            for(int i = 0; i < files.size();i++){

                System.out.println("Receiving file: " + files.get(i).getName());

                // Get folder for our app and store received files, if it doesn't exist make new folder
                File folder = new File("/mnt/sdcard/Mobile Systems");
                if(!folder.exists()){
                    folder.mkdirs();
                }

                File file = new File(folder,files.get(i).getName());

                //read file
                FileOutputStream fos = null;

                fos = new FileOutputStream(file);
                while (fileSize.get(i) > 0 && (n = dis.read(buf, 0, (int)Math.min(buf.length, fileSize.get(i)))) != -1)
                {
                    fos.write(buf,0,n);
                    long x = fileSize.get(i);
                    x = x-n;
                    fileSize.set(i,x);
                }

                fos.close();
            }

            serverSocket.close();
            System.out.println("Server Socket closed");

        } catch (IOException e) {
            xceptionFlag = true;
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!xceptionFlag){
            Toast.makeText(context,"Files saved to /mnt/sdcard/Mobile Systems",Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(context,"Something went wrong.",Toast.LENGTH_LONG).show();
        }

    }
}
