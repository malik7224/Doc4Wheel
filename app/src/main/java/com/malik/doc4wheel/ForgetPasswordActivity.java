package com.malik.doc4wheel;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.malik.doc4wheel.Login.LoginActivity;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText mEmail;
    private Button mResetButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mEmail = findViewById(R.id.forget_password_edittext);
        mResetButton = findViewById(R.id.forget_password_button);
        mAuth = FirebaseAuth.getInstance();



        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email;
                email = mEmail.getText().toString().trim();

                if(email.isEmpty()){

                    Toast.makeText(ForgetPasswordActivity.this,"Please enter your email address", Toast.LENGTH_SHORT).show();

                } else

                {

                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {

                                Toast.makeText(ForgetPasswordActivity.this,"Rest password Email sent",Toast.LENGTH_SHORT).show();
                                finish();
                                startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));

                            }
                            else
                            {

                                Toast.makeText(ForgetPasswordActivity.this, "Error in sending email", Toast.LENGTH_SHORT).show();


                            }

                        }
                    });


                }


            }
        });

    }
}

