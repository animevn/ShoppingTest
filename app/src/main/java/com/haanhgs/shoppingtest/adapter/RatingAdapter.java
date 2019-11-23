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
                .inflate(R.layout.recycler_item_rating, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rating rating = getSnapshot(position).toObject(Rating.class);
        if (rating != null){
            holder.bind(rating);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvName;
        final MaterialRatingBar ratingBar;
        final TextView tvContent;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_rating_name);
            ratingBar = itemView.findViewById(R.id.ratingbar_rating_dialog);
            tvContent = itemView.findViewById(R.id.tv_rating_content);
        }

        public void bind(Rating rating) {
            tvName.setText(rating.getUserName());
            ratingBar.setRating((float) rating.getRating());
            tvContent.setText(rating.getText());
        }
    }

}
