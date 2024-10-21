package com.nguyenquocthai.real_time_tracker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nguyenquocthai.real_time_tracker.Model.MembersDiffCallback;
import com.nguyenquocthai.real_time_tracker.Model.Users;
import com.nguyenquocthai.real_time_tracker.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberViewHolder> {
    List<Users> nameList;
    Context context;

    public MembersAdapter(List<Users> nameList, Context context) {
        this.nameList = nameList;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout,parent,false);
        MemberViewHolder memberViewHolder = new MemberViewHolder(v,context,nameList);

        return memberViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Users userobj = nameList.get(position);
        holder.nametxt.setText(userobj.getLastname()+" "+userobj.getFirstname());
        /*String strobj = namelist.get(position);
        holder.nametxt.setText(strobj);*/
        if(userobj.getIsOnline()==1){
            holder.status.setImageResource(R.drawable.ic_green_online);
        }
        else {
            holder.status.setImageResource(R.drawable.ic_red_offline);
        }
        //final String strobj1 = idlist.get(position);
        Picasso.get().load(userobj.getImage_url()).placeholder(R.drawable.ic_user).into(holder.circleImageView);
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (listener != null && currentPosition != RecyclerView.NO_POSITION) {
                listener.onMemberClick(nameList.get(currentPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }
    public void updateMembersList(List<Users> newUsersList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new MembersDiffCallback(this.nameList, newUsersList));
        this.nameList.clear();
        this.nameList.addAll(newUsersList);
        result.dispatchUpdatesTo(this);
    }
    public interface OnMemberClickListener {
        void onMemberClick(Users user);
    }
    private OnMemberClickListener listener;

    public void setOnMemberClickListener(OnMemberClickListener onMemberClickListener) {
        this.listener = onMemberClickListener;
    }

    public static class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView nametxt;
        CircleImageView circleImageView;
        ImageView status;
        Context c;
        List<Users> namearraylist;
        FirebaseAuth auth;
        FirebaseUser user;
        public MemberViewHolder (@NonNull View itemView, Context c, List<Users> namearraylist)
        {
            super(itemView);
            this.c = c;
            this.namearraylist = namearraylist;

            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

            nametxt = itemView.findViewById(R.id.textview_name);
            circleImageView=itemView.findViewById(R.id.lazyAvatar);
            status=itemView.findViewById(R.id.statusImageView);
        }

    }
}
