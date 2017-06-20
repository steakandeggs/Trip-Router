package com.example.anne.otp_android_client_v3;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.INotificationSideChannel;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anne.otp_android_client_v3.dictionary.ModeToDrawableDictionary;
import com.example.anne.otp_android_client_v3.custom_views.ExpandedItineraryView;
import com.example.anne.otp_android_client_v3.custom_views.ItineraryLegIconView;
import com.example.anne.otp_android_client_v3.fragments.DetailedSearchBarFragment;
import com.example.anne.otp_android_client_v3.listeners.SlidingPanelHeadOnSwipeTouchListener;
import com.example.anne.otp_android_client_v3.listeners.SlidingPanelTailOnSwipeTouchListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.common.collect.BiMap;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.collect.HashBiMap;
import com.google.maps.android.PolyUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;

import google_places_place_search.service.PlaceSearchService;
import google_places_place_search.service.PlaceSearchSvcApi;
import retrofit2.Call;
import retrofit2.Callback;
import vanderbilt.thub.otp.model.OTPPlanModel.GenericLocation;
import vanderbilt.thub.otp.model.OTPPlanModel.Itinerary;
import vanderbilt.thub.otp.model.OTPPlanModel.Leg;
import vanderbilt.thub.otp.model.OTPPlanModel.PlannerRequest;
import vanderbilt.thub.otp.model.OTPPlanModel.Response;
import vanderbilt.thub.otp.model.OTPPlanModel.TraverseMode;
import vanderbilt.thub.otp.model.OTPPlanModel.WalkStep;
import vanderbilt.thub.otp.service.OTPPlanService;
import vanderbilt.thub.otp.service.OTPPlanSvcApi;

