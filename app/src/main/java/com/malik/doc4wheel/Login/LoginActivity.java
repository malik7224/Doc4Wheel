package com.malik.doc4wheel.Login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.malik.doc4wheel.Customer.CustomerMapActivity;
import com.malik.doc4wheel.ForgetPasswordActivity;
import com.malik.doc4wheel.Mechanic.MechanicMapActivity;
import com.malik.doc4wheel.R;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mForgotButton, mRegisterButton;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    checkEmailVerification();
                }
            }
        };

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mForgotButton = findViewById(R.id.forgotButton);

        mLogin = findViewById(R.id.login);

        mRegisterButton = findViewById(R.id.register_textView);


        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
                finish();
            }
        });


        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();

                if(email.isEmpty())
                {

                    Toast.makeText(LoginActivity.this, "Please enter Email", Toast.LENGTH_SHORT).show();
                }
                else if (password.isEmpty())
                {

                    Toast.makeText(LoginActivity.this, "Please enter Password", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Snackbar.make(findViewById(R.id.layout), "Invalid Email or Password", Toast.LENGTH_SHORT).show();

                            }
                            else{

                                checkEmailVerification();

                            }
                        }
                    });
                }



            }

        });


        mForgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fortgetIntent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                startActivity(fortgetIntent);
                finish();
            }
        });


    }

    private void checkEmailVerification() {


        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        Boolean emailFlag = firebaseUser.isEmailVerified();

        if(emailFlag){

            checkUserAccType();

        }

        else
        {

            Toast.makeText(LoginActivity.this, "Please Verify your Email", Toast.LENGTH_LONG).show();
        }


    }



    private void checkUserAccType(){
        DatabaseReference mCustomerDatabase;
        String userID;

        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Intent intent = new Intent(LoginActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }else{
                    Intent intent = new Intent(LoginActivity.this, MechanicMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
