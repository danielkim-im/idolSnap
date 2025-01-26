package com.euichankim.idolsnapandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.euichankim.idolsnapandroid.Activity.SearchResultActivity;
import com.euichankim.idolsnapandroid.R;

import java.util.List;

public class DiscoverInterestedTagAdapter extends RecyclerView.Adapter<DiscoverInterestedTagAdapter.ViewHolder> {

    private Context mContext;
    private List<String> tagList;

    public DiscoverInterestedTagAdapter(Context mContext, List<String> tagList) {
        this.mContext = mContext;
        this.tagList = tagList;
    }

    @NonNull
    @Override
    public DiscoverInterestedTagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_interestedtag_discover, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscoverInterestedTagAdapter.ViewHolder holder, int position) {
        holder.textView.setText(tagList.get(position));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SearchResultActivity.class);
                intent.putExtra("tag", tagList.get(position));
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textView;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.it_discover_textview);
            cardView = itemView.findViewById(R.id.it_discover_cardview1);
        }
    }
}
