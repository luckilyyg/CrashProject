package com.crash;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private TextView tvCrashView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvCrashView = findViewById(R.id.tv_crash);
        tvCrashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //在点击事件里调用这个异常
                throw new RuntimeException("String");
            }
        });

    }
}
