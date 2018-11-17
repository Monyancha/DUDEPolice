package com.amsavarthan.dudepolice.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amsavarthan.dudepolice.R;
import com.amsavarthan.dudepolice.utils.NetworkUtil;
import com.amsavarthan.dudepolice.utils.UserDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.amsavarthan.dudepolice.utils.Utils.isOnline;

public class Dashboard extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_dashboard:
                    loadfragment(FragmentManager.newInstance("dashboard"));
                    return true;
                case R.id.navigation_investigating:
                    loadfragment(FragmentManager.newInstance("Investigating"));
                    return true;
                case R.id.navigation_received:
                    loadfragment(FragmentManager.newInstance("Received"));
                    return true;
                case R.id.navigation_closed:
                    loadfragment(FragmentManager.newInstance("Closed"));
                    return true;
            }
            return false;
        }
    };
    private BottomNavigationView navigation;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;

    private void initializeActivity() {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    public BroadcastReceiver NetworkChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            Log.i("Network reciever", "OnReceive: "+status);
            if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status != NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {

                    performUploadTask();
                    Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show();

                }
            }
        }

    };

    private void performUploadTask() {

        mFirestore.collection("Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        UserDatabase userDatabase=new UserDatabase(Dashboard.this);

                        Cursor rs = userDatabase.getData(1);
                        rs.moveToFirst();

                        final String nam = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_NAME));
                        String phon = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_PHONE));
                        String pos = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_POST));
                        String age = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_AGE));
                        String city = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_CITY));
                        String state = rs.getString(rs.getColumnIndex(UserDatabase.CONTACTS_COLUMN_STATE));

                        if (!rs.isClosed()) {
                            rs.close();
                        }

                        if(TextUtils.isEmpty(nam) && !documentSnapshot.exists()){

                            Toast.makeText(Dashboard.this, "It seems you have not setup your profile, taking you back...", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(Dashboard.this,ProfileSetup.class));
                            finish();

                        }else if(!documentSnapshot.exists()){

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("posting", pos);
                            userMap.put("phone", phon);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });


                        }else if(!documentSnapshot.getString("name").equals(nam)){
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("phone", phon);
                            userMap.put("posting", pos);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .update(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });
                        }else if(!documentSnapshot.getString("phone").equals(phon)){
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("phone", phon);
                            userMap.put("posting", pos);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .update(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });
                        }else if(!documentSnapshot.getString("posting").equals(pos)){
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("id", mCurrentUser.getUid());
                            userMap.put("name", nam);
                            userMap.put("age", age);
                            userMap.put("phone", phon);
                            userMap.put("posting", pos);
                            userMap.put("city", city);
                            userMap.put("state", state);


                            mFirestore.collection("Users")
                                    .document(mCurrentUser.getUid())
                                    .update(userMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("Update","success");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i("Update","error : "+e.getLocalizedMessage());
                                            e.printStackTrace();
                                        }
                                    });
                        }else{
                            Log.i("Update","...");
                        }

                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MultiDex.install(this);
        initializeActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mFirestore=FirebaseFirestore.getInstance();
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadfragment(FragmentManager.newInstance("dashboard"));

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isOnline(this)){
            performUploadTask();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(NetworkChangeReceiver);

    }

    public void loadfragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
    }


}
