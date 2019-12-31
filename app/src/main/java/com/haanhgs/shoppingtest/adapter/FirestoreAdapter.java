 package com.haanhgs.shoppingtest.adapter;

import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

public abstract class FirestoreAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> implements EventListener<QuerySnapshot> {

    private static final String TAG = "D.Firestore Adapter";
    private Query query;
    private ListenerRegistration registration;
    private final ArrayList<DocumentSnapshot> snapshots = new ArrayList<>();

    public FirestoreAdapter(Query query) {
        this.query = query;
    }

    protected DocumentSnapshot getSnapshot(int index) {
        return snapshots.get(index);
    }

    public void startListening() {
        if (query != null && registration == null) {
            registration = query.addSnapshotListener(this);
        }
    }

    public void stopListening() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
        snapshots.clear();
        notifyDataSetChanged();
    }

    public void setQuery(Query query) {
        stopListening();
        snapshots.clear();
        notifyDataSetChanged();
        this.query = query;
        startListening();
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    @SuppressWarnings("EmptyMethod")
    protected void onError(FirebaseFirestoreException e) {}

    @SuppressWarnings("EmptyMethod")
    protected void onDataChanged() {}

    protected void onDocumentAdded(DocumentChange change) {
        snapshots.add(change.getNewIndex(), change.getDocument());
        notifyItemInserted(change.getNewIndex());
    }

    protected void onDocumentModified(DocumentChange change) {
        if (change.getOldIndex() == change.getNewIndex()) {
            snapshots.set(change.getOldIndex(), change.getDocument());
            notifyItemChanged(change.getOldIndex());
        } else {
            snapshots.remove(change.getOldIndex());
            snapshots.add(change.getNewIndex(), change.getDocument());
            notifyItemMoved(change.getOldIndex(), change.getNewIndex());
        }
    }

    protected void onDocumentRemoved(DocumentChange change) {
        snapshots.remove(change.getOldIndex());
        notifyItemRemoved(change.getOldIndex());
    }

    @Override
    public void onEvent(QuerySnapshot documentSnapshots,
                        FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "onEvent:error", e);
            return;
        }
        for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
            DocumentSnapshot snapshot = change.getDocument();
            switch (change.getType()) {
                case ADDED:
                    onDocumentAdded(change);
                    break;
                case MODIFIED:
                    onDocumentModified(change);
                    break;
                case REMOVED:
                    onDocumentRemoved(change);
                    break;
            }
        }
        onDataChanged();
    }
}
