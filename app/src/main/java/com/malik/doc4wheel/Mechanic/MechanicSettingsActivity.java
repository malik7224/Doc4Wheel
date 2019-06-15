package com.malik.doc4wheel.Mechanic;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.malik.doc4wheel.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import co.ceryle.radiorealbutton.RadioRealButtonGroup;

public class MechanicSettingsActivity extends AppCompatActivity {

    private EditText mNameField, mPhoneField, mEmailField;

    private Button mBack, mConfirm;

    private ImageView mProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference mMechanicDatabase;

    private String userID;
    private String mName;
    private String mPhone;
    private String mEmail;
    private String mService;
    private String mProfileImageUrl;

    private Uri resultUri;

    private RadioRealButtonGroup mRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mechanic_settings);


        mNameField = findViewById(R.id.name);
        mPhoneField = findViewById(R.id.phone);
        mEmailField = findViewById(R.id.mechanic_email);

        mProfileImage = findViewById(R.id.profileImage);

        mRadioGroup = findViewById(R.id.radioRealButtonGroup);

        mBack = findViewById(R.id.back);
        mConfirm = findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mMechanicDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Mechanics").child(userID);

        getUserInfo();

        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }
    private void getUserInfo(){
        mMechanicDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName = map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone")!=null){
                        mPhone = map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("email")!=null){
                        mEmail = map.get("email").toString();
                        mEmailField.setText(mEmail);
                    }
                    if(map.get("service")!=null){
                        mService = map.get("service").toString();
                        switch (mService){
                            case"Mechanic":
                                mRadioGroup.setPosition(0);
                                break;
                            case"Electrician":
                                mRadioGroup.setPosition(1);
                                break;
                            case"Tyre Services":
                                mRadioGroup.setPosition(2);
                                break;
                        }
                    }
                    if(map.get("profileImageUrl")!=null){
                        mProfileImageUrl = map.get("profileImageUrl").toString();
                        Glide.with(getApplication()).load(mProfileImageUrl).apply(RequestOptions.circleCropTransform()).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    private void saveUserInformation() {
        mName = mNameField.getText().toString();
        mPhone = mPhoneField.getText().toString();
        mEmail = mEmailField.getText().toString();



        if (mName.isEmpty())
        {
            Toast.makeText(this, "please enter Name", Toast.LENGTH_SHORT).show();
        }
        else if (mPhone.isEmpty())
        {
            Toast.makeText(this, "please enter Phone", Toast.LENGTH_SHORT).show();
        }
        else if (mEmail.isEmpty())
        {
            Toast.makeText(this, "please enter Email", Toast.LENGTH_SHORT).show();
        }

        else{

            int selectId = mRadioGroup.getPosition();


            switch (selectId){
                case 0:
                    mService = "Mechanic";
                    break;
                case 1:
                    mService = "Electrician<";
                    break;
                case 2:
                    mService = "Tyre Services";
                    break;
            }

            Map userInfo = new HashMap();
            userInfo.put("name", mName);
            userInfo.put("phone", mPhone);
            userInfo.put("email", mEmail);
            userInfo.put("service", mService);
            mMechanicDatabase.updateChildren(userInfo);

            if(resultUri != null) {

                final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                byte[] data = baos.toByteArray();
                UploadTask uploadTask = filePath.putBytes(data);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        finish();
                        return;
                    }
                });
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map newImage = new HashMap();
                                newImage.put("profileImageUrl", uri.toString());
                                mMechanicDatabase.updateChildren(newImage);

                                finish();
                                return;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                finish();
                                return;
                            }
                        });
                    }
                });
            }else{
                finish();
            }


        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                Glide.with(getApplication())
                        .load(bitmap) // Uri of the picture
                        .apply(RequestOptions.circleCropTransform())
                        .into(mProfileImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Glide.with(getApplication()).load(mProfileImageUrl).apply(RequestOptions.circleCropTransform()).into(mProfileImage);

        }
    }
}
