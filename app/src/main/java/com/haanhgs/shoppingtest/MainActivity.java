package com.haanhgs.shoppingtest;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.adapter.RestaurantAdapter;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.Filters;
import com.haanhgs.shoppingtest.repo.RestaurantRepo;
import com.haanhgs.shoppingtest.viewmodel.AppViewModel;
import java.util.Collections;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements FilterDialogFragment.FilterListener{

    @BindView(R.id.tvCurrentFilter)
    TextView tvCurrentFilter;
    @BindView(R.id.tvCurrentSortBy)
    TextView tvCurrentSortBy;
    @BindView(R.id.bnClearFilter)
    ImageView bnClearFilter;
    @BindView(R.id.cvBar)
    CardView cvBar;
    @BindView(R.id.rvMain)
    RecyclerView rvMain;
    @BindView(R.id.clEmpty)
    ConstraintLayout clEmpty;
    @BindView(R.id.tbrMain)
    Toolbar tbrMain;

    public static final int LIMIT = 50;
    private static final String TAG = "MainActivity";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseFirestore firestore;
    private Query query;
    private FilterDialogFragment filterDialog;
    private RestaurantAdapter adapter;
    private AppViewModel viewModel;
    private FirebaseUser user;
    private RestaurantAdapter adapter1;

    private void initToolbar() {
        setSupportActionBar(tbrMain);
    }

    private void initFirestore() {
        firestore = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            query = firestore.collection("app")
                    .document(user.getUid()).collection("test")
                    .orderBy("avgRating", Query.Direction.DESCENDING)
                    .limit(LIMIT);
        }
    }

    private void openRestaurantDetail(DocumentSnapshot restaurant){
        Intent intent = new Intent(MainActivity.this, RestaurantDetailActivity.class);
        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());
        intent.putExtra(RestaurantDetailActivity.KEY_USER_ID, user.getUid());
        startActivity(intent);
    }

    private void initRecyclerView() {
        if (query == null) Log.w(TAG, "No query, not initializing RecyclerView");
        adapter = new RestaurantAdapter(query, this::openRestaurantDetail) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    rvMain.setVisibility(View.GONE);
                    clEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvMain.setVisibility(View.VISIBLE);
                    clEmpty.setVisibility(View.GONE);
                }
            }
            @Override protected void onError(FirebaseFirestoreException e){}
        };
        rvMain.setLayoutManager(new LinearLayoutManager(this));
        rvMain.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();
        viewModel = new ViewModelProvider(this).get(AppViewModel.class);
        FirebaseFirestore.setLoggingEnabled(true);
        initFirestore();
        initRecyclerView();
        filterDialog = new FilterDialogFragment();
    }

    private boolean shouldStartSignIn() {
        return (!viewModel.isSignIn() && user == null);
    }

    private void startUpdateData() {
        onFilter(viewModel.getFilters());
        if (adapter != null) {
            adapter.startListening();
        }
    }

    private void startSignIn() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        viewModel.setSignIn(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            viewModel.setSignIn(false);
            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
            startUpdateData();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }
        startUpdateData();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    private void onAddItemsClicked() {
        CollectionReference restaurants = firestore
                .collection("app").document(user.getUid()).collection("test");
        for (int i = 0; i < 10; i++) {
            Restaurant restaurant = RestaurantRepo.getRandom(this);
            restaurants.add(restaurant);
        }
    }

    @Override
    public void onFilter(Filters filters) {
        Query query = firestore.collection("app").document(user.getUid()).collection("test");
        if (filters.hasCategory()) {
            query = query.whereEqualTo("category", filters.getCategory());
        }
        if (filters.hasCity()) {
            query = query.whereEqualTo("city", filters.getCity());
        }
        if (filters.hasPrice()) {
            query = query.whereEqualTo("price", filters.getPrice());
        }
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        query = query.limit(LIMIT);
        this.query = query;
        adapter.setQuery(query);
        tvCurrentFilter.setText(Html.fromHtml(filters.getSearchDescription(this)));
        tvCurrentSortBy.setText(filters.getOrderDescription(this));
        viewModel.setFilters(filters);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_items:
                onAddItemsClicked();
                break;
            case R.id.menu_sign_out:
                AuthUI.getInstance().signOut(this);
                viewModel.setSignIn(false);
                onStart();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onButtonFilter() {
        filterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    public void onButtonClear() {
        filterDialog.resetFilters();
        onFilter(Filters.getDefault());
    }

    @OnClick({R.id.bnFilter, R.id.cvBar})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bnFilter:
                onButtonClear();
                break;
            case R.id.cvBar:
                onButtonFilter();
                break;
        }
    }
}
