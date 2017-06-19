package com.example.anne.otp_android_client_v3;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import static android.content.ContentValues.TAG;
import static vanderbilt.thub.otp.model.OTPPlanModel.TraverseMode.BICYCLE;
import static vanderbilt.thub.otp.model.OTPPlanModel.TraverseMode.BUS;
import static vanderbilt.thub.otp.model.OTPPlanModel.TraverseMode.CAR;
import static vanderbilt.thub.otp.model.OTPPlanModel.TraverseMode.SUBWAY;
import static vanderbilt.thub.otp.model.OTPPlanModel.TraverseMode.WALK;

/**
 * Created by Anne on 5/30/2017.
 */

public class DetailedSearchBarFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        LinearLayout ll = (LinearLayout) inflater
                .inflate(R.layout.detailed_search_bar_layout, container, false);

        final MainActivity activity = (MainActivity) getActivity();

        // Initialize Mode-ImageButton BiMap
        activity.addToModeButtonBiMap(WALK, (ImageButton) ll.findViewById(R.id.walk_mode_button));
        activity.addToModeButtonBiMap(CAR, (ImageButton) ll.findViewById(R.id.car_mode_button));
        activity.addToModeButtonBiMap(BUS, (ImageButton) ll.findViewById(R.id.bus_mode_button));
        activity.addToModeButtonBiMap(BICYCLE, (ImageButton) ll.findViewById(R.id.bike_mode_button));

        Log.d(TAG, "Added mode buttons in BiMap");

        // Initialize the mode buttons in the detailed search bar fragment
        activity.setUpModeButtons();

        // Set up the EditTexts
        final EditText sourceEditText = (EditText)
                ll.findViewById(R.id.detailed_search_bar_from_edittext);
        final EditText destinationEditText = (EditText)
                ll.findViewById(R.id.deatiled_search_bar_to_edittext);

        sourceEditText.setHorizontallyScrolling(true);
        destinationEditText.setHorizontallyScrolling(true);

        sourceEditText.setFocusable(false);
        destinationEditText.setFocusable(false);

        activity.setSourceBox(sourceEditText);
        activity.setDestinationBox(destinationEditText);

        // Initialize the text in the EditTexts
        if (activity.getCurrentSelectedSourcePlace() == null) sourceEditText.setText("My Location");
        else sourceEditText.setText(activity.getCurrentSelectedSourcePlace().getName());

        destinationEditText.setText(activity.getCurrentSelectedDestinationPlace().getName());

        // Set the onClickListeners for the EditTexts
        class EditTextOnClickListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {

                EditText et = (EditText) v;

                if (et == sourceEditText)
                    activity.launchGooglePlacesSearchWidget(MainActivity.SearchBarId.DETAILED_FROM);
                if (et == destinationEditText)
                    activity.launchGooglePlacesSearchWidget(MainActivity.SearchBarId.DETAILED_TO);

            }
        }

        sourceEditText.setOnClickListener(new EditTextOnClickListener());
        destinationEditText.setOnClickListener(new EditTextOnClickListener());

        // Set up the depart/arrive by TextView
        activity.setDepartureArrivalTimeTextView((TextView) ll.findViewById(R.id.depart_arrive));

        // Set the listener for the swap button
        ImageButton swapButton = (ImageButton) ll.findViewById(R.id.swap_source_destination_button);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Swap the contents of the EditTexts
                Editable tempEditable = sourceEditText.getText();
                sourceEditText.setText(destinationEditText.getText());
                destinationEditText.setText(tempEditable);

                // Swap the source and destination
                Place tempPlace = activity.getCurrentSelectedSourcePlace();
                activity.setCurrentSelectedOriginPlace(activity.getCurrentSelectedDestinationPlace());
                activity.setCurrentSelectedDestinationPlace(tempPlace);

                // Refresh the trip plan
                activity.planTrip(activity.getCurrentSelectedSourcePlace(),
                        activity.getCurrentSelectedDestinationPlace(), null, false);

            }
        });

        // Set the listener for the back button
        ImageButton backButton = (ImageButton) ll.findViewById(R.id.detailed_search_bar_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });

        // Initialize the depart/arrive time TextView
        TextView departArriveTime = (TextView) ll.findViewById(R.id.depart_arrive);
        departArriveTime.setText("Depart by/arrive by...");
        departArriveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetDepartOrArriveTimeDialogFragment dialog =
                        new SetDepartOrArriveTimeDialogFragment();
                dialog.show(getFragmentManager(),"Show set depart or arrive time dialog fragment");
            }
        });
        activity.setDepartureArrivalTimeTextView(departArriveTime);


        return ll;
    }



}
