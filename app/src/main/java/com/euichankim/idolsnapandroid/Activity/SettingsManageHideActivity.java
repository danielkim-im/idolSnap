package com.euichankim.idolsnapandroid.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ImageButton;

import com.euichankim.idolsnapandroid.R;

public class SettingsManageHideActivity extends AppCompatActivity {

    ImageButton backBtn;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_manage_hide);

        backBtn = findViewById(R.id.managehide_backbtn);
        recyclerView = findViewById(R.id.managehide_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(SettingsManageHideActivity.this, LinearLayoutManager.VERTICAL, false));


    }
}