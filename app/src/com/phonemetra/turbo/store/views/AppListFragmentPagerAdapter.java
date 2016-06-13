package com.phonemetra.turbo.store.views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.phonemetra.turbo.store.Store;
import com.phonemetra.turbo.store.R;
import com.phonemetra.turbo.store.compat.TabManager;
import com.phonemetra.turbo.store.data.AppProvider;
import com.phonemetra.turbo.store.views.fragments.AvailableAppsFragment;
import com.phonemetra.turbo.store.views.fragments.CanUpdateAppsFragment;
import com.phonemetra.turbo.store.views.fragments.InstalledAppsFragment;

/**
 * Used by the Store activity in conjunction with its ViewPager to support
 * swiping of tabs for both old devices (< 3.0) and new devices.
 */
public class AppListFragmentPagerAdapter extends FragmentPagerAdapter {

    private final Store parent;

    public AppListFragmentPagerAdapter(Store parent) {
        super(parent.getSupportFragmentManager());
        this.parent = parent;
    }

    private String getUpdateTabTitle() {
        int updateCount = AppProvider.Helper.count(parent, AppProvider.getCanUpdateUri());

        // TODO: Make RTL friendly, probably by having a different string for both tab_updates_none and tab_updates
        return parent.getString(R.string.tab_updates) + " (" + updateCount + ")";
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
        case TabManager.INDEX_AVAILABLE:
            return new AvailableAppsFragment();
        case TabManager.INDEX_INSTALLED:
            return new InstalledAppsFragment();
        default:
            return new CanUpdateAppsFragment();
        }
    }

    @Override
    public int getCount() { return TabManager.INDEX_COUNT; }

    @Override
    public String getPageTitle(int i) {
        switch (i) {
        case TabManager.INDEX_AVAILABLE:
            return parent.getString(R.string.tab_noninstalled);
        case TabManager.INDEX_INSTALLED:
            return parent.getString(R.string.inst);
        case TabManager.INDEX_CAN_UPDATE:
            return getUpdateTabTitle();
        default:
            return "";
        }
    }

}
