package com.nguyenquocthai.real_time_tracker.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nguyenquocthai.real_time_tracker.Utils.ProgressbarLoader;
import com.nguyenquocthai.real_time_tracker.R;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    // Lưu trạng thái
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUri != null) {
            outState.putString("savedImageUri", imageUri.toString());
        }
        outState.putBoolean("isImageBeingUpdated", isImageBeingUpdated);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initializeFirebase();
        initializeViews(view);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("savedImageUri")) {
                String savedImageUriString = savedInstanceState.getString("savedImageUri");
                imageUri = Uri.parse(savedImageUriString);
                Picasso.get().load(imageUri).into(imageView);
            }
            isImageBeingUpdated = savedInstanceState.getBoolean("isImageBeingUpdated", false);
        }

        setProfileDataListener();
        activityResultNew();
        setupImageViewListener();
        btnSave.setOnClickListener(v -> {
            mediaPlayer.start();
            saveProfile();
        });
        return view;
    }




    private void setProfileDataListener() {
        loader.showloader();
        //addValueEventListener() keep listening to query or database reference it is attached to.
        reference.child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("ProfileFragment", "Data changed");
                countFriend=0;
                if (snapshot.exists()) {
                    String firstname = snapshot.child("firstname").getValue(String.class);
                    String lastname = snapshot.child("lastname").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);
                    String image = snapshot.child("image_url").getValue(String.class);
                    String code = snapshot.child("circle_id").getValue(String.class);
                    firstnameEditText.setText(firstname);
                    lastnameEditText.setText(lastname);
                    emailEditText.setText(email);
                    passwordEditText.setText(password);
                    fullnameTextView.setText(lastname+" "+firstname);
                    codeTextView.setText(code);
                    if(image!="null" && !isImageBeingUpdated){
                        Picasso.get().load(image).into(imageView);
                    }
                    // executes onDataChange method immediately and after executing that method once, it stops listening to the reference location it is attached to.
                    checkReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dss : snapshot.getChildren()) {
                                String id = dss.child("circleMemberId").getValue(String.class);
                                countFriend++;
                                Log.d("Count",String.valueOf(countFriend));
                            }
                            loader.dismissloader();
                            friendTextView.setText(String.valueOf(countFriend));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            loader.dismissloader();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Data loading cancelled", error.toException());
            }
        });
        loader.dismissloader();
    }
    private void setupImageViewListener() {
        imageView.setOnClickListener(v -> {
            mediaPlayer.start();
            Log.d("ImageViewClick", "CircleImageView clicked");
            isImageBeingUpdated = true;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            pickImageLauncher.launch(i);
            //startActivityForResult(i, 12); it's Deprecated
        });
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
                                .getIntent(getActivity());
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
                        imageView.setImageURI(imageUri);
                        //Picasso.get().load(imageUri).into(imageView);
                    } else if (result.getResultCode() == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        // Handle the error
                        CropImage.ActivityResult cropResult = CropImage.getActivityResult(result.getData());
                        Exception error = cropResult.getError();
                        // Log or display the error
                    }
                });
    }
    private void uploadImage(String userid) {
        storage.child(userid + ".jpg").putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    updateUserImage(userid, downloadUrl);
                    isImageBeingUpdated = false;
                }));
    }

    private void updateUserImage(String userid, String downloadUrl) {
        reference.child(userid).child("image_url").setValue(downloadUrl);
    }

    private void saveProfile() {
        if(!firstnameEditText.getText().toString().isEmpty()
                || !lastnameEditText.getText().toString().isEmpty()
                || !emailEditText.getText().toString().isEmpty()
                || !passwordEditText.getText().toString().isEmpty() ){
            loader.showloader();
            Map<String, Object> update = new HashMap<>();
            update.put("firstname", firstnameEditText.getText().toString());
            update.put("lastname", lastnameEditText.getText().toString());
            update.put("email", emailEditText.getText().toString());
            update.put("password", passwordEditText.getText().toString());
            if(imageUri!=null){
                uploadImage(current_uid);
            }
            reference.child(current_uid).updateChildren(update).addOnCompleteListener(task -> {
                Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_SHORT).show();
                fullnameTextView.setText(lastnameEditText.getText().toString()+" "+firstnameEditText.getText().toString());
            });
            loader.dismissloader();
        }else{
            showToast("Please complete the information.");
        }

    }
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
    private void initializeViews(View view) {
        fullnameTextView = view.findViewById(R.id.textview_fullname);
        friendTextView = view.findViewById(R.id.textview_friend);
        codeTextView = view.findViewById(R.id.textview_code);
        firstnameEditText = view.findViewById(R.id.edittext_firstnameProfile);
        lastnameEditText = view.findViewById(R.id.edittext_lastnameProfile);
        emailEditText = view.findViewById(R.id.edittext_emailProfile);
        passwordEditText = view.findViewById(R.id.edittext_passwordProfile);
        imageView = view.findViewById(R.id.circleImageProfileView);
        btnSave = view.findViewById(R.id.save_button);
        checkReference= FirebaseDatabase.getInstance().getReference("users").child(current_uid).child("circle_members");
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        current_uid = user != null ? user.getUid() : "";
        loader = new ProgressbarLoader(getActivity());
        reference = FirebaseDatabase.getInstance().getReference("users");
        storage = FirebaseStorage.getInstance().getReference("users");
        mediaPlayer = MediaPlayer.create(getActivity(),R.raw.click);
    }
    private TextView fullnameTextView;
    private TextView friendTextView,codeTextView;
    private int countFriend=0;
    private EditText firstnameEditText, lastnameEditText, emailEditText, passwordEditText;
    private CircleImageView imageView;
    private Button btnSave;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference reference,checkReference;
    private static String current_uid;
    private Uri imageUri;
    private StorageReference storage;
    private ProgressbarLoader loader;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> cropImageLauncher;
    private boolean isImageBeingUpdated = false;
    private MediaPlayer mediaPlayer;

}
