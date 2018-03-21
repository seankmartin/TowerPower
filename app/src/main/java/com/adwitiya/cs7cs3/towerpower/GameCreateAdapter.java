package com.adwitiya.cs7cs3.towerpower;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

/**
 * Created by Balakumaran on 21-03-2018.
 */

public class GameCreateAdapter extends FirestoreAdapter{

    public interface OnGameSearchInitiatedListner {

        void OnGameSearchInitiated(DocumentSnapshot restaurant);

    }

    private OnGameSearchInitiatedListner mListener;

    public GameCreateAdapter(Query query, OnGameSearchInitiatedListner listener) {
        super(query);
        mListener = listener;
    }
}
