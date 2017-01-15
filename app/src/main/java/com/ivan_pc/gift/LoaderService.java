package com.ivan_pc.gift;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.io.File;

public class LoaderService extends Service {
    public LoaderService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String path = intent.getStringExtra(MainActivity.PATH_KEY);
        int taskNumber = intent.getIntExtra(MainActivity.TASK_NUM_KEY, 0);
        boolean isFirst = intent.getBooleanExtra(MainActivity.IS_FIRST_KEY, false);
        PendingIntent pendingIntent = intent.getParcelableExtra(MainActivity.PENDING_INTENT_KEY);
        Thread th = new Thread(new RunnableTaskDownloader(path, taskNumber, isFirst, pendingIntent, this));
        th.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
