package com.nguyenquocthai.real_time_tracker.Fragments;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.nguyenquocthai.real_time_tracker.Adapter.MembersAdapter;
import com.nguyenquocthai.real_time_tracker.Service.ListFriend;
import com.nguyenquocthai.real_time_tracker.Model.Users;
import com.nguyenquocthai.real_time_tracker.R;
import com.nguyenquocthai.real_time_tracker.Model.SharedViewModel;

import java.util.ArrayList;
import java.util.List;

public class MyCircleFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_circle, container, false);
        recyclerView= view.findViewById(R.id.myCircleRecyclerView);
        auth= FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        mediaPlayer = MediaPlayer.create(getActivity(),R.raw.click);
        nameList= new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        // Initialize your adapter with the empty list
        adapter = new MembersAdapter(nameList, getActivity());
        recyclerView.setAdapter(adapter);

        // Assuming ListFriend class can update the nameList with the latest data
        listFriend = new ListFriend(user.getUid());
        listFriend.getListFriend(new ListFriend.DataStatus() {
            @Override
            public void DataIsLoaded(List<Users> users) {
                adapter.updateMembersList(users);
            }
        });
        adapter.setOnMemberClickListener(new MembersAdapter.OnMemberClickListener() {
            @Override
            public void onMemberClick(Users user) {
                mediaPlayer.start();
                SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                viewModel.setLocationData(new LatLng(user.getLatitude(), user.getLongitude()));
                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                /*Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);*/
                //SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
                //viewModel.setOtherData(yourOtherData);
            }
        });
        return view;

    }
    private RecyclerView recyclerView;
    private MembersAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private List<Users> nameList;
    //DatabaseReference databaseReference, currentreference;
    private ListFriend listFriend;
    private MediaPlayer mediaPlayer;


}