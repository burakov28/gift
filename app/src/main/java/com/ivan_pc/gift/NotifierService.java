package com.ivan_pc.gift;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.icu.util.TimeUnit;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;

public class NotifierService extends Service {
    private static final String LOG_TAG = NotifierService.class.getSimpleName();
    public static boolean onWork;
    public NotifierService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onWork = true;
        new Thread(new RunnableChecker(this)).start();
        onWork = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onWork = false;
    }


    class RunnableChecker implements Runnable {
        Context context;

        RunnableChecker(Context context) {
            this.context = context;
        }
        @Override
        public void run() {
            String toStr = getFilesDir().toString();
            int taskNumber = 1;
            while (new File(toStr + "/" + Integer.toString(taskNumber)).exists()) ++taskNumber;
            --taskNumber;
            File to = new File(toStr + "/tmp");
            String from = RunnableTaskDownloader.DOWNLOAD_URL + Integer.toString(taskNumber) + "/" + getString(R.string.answer);

            RunnableTaskDownloader downloader = Utils.initDownloader(to, from);
            while (true) {
                try {
                    to.delete();
                    String current = Utils.readStringFromFile(new File(toStr + "/" + Integer.toString(taskNumber) + "/" + getString(R.string.answer)));
                    if (current.equals(MainActivity.NOT_IMPLEMENTED)) {
                        downloader.downloadToFile(to, from);
                        Utils.readStringFromFile(to);
                        if (!to.toString().equals(MainActivity.NOT_IMPLEMENTED)) {
                            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            Intent intent = new Intent(context, MainActivity.class);
                            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            Notification notif = new Notification.Builder(context)
                                    .setContentIntent(pIntent)
                                    .setContentTitle("Новая задачка")
                                    .setContentText("Пора скачать и решить новую задачку")
                                    .setSmallIcon(R.drawable.ic_card_giftcard_black)
                                    .setWhen(System.currentTimeMillis())
                                    .build();
                            notif.flags |= Notification.FLAG_AUTO_CANCEL;
                            nm.notify(0, notif);
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.d(LOG_TAG, "Can't load answer");
                }
                try {
                    Thread.sleep(60 * 1000);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, "Error on stop");
                    break;
                }

            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
