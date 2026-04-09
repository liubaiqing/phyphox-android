package de.rwth_aachen.phyphox;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Get reference to the button
        Button btnUnderstand = findViewById(R.id.btn_understand);

        // Set click listener for the button
        btnUnderstand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the main activity
                Intent intent = new Intent(SplashActivity.this, de.rwth_aachen.phyphox.ExperimentList.ExperimentListActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
