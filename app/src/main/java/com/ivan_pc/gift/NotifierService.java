package com.ivan_pc.gift;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NotifierService extends Service {
    public NotifierService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    String readStringFromFile(File source) {
        BufferedReader reader = null;
        StringBuilder tmp = null;
        try {
            reader = new BufferedReader(new FileReader(source));

            tmp = new StringBuilder();
            String newLine;
            while ((newLine = reader.readLine()) != null) {
                tmp.append(newLine);
            }

        } catch (IOException ignored) {

        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        if (tmp == null) return null;
        return tmp.toString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String path = getFilesDir().toString();
        int cur = 1;
        while (new File(path + "/" + Integer.toString(cur)).exists()) {
            ++cur;
        }
        --cur;


        String tmpPath = path;
        File tmpAnswer = new File(tmpPath + "/" + "tmpAnswer");


        File answer = new File("/" + Integer.toString(cur) + "/" + getString(R.string.answer));


        if (cur != 0) {
            RunnableTaskDownloader downloader = new RunnableTaskDownloader(tmpPath, cur, false, null, this);
            try {
                downloader.downloadToFile(tmpAnswer,
                        RunnableTaskDownloader.DOWNLOAD_URL + Integer.toString(cur) + "/" + getString(R.string.answer));
                if (answer.exists() && tmpAnswer.exists()) {
                    String was = readStringFromFile(answer);
                    String now = readStringFromFile(tmpAnswer);
                    if (was.equals(MainActivity.NOT_IMPLEMENTED) && !now.equals(MainActivity.NOT_IMPLEMENTED)) {

                    }
                }

            } catch (LoadException e) {

            } finally {
                tmpAnswer.delete();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
