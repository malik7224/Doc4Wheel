package com.malik.doc4wheel.Login;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.malik.doc4wheel.Mechanic.MechanicMapActivity;
import com.malik.doc4wheel.R;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class RegisterActivity extends AppCompatActivity {
    private EditText mEmail, mPassword;
    private TextView mLoginTextview;
    private Button mRegistration;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    private RadioRealButtonGroup mRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    checkUserAccType();
                }
            }
        };

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mLoginTextview = findViewById(R.id.login_textView);

        mRegistration = findViewById(R.id.register);

        mRadioGroup = findViewById(R.id.radioRealButtonGroup);
        mRadioGroup.setPosition(0);

        mLoginTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        mRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String accountType;
                int selectId = mRadioGroup.getPosition();

                if(email.isEmpty())
                {
                    Toast.makeText(RegisterActivity.this, "Please enter Email", Toast.LENGTH_SHORT).show();
                }
                 if (password.isEmpty())
                {
                    Toast.makeText(RegisterActivity.this, "Please enter Password", Toast.LENGTH_SHORT).show();
                }

                else {


                     switch (selectId){
                         case 0:
                             accountType = "Customers";
                             break;
                         case 1:
                             accountType = "Mechanic";
                             break;
                         default:
                             accountType = "Customers";
                     }


                     mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                         @Override
                         public void onComplete(@NonNull Task<AuthResult> task) {
                             if(!task.isSuccessful()){
                                 Toast.makeText(RegisterActivity.this, "sign up error", Toast.LENGTH_LONG).show();
                             }else{
                                 String user_id = mAuth.getCurrentUser().getUid();
                                 DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(accountType).child(user_id).child("name");
                                 current_user_db.setValue(email);
                                 if(accountType.equals("Mechanic"))
                                 {
                                     FirebaseDatabase.getInstance().getReference().child("Users").child(accountType).child(user_id).child("service").setValue("Mechanic");
                                 }

                                 senEmailVerification();

                             }
                         }
                     });

                 }


//                switch (selectId){
//                    case 0:
//                        accountType = "Customers";
//                        break;
//                    case 1:
//                        accountType = "Mechanic";
//                        break;
//                    default:
//                        accountType = "Customers";
//                }
//
//
//                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(!task.isSuccessful()){
//                            Toast.makeText(RegisterActivity.this, "sign up error", Toast.LENGTH_LONG).show();
//                        }else{
//                            String user_id = mAuth.getCurrentUser().getUid();
//                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(accountType).child(user_id).child("name");
//                            current_user_db.setValue(email);
//                            if(accountType.equals("Mechanic"))
//                            {
//                                FirebaseDatabase.getInstance().getReference().child("Users").child(accountType).child(user_id).child("service").setValue("Mechanic");
//                            }
//
//                            senEmailVerification();
//
//                        }
//                    }
//                });
            }
        });
    }

    private void senEmailVerification() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if(firebaseUser!=null){

            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()){

                        Toast.makeText(RegisterActivity.this, "Successfully Register, Verification email sent", Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        finish();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }
                    else
                    {

                        Toast.makeText(RegisterActivity.this, "Verification email has't been sent", Toast.LENGTH_LONG).show();

                    }

                }
            });


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
                    Intent intent = new Intent(RegisterActivity.this, CustomerMapActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }else{
                    Intent intent = new Intent(RegisterActivity.this, MechanicMapActivity.class);
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
