package com.example.eisti.beacon;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

public class MonitoringActivity extends AppCompatActivity implements BeaconConsumer {
    private static final String TAG = "BEACON";
    private BeaconManager beaconManager;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private TextView tv;
    private TextView tv2;
    private static DatabaseReference mDatabase;
    private static String message;
    private EditText etMessage;
    private Button btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);
        tv = (TextView) findViewById(R.id.tv);
        tv2 = (TextView) findViewById(R.id.tv2);
        etMessage = (EditText) findViewById(R.id.eMessage);
        etMessage.setVisibility(View.INVISIBLE);
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setVisibility(View.INVISIBLE);
        //iv = (ImageView) findViewById(R.id.imageView);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child("messages").child("message").setValue(etMessage.getText().toString());
                Toast.makeText(MonitoringActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

            }
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(final Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv2.setVisibility(View.VISIBLE);
                        btnSend.setVisibility(View.VISIBLE);

                        tv.setText("Access granted! Beacon #" + region.getId3());
                        mDatabase.child("messages").child("message").
                                addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Log.i(TAG, "testDB");
                                message = (String) dataSnapshot.getValue();
                                etMessage.setVisibility(View.VISIBLE);
                                tv2.setText("latest message posted: " + message);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        //iv.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void didExitRegion(final Region region) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv2.setVisibility(View.INVISIBLE);
                        btnSend.setVisibility(View.INVISIBLE);
                        tv.setText("Access non-granted ! ");
                        etMessage.setVisibility(View.INVISIBLE);
                        //iv.setVisibility(View.INVISIBLE);
                    }
                });

            }

            @Override
            public void didDetermineStateForRegion(int state, final Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: " + state + ", " + region.getId3());
                final String regionMinor = region.getId3().toString();
                if (state == MonitorNotifier.INSIDE) {
                    Log.i(TAG, "run inside");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tv.setText("I just saw an beacon for the first time !");
                            didEnterRegion(region);
                            // iv.setVisibility(View.VISIBLE);

                        }
                    });
                }
                if (state == MonitorNotifier.OUTSIDE) {
                    Log.i(TAG, "run outside");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText("I am outside range !");

                            //iv.setVisibility(View.INVISIBLE);

                        }
                    });

                }

            }
        });

        try {

            Identifier uuid = Identifier.parse("e2c56db5-dffb-48d2-b060d0f5a71096e0");
            Identifier major = Identifier.parse("0");

            Region region1 = new Region("Beacon1", uuid, major, Identifier.parse("1"));
            Log.d(TAG, "New region : " + region1);

           /* Region region2 = new Region("Beacon2", uuid, major, Identifier.parse("2"));
            Log.d(TAG, "New region : " + region2);

            Region region3 = new Region("Beacon3", uuid, major, Identifier.parse("3"));
            Log.d(TAG, "New region : " + region3);
*/
            beaconManager.startMonitoringBeaconsInRegion(region1);
            //beaconManager.startMonitoringBeaconsInRegion(region2);
           // beaconManager.startMonitoringBeaconsInRegion(region3);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}