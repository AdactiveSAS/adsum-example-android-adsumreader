package com.adactive.AdsumReader;


import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

public class StoreDescriptionDialog extends DialogFragment {
    public static final String ARG_STORE_NAME = "store_name";
    public static final String ARG_STORE_DESCRIPTION = "store_description";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getArguments().getString(ARG_STORE_NAME))
                .setMessage(android.text.Html.fromHtml(getArguments().getString(ARG_STORE_DESCRIPTION)).toString())
                .setNegativeButton(R.string.close, null);

        return builder.create();
    }
}
