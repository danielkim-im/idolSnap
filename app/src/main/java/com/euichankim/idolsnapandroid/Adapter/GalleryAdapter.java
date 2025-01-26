package com.euichankim.idolsnapandroid.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.euichankim.idolsnapandroid.R;;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context mContext;
    private List<String> items;
    protected GalleryListener galleryListener;

    public GalleryAdapter(Context mContext, List<String> items, GalleryListener galleryListener) {
        this.mContext = mContext;
        this.items = items;
        this.galleryListener = galleryListener;
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(mContext).inflate(R.layout.item_gallery, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryAdapter.ViewHolder holder, int position) {
        String item = items.get(position);

        Glide.with(mContext).load(item).into(holder.imageView);

        File file = new File(item);
        if (Integer.parseInt(String.valueOf(file.length()/1024/1024))>5){
            holder.alertCons.setVisibility(View.VISIBLE);
        }else{
            holder.alertCons.setVisibility(View.GONE);
        }

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ConstraintLayout alertCons;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.gallery_imageview);
            alertCons = itemView.findViewById(R.id.gallery_alertcons);
            alertCons.setVisibility(View.GONE);
        }
    }

    public interface GalleryListener{
        void onItemClick(String path);
    }
}