// TODO: Implement tab bar that shows which itinerary we are on
// TODO: Turn off sliding panel overlay

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = "MainActivity.java";

    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private final int POLYLINE_WIDTH = 23;

    private final int LOCATION_INTERVAL = 5000;

    public static final float DARK_OPACITY_PERCENTAGE = .70f;

    public static final float LIGHT_OPACITY_PERCENTAGE = .54f;

    public static final int DARK_OPACITY = (int) (DARK_OPACITY_PERCENTAGE * 255);

    private GoogleMap mMap;

    private GoogleApiClient mGoogleAPIClient;

    private SlidingUpPanelLayout mSlidingPanelLayout;

    private LinearLayout mSlidingPanelHead;

    private ScrollView mSlidingPanelTail;

    private LinearLayout mNavButtonsLayout;

    private ImageButton mFab;

    private ImageButton mRightArrowButton;

    private ImageButton mLeftArrowButton;

    public enum ActivityState {HOME, HOME_PLACE_SELECTED, HOME_STOP_SELECTED, HOME_BUS_SELECTED, TRIP_PLAN, NAVIGATION}

    private Stack<ActivityState> mStateStack;

    public enum SearchBarId {SIMPLE, DETAILED_FROM, DETAILED_TO}

    private SearchBarId lastEditedSearchBar;

    private CardView mSimpleSearchBar;

    private TextView mSimpleSearchBarText;

    private TripPlanPlace mOrigin = null;

    private TripPlanPlace mDestination = null;

    private DetailedSearchBarFragment mDetailedSearchBarFragment;

    private volatile LatLngBounds mMapBounds;

    private BiMap<TraverseMode, ImageButton> modeToImageButtonBiMap;

    private int mCurItineraryIndex;

    private List<Itinerary> mItineraryList;

    private List<Polyline> mPolylineList;

    private List<LatLng> mItineraryPointList;

    private Marker mDestinationMarker;

    private TextView mDepartureArrivalTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Activity created");

        // Setup
        setUpDrawer();
        setUpSearch();
        setUpMap();
        setUpModes();
        setUpSlidingPanel();
        setUpNavigationButtons();
        ModeToDrawableDictionary.setup(this);

        // Initialize state
        mStateStack = new Stack<>();
        setState(ActivityState.HOME);

    }

    /**
     * Callback that handles back button press
     */
    @Override
    public void onBackPressed() {

        Log.d(TAG, "Back pressed");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mSlidingPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {

            Log.d(TAG, peekState().toString());

            // Perform the corresponding necessary actions based on the current state
            switch (popState()) {
                case TRIP_PLAN:
                    Log.d(TAG, "Transitioning state from TRIP_PLAN to HOME");

                    // Remove destination marker from the map if it exists
                    if (mDestinationMarker != null) {
                        mDestinationMarker.remove();
                        mDestinationMarker = null;
                    }

                    // Remove previous itinerary from the map if it exists
                    if (mPolylineList != null) {
                        for (Polyline polyline : mPolylineList)
                            polyline.remove();
                        mPolylineList = null;
                    }

                    // Reset MyLocation button functionality (center map on current location when pressed)
                    resetMyLocationButton();

                    // Revert map shape & center/zoom to current location
                    setMapPadding(ActivityState.HOME);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(getCurrentCoordinates()));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                    // Hide navigation buttons
                    hideArrowButtons();
                    mNavButtonsLayout.setVisibility(View.GONE);

                    // Hide sliding panel
                    mSlidingPanelHead.removeAllViews();
                    mSlidingPanelTail.removeAllViews();
                    mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                    // Show simple search bar
                    mSimpleSearchBar.setVisibility(View.VISIBLE);
                    mSimpleSearchBarText.setText(getResources().getText(R.string.where_to));

                    // Remove detailed search bar fragment
                    super.onBackPressed();

                    break;

                case HOME_PLACE_SELECTED:

                    // Hide sliding panel
                    mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    mSlidingPanelHead.removeAllViews();
                    mSlidingPanelLayout.setTouchEnabled(true);

                    // Hide fab
                    mNavButtonsLayout.setVisibility(View.INVISIBLE);

                    // Revert simple search bar
                    mSimpleSearchBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_up));
                    mSimpleSearchBar.setVisibility(View.VISIBLE);
                    setMapPadding(ActivityState.HOME);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(getCurrentCoordinates()));
            }
        }
    }

    /**
     * Helper method for setting up the navigation drawer
     */
    private void setUpDrawer() {

        // Get drawer layout and navigation view
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        // Highlight 'Planner' menu item in the navigation view
        navigationView.getMenu().getItem(0).setChecked(true);

        // Set item selected listener
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.isChecked())
                            return false;
                        item.setChecked(true);

                        int id = item.getItemId();

                        // TODO: Implement settings screen
                        if (id == R.id.nav_planner) {
                        } else if (id == R.id.nav_settings) {
                        }

                        drawer.closeDrawer(GravityCompat.START);
                        return true;
                    }
                });
    }

    /**
     * Helper method for setting up the PlaceAutocompleteFragment and simple search bar
     */
    private void setUpSearch() {
        mSimpleSearchBar = (CardView) findViewById(R.id.simple_search_bar_card_view);
        mSimpleSearchBarText = (TextView) findViewById(R.id.simple_search_bar_text_view);
        mSimpleSearchBarText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchGooglePlacesSearchWidget(SearchBarId.SIMPLE);
            }
        });

        ImageView burger = (ImageView) findViewById(R.id.burger);
        burger.setAlpha(LIGHT_OPACITY_PERCENTAGE);
        burger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }
        });

    }

    /**
     * Helper method that launches the google places autocomplete search widget
     * Will invoke onActivityResult when the user selects a place
     */
    public void launchGooglePlacesSearchWidget(SearchBarId id) {

        // Record which search bar was clicked
        setLastEditedSearchBar(id);

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .setBoundsBias(getBoundsBias())
                            .build(MainActivity.this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException
                | GooglePlayServicesNotAvailableException e) {
            Log.d(TAG, "Error launching PlaceAutocomplete intent");
        }
    }

    /**
     * Invoked when when the user selects a place from the google places widget
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult invoked");

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            Log.d(TAG, "Place Autocomplete request result received");

            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Result is ok");

                // Get the place selected
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.d(TAG, "Place selected: " + place.getName());

                TripPlanPlace tripPlanPlace = new TripPlanPlace(place.getName(), place.getLatLng());

                // Make updates according to which search bar was edited
                if (lastEditedSearchBar == SearchBarId.SIMPLE) {
                    transitionState(ActivityState.HOME, ActivityState.TRIP_PLAN);
                    planTrip(new TripPlanPlace(), tripPlanPlace, null, false);

                } else if (lastEditedSearchBar == SearchBarId.DETAILED_FROM) {

                    // Set the text in the detailed from search bar
                    mDetailedSearchBarFragment.setOriginText(place.getName());
                    planTrip(tripPlanPlace, mDestination, null, false);

                } else if (lastEditedSearchBar == SearchBarId.DETAILED_TO) {

                    // Set the text in the detailed to search bar
                    mDetailedSearchBarFragment.setDestinationText(place.getName());
                    planTrip(mOrigin, tripPlanPlace, null, false);
                }

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());

            }
        }
    }


    /**
     * Helper method for setting up the Google Map
     */
    private void setUpMap() {
        // Obtain the SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // Acquire the GoogleMap (automatically initializes the maps system and the view)
        // Will trigger the OnMapReady callback when the map is ready to be used
        mapFragment.getMapAsync(this);
    }

    /**
     * Helper method for setting up the chosen modes of transportation
     */
    private void setUpModes() {
        // Create the mode-imagebutton bimap
        modeToImageButtonBiMap = HashBiMap.create();

        // TODO: Grab the actual default modes set by the user
        // Initialize default modes & select the default modes
        ModeSelectOptions.setDefaultModes(Arrays.asList(TraverseMode.WALK, TraverseMode.BUS));
        ModeSelectOptions.selectDefaultModes();
    }

    /**
     * Helper method for setting up the sliding panel layout
     */
    private void setUpSlidingPanel() {
        mSlidingPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingPanelHead = (LinearLayout) findViewById(R.id.sliding_panel_head);
        mSlidingPanelTail = (ScrollView) findViewById(R.id.sliding_panel_tail);

        mSlidingPanelHead.setOnTouchListener(new SlidingPanelHeadOnSwipeTouchListener(this, this));
        mSlidingPanelTail.setOnTouchListener(new SlidingPanelTailOnSwipeTouchListener(this, this));

    }

    private void setUpNavigationButtons() {
        mNavButtonsLayout = (LinearLayout) findViewById(R.id.navigation_buttons_layout);
        mFab = (ImageButton) findViewById(R.id.navigation_fab);
        mLeftArrowButton = (ImageButton) findViewById(R.id.left_button);
        mRightArrowButton = (ImageButton) findViewById(R.id.right_button);

        mLeftArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeSlidingPanelRight();
            }
        });
        mRightArrowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwipeSlidingPanelLeft();
            }
        });
    }

    /**
     * Callback triggered when the map is ready to be used
     * Sets up the Google API Client & enables the compass and location features for the map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        setMapPadding(ActivityState.HOME);
        mMap.getUiSettings().setCompassEnabled(true);

        // Build the GoogleApiClient
        if (checkLocationPermission()) {
            // Permission was already granted
            buildGoogleApiClient();
        }

        // Build the Google Place Search Service retrofit API
        PlaceSearchService.buildRetrofit(PlaceSearchSvcApi.GOOGLE_PLACE_SEARCH_API_URL);

        // Set the on camera idle listener
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                // Update current bounds & list of nearby places
                mMapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
            }
        });

        // Set the point of interest click listener
        mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {

                if (peekState() == ActivityState.NAVIGATION)
                    return;

                // Center camera on the selected POI
                mMap.animateCamera(CameraUpdateFactory.newLatLng(pointOfInterest.latLng));

                // Get the place & display details
                String id = pointOfInterest.placeId;
                Places.GeoDataApi.getPlaceById(mGoogleAPIClient, id)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess() && places.getCount() > 0) {
                                    Place myPlace = places.get(0).freeze();
                                    Log.i(TAG, "Point of interest Place found: " + myPlace.getName());
                                    selectPlaceOnMap(myPlace);
                                } else {
                                    Log.e(TAG, "Point of interest Place not found");
                                }
                                places.release();
                            }
                        });
            }
        });
    }


    /**
     * Helper method for obtaining location access permission
     * Checks if the manifest permission ACCESS_FINE_LOCATION has been granted
     * and, if not, requests the permission
     *
     * @return If permission was initially granted
     */
    public boolean checkLocationPermission() {
        // If permission has not already been granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Request permission
            // Invokes OnRequestPermissionsResult on result
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

            return false;

        } else { // Permission has already been granted
            return true;
        }
    }

    /**
     * Callback method invoked by response to permissions request
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)

                    // Permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        // Build the GoogleApiClient
                        if (mGoogleAPIClient == null) buildGoogleApiClient();
                    } else {
                        // Permission was denied
                        Toast.makeText(this, "Location access permission denied", Toast.LENGTH_LONG).show();
                        if (mGoogleAPIClient == null) buildGoogleApiClientWithoutLocationServices();
                    }
            }
            // Add other case lines to check for other permissions this app might request
        }
    }

    /**
     * Helper method that builds & connects the GoogleApiClient
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleAPIClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleAPIClient.connect();
    }

    /**
     * Helper method that builds & connects the GoogleApiClient without the
     * Location Services API
     */
    private synchronized void buildGoogleApiClientWithoutLocationServices() {
        mGoogleAPIClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        mGoogleAPIClient.connect();
    }

    /**
     * Callback invoked when the GoogleApiClient is connected
     * Requests location update to initialize the map with the current location
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG, "API Client connected");

        beginLocationRequests();

        // Enable the My Location button
        try { mMap.setMyLocationEnabled(true); } catch (SecurityException se) {}

        // Move map camera to current location
        mMap.moveCamera(CameraUpdateFactory.newLatLng(getCurrentCoordinates()));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    /**
     * Callback invoked when location update is received
     * Initializes map by repositioning camera
     * Stops the location updates
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");
        if (peekState() == ActivityState.NAVIGATION) {
            // Update camera
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition((new CameraPosition.Builder())
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .tilt(60)
                            .zoom(17)
                            .build()
                    )
            );

        } else {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleAPIClient, this);
        }
    }

    /**
     * Hook method invoked when the GoogleApiClient's connection is suspended
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {}

    /**
     * Hook method invoked when the GoogleApiClient connection fails
     * @param cr
     */
    @Override
    public void onConnectionFailed(ConnectionResult cr) {
        Toast.makeText(this, "GoogleApiClient connection failed", Toast.LENGTH_LONG).show();
    }

    /**
     * Helper method to show info about a location when clicked on the map
     */
    private void selectPlaceOnMap(final Place place) {

        // Set state
        if (isHomeState(peekState()) && !(peekState() == ActivityState.HOME))
            popState();
        setState(ActivityState.HOME_PLACE_SELECTED);

        // Hide simple search bar
        if (mSimpleSearchBar.getVisibility() != View.GONE) {
            mSimpleSearchBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_up));
            mSimpleSearchBar.setVisibility(View.GONE);
            setMapPadding(ActivityState.HOME_PLACE_SELECTED);
        }

        // Name text view
        TextView placeNameText = new TextView(this);
        placeNameText.setId(R.id.place_name_text_view);
        placeNameText.setHorizontallyScrolling(true);
        placeNameText.setEllipsize(TextUtils.TruncateAt.END);
        placeNameText.setGravity(Gravity.BOTTOM);
        placeNameText.setText(place.getName());
        placeNameText.setTextSize(18);
        placeNameText.setPadding(40,0,40,0);
        placeNameText.setTextColor(Color.BLACK);
        placeNameText.setAlpha(DARK_OPACITY_PERCENTAGE);

        // Address text view
        TextView placeAddressText = new TextView(this);
        placeAddressText.setHorizontallyScrolling(true);
        placeAddressText.setEllipsize(TextUtils.TruncateAt.END);
        placeAddressText.setGravity(Gravity.TOP);
        placeAddressText.setText(place.getAddress());
        placeAddressText.setPadding(40,0,40,0);
        placeAddressText.setTextSize(12);
        placeAddressText.setMaxLines(1);

        // Clear sliding panel head
        mSlidingPanelHead.removeAllViews();
        mSlidingPanelHead.setOrientation(LinearLayout.VERTICAL);

        // Show on sliding panel head
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        mSlidingPanelHead.addView(placeNameText, layoutParams);
        mSlidingPanelHead.addView(placeAddressText, layoutParams);

        mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        mSlidingPanelLayout.setTouchEnabled(false);


        // Show directions button
        mFab.setImageDrawable(getDrawable(R.drawable.ic_directions_white_24dp));
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionState(peekState(), ActivityState.TRIP_PLAN);
                planTrip(new TripPlanPlace(), new TripPlanPlace(place.getName(), place.getLatLng()),
                        null, false);
            }
        });
        mNavButtonsLayout.setVisibility(View.VISIBLE);


    }

    public ActivityState peekState() { return mStateStack.peek();}

    public ActivityState popState() { return mStateStack.pop(); }

    public void setState(ActivityState state) {
        mStateStack.push(state);
    }

    /**
     * Helper method to facilitate a state transition in the application
     * @param oldState
     * @param newState
     */
    public void transitionState(ActivityState oldState, ActivityState newState) {

        if (oldState == ActivityState.TRIP_PLAN && newState == ActivityState.TRIP_PLAN) {
            return;

        } else if (isHomeState(oldState) && newState == ActivityState.TRIP_PLAN) {

            Log.d(TAG, "Transitioning to TRIP_PLAN screen");

            // Hide simple search bar
            mSimpleSearchBar.setVisibility(View.GONE);

            // Create a new detailed search bar
            mDetailedSearchBarFragment = new DetailedSearchBarFragment();

            // Initialize a fragment transaction to show the detailed search bar
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.detailed_search_bar_frame, mDetailedSearchBarFragment)
                    .addToBackStack("Show detailed search bar for trip plan screen")
                    .commit();

            // Clear & show sliding panel
            mSlidingPanelHead.removeAllViews();
            mSlidingPanelTail.removeAllViews();
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            mSlidingPanelLayout.setTouchEnabled(true);

            // Resize map
            setMapPadding(ActivityState.TRIP_PLAN);

            // Set up navigation floating action button
            mFab.setImageDrawable(getDrawable(R.drawable.ic_navigation_white_24dp));
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    LatLng tripStartLocation = mOrigin.getLocation();
                    LatLng myLocation = getCurrentCoordinates();

                    if (tripStartLocation == null
                            || Math.abs(tripStartLocation.latitude - myLocation.latitude) > 0.0005
                            || Math.abs(tripStartLocation.longitude - myLocation.longitude) > 0.0005) {
                        Toast.makeText(MainActivity.this,
                                "Cannot launch navigation mode for trip " +
                                        "that does not begin at the current location",
                                Toast.LENGTH_LONG).show();
                    } else {
                        transitionState(ActivityState.TRIP_PLAN, ActivityState.NAVIGATION);
                    }
                }
            });

            // Show navigation buttons
            mNavButtonsLayout.setVisibility(View.VISIBLE);

            setState(ActivityState.TRIP_PLAN);

        } else if (oldState == ActivityState.TRIP_PLAN && newState == ActivityState.NAVIGATION) {

            // TODO Set up navigation mode
            Log.d(TAG, "Transitioning to NAVIGATION screen");

            // Exit if there is an error with the trip plan itineraries
            if (mItineraryList == null || mItineraryList.isEmpty() ||
                    mItineraryList.size() <= mCurItineraryIndex)
                return;

            setState(ActivityState.NAVIGATION);

            // Hide detailed search bar
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .remove(mDetailedSearchBarFragment)
                    .addToBackStack("Hide detailed search bar for navigation screen")
                    .commit();

            // Change FAB
            mFab.setBackground(getDrawable(R.drawable.white_circle));
            Drawable stop = getDrawable(R.drawable.ic_clear_black_24dp);
            stop.setAlpha(DARK_OPACITY);
            mFab.setImageDrawable(stop);
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            // Hide arrows
            hideArrowButtons();

            // Hide sliding panel
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

            // Show navigation instructions in info windows on map
            for (Leg leg : mItineraryList.get(mCurItineraryIndex).getLegs()) {
                for (WalkStep walkStep : leg.getSteps()) {
                    // TODO show steps on map in info windows
                }
            }

            // Expand map padding
            setMapPadding(ActivityState.NAVIGATION);

            // Start location requests to follow the user's location
            beginLocationRequests();


        } else {
            throw new RuntimeException("Invalid state transition request");
        }
    }

    /**
     * Helper method to initialize the mode buttons in the detailed search bar
     */
    public void setUpModeButtons() {

        // Get the current selected modes
        Set<TraverseMode> selectedModes = ModeSelectOptions.getSelectedModes();

        // Loop through the TraverseMode-ImageButtonId bimap
        for (Map.Entry<TraverseMode,ImageButton> entry: modeToImageButtonBiMap.entrySet()) {

            TraverseMode traverseMode = entry.getKey();
            ImageButton button = entry.getValue();

            // Initialize each button as selected or deselected
            if (selectedModes.contains(traverseMode))
                selectModeButton(button);
            else
                deselectModeButton(button);

            // Set on click listener for each button
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageButton button = (ImageButton) v;

                    if (button.isSelected())
                        deselectModeButton(button);
                    else
                        selectModeButton(button);

                    // Refresh the search
                    planTrip(mOrigin, mDestination, null, false);
                }
            });

        }
    }

    /**
     * Helper function for selecting a mode button
     */
    private void selectModeButton(ImageButton button) {
        Log.d(TAG, "Mode button was selected");

        // Select button and add corresponding mode to list of selected modes
        button.setSelected(true);
        ModeSelectOptions.addSelectedMode(modeToImageButtonBiMap.inverse().get(button));

        // Set white background, colored image
        button.setBackgroundResource(R.drawable.rounded_rectangle_white);
        button.setColorFilter(getResources().getColor(R.color.colorPrimary, null));
    }

    /**
     * Helper function for deselecting a mode button
     */
    private void deselectModeButton(ImageButton button) {
        Log.d(TAG, "Mode button was deselected");

        // Deselect button and remove corresponding mode from list of selected modes
        button.setSelected(false);
        ModeSelectOptions.removeSelectedMode(modeToImageButtonBiMap.inverse().get(button));

        // Set colored background, white image
        button.setBackgroundResource(R.drawable.rounded_rectangle_primary);
        button.setColorFilter(Color.WHITE);
    }

    /**
     * Gets the current location, makes a GET request to the OTP
     * server for a list of itineraries from the current location to mDestination,
     * and invokes displayItinerary on the first itinerary
     * @param origin
     * @param destination
     * @param time time by which the trip should depart or arrive by, pass null to use current time
     * @param departOrArriveBy false for depart, true for arrive
     * @return true if the request was successfully made, false otherwise
     */
    public boolean planTrip(@NonNull final TripPlanPlace origin,
                            @NonNull final TripPlanPlace destination,
                            @Nullable Date time, boolean departOrArriveBy) {

        // BEFORE PLANNING TRIP:

        // If no modes are selected, prompt user to choose a mode
        if (ModeSelectOptions.getNumSelectedModes() == 0) {
            Toast.makeText(this, "Please select at least one mode of transportation",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        // If public transport is selected, ensure that walk or bicycle or car is also selected
        Set<TraverseMode> selectedModes = ModeSelectOptions.getSelectedModes();
        if ((selectedModes.contains(TraverseMode.BUS)
                || selectedModes.contains(TraverseMode.SUBWAY))
                &&
                !(selectedModes.contains(TraverseMode.WALK)
                || selectedModes.contains(TraverseMode.BICYCLE)
                || selectedModes.contains(TraverseMode.CAR))) {
            Toast.makeText(this, "Please select at least one of walk, bike, or car modes",
                    Toast.LENGTH_LONG).show();
            return false;
        }


        // PLAN TRIP:

        Log.d(TAG, "Planning trip");
        Log.d(TAG, "Sliding panel state: " + mSlidingPanelLayout.getPanelState());

        // Set up origin and destination
        if (origin.isCurrentLocation())
            origin.setLocation(getCurrentCoordinates());
        if (destination.isCurrentLocation())
            destination.setLocation(getCurrentCoordinates());

        mOrigin = origin;
        mDestination = destination;

        // Get depart/arrive-by date & time
        if (time == null) // Get current time if a time was not provided
            time = new Date();
        String dateString = new SimpleDateFormat("MM-dd-yyyy").format(time);
        String timeString = new SimpleDateFormat("hh:mma").format(time);
        String displayTimeString = new SimpleDateFormat("hh:mm a").format(time);
        if (displayTimeString.charAt(0) == '0')
            displayTimeString = displayTimeString.substring(1);

        // Generate & display depart/arrive-by text
        String departOrArrive = departOrArriveBy ? "Arrive" : "Depart";
        setDepartureArrivalTimeText(departOrArrive + " by " + displayTimeString);

        // Reset MyLocation button functionality
        resetMyLocationButton();

        // Hide arrow buttons
        hideArrowButtons();

        // Remove destination marker from the map if it exists
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
            mDestinationMarker = null;
        }

        // Remove previous itinerary from the map if it exists
        if (mPolylineList != null) {
            for (Polyline polyline : mPolylineList)
                polyline.remove();
            mPolylineList = null;
        }

        // Clear sliding panel head and display loading text
        showSlidingPanelHeadMessage("LOADING RESULTS...");

        // Clear sliding panel tail
        mSlidingPanelTail.removeAllViews();

        // Draw and save a marker at the destination
        mDestinationMarker =  mMap.addMarker(new MarkerOptions()
                .position(destination.getLocation())
                .title(destination.getName()));

        // Create and set up a new trip planner request
        final PlannerRequest request = new PlannerRequest();


        request.setFrom(new GenericLocation(origin.getLatitude(),
               origin.getLongitude()));
        request.setTo(new GenericLocation(destination.getLatitude(),
                destination.getLongitude()));
        request.setModes(ModeSelectOptions.getSelectedModesString());
        Log.d(TAG, "Selected modes: " + ModeSelectOptions.getSelectedModesString());

        // Set up the OPTPlanService
        OTPPlanService.buildRetrofit(OTPPlanSvcApi.OTP_API_URL);
        String startLocation = Double.toString(request.getFrom().getLat()) +
                "," + Double.toString(request.getFrom().getLng());
        String endLocation = Double.toString(request.getTo().getLat()) +
                "," + Double.toString(request.getTo().getLng());


        // Make the request to OTP server
        Call<Response> response = OTPPlanService.getOtpService().getTripPlan(
                OTPPlanService.ROUTER_ID,
                startLocation,
                endLocation,
                request.getModes(),
                true,
                "TRANSFERS",
                dateString,
                timeString,
                departOrArriveBy
        );

        final long curTime = System.currentTimeMillis();
        response.enqueue(new Callback<Response>() {

            // Handle the request response
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                Log.d(TAG, "Received trip plan from server. Time: " +
                        (System.currentTimeMillis() - curTime));

                if (response.body().getPlan() == null
                        || response.body().getPlan().getItineraries() == null
                        || response.body().getPlan().getItineraries().isEmpty()) {
                    Log.d(TAG, "OTP request result was empty");
                    showSlidingPanelHeadMessage("No results");
                    return;
                }

                // Save & sort the list of itinerary results
                mItineraryList = response.body().getPlan().getItineraries();
                Collections.sort(mItineraryList, new Comparator<Itinerary>() {
                    @Override
                    public int compare(Itinerary o1, Itinerary o2) {
                        int numLegs1 = o1.getLegs().size();
                        int numLegs2 = o2.getLegs().size();

                        if (1.5 * o1.getDuration() <= o2.getDuration())
                            return -1;
                        else if (1.5 *  o2.getDuration() <= o1.getDuration())
                            return 1;
                        else if (numLegs1 < numLegs2) {
                            return -1;
                        } else if (numLegs1 > numLegs2)
                            return 1;
                        else
                            return 0;
                    }
                });

                // Drop itineraries that have duration 4x greater than that of the first
                if (!mItineraryList.isEmpty())
                    //noinspection Since15
                    mItineraryList.removeIf(new Predicate<Itinerary>() {
                        @Override
                        public boolean test(Itinerary itinerary) {
                            return (itinerary.getDuration() >
                                    4 * mItineraryList.get(0).getDuration());
                        }
                    });


                // Initialize stack of points along itinerary
                mItineraryPointList = new LinkedList<LatLng>();

                // Get the first itinerary in the results & display it
                displayItinerary(0,
                        origin.getLocation(), destination.getLocation(),
                        android.R.anim.slide_in_left);

            }

            @Override
            public void onFailure(Call<Response> call, Throwable throwable) {
                Log.d(TAG, "Request failed to get itineraries:\n" + throwable.toString());
                Toast.makeText(getApplicationContext(),
                        "Request to server failed", Toast.LENGTH_LONG).show();

                // Display "Request failed" on the sliding panel head
                showSlidingPanelHeadMessage("Request failed");

                // Move the camera to include just the origin and destination
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                        .include(origin.getLocation())
                        .include(destination.getLocation())
                        .build()
                        , 150)
                );
            }
        });

        Log.d(TAG, "Made request to OTP server");
        Log.d(TAG, "Starting point coordinates: " + origin.getLocation().toString());
        Log.d(TAG, "Destination coordinates: " + destination.getLocation().toString());
        return true;
    }


    /**
     * Helper method that resets the functionality of the map's My Location button
     *
     * pre: mMap and location services are set up and working
     */
    private void resetMyLocationButton() {
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(getCurrentCoordinates()));
                return false;
            }
        });
    }

    /**
     * Helper method that displays a message on the sliding panel head
     *
     * pre: mSlidingPanelHead has been initialized
     */
    private void showSlidingPanelHeadMessage(String message) {
        TextView textView = new TextView(MainActivity.this);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(15);
        textView.setText(message);
        mSlidingPanelHead.removeAllViews();
        mSlidingPanelHead.addView(textView,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
        );
    }

    /**
     * Displays an itinerary in polyline on the map and in graphical depiction
     * on the sliding panel layout head and tail.
     * Animates the entrance of the sliding panel contents.
     * Repositions map camera to fit the polyline path if it is out of frame,
     * or if repositionCameraUncondiationally is set to true.
     * This method does not reset the destination marker; that should have been
     * done in planTrip().
     *
     * pre: Activity is in state TRIP_PLAN
     *
     */
    public void displayItinerary(int itineraryIndex, LatLng origin, LatLng destination,
                                 int animationId) {

        Log.d(TAG, "Displaying itinerary");
        long time = System.currentTimeMillis();
        Log.d(TAG, "Sliding panel state: " + mSlidingPanelLayout.getPanelState());

        mCurItineraryIndex = itineraryIndex;

        Itinerary itinerary = mItineraryList.get(itineraryIndex);

        mItineraryPointList.clear();

//        // Log itinerary for debugging purposes
//                for (Leg leg : itinerary.getLegs())
//                    Log.d(TAG, leg.toString());

        // Clear slidingPanelHead
        mSlidingPanelHead.removeAllViews();
        mSlidingPanelHead.setOrientation(LinearLayout.HORIZONTAL);
        // Clear sliding panel tail
        mSlidingPanelTail.removeAllViews();

        if (itinerary == null) {
            Log.d(TAG, "Itinerary is null; failed to display");
            return;
        }

        // Remove polyline of previous itinerary if it exists
        if (mPolylineList != null) {
            for (Polyline polyline : mPolylineList)
                polyline.remove();
            mPolylineList = null;
        }

        // Get the legs of the itinerary and create a list for the corresponding polylines
        List<Leg> legList= itinerary.getLegs();
        List<Polyline> polylineList = new LinkedList<>();

        // Display each leg as a custom view in the itinerary summary and as a polyline on the map
        LinearLayout itinerarySummaryLegsLayout = new LinearLayout(this);
        itinerarySummaryLegsLayout.setGravity(Gravity.CENTER_VERTICAL);
        int index = 0;
        for (Leg leg : legList) {

            // Get a list of the points that make up the leg
            List<LatLng> points = PolyUtil.decode(leg.getLegGeometry().getPoints());

            // Add those points to the list of points that make up the itinerary
            mItineraryPointList.addAll(points);

            // Create a polyline options object for the leg
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(POLYLINE_WIDTH);

            // Create a new custom view representing this leg of the itinerary
            int paddingBetweenModeIconAndDurationText =
                    (leg.getMode().equals(TraverseMode.BICYCLE.toString())) ? 13 : 0;

            ItineraryLegIconView view = new ItineraryLegIconView(this,
                    paddingBetweenModeIconAndDurationText);

            // Configure the polyline and custom view based on the mode of the leg
            Drawable d = ModeToDrawableDictionary.getDrawable(leg.getMode());
            d.setAlpha(DARK_OPACITY);
            view.setIcon(d);

            switch (leg.getMode()) {
                case ("WALK"):
                    polylineOptions
                            .color(getResources().getColor(R.color.colorPrimary, null))
                            .pattern(Arrays.asList(new Dot(), new Gap(10)));
                    view.setLegDuration((int) Math.ceil(leg.getDuration()/60));
                    break;
                case ("BICYCLE"):
                    polylineOptions
                            .color(getResources().getColor(R.color.colorPrimary, null))
                            .pattern(Arrays.asList(new Dash(30), new Gap(10)));
                    view.setLegDuration((int) Math.ceil(leg.getDuration()/60));
                    break;
                case ("CAR"):
                    polylineOptions.color(getResources().getColor(R.color.colorPrimary, null));

                    break;
                case ("BUS"):
                    polylineOptions.color(Color.parseColor("#" + leg.getRouteColor()));
                    view.setRouteName(leg.getRoute());
                    view.setRouteColor(Color.parseColor("#" + leg.getRouteColor()));
                    view.setRouteNameColor(Color.WHITE);
                    view.setShowRoute(true);
                    break;
                case ("SUBWAY"):
                    polylineOptions.color(Color.parseColor("#" + leg.getRouteColor()));
                    view.setRouteName(leg.getRoute());
                    view.setRouteColor(Color.parseColor("#" + leg.getRouteColor()));
                    view.setRouteNameColor(Color.WHITE);
                    view.setShowRoute(true);
                    break;
                default: polylineOptions.color(Color.GRAY);
            }

            // Draw the polyline leg to the map and save it to the list
            polylineList.add(mMap.addPolyline(polylineOptions));

            // Add chevron icon to the sliding panel drawer handle
            // (except in front of the first mode icon)
            if (index!= 0) {
                ImageView arrow = new ImageView(this);
                arrow.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
                arrow.setScaleType(ImageView.ScaleType.FIT_CENTER);
                arrow.setAlpha(DARK_OPACITY_PERCENTAGE);
                itinerarySummaryLegsLayout.addView(arrow, index,
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
                ++index;
            }

            // Add the leg custom view to the created linear layout
            itinerarySummaryLegsLayout.addView(view, index,
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
            ++index;

        }

        // Add the itinerary summary layout to the sliding panel head
        mSlidingPanelHead.addView(itinerarySummaryLegsLayout, new LinearLayout
                .LayoutParams(mSlidingPanelHead.getWidth() - 230,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Add the itinerary duration view to the sliding panel head
        TextView duration = new TextView(this);
        duration.setGravity(Gravity.CENTER);
        duration.setTextColor(Color.BLACK);
        duration.setHorizontallyScrolling(false);
        duration.setAlpha(DARK_OPACITY_PERCENTAGE);
        duration.setTextSize(13);
        duration.setText(getDurationString(itinerary.getDuration()));
        duration.setPadding(0,0,0,0);
        mSlidingPanelHead.addView(duration, new LinearLayout
                .LayoutParams(230, ViewGroup.LayoutParams.MATCH_PARENT));

        // Add the expanded itinerary view to the sliding panel tail
        ExpandedItineraryView itineraryView = new ExpandedItineraryView(this);
        itineraryView.setPadding(0,50,0,150);

        // Configure the start and end points of the itinerary
        if (legList.size() != 0)
            legList.get(0).setFrom(new vanderbilt.thub.otp.model.OTPPlanModel.Place(
                    origin.latitude, origin.longitude, mDetailedSearchBarFragment.getOriginText())
            );
        legList.get(legList.size() - 1).setTo(new vanderbilt.thub.otp.model.OTPPlanModel.Place(
                destination.latitude, destination.longitude,
                mDetailedSearchBarFragment.getDestinationText())
        );
        itineraryView.setItinerary(itinerary);
        mSlidingPanelTail.addView(itineraryView, new ScrollView
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Animate the sliding panel components into view
        mSlidingPanelHead.startAnimation(AnimationUtils.loadAnimation(this, animationId));
        mSlidingPanelTail.startAnimation(AnimationUtils.loadAnimation(this, animationId));

        // Save the list of polylines drawn on the map
        mPolylineList =  polylineList;

        // Reconfigure map camera & My Location button
        if (!mPolylineList.isEmpty()) {

            // Find the topmost, bottommost, leftmost, and rightmost points in all the PolyLines
            LatLng top = (origin.latitude >= destination.latitude) ? origin : destination;
            LatLng bottom = (origin.latitude <= destination.latitude) ? origin : destination;
            LatLng right = (origin.longitude >= destination.longitude) ? origin : destination;
            LatLng left = (origin.longitude <= destination.longitude) ? origin : destination;

            for (Polyline polyline : mPolylineList) {
                for (LatLng point : polyline.getPoints()){
                    top = (top.latitude >= point.latitude) ? top : point;
                    bottom = (bottom.latitude <= point.latitude) ? bottom : point;
                    right = (right.longitude >= point.longitude) ? right : point;
                    left = (left.longitude <= point.longitude) ? left : point;
                }
            }

            // Clicking the My Location button will center the map on the itinerary
            final LatLng finalTop = top;
            final LatLng finalBottom = bottom;
            final LatLng finalRight = right;
            final LatLng finalLeft = left;

            mMap.setOnMyLocationButtonClickListener(
                    new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                            new LatLngBounds.Builder()
                            .include(finalTop)
                            .include(finalBottom)
                            .include(finalLeft)
                            .include(finalRight)
                            .build(), 170)
                    );
                    return true;
                }
            });


            // Move the camera to include all four points
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds.Builder()
                    .include(top)
                    .include(bottom)
                    .include(right)
                    .include(left)
                    .build(), 170)
            );

        }

        // Show the left arrow button if this is not the first itinerary
        if (mCurItineraryIndex != 0) {
            showLeftArrowButton();
        }
        // Show the right arrow button if this is not the last itinerary
        if (mCurItineraryIndex != mItineraryList.size() - 1) {
            showRightArrowButton();
        }

        Log.d(TAG, "Done displaying itinerary. Time: " + (System.currentTimeMillis() - time));
        Log.d(TAG, "Sliding panel state: " + mSlidingPanelLayout.getPanelState());

    }

    /**
     * Helper function that returns a string representing time in terms of
     * days, hours, and minutes, or seconds if shorter than a single minute
     * @param seconds
     * @return
     */
    public static String getDurationString(double seconds) {

        long totalMins = (long) seconds/60;

        long totalHours = totalMins/60;
        long remainderMins = totalMins%60;

        long days = totalHours/24;
        long remainderHours = totalHours%24;

        String duration = "";

        if (days != 0)
            duration += (days + " days ");
        if (remainderHours != 0)
            duration += (remainderHours + " h ");
        if (remainderMins != 0)
            duration += (remainderMins + " m ");

        if (duration.equals(""))
            duration = seconds + " sec ";

        // Slice off the extra space at the end
        duration = duration.substring(0, duration.length() - 1);

        return duration;
    }

    /**
     * Helper method that returns the coordinates of a Place, or, if the Place is null,
     * returns the current location
     * Returns null if a security exception was thrown
     */
    @Nullable
    private LatLng getCurrentCoordinates() {

        LatLng latLng = null;

        try {
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleAPIClient);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (SecurityException se) {
            Toast.makeText(this, "Location access permission denied", Toast.LENGTH_LONG).show();
            throw se;
        }

        return latLng;
    }

    /**
     *  Helper function to generate latitude and longitude bounds to bias the results of a Google
     *  Places autocomplete prediction to a 20-mile-wide square centered at the current location
     *  If the current location is unavailable, returns bounds encompassing the whole globe
     */
    public LatLngBounds getBoundsBias() {

        try {
            // Get current location
            Location location = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleAPIClient);

            if (location != null) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Return bounds for a 20-mile-wide square centered at the current location
                return new LatLngBounds(new LatLng(latitude - .145, longitude - .145),
                        new LatLng(latitude + .145, longitude - .145));
            } else {
                Log.d(TAG, "Current location for setting search bounds bias was null");
                // If location is null, return bounds for the whole globe
                return new LatLngBounds(new LatLng(-90,-180), new LatLng(90, 180));
            }

        } catch (SecurityException se) {

            // If we cannot access the current location, return bounds for the whole globe
            return new LatLngBounds(new LatLng(-90,-180), new LatLng(90, 180));
        }
    }

    public void onSwipeSlidingPanelLeft() {

        Log.d(TAG, "Handling swipe left");

        // Do nothing if we are displaying the last itinerary
        if (mItineraryList == null || mCurItineraryIndex == mItineraryList.size() - 1)
            return;
        if (mSlidingPanelHead == null || mSlidingPanelTail == null)
            return;

        ++mCurItineraryIndex;
        Animation slideOutLeft = AnimationUtils
                .loadAnimation(this, R.anim.slide_out_left);
        slideOutLeft.setAnimationListener(new SwipeLeftAnimationListener());
        mSlidingPanelHead.startAnimation(slideOutLeft);
        mSlidingPanelTail.startAnimation(slideOutLeft);

        if (mCurItineraryIndex == mItineraryList.size() - 1) {
            hideRightArrowButton();
        }

    }

    private class SwipeLeftAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            displayItinerary(mCurItineraryIndex,
                    mOrigin.getLocation(), mDestination.getLocation(), R.anim.slide_in_right);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }

    public void onSwipeSlidingPanelRight() {

        Log.d(TAG, "Handling swipe right");

        // Do nothing if we are displaying the first itinerary
        if (mItineraryList == null || mCurItineraryIndex == 0)
            return;
        if (mSlidingPanelHead == null || mSlidingPanelTail == null)
            return;

        --mCurItineraryIndex;
        Animation slideOutRight = AnimationUtils
                .loadAnimation(this, R.anim.slide_out_right);
        slideOutRight.setAnimationListener(new SwipeRightAnimationListener());
        mSlidingPanelHead.startAnimation(slideOutRight);
        mSlidingPanelTail.startAnimation(slideOutRight);

        if (mCurItineraryIndex == 0) {
            hideLeftArrowButton();
        }

    }

    private class SwipeRightAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            displayItinerary(mCurItineraryIndex,
                    mOrigin.getLocation(), mDestination.getLocation(), R.anim.slide_in_left);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }

    public TripPlanPlace getmOrigin() {
        return mOrigin;
    }

    public TripPlanPlace getmDestination() {
        return mDestination;
    }

    public void setmOrigin(TripPlanPlace place) {
        mOrigin = place;
    }

    public void setmDestination(TripPlanPlace place) {
        mDestination = place;
    }

    public void addToModeButtonBiMap(TraverseMode mode, ImageButton button) {
        modeToImageButtonBiMap.forcePut(mode, button);
    }

    private void setLastEditedSearchBar(SearchBarId id) {lastEditedSearchBar = id;}

    public void setDepartureArrivalTimeTextView(TextView tv) {
        mDepartureArrivalTimeTextView = tv;
    }

    public void setDepartureArrivalTimeText(String string) {
        if (mDepartureArrivalTimeTextView != null)
            mDepartureArrivalTimeTextView.setText(string);
    }

    /**
     * Helper method to toggle the sliding panel between expanded and collapsed
     */
    public void toggleSlidingPanel() {
        if (mSlidingPanelLayout == null)
            throw new RuntimeException("Sliding panel layout reference is null");
        SlidingUpPanelLayout.PanelState panelState = mSlidingPanelLayout.getPanelState();

        if (panelState == SlidingUpPanelLayout.PanelState.EXPANDED)
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else if (panelState == SlidingUpPanelLayout.PanelState.COLLAPSED)
            mSlidingPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    /**
     * Helper method to set the padding for the Google Map
     * @param state
     */
    public void setMapPadding(ActivityState state) {
        switch (state) {
            case HOME:
                mMap.setPadding(12,175,12,12);
                break;
            case HOME_PLACE_SELECTED:
                mMap.setPadding(12,12,12,12);
                break;
            case HOME_STOP_SELECTED:
                mMap.setPadding(12,12,12,12);
                break;
            case HOME_BUS_SELECTED:
                mMap.setPadding(12,12,12,12);
                break;
            case TRIP_PLAN:
                mMap.setPadding(12,550,12,12);
                break;
            case NAVIGATION:
                mMap.setPadding(12,12,12,12);
        }
    }

    /**
     * Helper method to start sending location requests
     * OnLocationChanged callback will be invoked on each response
     */
    private void beginLocationRequests() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);

        if (checkLocationPermission())
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleAPIClient, locationRequest, this);
    }

    private void hideLeftArrowButton() {
        mLeftArrowButton.setClickable(false);
        mLeftArrowButton.setVisibility(View.INVISIBLE);
    }

    private void showLeftArrowButton() {
        mLeftArrowButton.setVisibility(View.VISIBLE);
        mLeftArrowButton.setClickable(true);
    }

    private void hideRightArrowButton() {
        mRightArrowButton.setClickable(false);
        mRightArrowButton.setVisibility(View.INVISIBLE);
    }

    private void showRightArrowButton() {
        mRightArrowButton.setVisibility(View.VISIBLE);
        mRightArrowButton.setClickable(true);
    }

    private void hideArrowButtons() {
        hideLeftArrowButton();
        hideRightArrowButton();
    }

    /**
     * Helper method that returns true if the state is one of HOME,
     * HOME_PLACE_SELECTED, HOME_STOP_SELECTED, or HOME_BUS_SELECTED
     * @param state
     * @return
     */
    private boolean isHomeState(ActivityState state) {
        return (state == ActivityState.HOME ||
                state == ActivityState.HOME_PLACE_SELECTED ||
                state == ActivityState.HOME_STOP_SELECTED ||
                state == ActivityState.HOME_BUS_SELECTED);
    }

}
