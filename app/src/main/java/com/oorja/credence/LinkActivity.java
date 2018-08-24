package com.oorja.credence;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class LinkActivity extends AppCompatActivity {

    private static final String TAG = "LinkActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);

        Intent in = getIntent();
        Uri data = in.getData();
        Log.d(TAG, "uri: " + data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
