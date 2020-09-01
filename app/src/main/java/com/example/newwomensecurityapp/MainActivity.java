package com.example.newwomensecurityapp;
// This app is working
import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener

{

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;

    String Name,Mobile,BuddyName,BuddyMobile;
    TextView tv;
    protected static final int RESULT_SPEECH = 1;


    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            File f = new File("/sdcard/ram.txt");//userdata
            FileInputStream fin = new FileInputStream(f);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(fin));
            String data = br.readLine();
            String finalData[] = data.split(",");
            Name = finalData[0];
            Mobile = finalData[1];
            BuddyName = finalData[2];
            BuddyMobile = finalData[3];
            setContentView(R.layout.speak);
            tv = (TextView) findViewById(R.id.textView2);
            Toast.makeText(this,"Welcome :"+Name,
                    Toast.LENGTH_SHORT).show();
        }
        catch(Exception ex){
            setContentView(R.layout.activity_main);
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();



        permissionStatus = getSharedPreferences("permissionStatus"
                ,MODE_PRIVATE);


        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();

                                ActivityCompat.requestPermissions(
                                        MainActivity.this,
                                        new String[]{
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                builder.show();
            } else if (permissionStatus.getBoolean(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                sentToSettings = true;
                                Intent intent = new Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                                Uri uri = Uri.fromParts("package",
                                        getPackageName(), null);
                                intent.setData(uri);
                                startActivityForResult(intent,
                                        REQUEST_PERMISSION_SETTING);
                                Toast.makeText(getBaseContext(),
                                        "Go to Permissions to Grant Storage",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                builder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE,true);
            editor.commit();

        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }
    private void proceedAfterPermission() {
        //We've got the permission, now we can proceed further
        Toast.makeText(getBaseContext(), "We got the Storage Permission",
                Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "ACCESS_FINE_LOCATION permission OK");
        } else {
            Log.d(TAG, "ACCESS_FINE_LOCATION permission NG");
            return;
        }
        PendingResult<Status> pendingResult =
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;



    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());

            String url = "http://maps.google.com/maps?&daddr="+
                    lat+"," +lng;

            ////Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
            //         Uri.parse(url));
            // startActivity(intent);

            try {
                String firstmsg = "I Need help \nMy location is " +url ;

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(BuddyMobile,
                        null, firstmsg, null, null);
                Toast.makeText(getApplicationContext(), "SMS Sent!",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(),
                        "SMS failed, please try again later!",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }




        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }






    public void submitclick(View v)
    {
        EditText t1 = (EditText) findViewById(R.id.editText);
        EditText t2 = (EditText) findViewById(R.id.editText2);
        EditText t3 = (EditText) findViewById(R.id.editText3);
        EditText t4 = (EditText) findViewById(R.id.editText4);

        Name = t1.getText().toString();
        Mobile = t2.getText().toString();
        BuddyName = t3.getText().toString();
        BuddyMobile = t4.getText().toString();

        if(Name.length()==0 || Mobile.length()==0
                || BuddyName.length()==0 || BuddyMobile.length()==0)

        {
            Toast.makeText(this,
                    "Value cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg =Name+","+Mobile+","+BuddyName+","+BuddyMobile;
            File f = new File("/sdcard/ram.txt");//userdata
            f.createNewFile();
            FileOutputStream fOut = new FileOutputStream(f);
            OutputStreamWriter myOutWriter =new OutputStreamWriter(fOut);
            myOutWriter.append(msg);
            myOutWriter.close();
            fOut.close();
            Toast.makeText(this,
                    "Done writing SD 'ram.txt'",
                    Toast.LENGTH_SHORT).show();

            String firstmsg = "Hi " + BuddyName
                    + "\nI have added as a helping person";
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(BuddyMobile,
                    null, firstmsg, null, null);

            Toast.makeText(this, "SMS Sent!",
                    Toast.LENGTH_LONG).show();
            setContentView(R.layout.speak);
            tv = (TextView) findViewById(R.id.textView2);
        }
        catch(Exception ex)
        {
            Toast.makeText(this, ex.toString(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void speakclick(View v)
    {
        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                "en-US");
        try {
            startActivityForResult(intent, RESULT_SPEECH);
            tv.setText("");
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(this,
                    "Ops! Your device doesn't support Speech to " +
                            "Text", Toast.LENGTH_SHORT);
            t.show();
        }
    }

    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> text = data
                            .getStringArrayListExtra(
                                    RecognizerIntent.EXTRA_RESULTS);
                    tv.setText(text.get(0));
                    if (tv.getText().equals("help")) {
                        updateUI();
                    }
                }
                break;
            }
        }
    }
}
