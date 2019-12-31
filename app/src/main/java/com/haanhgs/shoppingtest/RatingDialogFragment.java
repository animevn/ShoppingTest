package com.haanhgs.shoppingtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.haanhgs.shoppingtest.model.Rating;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingDialogFragment extends DialogFragment {

    public static final String TAG = "RatingDialog";
    @BindView(R.id.mrbRating)
    MaterialRatingBar mrbRating;
    @BindView(R.id.tvContent)
    EditText tvContent;
    @BindView(R.id.bnCancel)
    Button bnCancel;
    @BindView(R.id.bnSubmit)
    Button bnSubmit;

    interface RatingListener {
        void onRating(Rating rating);
    }

    private RatingListener ratingListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RatingListener) {
            ratingListener = (RatingListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_rating, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void onSubmitClicked(View view) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            Rating rating = new Rating(
                    FirebaseAuth.getInstance().getCurrentUser(),
                    mrbRating.getRating(),
                    tvContent.getText().toString());
            if (ratingListener != null) {
                ratingListener.onRating(rating);
            }
            dismiss();
        }
    }

    public void onCancelClicked(View view) {
        dismiss();
    }

    @OnClick({R.id.bnCancel, R.id.bnSubmit})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bnCancel:
                onCancelClicked(view);
                break;
            case R.id.bnSubmit:
                onSubmitClicked(view);
                break;
        }
    }
}
