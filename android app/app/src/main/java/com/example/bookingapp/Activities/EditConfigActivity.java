package com.example.bookingapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.bookingapp.R;
import com.example.bookingapp.Utils.Helper;

public class EditConfigActivity extends AppCompatActivity {

   EditText configEdit;
   Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_config);

        configEdit = findViewById(R.id.config_text_edit);
        saveBtn = findViewById(R.id.save_btn);

        String configText = Helper.getConfigText(EditConfigActivity.this);
        configEdit.setText(configText);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = configEdit.getText().toString();
                Helper.updateConfigFile(EditConfigActivity.this,text);
                Toast.makeText(EditConfigActivity.this,"Config file has been updated",Toast.LENGTH_SHORT).show();
            }
        });
    }
}