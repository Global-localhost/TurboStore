package com.phonemetra.turbo.store.views.fragments;

import android.net.Uri;

import com.phonemetra.turbo.store.R;
import com.phonemetra.turbo.store.data.AppProvider;
import com.phonemetra.turbo.store.views.AppListAdapter;
import com.phonemetra.turbo.store.views.InstalledAppListAdapter;

public class InstalledAppsFragment extends AppListFragment {

    @Override
    protected AppListAdapter getAppListAdapter() {
        return new InstalledAppListAdapter(getActivity(), null);
    }

    @Override
    protected String getEmptyMessage() {
        return getActivity().getString(R.string.empty_installed_app_list);
    }

    @Override
    protected String getFromTitle() {
        return getString(R.string.inst);
    }

    @Override
    protected Uri getDataUri() {
        return AppProvider.getInstalledUri();
    }

}
