package com.haanhgs.shoppingtest.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.R;
import com.haanhgs.shoppingtest.model.Rating;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
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
        if (rating != null) {
            holder.bind(rating);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvRatingName;
        @BindView(R.id.tvDate)
        TextView tvRatingDate;
        @BindView(R.id.mrbRating)
        MaterialRatingBar ratingbarRatingDialog;
        @BindView(R.id.tvContent)
        TextView tvRatingContent;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Rating rating) {
            tvRatingName.setText(rating.getUserName());
            ratingbarRatingDialog.setRating((float) rating.getRating());
            tvRatingContent.setText(rating.getText());
        }
    }

}
