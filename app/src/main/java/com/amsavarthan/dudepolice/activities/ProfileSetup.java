package com.amsavarthan.dudepolice.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.amsavarthan.dudepolice.R;
import com.amsavarthan.dudepolice.utils.UserDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.amsavarthan.dudepolice.utils.Utils.isOnline;


public class ProfileSetup extends AppCompatActivity implements LocationListener {

    private static final String TAG = ProfileSetup.class.getSimpleName();
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private String phone;
    private EditText name_field, age_field, phone_field, city_field, state_field,post_field;
    private ProgressDialog mDialog;
    public static Activity profile_activity;
    public final int PICK_CONTACT = 1001;
    private LocationManager locationManager;
    private double latitude,longitude;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private int clocationPermissionCheck = PackageManager.PERMISSION_DENIED;
    private int flocationPermissionCheck = PackageManager.PERMISSION_DENIED;
    private int plocationPermissionCheck = PackageManager.PERMISSION_DENIED;

    private void verifyAndRequestPermission() {

        clocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        flocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        plocationPermissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);


        if (clocationPermissionCheck != PackageManager.PERMISSION_GRANTED || plocationPermissionCheck != PackageManager.PERMISSION_GRANTED || flocationPermissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProfileSetup.this,
                    new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        plocationPermissionCheck = PackageManager.PERMISSION_GRANTED;
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        clocationPermissionCheck = PackageManager.PERMISSION_GRANTED;
                    }
                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        flocationPermissionCheck = PackageManager.PERMISSION_GRANTED;
                    }

                }
            }
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        profile_activity = this;

        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
            verifyAndRequestPermission();

        UserDatabase userDatabase=new UserDatabase(this);
        try {
            if(mCurrentUser!=null) {
                Cursor rs = userDatabase.getData(1);
                rs.moveToFirst();
                String nam = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_NAME));
                if (!rs.isClosed()) {
                    rs.close();
                }
                if (!TextUtils.isEmpty(nam)) {
                    startActivity(new Intent(ProfileSetup.this, Dashboard.class));
                    finish();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        phone=getIntent().getStringExtra("phone");
        name_field=findViewById(R.id.name);
        age_field=findViewById(R.id.age);
        phone_field=findViewById(R.id.phone);
        city_field=findViewById(R.id.city);
        state_field=findViewById(R.id.state);
        post_field=findViewById(R.id.posting);

        mDialog=new ProgressDialog(this);
        mDialog.setMessage("Just a sec...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        try{
            mFirestore.collection("Cops")
                    .document(mCurrentUser.getUid())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()) {
                                name_field.setText(documentSnapshot.getString("name"));
                                age_field.setText(documentSnapshot.getString("age"));
                                post_field.setText(documentSnapshot.getString("posting"));
                                phone_field.setText(documentSnapshot.getString("phone"));
                                city_field.setText(documentSnapshot.getString("city"));
                                state_field.setText(documentSnapshot.getString("state"));
                            }else{

                                if(!TextUtils.isEmpty(phone)) {
                                    phone_field.setText(phone);
                                }else{
                                    phone_field.setText(mCurrentUser.getPhoneNumber());
                                }

                                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                if (ActivityCompat.checkSelfPermission(ProfileSetup.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ProfileSetup.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                onLocationChanged(location);
                                getLocationDetails(location);

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    public void onFabClicked(View view) {

        if(validate()){

            mDialog.show();
            if(!isOnline(this)){

                UserDatabase userDatabase = new UserDatabase(ProfileSetup.this);
                try {
                    userDatabase.deleteContact(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                userDatabase.insertContact(name_field.getText().toString(), phone_field.getText().toString(), city_field.getText().toString(),post_field.getText().toString(),state_field.getText().toString(),age_field.getText().toString());

                mDialog.dismiss();
                Toast.makeText(ProfileSetup.this, "Welcome " + name_field.getText().toString(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ProfileSetup.this, Dashboard.class));
                finish();

            }else {

                mFirestore.collection("Cops")
                        .document(mCurrentUser.getUid())
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (!documentSnapshot.exists()) {

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("id", mCurrentUser.getUid());
                                    userMap.put("name", name_field.getText().toString());
                                    userMap.put("age", age_field.getText().toString());
                                    userMap.put("posting", post_field.getText().toString());
                                    userMap.put("phone", phone_field.getText().toString());
                                    userMap.put("city", city_field.getText().toString());
                                    userMap.put("state", state_field.getText().toString());


                                    mFirestore.collection("Cops")
                                            .document(mCurrentUser.getUid())
                                            .set(userMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    UserDatabase userDatabase = new UserDatabase(ProfileSetup.this);
                                                    try {
                                                        userDatabase.deleteContact(1);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    userDatabase.insertContact(name_field.getText().toString(), phone_field.getText().toString(), city_field.getText().toString(),post_field.getText().toString(),state_field.getText().toString(),age_field.getText().toString());

                                                    mDialog.dismiss();
                                                    Toast.makeText(ProfileSetup.this, "Welcome " + name_field.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ProfileSetup.this, Dashboard.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mDialog.dismiss();
                                                    Log.e(TAG, e.getLocalizedMessage());
                                                }
                                            });

                                } else {

                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("id", mCurrentUser.getUid());
                                    userMap.put("name", name_field.getText().toString());
                                    userMap.put("age", age_field.getText().toString());
                                    userMap.put("posting", post_field.getText().toString());
                                    userMap.put("phone", phone_field.getText().toString());
                                    userMap.put("city", city_field.getText().toString());
                                    userMap.put("state", state_field.getText().toString());


                                    mFirestore.collection("Cops")
                                            .document(mCurrentUser.getUid())
                                            .update(userMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    UserDatabase userDatabase = new UserDatabase(ProfileSetup.this);
                                                    try {
                                                        userDatabase.deleteContact(1);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    userDatabase.insertContact(name_field.getText().toString(), phone_field.getText().toString(), city_field.getText().toString(),post_field.getText().toString(),state_field.getText().toString(),age_field.getText().toString());


                                                    mDialog.dismiss();
                                                    Toast.makeText(ProfileSetup.this, "Welcome " + name_field.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(ProfileSetup.this, Dashboard.class));
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    mDialog.dismiss();
                                                    Log.e(TAG, e.getLocalizedMessage());
                                                }
                                            });

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                });
            }


        }

    }

    public void onChangePhoneClicked(View view) {

        startActivity(new Intent(ProfileSetup.this, Login.class).putExtra("phone",phone_field.getText().toString()));

    }

    private boolean validate() {
        String name = name_field.getText().toString();
        String age = age_field.getText().toString();
        String phoneNumber = phone_field.getText().toString();
        String state = state_field.getText().toString();
        String city = city_field.getText().toString();
        String post = post_field.getText().toString();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(post) || TextUtils.isEmpty(age) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(state) || TextUtils.isEmpty(city)) {

            Snackbar.make(findViewById(R.id.layout), "Fill all details.",
                    Snackbar.LENGTH_SHORT).show();

            return false;
        }


        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.i("Location", "latitude: " + latitude + "; longitude: " + longitude);


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void getLocationDetails(Location location){

        try {
            Geocoder geocoder=new Geocoder(this);
            List<Address> addresses=null;
            addresses=geocoder.getFromLocation(latitude,longitude,1);

            String city=addresses.get(0).getLocality();
            String state=addresses.get(0).getAdminArea();
            city_field.setText(city);
            state_field.setText(state);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
