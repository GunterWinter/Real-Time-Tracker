package com.nguyenquocthai.real_time_tracker.Service;

import static android.app.PendingIntent.getActivity;

import android.app.Activity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nguyenquocthai.real_time_tracker.Model.CircleJoin;
import com.nguyenquocthai.real_time_tracker.Utils.ProgressbarLoader;

public class AcceptShare {

    public AcceptShare(String userID, Activity myactivity) {
        this.userID = userID;
        this.myactivity = myactivity;
    }

    public void Execute(){
        initializeViews();
        currentID=user.getUid();
        currentReference = FirebaseDatabase.getInstance().getReference().child("users").child(currentID).child("circle_members");
        loader.showloader();
        friendReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("circle_members");
        checker =new CircleMemberChecker(currentID);
        checker.checkIfMember(userID, new CircleMemberChecker.CircleMemberCheckListener() {
                    @Override
                    public void onCheckComplete(boolean isMember) {
                        if (isMember) {
                            Toast.makeText(myactivity, "You are already friends", Toast.LENGTH_SHORT).show();
                            loader.dismissloader();
                        }else {
                            CircleJoin join = new CircleJoin(userID); // friend
                            CircleJoin join1 = new CircleJoin(user.getUid()); // my
                            //set my current id people's code
                            currentReference.child(userID).setValue(join);
                            //set friend id my code
                            friendReference.child(currentID).setValue(join1)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(myactivity, "joined success", Toast.LENGTH_SHORT).show();
                                            loader.dismissloader();
                                        }
                                    });
                        }
                    }
                });
    }
    private void initializeViews() {
        loader = new ProgressbarLoader(myactivity);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }
    private String currentID;
    private String userID;
    private Activity myactivity;

    private DatabaseReference currentReference,friendReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ProgressbarLoader loader;
    private CircleMemberChecker checker;
}
