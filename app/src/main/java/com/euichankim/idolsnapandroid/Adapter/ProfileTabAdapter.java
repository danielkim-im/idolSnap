package com.euichankim.idolsnapandroid.Adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.euichankim.idolsnapandroid.Fragment.ProfileCollectionFragment;
import com.euichankim.idolsnapandroid.Fragment.ProfileSnapFragment;

public class ProfileTabAdapter extends FragmentStateAdapter {


    String user_id;
    String currentContext;
    public ProfileTabAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, String user_id, String currentContext) {
        super(fragmentManager, lifecycle);
        this.user_id = user_id;
        this.currentContext = currentContext;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position==1){
            return new ProfileCollectionFragment(user_id);
        }
        return new ProfileSnapFragment(user_id, currentContext);
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
