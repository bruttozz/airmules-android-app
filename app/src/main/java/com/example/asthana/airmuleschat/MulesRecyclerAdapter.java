package com.example.asthana.airmuleschat;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Rating;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;


public class MulesRecyclerAdapter extends
        RecyclerView.Adapter<MulesRecyclerAdapter.ViewHolder> {

    private List<UserClass> availableMules;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public RatingBar muleRatingBar;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.dialogTxtMuleName);
            muleRatingBar= itemView.findViewById(R.id.dialogMuleRating);
        }
    }


    // Store a member variable for the contacts

    // Pass in the contact array into the constructor
    public MulesRecyclerAdapter(List<UserClass> availableMules) {
        this.availableMules = availableMules;
    }
    @Override
    public MulesRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.dialog_spinner_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(MulesRecyclerAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        UserClass mule = availableMules.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.nameTextView;
        textView.setText(mule.getName());
        RatingBar rating = viewHolder.muleRatingBar;
        rating.setRating(mule.getRating());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return availableMules.size();
    }

    public void updateData(List<UserClass> availableMules){
        this.availableMules = availableMules;
        notifyDataSetChanged();
    }
}