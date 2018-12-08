package com.example.asthana.airmuleschat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;



public class MulesRadioAdapter extends ArrayAdapter<UserClass> {

    private int selectedIndex = -1;
    private String selectedUser;
    public MulesRadioAdapter(Context context, int activity_radio_button, List<UserClass> availableMules) {
        super(context, activity_radio_button, availableMules);
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public String getSelectedItem()
    {
        if (selectedUser != null)
            return selectedUser;
        else
            return "No selected user";
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.dialog_radio_button_item, null);
        }

        RadioButton rbSelect = (RadioButton) v.findViewById(R.id.radioButton);

        rbSelect.setChecked(position == selectedIndex);
        rbSelect.setTag(position);
        rbSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIndex = (Integer)view.getTag();
                notifyDataSetChanged();
            }
        });

        UserClass mule = getItem(position);

        if (mule != null) {
            TextView muleName = v.findViewById(R.id.dialogTxtMuleName);
            RatingBar muleRating =  v.findViewById(R.id.dialogMuleRating);

            muleName.setText(mule.getName());
            muleRating.setRating(mule.getRating());

            if (rbSelect.isChecked()){
                selectedUser = mule.getName();
            }
        }

        return v;
    }
}