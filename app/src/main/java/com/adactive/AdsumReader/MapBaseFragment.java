package com.adactive.AdsumReader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.adactive.nativeapi.AdActiveEventListener;
import com.adactive.nativeapi.CheckForUpdatesNotice;
import com.adactive.nativeapi.CheckStartNotice;
import com.adactive.nativeapi.Coordinates3D;
import com.adactive.nativeapi.MapView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static java.security.AccessController.getContext;

//import com.adactive.nativeapi.DataObject.Store;

public class MapBaseFragment extends MainActivity.PlaceholderFragment {

    static private boolean isMapLoaded = false;
    static private MapView.CameraMode currentCameraMode = MapView.CameraMode.FULL;


    //GPS

    // Don't initialize location manager, retrieve it from system services.
    private LocationManager locationManager;
    private LocationListener locationListener;
    private long minTime = 5; // Minimum time interval for update in seconds, i.e. 5 seconds.
    private long minDistance = 1; // Minimum distance change for update in meters, i.e. 10 meters.

    private View rootView;
    private MapView map;
    private LinearLayout mapContainer;

    private ViewGroup container;

    private RelativeLayout gMapcontainer;
    private RelativeLayout mapContainerSmall;

    private FloatingActionsMenu setLevel;
    private FloatingActionButton setSiteView;
    private FloatingActionButton deletePath;

    private FloatingActionButton preSelectedFloorButton;
    private Map<Integer, FloatingActionButton> floorButtonsMap = new HashMap<>();

    private SearchBox search;
    private boolean isMenuEnabled = true;

    private ArrayList<String> storesNamesList = new ArrayList<>();
    private ArrayList<Integer> storesIdsList = new ArrayList<>();

    //private Map<String, Store> storesNamesMap = new HashMap<>();
    //  private Map<Integer, Store> storesIdsMap = new HashMap<>();

    private AdActiveEventListener adActiveEventListener;

    public static MapBaseFragment newInstance(MapView map) {
        MapBaseFragment fragment = new MapBaseFragment();
        fragment.setMap(map);
        return fragment;
    }

    public MapBaseFragment() {
    }

    public void setMap(MapView m) {
        map = m;

        adActiveEventListener = new AdActiveEventListener() {
            @Override
            public void OnPOIClickedHandler(int[] POIs, int place) {
                doPOIClicked(place);
            }

            @Override
            public void OnBuildingClickedHandler(int i) {
                doBuildingClicked(i);
            }

            @Override
            public void OnFloorChangedHandler(int floorId) {
                if (!floorButtonsMap.isEmpty()) {
                    doFloorChanged(floorId);
                }
            }


            @Override
            public void OnFloorClickedHandler(int i) {
            }

            @Override
            public void OnTextClickedHandler(int[] POIs, int place) {
                doTextClicked(POIs[0]);
            }

            @Override
            public void OnMapLoadedHandler() {
            }

            @Override
            public void OnAdActiveViewStartHandler(int stateId) {
                if (stateId == CheckStartNotice.ADACTIVEVIEW_DID_START) {
                    doMapLoaded();
                }
            }

            @Override
            public void OnCheckForUpdatesHandler(int i) {
                if (i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESFOUND || i == CheckForUpdatesNotice.CHECKFORUPDATES_UPDATESNOTFOUND) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.findViewById(R.id.map).setVisibility(View.VISIBLE);
                            rootView.findViewById(R.id.progress_container).setVisibility(View.GONE);
                            isMenuEnabled = true;
                        }
                    });

