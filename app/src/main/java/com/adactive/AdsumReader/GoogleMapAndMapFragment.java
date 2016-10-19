package com.adactive.AdsumReader;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adactive.nativeapi.AdActiveEventListener;

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
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ambroise on 14/10/2016.
 */

public class GoogleMapAndMapFragment extends MainActivity.PlaceholderFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private MapView map;
    private AdActiveEventListener adActiveEventListener;
    private View rootView;
    private MapView.CameraMode currentCameraMode;

    private RelativeLayout mapContainerSmall;
    private TextView mTapTextView;

    private com.google.android.gms.maps.MapView mapView;
    private Map<Integer, FloatingActionButton> floorButtonsMap = new HashMap<>();
    private FloatingActionButton preSelectedFloorButton;
    private FloatingActionsMenu setLevelSmall;

    public static GoogleMapAndMapFragment newInstance(MapView map) {
        GoogleMapAndMapFragment fragment = new GoogleMapAndMapFragment();
        fragment.setMap(map);
        return fragment;
    }

    private void setMap(MapView m) {
        map = m;
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
        mapContainerSmall.addView(map);

        setLevelSmall = (FloatingActionsMenu) rootView.findViewById(R.id.set_levelsmall);

        //Explainatory Dialog
        Bundle args = new Bundle();
        args.putString(StoreDescriptionDialog.ARG_STORE_NAME, "Notice");
        args.putString(StoreDescriptionDialog.ARG_STORE_DESCRIPTION, "Click on GoogleMap to center AdsumMap on Position clicked. If AdsumMap doesn't contain positions, Google Map will go to Notre Dame of Paris");
        StoreDescriptionDialog storeDialog = new StoreDescriptionDialog();
        storeDialog.setArguments(args);
        storeDialog.show(getFragmentManager(), "storeDescription");

        doBuildingClicked(map.getCurrentBuilding());

        return rootView;
    }

    //GoogleMap callbacks:

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setOnMapClickListener(this);

        getCoordinates();
        mTapTextView = (TextView) getActivity().findViewById(R.id.gmapTV);
        mTapTextView.setText("Lat=" + lati + " Long=" + longi);
        map.setCurrentPosition(lati, longi, map.getCurrentFloor());
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(lati, longi)).zoom(18 ).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lati,longi))
                .flat(true));

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
    public void onMapClick(LatLng latLng) {
        GoogleMap googleMap = mapView.getMap();
        mTapTextView = (TextView) getActivity().findViewById(R.id.gmapTV);
        mTapTextView.setText("Lat=" + latLng.latitude + " Long=" + latLng.longitude);
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .flat(true));
        map.setCurrentPosition(latLng.latitude, latLng.longitude, map.getCurrentFloor());
        map.centerOnPlace(0);
    }

    //end google map callbacks

    private void doBuildingClicked(int i) {
        final int[] floors = map.getBuildingFloors(i);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                map.unLightAll();

                // Remove all the former floorButtons of the menu */
                for (Integer floorId : floorButtonsMap.keySet()) {
                    setLevelSmall.removeButton(floorButtonsMap.get(floorId));
                }
                floorButtonsMap.clear();

                // Add all the new floorButtons on the menu
                FloatingActionButton floorButton = null;
                for (int i = floors.length - 1; i >= 0; --i) {
                    floorButton = createFloorButton(i, floors[i]);
                    //floorButtons.add(floorButton);
                    floorButtonsMap.put(floors[i], floorButton);
                    setLevelSmall.addButton(floorButton);
                }

                // Disable the current floor button
                if (floorButton != null) {
                    preSelectedFloorButton = floorButton;
                    floorButton.setEnabled(false);
                }

                // Make the setLevelSmall button visible
                setLevelSmall.setVisibility(View.VISIBLE);
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
                .buildRound(Integer.toString(level), Color.TRANSPARENT);

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

    private double longi =2.349315;
    private double lati = 48.853261;

    private void getCoordinates() {
        String path = "/data/data/com.adactive.mall/files/content.db";
        File database = new File(path);
        Log.e("data", String.valueOf(database.exists()));

        try {

            SQLiteDatabase db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            Cursor cursor = db.query(("geolocalisation"), new String[]{"longitude", "latitude"}, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                cursor.moveToNext();
                longi = cursor.getDouble(cursor.getColumnIndex("longitude"));
                lati = cursor.getDouble(cursor.getColumnIndex("latitude"));
            }

        } catch (SQLException sqlexception) {
            Log.e("Sql esception", String.valueOf(sqlexception));
        }


    }

}
