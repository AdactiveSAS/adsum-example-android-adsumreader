package com.adactive.AdsumReader.Structure;

import com.adactive.nativeapi.DataObject.Poi;
import com.adactive.nativeapi.DataObject.Store;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ambroise on 18/10/2016.
 */

public class PoiCollection {

    private List<Poi> pois = null;

    private Map<Integer, Store> mStoreList = new HashMap();
    private Map<String, Integer> mIdNameList = new HashMap<>();
    private Map<String, Poi> mPoiName = new HashMap<>();
    private Map<Integer, Poi> mPoiInteger = new HashMap<>();

    private List<String> mNameList = new ArrayList<>();
    private List<Integer> mIdList = new ArrayList<>();

    private List<String> wfNameList = new ArrayList<>();
    private List<Integer> wfIdList = new ArrayList<>();


    public PoiCollection(List<Poi> aPois) {
        pois = aPois;

        for (Poi o : pois) {
            if (o.getName() != null) {

                mIdNameList.put(o.getName(), o.getId());

                mPoiName.put(o.getName(), o);
                mPoiInteger.put(o.getId(), o);
                mNameList.add(o.getName());

            }
            //select pois with a place
            if (!o.getPlaces().isEmpty()) {
                wfNameList.add(o.getName());
            }
        }
        sortPoiNameMap();
    }

    private void sortPoiNameMap() {
        Collections.sort(mNameList, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(wfNameList,String.CASE_INSENSITIVE_ORDER);
        sortIdList();
    }

    private void sortIdList() {
        //id lists are sorted in the same order as the name lists
        for (String storeName : mNameList) {
            mIdList.add(mIdNameList.get(storeName));
        }
        for (String name :wfNameList){
            wfIdList.add(mIdNameList.get(name));
        }

    }

    public Poi getByName(String name) {
        return mPoiName.get(name);
    }

    public Poi getById(Integer id) {
        return mPoiInteger.get(id);
    }

    public List<String> getPoiNamesSortedList() {
        return mNameList;
    }

    public List<Integer> getPoiIdSortedList() {
        return mIdList;
    }

    public List<String> getWfNameList(){
        //optimised list for wayfinding, returns only pois with places
        return wfNameList;
    }

    public List<Integer> getWfIdList(){
        return wfIdList;
    }

    public Store getStoresbyId(Integer id) {
        return mStoreList.get(id);

    }
}
