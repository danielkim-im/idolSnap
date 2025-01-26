package com.euichankim.idolsnapandroid.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.euichankim.idolsnapandroid.Activity.SearchResultActivity;
import com.euichankim.idolsnapandroid.Model.UserSearchRecord;
import com.euichankim.idolsnapandroid.R;;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    Context mContext;
    List<UserSearchRecord> tags;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    public SearchAdapter(Context mContext, List<UserSearchRecord> tags) {
        this.mContext = mContext;
        this.tags = tags;
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        holder.textView.setText(tags.get(position).getSearched());
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSearchRecord(tags.get(position).getSearched());
                Intent intent = new Intent(mContext, SearchResultActivity.class);
                intent.putExtra("tag", tags.get(position).getSearched());
                mContext.startActivity(intent);
                ((Activity) mContext).finish();
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout constraintLayout;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            constraintLayout = itemView.findViewById(R.id.searchitem_cons);
            textView = itemView.findViewById(R.id.searchitem_textview);
        }
    }

    private void saveSearchRecord(String searched) {
        if (searched.trim().length() != 0) {
            String key = searched.replaceAll("\\s", "").toLowerCase();
            Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
            calendar.add(Calendar.DATE, 90);
            UserSearchRecord userSearchRecord = new UserSearchRecord(searched.replaceAll("\\s+$", ""), key, FieldValue.serverTimestamp(), calendar.getTime());
            firestore.collection("user").document(mAuth.getCurrentUser().getUid())
                    .collection("search_record").document(key).set(userSearchRecord)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // saved search record
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
