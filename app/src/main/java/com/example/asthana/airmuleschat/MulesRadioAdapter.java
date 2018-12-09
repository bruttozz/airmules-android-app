package com.example.asthana.airmuleschat;

import android.content.Context;
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
    private UserClass selectedUser;

    public MulesRadioAdapter(Context context, int activity_radio_button, List<UserClass> availableMules) {
        super(context, activity_radio_button, availableMules);
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public UserClass getSelectedItem() {
        return selectedUser;
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
                selectedIndex = (Integer) view.getTag();
                notifyDataSetChanged();
            }
        });

        UserClass mule = getItem(position);

        if (mule != null) {
            TextView muleName = v.findViewById(R.id.dialogTxtMuleName);
            RatingBar muleRating = v.findViewById(R.id.dialogMuleRating);

            //Put this here so that we can see the radio button until we update the GUI
            muleName.setText(mule.getName().substring(0, 1));
            muleRating.setRating(mule.getRating());

            if (rbSelect.isChecked()) {
                selectedUser = mule;
            }
        }

        return v;
    }
}