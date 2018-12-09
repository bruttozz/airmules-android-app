package com.example.asthana.airmuleschat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private ArrayList<Request> userRequests = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(ArrayList<Request> list, Context context) {
        userRequests = list;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {

        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView departure;
        TextView arrival;
        TextView item;
        TextView date;
        Button deleteRequest;
        RelativeLayout parentLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            departure = itemView.findViewById(R.id.user_txtViewDeparture);
            arrival = itemView.findViewById(R.id.user_txtViewArrival);
            item = itemView.findViewById(R.id.user_txtViewItem);
            date = itemView.findViewById(R.id.user_txtViewDate);
            deleteRequest = itemView.findViewById(R.id.btnDeleteRequest);
            parentLayout = itemView.findViewById(R.id.parentid);


        }
    }
}
