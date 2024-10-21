package com.nguyenquocthai.real_time_tracker.Service;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CircleMemberChecker { //

    private DatabaseReference databaseReference;

    public CircleMemberChecker(String currentUserId) {
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("circle_members");
    }

    public void checkIfMember(String memberId, final CircleMemberCheckListener listener) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isMember = false;
                for (DataSnapshot dss : snapshot.getChildren()) {
                    String id = dss.child("circleMemberId").getValue(String.class);
                    if (id != null && id.equals(memberId)) {
                        isMember = true;
                        break;
                    }
                }
                listener.onCheckComplete(isMember); // call back
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCheckComplete(false);
            }
        });
    }
    public interface CircleMemberCheckListener {
        void onCheckComplete(boolean isMember);
    }
}

