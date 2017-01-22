package com.ivan_pc.gift;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Ivan-PC on 02.01.2017.
 */

public class RunnableTaskDownloader implements Runnable {
    public static final int BLOCK_SIZE = 16 * 1024;
    private static final String LOG_TAG = RunnableTaskDownloader.class.getSimpleName();
    public static final String DOWNLOAD_URL = "https://raw.githubusercontent.com/burakov28/gift/master/tasks/";

    private final PendingIntent pendingIntent;
    private final String pathToSave;
    private final Context context;
    private final int taskNumber;
    private final boolean isFirst;

    RunnableTaskDownloader(String pathToSave, int taskNumber, boolean isFirst, PendingIntent pendingIntent, Context context) {
        this.pendingIntent = pendingIntent;
        this.pathToSave = pathToSave;
        this.context = context;
        this.taskNumber = taskNumber;
        this.isFirst = isFirst;
    }

    public void downloadToFile(File to, String from) throws LoadException {
        Log.d(LOG_TAG, "FROM: " + from);
        Log.d(LOG_TAG, "TO: " + to.toString());

        HttpURLConnection connection = null;
        InputStream downloadFrom = null;
        FileOutputStream downloadTo = null;
        URL url = null;

        try {
            url = new URL(from);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            downloadFrom = new BufferedInputStream(connection.getInputStream(), BLOCK_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
            if (connection != null) connection.disconnect();
            try {
                downloadFrom.close();
            } catch(IOException q) {
                q.printStackTrace();
            }
            throw new LoadException(context.getString(R.string.open_connection_error));
        }

        if (to.exists()) {
            to.delete();
        }

        try {
            to.createNewFile();
            downloadTo = new FileOutputStream(to, true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new LoadException(context.getString(R.string.file_system_error));
        }
        byte[] bytes = new byte[BLOCK_SIZE];
        int count;
        try {
            while ((count = downloadFrom.read(bytes, 0, BLOCK_SIZE)) != -1) {

                try {
                    downloadTo.write(bytes, 0, count);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new LoadException(context.getString(R.string.file_system_error));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new LoadException(context.getString(R.string.error_while_downloading));
        } finally {
            connection.disconnect();
        }
        try {
            downloadTo.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new LoadException(context.getString(R.string.file_system_error));
        }
    }

    private boolean checkFiles() {
        if ((new File(pathToSave + context.getString(R.string.photo_name)).exists()) &&
                (new File(pathToSave + context.getString(R.string.question)).exists()) &&
                (new File(pathToSave + context.getString(R.string.answer)).exists()) &&
                (new File(pathToSave + context.getString(R.string.description))).exists()) {
            BufferedReader reader = null;
            String rightAnswer;
            try {
                reader = new BufferedReader(new FileReader(new File(pathToSave + context.getString(R.string.answer))));
                rightAnswer = reader.readLine();
                return !(rightAnswer.equalsIgnoreCase(MainActivity.NOT_IMPLEMENTED));
            } catch (IOException e) {
                return false;
            } finally {
                try { if (reader != null) reader.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }
        return false;
    }

    @Override
    public void run() {

        new File(pathToSave).mkdirs();
        if (!checkFiles()) {
            try {
                downloadToFile(new File(pathToSave + context.getString(R.string.photo_name)),
                        DOWNLOAD_URL + Integer.toString(taskNumber) + "/" + context.getString(R.string.photo_name));
                downloadToFile(new File(pathToSave + context.getString(R.string.question)),
                        DOWNLOAD_URL + Integer.toString(taskNumber) + "/" + context.getString(R.string.question));
                downloadToFile(new File(pathToSave + context.getString(R.string.answer)),
                        DOWNLOAD_URL + Integer.toString(taskNumber) + "/" + context.getString(R.string.answer));
                downloadToFile(new File(pathToSave + context.getString(R.string.description)),
                        DOWNLOAD_URL + Integer.toString(taskNumber) + "/" + context.getString(R.string.description));
            } catch (LoadException e) {
                try {
                    pendingIntent.send(context, MainActivity.ERROR_CODE, new Intent()
                            .putExtra(MainActivity.ERROR_KEY, e.getReason()));
                } catch (PendingIntent.CanceledException pe) {
                    pe.printStackTrace();
                }
                e.printStackTrace();
            }
        }

        try {
            Intent intent = new Intent().putExtra(MainActivity.IS_FIRST_KEY, isFirst);
            pendingIntent.send(context, MainActivity.END_CODE, intent);
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}