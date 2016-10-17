package com.adactive.AdsumReader;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adactive.nativeapi.AdActiveEventListener;
import com.adactive.nativeapi.CheckForUpdatesNotice;
import com.adactive.nativeapi.CheckStartNotice;
import com.adactive.nativeapi.Coordinates3D;

import com.adactive.nativeapi.MapView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ambroise on 14/10/2016.
 */

public class GoogleMapAndMapFragment extends MainActivity.PlaceholderFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {
    private MapView map;
    private AdActiveEventListener adActiveEventListener;
    private View rootView;
    private MapView.CameraMode currentCameraMode;

    private RelativeLayout mapContainerSmall;
    private TextView mTapTextView;


    private com.google.android.gms.maps.MapView mapView;
    private GoogleMap googleMap;
    private Map<Integer, FloatingActionButton> floorButtonsMap = new HashMap<>();
    private FloatingActionButton preSelectedFloorButton;
    private FloatingActionsMenu setLevel;

    public static GoogleMapAndMapFragment newInstance(MapView map) {
        GoogleMapAndMapFragment fragment = new GoogleMapAndMapFragment();
        fragment.setMap(map);
        return fragment;
    }

    private void setMap(MapView m) {
        map = m;

        adActiveEventListener = new AdActiveEventListener() {
            @Override
            public void OnPOIClickedHandler(int[] POIs, int place) {
            }

            @Override
            public void OnBuildingClickedHandler(int i) {
            }

            @Override
            public void OnFloorChangedHandler(int floorId) {
            }


            @Override
            public void OnFloorClickedHandler(int i) {
            }

            @Override
            public void OnTextClickedHandler(int[] POIs, int place) {
            }

            @Override
            public void OnMapLoadedHandler() {
            }

            @Override
            public void OnAdActiveViewStartHandler(int stateId) {
                if (stateId == CheckStartNotice.ADACTIVEVIEW_DID_START) {

                }
            }

            @Override
            public void OnCheckForUpdatesHandler(int i) {
                if (i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESFOUND || i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESNOTFOUND) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.findViewById(R.id.map_small).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.progress_container_small).setVisibility(View.GONE);

                        }
                    });

                    map.start();
                }
            }

            @Override
            public void OnFloorIntersectedAtPositionHandler(int i, Coordinates3D coordinates3D) {

            }
        };

        //map.addEventListener(adActiveEventListener);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_map_gmap, container, false);

        //load google map
        mapView = (com.google.android.gms.maps.MapView) rootView.findViewById(R.id.gmap_container);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);

        //loading of the map
        mapContainerSmall = (RelativeLayout) rootView.findViewById(R.id.map_container_small);
        currentCameraMode = MapView.CameraMode.ORTHO;

        if (!map.isMapDataAvailable()) {
            rootView.findViewById(R.id.map_small).setVisibility(View.GONE);
            rootView.findViewById(R.id.progress_container_small).setVisibility(View.VISIBLE);
        }

        mapContainerSmall.addView(map);

        //search = ((MainActivity) getActivity()).getSearchBox();
        //search.enableVoiceRecognition(this);
        setLevel = (FloatingActionsMenu) rootView.findViewById(R.id.set_levelsmall);
        doBuildingClicked(map.getCurrentBuilding());


        Bundle args = new Bundle();
        args.putString(StoreDescriptionDialog.ARG_STORE_NAME, "Notice");
        args.putString(StoreDescriptionDialog.ARG_STORE_DESCRIPTION, "Click long on google Map to center Adsum Map on Position clicked");
        StoreDescriptionDialog storeDialog = new StoreDescriptionDialog();
        storeDialog.setArguments(args);
        storeDialog.show(getFragmentManager(), "storeDescription");


        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMapLongClickListener(this);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(17.385044, 78.486671)).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));


    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mTapTextView = (TextView) getActivity().findViewById(R.id.gmapTV);
        mTapTextView.setText("Lat=" + latLng.latitude + " Long=" + latLng.longitude);
        map.setCurrentPosition(latLng.latitude, latLng.longitude, map.getCurrentFloor());
        map.centerOnPlace(0);
    }

    @Override
    public void onMapClick(LatLng point) {
        mTapTextView = (TextView) getActivity().findViewById(R.id.gmapTV);
        Log.e("taped", String.valueOf(point));
        mTapTextView.setText("Lat=" + point.latitude + " Long=" + point.longitude);
    }

    private void doBuildingClicked(int i) {
        final int[] floors = map.getBuildingFloors(i);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.unLightAll();

                // Remove all the former floorButtons of the menu */
                for (Integer floorId : floorButtonsMap.keySet()) {
                    setLevel.removeButton(floorButtonsMap.get(floorId));
                }
                floorButtonsMap.clear();

                // Add all the new floorButtons on the menu
                FloatingActionButton floorButton = null;
                for (int i = floors.length - 1; i >= 0; --i) {
                    floorButton = createFloorButton(i, floors[i]);
                    //floorButtons.add(floorButton);
                    floorButtonsMap.put(floors[i], floorButton);
                    setLevel.addButton(floorButton);
                }

                // Disable the current floor button
                if (floorButton != null) {
                    preSelectedFloorButton = floorButton;
                    floorButton.setEnabled(false);
                }

                // Make the setLevel button visible
                setLevel.setVisibility(View.VISIBLE);
            }
        });

        map.setCurrentBuilding(i);
    }

    private FloatingActionButton createFloorButton(int level, final int floorId) {
        FloatingActionButton floorButton = new FloatingActionButton(getActivity().getBaseContext());
        floorButton.setSize(FloatingActionButton.SIZE_MINI);
        floorButton.setColorNormalResId(R.color.white);
        floorButton.setColorPressedResId(R.color.white_pressed);

        TextDrawable floor_icon = TextDrawable.builder()
                .beginConfig()
                .fontSize(30)
                .textColor(Color.BLACK)
                .endConfig()
                .buildRect(Integer.toString(level), Color.TRANSPARENT);

        floorButton.setIconDrawable(floor_icon);

        floorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFloorChanged(floorId);
                map.setCurrentFloor(floorId);
            }
        });

        return floorButton;
    }

    private void doFloorChanged(final int floorId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (preSelectedFloorButton != null) {
                    preSelectedFloorButton.setEnabled(true);
                }

                // Disable the current floor button
                preSelectedFloorButton = floorButtonsMap.get(floorId);
                preSelectedFloorButton.setEnabled(false);
            }
        });

    }

}
