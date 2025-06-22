package com.ashen.bdonorapp.RequestModule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.ashen.bdonorapp.R;

public class RunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run); // Your main activity's layout

        Button addRequestButton = findViewById(R.id.button_add_request); // Assuming you add this button in activity_main.xml
        if (addRequestButton != null) {
            addRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RunActivity.this, AddBloodRequestActivity.class);
                    startActivity(intent);
                }
            });
        }

        Button viewRequestsButton = findViewById(R.id.button_view_requests); // Assuming you add this button
        if (viewRequestsButton != null) {
            viewRequestsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(RunActivity.this, RequestCardActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

}