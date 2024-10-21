package com.nguyenquocthai.real_time_tracker.Service;


import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nguyenquocthai.real_time_tracker.Model.Users;

import java.util.ArrayList;
import java.util.List;

public class ListFriend {
    private List<Users> nameList;
    private String current_uid;
    private DatabaseReference databaseReference, currentreference;

    public interface DataStatus {
        void DataIsLoaded(List<Users> users); // Updated to handle a single user
    }

    public ListFriend(String current_uid) {
        this.current_uid = current_uid;
        this.nameList = new ArrayList<>();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
        this.currentreference = FirebaseDatabase.getInstance().getReference("users").child(current_uid).child("circle_members");
    }

    public void getListFriend(final DataStatus dataStatus) {
        currentreference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                for (DataSnapshot dss : dataSnapshot.getChildren()) {
                    //friend ID
                    String circleid = dss.child("circleMemberId").getValue(String.class);
                    if (circleid != null) {
                        attachStatusListenerForUser(circleid, dataStatus);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void attachStatusListenerForUser(String userId, final DataStatus dataStatus) {
        // Get Friend's userId to search database and set nameList
        databaseReference.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if (user != null) {
                    int index = findUserInList(user);
                    if (index >= 0) {
                        nameList.set(index, user);
                    } else {
                        nameList.add(user);
                    }
                    dataStatus.DataIsLoaded(nameList); // Notifies about a single user
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private int findUserInList(Users user) {
        for (int i = 0; i < nameList.size(); i++) {
            if (nameList.get(i).getId().equals(user.getId())) {
                return i;
            }
        }
        return -1;
    }
}