                    map.start();
                }
            }

            @Override
            public void OnFloorIntersectedAtPositionHandler(int i, Coordinates3D coordinates3D) {

            }
        };

        map.addEventListener(adActiveEventListener);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void showGmap() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("methodName", "myMethod");
        startActivity(intent);
        //MainActivity mainActivity=new MainActivity();
        //mainActivity.launchDoubleMap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup acontainer, Bundle savedInstanceState) {
        this.container = acontainer;

        rootView = inflater.inflate(R.layout.fragment_map, container, false);


        //TODO Comment when ORTHO will be fix
        currentCameraMode = MapView.CameraMode.FULL;

        mapContainer = (LinearLayout) rootView.findViewById(R.id.map_container);
        setSiteView = (FloatingActionButton) rootView.findViewById(R.id.set_site_view);
        setLevel = (FloatingActionsMenu) rootView.findViewById(R.id.set_level);
        deletePath = (FloatingActionButton) rootView.findViewById(R.id.delete_path);

        if (!map.isMapDataAvailable()) {
            rootView.findViewById(R.id.map).setVisibility(View.GONE);
            rootView.findViewById(R.id.progress_container).setVisibility(View.VISIBLE);
            isMenuEnabled = false;
        }


        if(map.getParent()!=null)
            ((ViewGroup)map.getParent()).removeView(map);
        mapContainer.addView(map);

        if (isMapLoaded) {
            doMapLoaded();
        }

        search = ((MainActivity) getActivity()).getSearchBox();
        search.enableVoiceRecognition(this);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        this.locationListener = new LocationListener() {

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
                Toast.makeText(getActivity(), "Provider enabled: " + provider, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(getActivity(),
                        "Provider disabled: " + provider, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onLocationChanged(Location location) {
                // Do work with new location. Implementation of this method will be covered later.
                //   mMap.setGPSCoordinatesAsStartPoint(location.getLatitude(), location.getLongitude(), mMap.getCurrentFloor());
                if (location != null) {
                    //Toast.makeText(getActivity(), "latitude : " + location.getLatitude() + " longitude : " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, minTime, minDistance, locationListener);


        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!((MainActivity) getActivity()).isNavigationDrawerOpen()) {
            inflater.inflate(R.menu.map_menu, menu);

            // Set the switch title according to the current mode
            if (currentCameraMode == MapView.CameraMode.FULL) {
                menu.findItem(R.id.switch_camera).setTitle(getString(R.string.action_switch_2D));
            } else if (currentCameraMode == MapView.CameraMode.ORTHO) {
                menu.findItem(R.id.switch_camera).setTitle(getString(R.string.action_switch_3D));
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPause() {
        if (map != null)
            map.onPause();
        super.onPause();


    }

    @Override
    public void onResume() {

        if (map != null)
            map.onResume();
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (isMenuEnabled) {
            // Change the icon in the action bar and the camera mode
            if (id == R.id.switch_camera) {
                doSwitchCamera(item);
                return true;
            }

            // Show the wayfinding dialog
            if (id == R.id.wayfinding) {
                doWayfinding();
                return true;
            }

            // Open the search menu
            if (id == R.id.search) {
                doOpenSearch();
                return true;
            }
            if (id == R.id.precision_GPS) {
                showGmap();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapContainer.removeView(map);
        map.removeEventListener(adActiveEventListener);

        locationManager.removeUpdates(this.locationListener);
        //Close the keyboard
        /*
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        */

        if (!isMenuEnabled) {
            search.toggleSearch();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (isAdded() && requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == getActivity().RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            search.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
        private void initializeArrays(Store[] storesArray) {
            for(Store store : storesArray) {
                if(store.getName() != null && map.getPOIPlaces(store.getId()).length != 0) {
                    storesNamesMap.put(store.getName(), store);
                    storesIdsMap.put(store.getId(), store);
                    storesNamesList.add(store.getName());
                }
            }

            // Sort the store names et ids lists by aplhabetical order
            Collections.sort(storesNamesList, String.CASE_INSENSITIVE_ORDER);
            for(String storeName : storesNamesList) {
              /  storesIdsList.add(storesNamesMap.get(storeName).getId());
            }
        }
    */
    private void doMapLoaded() {

        //   initializeArrays(map.getALLStore());
        final boolean isInBuilding = map.getCurrentBuilding() != -1;

        // Configure the map
        map.customizeInactivePlaces(getString(R.string.inactive_color));
        map.limitCameraMovement(true);
        map.setSiteVisible(false);
        map.setCameraMode(currentCameraMode);
        map.resetPath();

        setSiteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSetSiteView();
            }
        });

        deletePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.resetPath();
                map.unLightAll();
                deletePath.setVisibility(View.GONE);
            }
        });

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInBuilding) {
                    setSiteView.setIcon(R.drawable.ic_chevron_left_black_48dp);
                    setLevel.setVisibility(View.VISIBLE);
                } else {
                    setSiteView.setIcon(R.drawable.ic_home_black_48dp);
                    setLevel.setVisibility(View.GONE);
                }

                setSiteView.setVisibility(View.VISIBLE);
            }
        });

        if (isInBuilding) {
            doBuildingClicked(map.getCurrentBuilding());
        }

        isMapLoaded = true;

    }

    private void doSetSiteView() {

        // Collapse the setLevel menu
        setLevel.collapse();

        // Change the icon of the setSiteView button (into home)
        setSiteView.setIcon(R.drawable.ic_home_black_48dp);

        // Make the setLevel button invisible
        setLevel.setVisibility(View.GONE);

        map.setSiteView();
    }

    private void doPOIClicked(int placeId) {
        map.unLightAll();
        map.highLightPlace(placeId, getString(R.string.highlight_color));
        map.centerOnPlace(placeId);
    }

    private void doTextClicked(int POI) {
        Bundle args = new Bundle();

        // String description = storesIdsMap.get(POI).getDescription() != null ? storesIdsMap.get(POI).getDescription() : getString(R.string.no_description);

        //  args.putString(StoreDescriptionDialog.ARG_STORE_NAME, storesIdsMap.get(POI).getName());
        // args.putString(StoreDescriptionDialog.ARG_STORE_DESCRIPTION, description);

        StoreDescriptionDialog storeDialog = new StoreDescriptionDialog();
        storeDialog.setArguments(args);

        storeDialog.show(getFragmentManager(), "storeDescription");
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

                // Change the icon of the setSiteView button (into arrow)
                setSiteView.setIcon(R.drawable.ic_chevron_left_black_48dp);

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

    private void doSwitchCamera(MenuItem item) {
        if (currentCameraMode == MapView.CameraMode.FULL) {
            currentCameraMode = MapView.CameraMode.ORTHO;
            item.setTitle(getString(R.string.action_switch_3D));
        } else if (currentCameraMode == MapView.CameraMode.ORTHO) {
            currentCameraMode = MapView.CameraMode.FULL;
            item.setTitle(getString(R.string.action_switch_2D));
        }

        map.setCameraMode(currentCameraMode);
    }

    private void doWayfinding() {
        Bundle args = new Bundle();

        args.putStringArrayList(WayfindingDialog.ARG_STORES_NAMES_LIST, storesNamesList);
        args.putIntegerArrayList(WayfindingDialog.ARG_STORES_IDS_LIST, storesIdsList);

        WayfindingDialog wayfindingDialog = new WayfindingDialog();
        wayfindingDialog.setArguments(args);
        wayfindingDialog.setMap(map);
        wayfindingDialog.setDeletePath(deletePath);

        wayfindingDialog.show(getFragmentManager(), "wayfinding");
    }

    private void doOpenSearch() {
        map.onPause();
        isMenuEnabled = false;
        search.revealFromMenuItem(R.id.search, getActivity());

        for (String storeName : storesNamesList) {
            SearchResult option = new SearchResult(storeName, getResources().getDrawable(R.drawable.ic_store_black_48dp));
            search.addSearchable(option);
        }

        search.setSearchListener(new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {
            }

            @Override
            public void onSearchClosed() {
                doCloseSearch();
            }

            @Override
            public void onSearchTermChanged() {
            }

            @Override
            public void onSearch(String searchTerm) {
                doSearch(searchTerm);
            }

            @Override
            public void onSearchCleared() {
            }
        });


    }

    private void doCloseSearch() {
        map.onResume();
        isMenuEnabled = true;
        search.clearSearchable();
        search.clearResults();
        search.setSearchString("");
        search.hideCircularly(getActivity());
    }

    private void doSearch(String searchTerm) {
        if (storesNamesList.contains(searchTerm)) {

            //TODO Comment when ORTHO will be fix
            /*
            int POI = storesNamesMap.get(searchTerm).getId();
            map.unLightAll();
            map.highLightPOI(POI, getString(R.string.highlight_color));
            map.centerOnPOI(POI);
            */


            //map.centerOnPlace(placeId);
        } else {
            Toast.makeText(getActivity(), searchTerm + getString(R.string.search_error), Toast.LENGTH_SHORT).show();
        }
    }
//GPS functions


}