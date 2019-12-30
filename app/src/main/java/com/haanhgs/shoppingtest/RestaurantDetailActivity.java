package com.haanhgs.shoppingtest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.adapter.RatingAdapter;
import com.haanhgs.shoppingtest.model.Rating;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.RestaurantRepo;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RestaurantDetailActivity extends AppCompatActivity implements
        EventListener<DocumentSnapshot>,
        RatingDialogFragment.RatingListener {

    @BindView(R.id.ivRestaurant)
    ImageView ivRestaurant;
    @BindView(R.id.ivBack)
    ImageView ivBack;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.mrbRestaurant)
    MaterialRatingBar mrbRestaurant;
    @BindView(R.id.tvNumRating)
    TextView tvNumRating;
    @BindView(R.id.tvCategory)
    TextView tvCategory;
    @BindView(R.id.tvCity)
    TextView tvCity;
    @BindView(R.id.tvPrice)
    TextView tvPrice;
    @BindView(R.id.fabRestaurant)
    FloatingActionButton fabRestaurant;
    @BindView(R.id.rvRestaurant)
    RecyclerView rvRestaurant;
    @BindView(R.id.lnEmpty)
    LinearLayout lnEmpty;

    private static final String TAG = "RestaurantDetail";
    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";
    public static final String KEY_USER_ID = "key_user_id";

    private RatingDialogFragment ratingDialog;
    private FirebaseFirestore firestore;
    private DocumentReference restaurantRef;
    private ListenerRegistration restaurantRegistration;
    private RatingAdapter ratingAdapter;
    private String restaurantId;
    private String uId;

    private void initFireStore(){
        firestore = FirebaseFirestore.getInstance();
        restaurantRef = firestore.collection("app").document(uId)
                .collection("test").document(restaurantId);
    }

    private void getIntentResult(){
        // Get restaurant ID from extras
        String restaurantTemp = null;
        String uIdTemp = null;
        if (getIntent().getExtras() != null) {
            restaurantTemp = getIntent().getExtras().getString(KEY_RESTAURANT_ID);
            uIdTemp = getIntent().getExtras().getString(KEY_USER_ID);
        }
        if (restaurantTemp == null || uIdTemp == null) {
            onBackPressed();
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }else {
            restaurantId = restaurantTemp;
            uId = uIdTemp;
        }
    }

    private void initAdapter(){
        Query ratingsQuery = restaurantRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(MainActivity.LIMIT);
        ratingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    rvRestaurant.setVisibility(View.GONE);
                    lnEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvRestaurant.setVisibility(View.VISIBLE);
                    lnEmpty.setVisibility(View.GONE);
                }
            }
        };
    }

    private void initRecyclerView(){
        rvRestaurant.setLayoutManager(new LinearLayoutManager(this));
        rvRestaurant.setAdapter(ratingAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_detail);
        ButterKnife.bind(this);
        getIntentResult();
        initFireStore();
        initAdapter();
        initRecyclerView();
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
        return firestore.runTransaction(transaction -> {
            Restaurant restaurant = transaction.get(restaurantRef).toObject(Restaurant.class);

            // Compute new number of ratings
            if (restaurant != null) {
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
        tvName.setText(restaurant.getName());
        mrbRestaurant.setRating((float) restaurant.getAvgRating());
        tvNumRating.setText(getString(R.string.fmt_num_ratings, restaurant.getNumRatings()));
        tvCity.setText(restaurant.getCity());
        tvCategory.setText(restaurant.getCategory());
        tvPrice.setText(RestaurantRepo.getPriceString(restaurant));
        Glide.with(ivRestaurant.getContext()).load(restaurant.getPhoto()).into(ivRestaurant);
    }

    @Override
    public void onRating(Rating rating) {
        addRating(restaurantRef, rating).addOnSuccessListener(this, aVoid -> {
            Log.d(TAG, "Rating added");
            hideKeyboard();
            rvRestaurant.smoothScrollToPosition(0);
        }).addOnFailureListener(this, e -> {
            Log.w(TAG, "Add rating failed", e);
            hideKeyboard();
            Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                    Snackbar.LENGTH_SHORT).show();
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (view != null && manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    public void onAddRatingClicked(View view) {
        ratingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @OnClick({R.id.ivBack, R.id.fabRestaurant})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivBack:
                onBackArrowClicked(view);
                break;
            case R.id.fabRestaurant:
                onAddRatingClicked(view);
                break;
        }
    }
}
