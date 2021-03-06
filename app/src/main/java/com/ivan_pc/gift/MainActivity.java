package com.ivan_pc.gift;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TASK_NUM_KEY = "task_key";
    public static final String PENDING_INTENT_KEY = "pending_key";
    public static final String PATH_KEY = "path_key";
    public static final String ERROR_KEY = "error_key";
    public static final String NOT_IMPLEMENTED = "~~~~bad~~~~response~~~!!";
    public static final String DESCRIPTION_KEY = "description_key";
    public static final String IS_FIRST_KEY = "is_first";


    private static final String SAVED_TASK = "saved_task";

    public static final int END_CODE = 0;
    public static final int ERROR_CODE = -1;
    private static final int GROUP_ID = 39428023;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private int openedTask;
    private int lastTask;

    SharedPreferences prefs;
    Sender sender;

    TextView question, questNumber, errorReport;
    EditText answer;
    SimpleDraweeView draweeView;
    ScrollView scrollView;
    Button scrollRefreshButton, globalRefreshButton;
    ProgressBar progressBar;
    NavigationView navigationView;
    View fabButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!NotifierService.onWork) startService(new Intent(this, NotifierService.class));
        prefs = getPreferences(MODE_PRIVATE);
        sender = new Sender();
        //editor.putInt(SAVED_TASK, 1);
        //editor.apply();
        //clear();

        Fresco.initialize(this);




        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            openedTask = savedInstanceState.getInt(TASK_NUM_KEY, -1);
        }

        lastTask = prefs.getInt(SAVED_TASK, 1);
        if (openedTask <= 0 || openedTask > lastTask) {
            openedTask = lastTask;
        }

        question = (TextView) findViewById(R.id.question);
        answer = (EditText) findViewById(R.id.answer);
        draweeView = (SimpleDraweeView) findViewById(R.id.image);
        scrollView = (ScrollView) findViewById(R.id.displayScrollView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        scrollRefreshButton = (Button) findViewById(R.id.scrollRefreshButton);
        globalRefreshButton = (Button) findViewById(R.id.globalRefreshButton);
        questNumber = (TextView) findViewById(R.id.task_number);
        errorReport = (TextView) findViewById(R.id.errorReport);
        fabButton = findViewById(R.id.fabButton);

        //deleteFiles(task);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu navMenu = navigationView.getMenu();

        for (int i = 0; i < lastTask; ++i) {
            MenuItem item = navMenu.add(GROUP_ID, i + 1, i + 1, getString(R.string.task_with_number) + Integer.toString(i + 1));
            if (i + 1 < lastTask) {
                item.setIcon(R.drawable.ic_check_circle_black);
            }
            else {
                item.setIcon(R.drawable.ic_lock_open_black);
            }
        }

        navigationView.setNavigationItemSelectedListener(this);

        openTask(openedTask, false);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(TASK_NUM_KEY, openedTask);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



    void clear() {
        String path = getFilesDir().toString() + "/";
        for (int i = 1; i < 10; ++i) {
            String cpath = path + Integer.toString(i) + "/";

            new File(cpath + getString(R.string.photo_name)).delete();
            new File(cpath + getString(R.string.answer)).delete();
            new File(cpath + getString(R.string.question)).delete();
            new File(cpath + getString(R.string.description)).delete();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        openTask(id, false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void openTask(int taskNumber, boolean isFirst) {
        toStandbyMode();
        openedTask = taskNumber;
        String path = getFilesDir() + "/" + Integer.toString(taskNumber) + "/";
        Intent intent;
        PendingIntent pendingIntent = createPendingResult(taskNumber, new Intent(), 0);
        intent = new Intent(this, LoaderService.class).putExtra(PATH_KEY, path).
                putExtra(PENDING_INTENT_KEY, pendingIntent)
                .putExtra(TASK_NUM_KEY, taskNumber)
                .putExtra(IS_FIRST_KEY, isFirst);
        startService(intent);
    }

    void deleteFiles(int taskNumber) {
        String path = getFilesDir() + "/" + Integer.toString(taskNumber) + "/";
        new File(path + getString(R.string.question)).delete();
        new File(path + getString(R.string.photo_name)).delete();
        new File(path + getString(R.string.answer)).delete();
        new File(path + getString(R.string.description)).delete();
    }

    void reloadTask(int taskNumber) {
        deleteFiles(taskNumber);
        openTask(taskNumber, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == openedTask && resultCode == END_CODE) {
            stopService(new Intent(this, RunnableTaskDownloader.class));
            toDisplayMode(openedTask, data.getBooleanExtra(IS_FIRST_KEY, false));
        }

        if (resultCode == ERROR_CODE) {
            toErrorMode(data.getStringExtra(ERROR_KEY));
        }
    }

    void toStandbyMode() {
        globalRefreshButton.setVisibility(View.INVISIBLE);
        errorReport.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        fabButton.setVisibility(View.INVISIBLE);
    }

    void toDisplayMode(int taskNumber, boolean isFirst) {
        errorReport.setVisibility(View.INVISIBLE);
        globalRefreshButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        answer.setText("");



        String path = getFilesDir() + "/" + Integer.toString(taskNumber) + "/";

        questNumber.setText(getString(R.string.task_with_number) + Integer.toString(taskNumber));
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(new File(path + getString(R.string.description))));
            String description = reader.readLine();
            if (description.equals("")) {
                toErrorMode(getString(R.string.description_error));
                return;
            }
            if (!description.equals(NOT_IMPLEMENTED)) {
                fabButton.setVisibility(View.VISIBLE);
                if (isFirst) {
                    Intent intent = new Intent(this, Main2Activity.class);
                    intent.putExtra(DESCRIPTION_KEY, description);
                    startActivity(intent);
                }

            }
        } catch (IOException e) {
            toErrorMode(getString(R.string.description_error));
            return;
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        try {
            reader = new BufferedReader(new FileReader(new File(path + getString(R.string.question))));
            question.setText(reader.readLine());
        } catch (IOException e) {
            toErrorMode(getString(R.string.question_error));
            return;
        } finally {
            try { reader.close(); } catch (IOException e) { e.printStackTrace(); }
        }
        scrollView.setVisibility(View.VISIBLE);
        draweeView.setImageURI(Uri.fromFile(new File(path + getString(R.string.photo_name))));
    }
    void toErrorMode(String report) {
        errorReport.setText(report);
        errorReport.setVisibility(View.VISIBLE);
        globalRefreshButton.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        fabButton.setVisibility(View.INVISIBLE);
    }

    public void commitAnswer(View view) {
        String path = getFilesDir() + "/" + Integer.toString(openedTask) + "/";
        BufferedReader reader = null;
        String rightAnswer;
        StringBuilder tmp = new StringBuilder();
        try {
            reader = new BufferedReader(new FileReader(new File(path + getString(R.string.answer))));
            String current;
            while ((current = reader.readLine()) != null) {
                tmp.append(current);
            }
            rightAnswer = tmp.toString();
        } catch (IOException e) {
            rightAnswer = null;
        } finally {
            try { if (reader != null) reader.close(); } catch (IOException e) { e.printStackTrace(); }
        }

        if (rightAnswer == null || rightAnswer.equals("")) {
            toErrorMode("При скачивании правильного ответа произошла ошибка, перезагрузите");
            return;
        }

        String cans = answer.getText().toString();
        cans = rebuildString(cans);
        rightAnswer = rebuildString(rightAnswer);

        Log.d(LOG_TAG, "RIGHT_ANSWER: " + rightAnswer);
        Log.d(LOG_TAG, "ANSWER: " + cans);



        if (cans.equalsIgnoreCase(rightAnswer)) {
            SharedPreferences.Editor editor = prefs.edit();
            ++openedTask;
            boolean isFirst = false;
            if (openedTask > lastTask) {
                lastTask = openedTask;
                editor.putInt(SAVED_TASK, openedTask);
                editor.apply();


                sender.send("SolveTaskNumber:" + Integer.toString(lastTask - 1));

                isFirst = true;
                Menu navMenu = navigationView.getMenu();
                MenuItem item = navMenu.add(GROUP_ID, lastTask, lastTask, getString(R.string.task_with_number) + Integer.toString(lastTask));
                item.setIcon(R.drawable.ic_lock_open_black);

                navMenu.getItem(lastTask - 2).setIcon(R.drawable.ic_check_circle_black);
            }



            Toast.makeText(this, getString(R.string.answered), Toast.LENGTH_SHORT).show();
            openTask(openedTask, isFirst);
        }
        else {
            Toast.makeText(this, getString(R.string.incorrect), Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, "SIZES right: " + Integer.toString(rightAnswer.length()) + " my " + Integer.toString(cans.length()));
            for (int i = 0; i < Math.min(cans.length(), rightAnswer.length()); ++i) {
                if (rightAnswer.charAt(i) != cans.charAt(i)) {
                    Log.d(LOG_TAG, "DIFFER AT: " + Integer.toString(i) + " " + cans.charAt(i) +  " " + rightAnswer.charAt(i));

                }
            }
        }
    }

    private static boolean isRussianLetter(char c) {
        return ((('а' <= c) && (c <= 'я')) || (('А' <= c) && (c <= 'Я')));
    }

    private static boolean isEnglishLetter(char c) {
        return ((('a' <= c) && (c <= 'z')) || (('A' <= c) && (c <= 'Z')));
    }

    private static boolean isNumber(char c) {
        return (('1' <= c) && (c <= '9'));
    }

    private static String rebuildString(String s) {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            if (isEnglishLetter(s.charAt(i)) || isRussianLetter(s.charAt(i)) || isNumber(s.charAt(i))) {
                tmp.append(s.charAt(i));
            }
        }
        return tmp.toString();
    }

    public void refresh(View view) {
        reloadTask(openedTask);
    }

    public void fabClick(View view) {
        String path = getFilesDir() + "/" + Integer.toString(openedTask) + "/";
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(new File(path + getString(R.string.description))));
            String description = reader.readLine();
            if (description.equals("")) {
                toErrorMode(getString(R.string.description_error));
                return;
            }
            if (!description.equals(NOT_IMPLEMENTED)) {
                Intent intent = new Intent(this, Main2Activity.class);
                intent.putExtra(DESCRIPTION_KEY, description);
                startActivity(intent);
            }
        } catch (IOException e) {
            toErrorMode(getString(R.string.description_error));
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}

