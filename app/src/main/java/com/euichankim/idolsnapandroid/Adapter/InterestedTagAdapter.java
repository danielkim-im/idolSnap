package com.euichankim.idolsnapandroid.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.euichankim.idolsnapandroid.Interface.IdolListListener;
import com.euichankim.idolsnapandroid.Model.IdolList;
import com.euichankim.idolsnapandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InterestedTagAdapter extends RecyclerView.Adapter<InterestedTagAdapter.ViewHolder> {

    private List<IdolList> artist;
    private IdolListListener idolListListener;

    public InterestedTagAdapter(List<IdolList> artist, IdolListListener idolListListener) {
        this.artist = artist;
        this.idolListListener = idolListListener;
    }

    @NonNull
    @Override
    public InterestedTagAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interestedtag_setup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InterestedTagAdapter.ViewHolder holder, int position) {
        holder.bindIdol(artist.get(position));
    }

    @Override
    public int getItemCount() {
        return artist.size();
    }

    public List<String> getSelectedArtists() {
        List<String> selectedArtists = new ArrayList<>();
        for (IdolList idolList : artist) {
            if (idolList.isSelected) {
                selectedArtists.add(idolList.getTag_eng());
                selectedArtists.add(idolList.getTag_kr());
            }
        }
        return selectedArtists;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.ititem_text);
            linearLayout = itemView.findViewById(R.id.ititem_lin);
        }

        void bindIdol(final IdolList artist) {
            if (Locale.getDefault().getLanguage().equals("ko")) {
                textView.setText(artist.getName_kr());
            } else {
                textView.setText(artist.getName_eng());
            }
            if (artist.isSelected) {
                linearLayout.setBackgroundResource(R.color.red);
            } else {
                linearLayout.setBackgroundResource(R.drawable.background_interestedtag1);
            }
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (artist.isSelected) {
                        linearLayout.setBackgroundResource(R.drawable.background_interestedtag1);
                        artist.isSelected = false;
                        if (getSelectedArtists().size() == 0) {
                            idolListListener.onArtistSelected(false);
                        }
                    } else {
                        // 10 equals to 5 idol with tag in both eng and kr
                        if (!(getSelectedArtists().size() >= 10)) {
                            linearLayout.setBackgroundResource(R.color.red);
                            artist.isSelected = true;
                            idolListListener.onArtistSelected(true);
                        } else {
                            Toast.makeText(itemView.getContext(), itemView.getContext().getString(R.string.bias_chooseupto5), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
