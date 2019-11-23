package com.haanhgs.shoppingtest.viewmodel;

import com.haanhgs.shoppingtest.repo.Filters;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private boolean isSigningIn;
    private Filters filters;

    public MainActivityViewModel() {
        isSigningIn = false;
        filters = Filters.getDefault();
    }

    public boolean getIsSigningIn() {
        return isSigningIn;
    }

    public void setIsSigningIn(boolean isSigningIn) {
        this.isSigningIn = isSigningIn;
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }
}
