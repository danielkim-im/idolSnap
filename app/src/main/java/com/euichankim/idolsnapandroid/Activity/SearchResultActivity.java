package com.euichankim.idolsnapandroid.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.euichankim.idolsnapandroid.Adapter.SearchResultTabAdapter;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.material.tabs.TabLayout;

public class SearchResultActivity extends AppCompatActivity {

    TabLayout tabLayout;
    public static ViewPager2 viewPager2;
    public static int currentViewPagerPosition;
    SearchResultTabAdapter tabAdapter;
    TextView toolbarTextview;
    public static String searchedTag;
    ImageButton backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        searchedTag = getIntent().getStringExtra("tag");

        toolbarTextview = findViewById(R.id.searchresult_toolbar_txt);
        backBtn = findViewById(R.id.searchresult_backBtn);
        tabLayout = findViewById(R.id.searchresult_tablayout);
        viewPager2 = findViewById(R.id.searchresult_viewpager2);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.snap)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.user)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.hashtag)));

        FragmentManager fragmentManager = getSupportFragmentManager();
        tabAdapter = new SearchResultTabAdapter(fragmentManager, getLifecycle());
        viewPager2.setAdapter(tabAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
                currentViewPagerPosition = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbarTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchResultActivity.this, SearchActivity.class);
                intent.putExtra("text", searchedTag);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });

        toolbarTextview.setText(searchedTag);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}