package com.kekebox.hukewei.javlibraryapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kekebox.hukewei.javlibraryapp.R;

/**
 * Created by hukewei on 02/05/15.
 */
public class AboutFragment extends Fragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        //setHasOptionsMenu(true);
        return rootView;
    }
}
