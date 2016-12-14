package com.example.eisti.beacon;

import android.content.Intent;
import android.icu.text.DateFormat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText etMessage;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etMessage = (EditText) findViewById(R.id.etMessage);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("messages").child("message").setValue(etMessage.getText().toString());
                Toast.makeText(MainActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, MonitoringActivity.class));
            }
        });
    }
}
