package com.example.bhati.routeapplication.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.bhati.routeapplication.R;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private Button btnSignup;
    private Button btnLogin;
    private LinearLayout ll;
    private FirebaseAuth mAuth;
    private EditText edTxtEmail;
    private EditText edTxtPassword;
    String email , password ;
    private CheckBox checkBox;
    private boolean isRemembered;
    private boolean isCheckBoxIsCheck;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        preferences = getSharedPreferences("Remember_Me" , MODE_PRIVATE);
        isRemembered = preferences.getBoolean("remember" , false);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, Home.class));
            finish();
        } else {

            setContentView(R.layout.activity_login);

            //MyAlertMessageNoGps();
            edTxtPassword = findViewById(R.id.password);
            edTxtEmail = findViewById(R.id.email);
             checkBox = findViewById(R.id.checkBox);
            ll = findViewById(R.id.ll);
            btnSignup = findViewById(R.id.btnSignup);
            btnLogin = findViewById(R.id.btnLogin);

    //        if (isRemembered)
    //        {
    //            checkBox.setChecked(true);
    //            startActivity(new Intent(this , LoginActivity.class));
    //            finish();
    //        }
    //        else
    //        {
    //            checkBox.setChecked(false);
    //        }
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> isCheckBoxIsCheck = isChecked);
            btnSignup.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                finish();
            });
            btnLogin.setOnClickListener(v -> {
                if (isCheckBoxIsCheck)
                {
                    editor = preferences.edit();
                    editor.putBoolean("remember" , true);
                    editor.apply();
                }
                else
                {
                    editor = preferences.edit();
                    editor.putBoolean("remember" , false);
                    editor.apply();
                }
                email = edTxtEmail.getText().toString();
                password = edTxtPassword.getText().toString();
                if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    Snackbar.make(ll, "Fill all the fields to proceed", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                final ProgressDialog mProgressDialog = new ProgressDialog(LoginActivity.this);
                mProgressDialog.setMessage("Please Wait........");
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(ll, "Login Successfully", Snackbar.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this, Home.class));
                        finish();
                    } else {
                        mProgressDialog.dismiss();
                        Snackbar.make(ll, "Something went wrong, please try again later.", Snackbar.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
