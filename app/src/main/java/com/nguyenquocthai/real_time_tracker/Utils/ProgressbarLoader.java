package com.nguyenquocthai.real_time_tracker.Utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nguyenquocthai.real_time_tracker.R;


public class ProgressbarLoader {
    private Activity myactivity;
    private AlertDialog dialog;

    public ProgressbarLoader(Activity myactivity) {
        this.myactivity = myactivity;
    }

    public void showloader(){
        AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
        builder.setCancelable(false);

        // Inflate the layout for the dialog
        LayoutInflater inflater = myactivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_bar, null);
        builder.setView(dialogView);

        // Set the title text
        TextView txtTitle = dialogView.findViewById(R.id.txt_title);
        txtTitle.setText("Loading...");

        dialog = builder.create();
        dialog.show();
    }
    public void showloader(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(myactivity);
        builder.setCancelable(false);

        // Inflate the layout for the dialog
        LayoutInflater inflater = myactivity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.progress_bar, null);
        builder.setView(dialogView);

        // Set the title text
        TextView txtTitle = dialogView.findViewById(R.id.txt_title);
        if (title != null && !title.isEmpty()) {
            txtTitle.setText(title);
        }

        dialog = builder.create();
        dialog.show();
    }

    public void dismissloader(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
