package com.euichankim.idolsnapandroid.Adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.euichankim.idolsnapandroid.Model.UserStrikeRecord;
import com.euichankim.idolsnapandroid.R;;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class UserStrikeRecordAdapter extends RecyclerView.Adapter<UserStrikeRecordAdapter.ViewHolder> {

    private Context mContext;
    private List<UserStrikeRecord> records;
    FirebaseAuth mAuth;
    FirebaseFirestore firestore;

    public UserStrikeRecordAdapter(Context mContext, List<UserStrikeRecord> records) {
        this.mContext = mContext;
        this.records = records;
    }

    @NonNull
    @Override
    public UserStrikeRecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user_strikerecord, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserStrikeRecordAdapter.ViewHolder holder, int position) {
        String reason = null;
        if (records.get(position).getReason() != null) {
            reason = "\n\n" + "Vision AI Scan Result:" +
                    "\n" + "Adult: " + records.get(position).getReason().get("adult") +
                    "\n" + "Spoof: " + records.get(position).getReason().get("spoof") +
                    "\n" + "Medical: " + records.get(position).getReason().get("medical") +
                    "\n" + "Violence: " + records.get(position).getReason().get("violence") +
                    "\n" + "Racy: " + records.get(position).getReason().get("racy");
        }

        String report_type = records.get(position).getStrike_type();

        String descTxt = "StrikeID:\n" + records.get(position).getStrike_id() +
                "\n\n" + "Report Type: " + report_type +
                "\n\n" + "Violation found in: " + records.get(position).getStriked() + reason;
        holder.dateTxt.setText(timestampToString(((Timestamp) records.get(position).getStriked_at()).getSeconds() * 1000));
        holder.descTxt.setText(descTxt);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView descTxt, dateTxt;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();

            descTxt = itemView.findViewById(R.id.strikerecord_textview);
            dateTxt = itemView.findViewById(R.id.strikerecord_dateTxt);
            imageView = itemView.findViewById(R.id.strikerecord_imageview);
        }
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date;
        if (Locale.getDefault().getLanguage().equals("ko")) {
            date = DateFormat.format("M월 d일 yyyy년", calendar).toString();
            //date = DateFormat.format("MMM d", calendar).toString();
        } else {
            date = DateFormat.format("MMM d yyyy", calendar).toString();
        }
        return date;
    }
}
