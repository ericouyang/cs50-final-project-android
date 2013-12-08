package net.cs50.recipes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AboutFragment extends Fragment {

    private static final String TAG = "about_fragment";

    public AboutFragment() {
    }

    public static AboutFragment findOrCreateFragment(FragmentManager fm, int containerId) {
        Log.i(TAG, "attempting to reload old fragment");

        AboutFragment fragment = (AboutFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            Log.i(TAG, "no old fragment, creating a new one");
            fragment = new AboutFragment();
            fm.beginTransaction().replace(containerId, fragment, TAG).commit();
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        return rootView;
    }
}
