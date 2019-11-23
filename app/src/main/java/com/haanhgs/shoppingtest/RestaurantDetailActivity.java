package com.haanhgs.shoppingtest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantDetailActivity extends AppCompatActivity implements
        View.OnClickListener,
        EventListener<DocumentSnapshot>,
        RatingDialogFragment.RatingListener {

    private static final String TAG = "RestaurantDetail";
    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";
    public static final String KEY_USER_ID = "key_user_id";
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

    private void initViews(){
        imageView = findViewById(R.id.restaurant_image);
        nameView = findViewById(R.id.restaurant_name);
        ratingIndicator = findViewById(R.id.restaurant_rating);
        numRatingsView = findViewById(R.id.restaurant_num_ratings);
        cityView = findViewById(R.id.restaurant_city);
        categoryView = findViewById(R.id.restaurant_category);
        priceView = findViewById(R.id.restaurant_price);
        emptyView = findViewById(R.id.view_empty_ratings);
        ratingsRecycler = findViewById(R.id.recycler_ratings);

        findViewById(R.id.restaurant_button_back).setOnClickListener(this);
        findViewById(R.id.fab_show_rating_dialog).setOnClickListener(this);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        initViews();


        // Get restaurant ID from extras
        String restaurantId = null;
        String uId = null;
        if (getIntent().getExtras() != null){
            restaurantId = getIntent().getExtras().getString(KEY_RESTAURANT_ID);
            uId = getIntent().getExtras().getString(KEY_USER_ID);
        }
        if (restaurantId == null || uId == null) {
            onBackPressed();
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }
        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        restaurantRef = firestore.collection("app").document(uId).collection("test").document(restaurantId);

        // Get ratings
        Query ratingsQuery = restaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MainActivity.LIMIT);

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

        ratingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        ratingsRecycler.setAdapter(ratingAdapter);

        ratingDialog = new RatingDialogFragment();
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
                if (restaurant != null){
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
                }
                return null;
            }
        });

    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "restaurant:onEvent", e);
            return;
        }
        Restaurant restaurant = snapshot.toObject(Restaurant.class);
        if (restaurant != null) onRestaurantLoaded(restaurant);
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

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        ratingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        addRating(restaurantRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        hideKeyboard();
                        ratingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if (view != null && manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
