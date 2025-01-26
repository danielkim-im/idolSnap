package com.euichankim.idolsnapandroid.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.euichankim.idolsnapandroid.Fragment.SearchResultSnapFragment;
import com.euichankim.idolsnapandroid.Fragment.SearchResultTagFragment;
import com.euichankim.idolsnapandroid.Fragment.SearchResultUserFragment;

public class SearchResultTabAdapter extends FragmentStateAdapter {

    public SearchResultTabAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position==1){
            return new SearchResultUserFragment();
        }else if (position==2){
            return new SearchResultTagFragment();
        }
        return new SearchResultSnapFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
