package com.euichankim.idolsnapandroid.ViewHolder;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.euichankim.idolsnapandroid.Activity.HashtagActivity;
import com.euichankim.idolsnapandroid.Model.Tag;
import com.euichankim.idolsnapandroid.R;;

public class Tag_ViewHolder extends RecyclerView.ViewHolder {

    TextView textView;

    public Tag_ViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.itemtag_tagtxt);
    }

    public void bind(Tag tag){
        textView.setText(tag.getTag());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tagIntent = new Intent(itemView.getContext(), HashtagActivity.class);
                tagIntent.putExtra("tag", tag.getTag());
                itemView.getContext().startActivity(tagIntent);
                ((Activity) itemView.getContext()).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
