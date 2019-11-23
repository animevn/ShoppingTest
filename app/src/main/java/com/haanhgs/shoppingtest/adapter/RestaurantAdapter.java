package com.haanhgs.shoppingtest.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.R;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.RestaurantRepo;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantAdapter extends FirestoreAdapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantSelectedListener {
        void onRestaurantSelected(DocumentSnapshot restaurant);
    }

    private final OnRestaurantSelectedListener mListener;

    public RestaurantAdapter(Query query, OnRestaurantSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.recycler_item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView ivImage;
        final TextView tvName;
        final MaterialRatingBar ratingBar;
        final TextView tvNumRating;
        final TextView tvPrice;
        final TextView tvCategory;
        final TextView tvCity;

        public ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_restaurant_image);
            tvName = itemView.findViewById(R.id.tv_restaurant_name);
            ratingBar = itemView.findViewById(R.id.ratingbar_restaurant);
            tvNumRating = itemView.findViewById(R.id.tv_restaurant_num_rating);
            tvPrice = itemView.findViewById(R.id.tv_restaurant_price);
            tvCategory = itemView.findViewById(R.id.tv_restaurant_category);
            tvCity = itemView.findViewById(R.id.tv_restaurant_city);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnRestaurantSelectedListener listener) {

            Restaurant restaurant = snapshot.toObject(Restaurant.class);
            Resources resources = itemView.getResources();
            if (restaurant != null){
                // Load image
                Glide.with(ivImage.getContext())
                        .load(restaurant.getPhoto())
                        .into(ivImage);
                tvName.setText(restaurant.getName());
                ratingBar.setRating((float) restaurant.getAvgRating());
                tvCity.setText(restaurant.getCity());
                tvCategory.setText(restaurant.getCategory());
                tvNumRating.setText(resources.getString(R.string.fmt_num_ratings,
                        restaurant.getNumRatings()));
                tvPrice.setText(RestaurantRepo.getPriceString(restaurant));
            }
            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onRestaurantSelected(snapshot);
                    }
                }
            });
        }
    }
}
