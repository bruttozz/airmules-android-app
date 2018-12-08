package com.example.asthana.airmuleschat;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewMulesDialogFragment extends DialogFragment {
    private RecyclerView mRecyclerView;

    // this method create view for your Dialog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate layout with recycler view
        View v = inflater.inflate(R.layout.dialog_mules, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.viewMulesDialogRecycler);
        //setadapter
        //get your recycler view and populate it.
        return v;
    }
}

