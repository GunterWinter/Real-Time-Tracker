package com.nguyenquocthai.real_time_tracker.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nguyenquocthai.real_time_tracker.Service.CircleMemberChecker;
import com.nguyenquocthai.real_time_tracker.Model.NotificationItem;
import com.nguyenquocthai.real_time_tracker.Model.Users;
import com.nguyenquocthai.real_time_tracker.Utils.ProgressbarLoader;
import com.nguyenquocthai.real_time_tracker.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class JoinCircleFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_circle, container, false);
        initializeViews(view);
        getMyProfile();
        joinbtn.setOnClickListener(v -> {
            mediaPlayer.start();
            joinbtnlistener();
        });
        return view;
    }
    private void getMyProfile(){
        databaseReference.child(currentID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users = snapshot.getValue(Users.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void joinbtnlistener() {
        loader.showloader();
        Query query = databaseReference.orderByChild("circle_id").equalTo(pinview.getValue());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dss : snapshot.getChildren()) {
                        String firstname = dss.child("firstname").getValue(String.class);
                        String lastname = dss.child("lastname").getValue(String.class);
                        String fcmToken = dss.child("fcmToken").getValue(String.class);
                        String broId = dss.child("id").getValue(String.class);

                        CircleMemberChecker checker = new CircleMemberChecker(currentID);
                        checker.checkIfMember(broId, new CircleMemberChecker.CircleMemberCheckListener() {
                                    @Override
                                    public void onCheckComplete(boolean isMember) {
                                        if (isMember) {
                                            Toast.makeText(getActivity(), "You are already friends with " + lastname + " " + firstname, Toast.LENGTH_SHORT).show();
                                        } else {
                                            sendNotification(currentID, users.getLastname() + " " + users.getFirstname(), fcmToken);
                                            addNotification(broId,users.getLastname() + " " + users.getFirstname()+" wanna make friend with you :D",users.getImage_url());
                                            Toast.makeText(getActivity(), "Invited", Toast.LENGTH_SHORT).show();
                                        }
                                        loader.dismissloader();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getActivity(), "This code is not available", Toast.LENGTH_SHORT).show();
                    loader.dismissloader();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loader.dismissloader();
            }
        });
    }
    // upload firebase
    private void addNotification(String broId,String message,String avatar) {

        countReference.child(broId).child("notification").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                id=(int) snapshot.getChildrenCount();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        NotificationItem ob = new NotificationItem(currentID,message,System.currentTimeMillis(),avatar); // thông báo của mình
        databaseReference.child(broId).child("notification").child(String.valueOf(id)).setValue(ob); // set lên thông báo của bạn
    }
    private void sendNotification(String userID,String name,String fcmToken){
        try {
            JSONObject jsonObject = new JSONObject();

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",name);
            notificationObj.put("body","You have a request location from "+name);

            JSONObject dataObj = new JSONObject();
            dataObj.put("userID",userID);
            dataObj.put("name",name);

            jsonObject.put("notification",notificationObj);
            jsonObject.put("data",dataObj);
            jsonObject.put("to",fcmToken);
            callAPI(jsonObject);

        }catch (Exception e){
        }
    }
    private void callAPI(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();
        String url ="https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(),JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAAn4bypaU:APA91bGpG43P1_I4wuEhJIdlxeOlkf8WkuUglKz82oToGtZQCHXsDB_Jhy8visH2tx1w0j-hA1vhBcApVujSiZlaNRjUmLPTRV_gbU2yED4btAJ-7LrC0a6gBi5YN1Q9Oqbacwnxa_0e")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
    private void initializeViews(View view) {
        pinview= view.findViewById(R.id.pinview);
        joinbtn = view.findViewById(R.id.join_button);
        loader = new ProgressbarLoader(getActivity());
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        currentID = user != null ? user.getUid() : "";
        countReference= FirebaseDatabase.getInstance().getReference().child("users");
        databaseReference= FirebaseDatabase.getInstance().getReference().child("users");
        mediaPlayer = MediaPlayer.create(getActivity(),R.raw.click);
    }
    private String currentID;
    private Pinview pinview;
    private Button joinbtn;
    private DatabaseReference databaseReference,countReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ProgressbarLoader loader;
    private int id;
    private Users users;
    private MediaPlayer mediaPlayer;

}