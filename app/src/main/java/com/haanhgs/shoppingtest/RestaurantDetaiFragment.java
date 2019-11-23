package com.haanhgs.shoppingtest;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.haanhgs.shoppingtest.adapter.RatingAdapter;
import com.haanhgs.shoppingtest.model.Rating;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.RestaurantRepo;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantDetaiFragment extends Fragment implements
        EventListener<DocumentSnapshot>,
        View.OnClickListener,
        RatingDialogFragment.RatingListener {

    private String restaurantId;
    private static final String TAG = "RestaurantDetail";
    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";
    private ImageView imageView;
    private TextView nameView;
    private MaterialRatingBar ratingIndicator;
    private TextView numRatingsView;
    private TextView cityView;
    private TextView categoryView;
    private TextView priceView;
    private ViewGroup emptyView;
    private RecyclerView ratingsRecycler;
    private RatingDialogFragment ratingDialog;
    private FirebaseFirestore firestore;
    private DocumentReference restaurantRef;
    private ListenerRegistration restaurantRegistration;
    private RatingAdapter ratingAdapter;
    private Context context;

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void initViews(View view){
        imageView = view.findViewById(R.id.restaurant_image);
        nameView = view.findViewById(R.id.restaurant_name);
        ratingIndicator = view.findViewById(R.id.restaurant_rating);
        numRatingsView = view.findViewById(R.id.restaurant_num_ratings);
        cityView = view.findViewById(R.id.restaurant_city);
        categoryView = view.findViewById(R.id.restaurant_category);
        priceView = view.findViewById(R.id.restaurant_price);
        emptyView = view.findViewById(R.id.view_empty_ratings);
        ratingsRecycler = view.findViewById(R.id.recycler_ratings);
        view.findViewById(R.id.restaurant_button_back).setOnClickListener(this);
        view.findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this);
    }

    public void onBackArrowClicked(View view) {
        if (getActivity() != null) ((MainActivity)getActivity()).clickBackArrowToReturn();
    }

    public void onAddRatingClicked(View view) {
        ratingDialog.show(getFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.restaurant_button_back:
                onBackArrowClicked(v);
                break;
            case R.id.fab_show_rating_dialog:
                onAddRatingClicked(v);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant_detail, container, false);
        initViews(view);
        if (getActivity() != null) ((MainActivity)getActivity()).showBackArrowToolbar();

        // Get restaurant ID from extras
        if (restaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        restaurantRef = firestore.collection("restaurants").document(restaurantId);

        // Get ratings
        Query ratingsQuery = restaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        ratingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    ratingsRecycler.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    ratingsRecycler.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
            }
        };

        ratingsRecycler.setLayoutManager(new LinearLayoutManager(context));
        ratingsRecycler.setAdapter(ratingAdapter);

        ratingDialog = new RatingDialogFragment();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ratingAdapter.startListening();
        restaurantRegistration = restaurantRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ratingAdapter.stopListening();

        if (restaurantRegistration != null) {
            restaurantRegistration.remove();
            restaurantRegistration = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null) ((MainActivity)getActivity()).hideBackArrowToolbar();
    }

    private void onRestaurantLoaded(Restaurant restaurant) {
        nameView.setText(restaurant.getName());
        ratingIndicator.setRating((float) restaurant.getAvgRating());
        numRatingsView.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
        cityView.setText(restaurant.getCity());
        categoryView.setText(restaurant.getCategory());
        priceView.setText(RestaurantRepo.getPriceString(restaurant));

        // Background image
        Glide.with(imageView.getContext())
                .load(restaurant.getPhoto())
                .into(imageView);
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot snapshot,
                        @Nullable FirebaseFirestoreException e) {

        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }

        onRestaurantLoaded(snapshot.toObject(Restaurant.class));

    }

    private Task<Void> addRating(final DocumentReference restaurantRef, final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = restaurantRef.collection("ratings")
                .document();

        // In a transaction, add the new rating and update the aggregate totals
        return firestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(@NonNull Transaction transaction)
                    throws FirebaseFirestoreException {

                Restaurant restaurant = transaction.get(restaurantRef)
                        .toObject(Restaurant.class);

                // Compute new number of ratings
                int newNumRatings = restaurant.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = restaurant.getAvgRating() *
                        restaurant.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) /
                        newNumRatings;

                // Set new restaurant info
                restaurant.setNumRatings(newNumRatings);
                restaurant.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(restaurantRef, restaurant);
                transaction.set(ratingRef, rating);

                return null;
            }
        });

    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onRating(Rating rating) {
        addRating(restaurantRef, rating)
                .addOnSuccessListener((Executor) this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        hideKeyboard();
                        ratingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener((Executor) this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        hideKeyboard();
                    }
                });
    }

}
