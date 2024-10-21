package com.nguyenquocthai.real_time_tracker.Fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nguyenquocthai.real_time_tracker.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AboutFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        githubImageView = view.findViewById(R.id.githubImageView);
        githubImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGitHubLink();
            }
        });
        return view;
    }
    private void openGitHubLink() {
        Uri githubUri = Uri.parse("https://github.com/GunterWi/63CLC1_MobileDev/tree/master/Real_Time_Tracker");
        Intent intent = new Intent(Intent.ACTION_VIEW, githubUri);
        startActivity(intent);
    }
    private ImageView githubImageView;

}