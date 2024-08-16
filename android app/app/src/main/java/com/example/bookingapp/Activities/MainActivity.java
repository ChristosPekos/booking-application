package com.example.bookingapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.bookingapp.R;
import com.example.bookingapp.Utils.Helper;


public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button starBtn;
    Button editBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.select_option);
        starBtn = findViewById(R.id.search_activity_btn);
        editBtn = findViewById(R.id.edit_config_activity);

        Helper.initializeConfigFile(MainActivity.this);
        starBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SearchActivity.class);
                startActivity(intent);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),EditConfigActivity.class);
                startActivity(intent);
            }
        });
    }
}