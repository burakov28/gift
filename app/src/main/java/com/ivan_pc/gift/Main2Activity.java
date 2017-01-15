package com.ivan_pc.gift;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        TextView tv = (TextView) findViewById(R.id.textView3);
        tv.setText(intent.getStringExtra(MainActivity.DESCRIPTION_KEY));
        setContentView(R.layout.activity_main2);
    }
}
