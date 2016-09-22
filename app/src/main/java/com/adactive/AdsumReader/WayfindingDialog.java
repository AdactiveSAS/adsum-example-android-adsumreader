package com.adactive.AdsumReader;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.adactive.nativeapi.MapView;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class WayfindingDialog extends DialogFragment {

    public final static String ARG_STORES_NAMES_LIST = "stores_names_list";
    public final static String ARG_STORES_IDS_LIST = "stores_ids_list";

    private MapView map;
    private FloatingActionButton deletePath;

    private View rootView;
    private Button goButton;

    private int lastFromIndex = -1;
    private int lastToIndex = -1;

    public void setMap(MapView map) {
        this.map = map;
    }

    public void setDeletePath(FloatingActionButton deletePath) {
        this.deletePath = deletePath;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        rootView = inflater.inflate(R.layout.dialog_wayfinding, null);
        final AutoCompleteTextView actvFrom = (AutoCompleteTextView) rootView.findViewById(R.id.from_location);
        final AutoCompleteTextView actvTo = (AutoCompleteTextView) rootView.findViewById(R.id.to_location);

        final List<String> storesNamesList = getArguments().getStringArrayList(ARG_STORES_NAMES_LIST);
        final List<Integer> storesIdsList = getArguments().getIntegerArrayList(ARG_STORES_IDS_LIST);

        ArrayAdapter<String> storesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1,  storesNamesList);
        actvFrom.setAdapter(storesAdapter);
        actvTo.setAdapter(storesAdapter);

        final AlertDialog wfDialog = builder.setView(rootView)
                .setTitle(R.string.wayfinding_title)
                .setPositiveButton(R.string.go, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        wfDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                goButton = wfDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                goButton.setEnabled(false);
                goButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int fromId = storesIdsList.get(lastFromIndex);
                        int toId = storesIdsList.get(lastToIndex);

                        map.highLightPOI(fromId, getString(R.string.highlight_color));
                        map.highLightPOI(toId, getString(R.string.highlight_color));

                        //map.setPoiAsStartPoint(fromId);

                        map.drawPathToPoi(storesIdsList.get(lastToIndex));

                        deletePath.setVisibility(View.VISIBLE);
                        wfDialog.dismiss();
                    }
                });
            }
        });

        actvFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                lastFromIndex = storesNamesList.indexOf(s.toString());
                if (lastFromIndex != -1) {
                    if (lastToIndex != -1 && lastFromIndex != lastToIndex) {
                        goButton.setEnabled(true);
                    }
                } else {
                    goButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        actvTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                lastToIndex = storesNamesList.indexOf(s.toString());
                if (lastToIndex != -1) {
                    if (lastFromIndex != -1 && lastToIndex != lastFromIndex) {
                        goButton.setEnabled(true);
                    }
                } else {
                    goButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return wfDialog;
    }
}
