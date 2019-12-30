package com.haanhgs.shoppingtest.viewmodel;

import com.haanhgs.shoppingtest.repo.Filters;
import androidx.lifecycle.ViewModel;

public class AppViewModel extends ViewModel {

    private boolean signIn;
    private Filters filters;

    public AppViewModel() {
        signIn = false;
        filters = Filters.getDefault();
    }

    public boolean isSignIn() {
        return signIn;
    }

    public void setSignIn(boolean isSignIn) {
        this.signIn = isSignIn;
    }

    public Filters getFilters() {
        return filters;
    }

    public void setFilters(Filters filters) {
        this.filters = filters;
    }
}
