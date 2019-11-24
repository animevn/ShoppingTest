package com.haanhgs.shoppingtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.haanhgs.shoppingtest.model.Rating;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RatingDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "RatingDialog";

    private MaterialRatingBar ratingBar;
    private EditText tvContent;

    interface RatingListener {
        void onRating(Rating rating);
    }

    private RatingListener ratingListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rating, container, false);
        ratingBar = view.findViewById(R.id.ratingbar_rating_dialog);
        tvContent = view.findViewById(R.id.tv_rating_dialog_content);
        view.findViewById(R.id.bn_rating_dialog_submit).setOnClickListener(this);
        view.findViewById(R.id.bn_rating_dialog_cancel).setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof RatingListener) {
            ratingListener = (RatingListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null){
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
//        if (getDialog() != null && getDialog().getWindow() != null){
//            getDialog().getWindow().setLayout(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_rating_dialog_submit:
                onSubmitClicked(v);
                break;
            case R.id.bn_rating_dialog_cancel:
                onCancelClicked(v);
                break;
        }
    }

    public void onSubmitClicked(View view) {
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            Rating rating = new Rating(
                    FirebaseAuth.getInstance().getCurrentUser(),
                    ratingBar.getRating(),
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
}
