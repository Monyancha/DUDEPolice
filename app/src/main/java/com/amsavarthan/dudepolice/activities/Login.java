package com.amsavarthan.dudepolice.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amsavarthan.dudepolice.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

import static com.amsavarthan.dudepolice.activities.ProfileSetup.profile_activity;


public class Login extends AppCompatActivity {

    private static final String TAG = Login.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private String phonenumber,verificationcode;
    private EditText phonenumber_field,verificationcode_field;
    private LinearLayout initialView,finalView;
    private TextView resend;
    private ProgressDialog mDialog;
    private boolean secondary;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();

                            mFirestore.collection("Cops")
                                    .document(user.getUid())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String name=documentSnapshot.getString("name");
                                            if(TextUtils.isEmpty(name)){
                                                startActivity(new Intent(Login.this, ProfileSetup.class).putExtra("phone", phonenumber));
                                                finish();
                                            }else{

                                                startActivity(new Intent(Login.this, Dashboard.class));
                                                Toast.makeText(Login.this, "Welcome back " + name, Toast.LENGTH_SHORT).show();
                                                finish();

                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            mDialog.dismiss();
                                            Log.e(TAG,e.getLocalizedMessage());
                                        }
                                    });


                        } else {
                            mDialog.dismiss();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Snackbar.make(findViewById(R.id.layout), "Invalid code.",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }


    private void initializeActivity() {

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/bold.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initializeActivity();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth= FirebaseAuth.getInstance();
        mFirestore= FirebaseFirestore.getInstance();

        initialView=findViewById(R.id.initialView);
        finalView=findViewById(R.id.finalView);

        phonenumber_field=findViewById(R.id.phonenumber);
        verificationcode_field=findViewById(R.id.verify);
        resend=findViewById(R.id.resend);

        phonenumber=null;
        verificationcode=null;

        String phonenumber_reset=getIntent().getStringExtra("phone");
        if(!TextUtils.isEmpty(phonenumber_reset)){
            secondary=true;
            phonenumber_field.setText(phonenumber_reset);
        }else{
            secondary=false;
        }

        mDialog=new ProgressDialog(this);
        mDialog.setMessage("Please wait...");
        mDialog.setIndeterminate(true);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(false);

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mDialog.dismiss();
                Log.w(TAG, "onVerificationFailed", e);
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Snackbar.make(findViewById(R.id.layout), "Invalid phone number.",
                            Snackbar.LENGTH_SHORT).show();
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(R.id.layout), "SMS Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                }else{
                    Snackbar.make(findViewById(R.id.layout), e.getLocalizedMessage(),
                            Snackbar.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                mDialog.dismiss();
                showFinalView();
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);
                mVerificationId = verificationId;
                mResendToken = token;

            }
        };

    }

    private void showFinalView() {

        initialView.animate()
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        initialView.setVisibility(View.GONE);

                        resend.setVisibility(View.VISIBLE);
                        resend.setAlpha(0.0f);
                        finalView.setVisibility(View.VISIBLE);
                        finalView.setAlpha(0.0f);

                        resend.animate()
                                .alpha(1.0f)
                                .setDuration(300)
                                .start();

                        finalView.animate()
                                .alpha(1.0f)
                                .setDuration(300)
                                .start();

                    }
                })
                .start();

    }

    public void onSendFabClicked(View view) {
        hideKeyboard(this);
        if(validatePhoneNumber()) {
            if(secondary){
                try {
                    profile_activity.finish();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            phonenumber=phonenumber_field.getText().toString();
            startPhoneNumberVerification(phonenumber);
        }
    }

    public void onVerifyFabClicked(View view) {
        hideKeyboard(this);
        verificationcode=verificationcode_field.getText().toString();
        verifyPhoneNumberWithCode(mVerificationId,verificationcode);
    }

    public void onResendClick(View view) {
        resendVerificationCode(phonenumber,mResendToken);
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                30,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        mDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        mDialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                30,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }

    private boolean validatePhoneNumber() {
        mDialog.show();
        String phoneNumber = phonenumber_field.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            Snackbar.make(findViewById(R.id.layout), "Invalid phone number.",
                    Snackbar.LENGTH_SHORT).show();
            mDialog.dismiss();
            return false;
        }
        return true;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
