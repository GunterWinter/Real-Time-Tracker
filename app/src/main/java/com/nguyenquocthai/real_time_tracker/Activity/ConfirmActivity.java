package com.nguyenquocthai.real_time_tracker.Activity;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nguyenquocthai.real_time_tracker.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyenquocthai.real_time_tracker.Model.Users;
import com.nguyenquocthai.real_time_tracker.Utils.ProgressbarLoader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        Initiation();
        activityResultNew();
        handleIncomingIntent();
        setupImageViewListener();
        setupConfirmButtonListener();
        loader.dismissloader();
    }
    private void activityResultNew() {
        //start image
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        // Start cropping activity
                        Intent cropIntent = CropImage.activity(result.getData().getData())
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .getIntent(this);
                        cropImageLauncher.launch(cropIntent);
                    }
                });
        // if picked an image then crop
        // Setup the launcher for cropping the image
        cropImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                        imageUri = cropResult.getUri();
                        circleImageView.setImageURI(imageUri);
                    } else if (result.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        // Handle the error
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                        Exception error = cropResult.getError();
                        // Log or display the error
                    }
                });
    }

    private void handleIncomingIntent() {
        loader.showloader("Generating Code...");
        Intent myIntent = getIntent();
        if (myIntent != null) {
            email = myIntent.getStringExtra("email");
            password = myIntent.getStringExtra("password");
            firstname = myIntent.getStringExtra("firstname");
            lastname = myIntent.getStringExtra("lastname");
            readEmail.setText(email);
            readPassword.setText(password);
            readFirstName.setText(firstname);
            readLastName.setText(lastname);
        }
        Code.setText(generateAndCheckCode());
    }
    //set click image
    private void setupImageViewListener() {
        circleImageView.setOnClickListener(v -> {
            mediaPlayer.start();
            Log.d("ImageViewClick", "CircleImageView clicked");
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            pickImageLauncher.launch(i);
            //startActivityForResult(i, 12); it's Deprecated
        });
    }
    private void setupConfirmButtonListener() {
        confirm.setOnClickListener(v -> {
            mediaPlayer.start();
            signupListener();
        });
    }

    private void signupListener() {
        loader.showloader();

        if (imageUri == null) {
            showToast("Please choose an image");
            loader.dismissloader();
            return;
        }

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String strdate = dateFormat.format(date);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (!task.isSuccessful()) {
                        showToast("database response error");
                        loader.dismissloader();
                        return;
                    }

                    handleUserRegistration(strdate);
                });
    }

    private void handleUserRegistration(String strdate) {
        String userid = auth.getCurrentUser().getUid();
        Users info = new Users(userid, readFirstName.getText().toString(), readLastName.getText().toString(), Code.getText().toString(), email, password, strdate, "null", 0, 0,0,"null");

        reference.child(userid).setValue(info)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        showToast("Error setting user info");
                        loader.dismissloader();
                        return;
                    }
                    uploadImage(userid);
                });
    }

    private void uploadImage(String userid) {
        storage.child(userid + ".jpg").putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateUserImage(userid, downloadUrl);
                }));
    }

    private void updateUserImage(String userid, String downloadUrl) {
        reference.child(userid).child("image_url").setValue(downloadUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        showToast("Create account succesfully");
                        navigateToLogin();
                    } else {
                        showToast("Error updating image");
                    }
                    loader.dismissloader();
                });
    }

    private void showToast(String message) {
        Toast.makeText(ConfirmActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        Intent in = new Intent(ConfirmActivity.this, LoginActivity.class);
        startActivity(in);
        finish();
    }

    // Code is deprecated
  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //start cropimage
        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            CropImage.activity(data.getData())
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }
        //after chose
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                circleImageView.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }*/

    private String generateAndCheckCode() {
        final String code = generateCode();
        reference.child("users").orderByChild("circle_id").equalTo(code).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // If the code exists, recursively call the method again
                    generateAndCheckCode();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        return code;
    }


    private String generateCode() {
        Random r = new Random();
        int intcode = 100000 + r.nextInt(900000);
        return String.valueOf(intcode);
    }
    private void Initiation() {
        readEmail = findViewById(R.id.editText_readmail);
        readPassword = findViewById(R.id.editText_readpassword);
        readFirstName = findViewById(R.id.editText_firstnamechange);
        readLastName = findViewById(R.id.editText_lastnamechange);
        confirm = findViewById(R.id.confirm_button);
        circleImageView = findViewById(R.id.circleImageProfileView);
        Code = findViewById(R.id.txtcircle_id);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");
        storage = FirebaseStorage.getInstance().getReference("users");
        loader = new ProgressbarLoader(ConfirmActivity.this);
        mediaPlayer = MediaPlayer.create(this,R.raw.click);
    }

    private EditText readEmail;
    private EditText readPassword;
    private EditText readFirstName;
    private EditText readLastName;
    private Button confirm;
    private TextView Code;
    private String email, password, firstname, lastname;
    private Uri imageUri;
    private CircleImageView circleImageView;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference;
    private StorageReference storage;
    private ProgressbarLoader loader;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private MediaPlayer mediaPlayer;

}