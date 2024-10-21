package com.nguyenquocthai.real_time_tracker.Model;

import androidx.recyclerview.widget.DiffUtil;


import java.util.List;

public class MembersDiffCallback extends DiffUtil.Callback {
    private final List<Users> oldList;
    private final List<Users> newList;

    public MembersDiffCallback(List<Users> oldList, List<Users> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}
