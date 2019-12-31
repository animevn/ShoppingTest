package com.haanhgs.shoppingtest.adapter;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.R;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.RestaurantRepo;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantAdapter extends FirestoreAdapter<RestaurantAdapter.ViewHolder> {

    public interface OnRestaurantSelectedListener {
        void onRestaurantSelected(DocumentSnapshot restaurant);
    }

    private final OnRestaurantSelectedListener listener;

    public RestaurantAdapter(Query query, OnRestaurantSelectedListener listener) {
        super(query);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.recycler_item_restaurant, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivImage)
        ImageView ivRestaurantImage;
        @BindView(R.id.tvName)
        TextView tvRestaurantName;
        @BindView(R.id.tvPrice)
        TextView tvRestaurantPrice;
        @BindView(R.id.mrbRestaurant)
        MaterialRatingBar ratingbarRestaurant;
        @BindView(R.id.tvNumRating)
        TextView tvRestaurantNumRating;
        @BindView(R.id.tvCategory)
        TextView tvRestaurantCategory;
        @BindView(R.id.tvCity)
        TextView tvRestaurantCity;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnRestaurantSelectedListener listener) {

            Restaurant restaurant = snapshot.toObject(Restaurant.class);
            Resources resources = itemView.getResources();
            if (restaurant != null) {
                Glide.with(ivRestaurantImage.getContext())
                        .load(restaurant.getPhoto())
                        .into(ivRestaurantImage);
                tvRestaurantName.setText(restaurant.getName());
                ratingbarRestaurant.setRating((float) restaurant.getAvgRating());
                tvRestaurantCity.setText(restaurant.getCity());
                tvRestaurantCategory.setText(restaurant.getCategory());
                tvRestaurantNumRating.setText(resources.getString(R.string.fmt_num_ratings,
                        restaurant.getNumRatings()));
                tvRestaurantPrice.setText(RestaurantRepo.getPriceString(restaurant));
            }
            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onRestaurantSelected(snapshot);
                }
            });
        }
    }
}
