package com.haanhgs.shoppingtest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.R;
import com.haanhgs.shoppingtest.model.Rating;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingAdapter extends FirestoreAdapter<RatingAdapter.ViewHolder> {

    public RatingAdapter(Query query) {
        super(query);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rating, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rating rating = getSnapshot(position).toObject(Rating.class);
        if (rating != null){
            holder.bind(rating);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameView;
        MaterialRatingBar ratingBar;
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.rating_item_name);
            ratingBar = itemView.findViewById(R.id.rating_item_rating);
            textView = itemView.findViewById(R.id.rating_item_text);
        }

        public void bind(Rating rating) {
            nameView.setText(rating.getUserName());
            ratingBar.setRating((float) rating.getRating());
            textView.setText(rating.getText());
        }
    }

}
