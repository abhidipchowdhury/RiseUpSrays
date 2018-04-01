package com.abhidip.strays.riseupsrays;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;


import com.abhidip.strays.riseupsrays.databinding.ActivitySignUpBinding;
import com.abhidip.strays.util.GPSUtil;
import com.abhidip.strays.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import java.util.concurrent.TimeUnit;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "PhoneAuthActivity";

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private FirebaseUser currentUser;
    private UserDetails userDetails;

    // Util Class for GPS
    private GPSUtil gpsUtil;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    private PhoneAuthProvider mPhoneAuthProvider;

    // Firebase real time database
    FirebaseDatabase database;
    DatabaseReference reference;


    private String mobileNumber;

    Toolbar toolbar;

    // UI widgets
    Button signUpButton;
    EditText fullName;
    EditText password;
    EditText email;
    EditText mobile;
    EditText otp;
    EditText aadhar;
    EditText location;
    Button nextButton;
    Button previousButton;
    Button verifyOtp;
    Button resendOtp;
    ViewFlipper flipper;
    TextView statusText;
    private int count = 0;

    // Flag Variables
    private boolean generateUserId = false;
    private boolean generateLatLong = false;
    private boolean mVerificationInProgress = false;
    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
       // binding = DataBindingUtil.setContentView(this, R.layout.sign_up_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Rise Up...Strays");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(10f);
        }

        // bind all the views
        //bindUiViews();
        setUpUiViews();
        userDetails = new UserDetails();
        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        mPhoneAuthProvider = PhoneAuthProvider.getInstance();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        if (ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not
            ActivityCompat.requestPermissions(SignUpActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }

        gpsUtil = new GPSUtil(getApplicationContext());
        // By default, the next button will be deactivated
        nextButton.setEnabled(false);
    }


    private void bindUiViews (){
        fullName = binding.fullName;
        fullName.addTextChangedListener(fullNameTextWatcher);
        email = binding.userEmailId;
        email.addTextChangedListener(emailTextWatcher);
        password = binding.password;
        mobile = binding.mobileNumber;
        mobile.addTextChangedListener(mobileTextWatcher);
        otp = binding.otp;
        otp.addTextChangedListener(otpTextWatcher);
        aadhar = binding.aadhar;
        aadhar.addTextChangedListener(aadharTextWatcher);
        nextButton = binding.nextButton;
        nextButton.setOnClickListener(nextButtonListener);
        verifyOtp = binding.verifyOtp;
        verifyOtp.setEnabled(false);
        resendOtp = binding.resendOtp;
        resendOtp.setEnabled(false);
        statusText = binding.statusText;
        flipper =  (ViewFlipper) findViewById(R.id.view_flipper);
    }

    // Binds all the ui elements.
    private void setUpUiViews ()
    {
       // signUpButton = (Button) findViewById(R.id.signUpBtn);
        fullName = (EditText) findViewById(R.id.fullName);
        fullName.addTextChangedListener(fullNameTextWatcher);
        email = (EditText) findViewById(R.id.userEmailId);
        email.addTextChangedListener(emailTextWatcher);
        password = (EditText) findViewById(R.id.password);
        mobile = (EditText) findViewById(R.id.mobileNumber);
        mobile.addTextChangedListener(mobileTextWatcher);
        otp = (EditText) findViewById(R.id.otp);
        otp.addTextChangedListener(otpTextWatcher);
        aadhar = (EditText) findViewById(R.id.aadhar);
        aadhar.addTextChangedListener(aadharTextWatcher);
        //location = (EditText) findViewById(R.id.location);
        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(nextButtonListener);
        verifyOtp = (Button) findViewById(R.id.verifyOtp);
        verifyOtp.setEnabled(false);
        resendOtp = (Button) findViewById(R.id.resendOtp);
        resendOtp.setEnabled(false);
        statusText = (TextView) findViewById(R.id.statusText);
        flipper =  (ViewFlipper) findViewById(R.id.view_flipper);
    }

    // Listeners for buttons and other widgets go here
    private TextWatcher fullNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if( !TextUtils.isEmpty(fullName.getText().toString()) && s.length()>=3)
            {
                nextButton.setEnabled(true);
                userDetails.setName(fullName.getText().toString());
            }
            else
                nextButton.setEnabled(false);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    private TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if  (!TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches())
            {
                userDetails.setEmail(email.getText().toString());
                nextButton.setEnabled(true);
                //  flag set to True
                generateLatLong = true;
            }
            else
                nextButton.setEnabled(false);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    private TextWatcher aadharTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if  (!TextUtils.isEmpty(s) && s.length() == 16)
            {
                userDetails.setAadhar(aadhar.toString());
                nextButton.setEnabled(true);
                // Flag set to true, now clicking on next button will generate the userId
                generateUserId = true;
            }
            else
                nextButton.setEnabled(false);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    private TextWatcher mobileTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if  (!TextUtils.isEmpty(s) && s.length() == 10)
            {
                userDetails.setMobile(mobile.toString());
                nextButton.setEnabled(true);
            }
            else
                nextButton.setEnabled(false);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    private TextWatcher otpTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.length() == 6)
                verifyOtp.setEnabled(true);
            else
                verifyOtp.setEnabled(false);
        }
        @Override
        public void afterTextChanged(Editable s) {}
    };

    private OnClickListener nextButtonListener = new OnClickListener() {
        public void onClick(View v) {
            // Code here executes on main thread after user presses button
            if (v == nextButton) {
                flipper.showNext();
                nextButton.setEnabled(false);
            }
            // OTP Sending
            if( !TextUtils.isEmpty(mobile.getText().toString()) && count == 0)
            {
                Toast toast=Toast.makeText(getApplicationContext(),"Sending OTP To your number", Toast.LENGTH_SHORT);
                toast.setMargin(50,50);
                toast.show();

                startPhoneNumberVerification(mobile.getText().toString());
                count ++;
            }
            // Create the user id
            if (generateUserId) {
                generateUserName();
                generateUserId = false;
            }

            if (generateLatLong) {
                Location location = gpsUtil.getLocation();
                if (location != null) {
                    userDetails.setLatitude(location.getLatitude());
                    userDetails.setLongitude(location.getLongitude());
                    Toast.makeText(getApplicationContext(), "LAT : "+userDetails.getLatitude()+" \n LON: "+ userDetails.getLongitude(), Toast.LENGTH_SHORT).show();
                }

                generateLatLong = false;

                //below code snippet for testing, should be deleted later
                reference.push().setValue(userDetails);
            }

        }
    };

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = mAuth.getCurrentUser().getUid();

        //    Intent authIntent = new Intent(SignUpActivity.this, HomeActivity.class);
          //  startActivity(authIntent);
        }
    }

    private void startPhoneNumberVerification(String phoneNumber) {

        mobileNumber = "91"+phoneNumber;
        setUpVerificationCallBacks();

        // [START start_phone_auth]
        mPhoneAuthProvider.verifyPhoneNumber(
                mobileNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true;
    }

    // Call back functions for Firabase user verification.
    private void  setUpVerificationCallBacks() {

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "onVerificationCompleted:" + credential);
                //statusText.setText("Success");
                verifyOtp.setEnabled(false);
                otp.setText("");
                resendOtp.setEnabled(false);
               // nextButton.setEnabled(true);
               // FirebaseAuth.getInstance().signOut();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w(TAG, "onVerificationFailed", e);
                mVerificationInProgress = false;

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    mobile.setError("Invalid phone number.");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {

                }

                Toast toast=Toast.makeText(getApplicationContext(),"Verification Failed", Toast.LENGTH_SHORT);
                toast.setMargin(50,50);
                toast.show();

                statusText.setText("Failed");
                resendOtp.setEnabled(true);
                // For Testing purpose only, will be deleted later
                nextButton.setEnabled(true);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
                verifyOtp.setEnabled(true);
            }
        };
        // [END phone_auth_callbacks]*/
    }

    // Called upon clicking the verify button
    public void vefifyCode(View view) {
        String code = otp.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    // Verifies the otp code ang signs in the user using the phone number
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            statusText.setText("Success");

                            nextButton.setEnabled(true);
                            //FirebaseUser user = task.getResult().getUser();
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                            statusText.setText("Failed");

                            // For Testing purpose only, will be deleted later
                            nextButton.setEnabled(true);
                        }
                    }
                });
    }

    // Resend OTP
    public void resendCode(View view) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                mResendToken);
    }

    // Generate User id ( 1st 3 letters of full name + last 4 digits of aadhar)
    private void generateUserName(){
        String aadharNumber =  aadhar.getText().toString();
        String name = fullName.getText().toString();
        userDetails.setUserName(name.substring(0, 3)  + aadharNumber.substring( aadharNumber.length() - 4, aadharNumber.length()));
    }
}
