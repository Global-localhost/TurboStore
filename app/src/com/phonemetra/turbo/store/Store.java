/*
 * Copyright (C) 2010-12  Ciaran Gultnieks, ciaran@ciarang.com
 * Copyright (C) 2009  Roberto Jacinto, roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.phonemetra.turbo.store;

import android.app.NotificationManager;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.phonemetra.turbo.store.compat.TabManager;
import com.phonemetra.turbo.store.data.AppProvider;
import com.phonemetra.turbo.store.data.NewRepoConfig;
import com.phonemetra.turbo.store.installer.InstallIntoSystemDialogActivity;
import com.phonemetra.turbo.store.views.AppListFragmentPagerAdapter;
import com.phonemetra.turbo.store.views.ManageReposActivity;
import com.phonemetra.turbo.store.views.swap.ConnectSwapActivity;
import com.phonemetra.turbo.store.views.swap.SwapActivity;

//AirPush Bundle 1 SDK
import com.lqtyukk.gcdqlok220440.AdConfig;
import com.lqtyukk.gcdqlok220440.AdConfig.AdType;
import com.lqtyukk.gcdqlok220440.AdListener;
import com.lqtyukk.gcdqlok220440.EulaListener;
import com.lqtyukk.gcdqlok220440.Main;

public class Store extends ActionBarActivity  implements EulaListener, AdListener {

    private static final String TAG = "TurboStore";

    public static final int REQUEST_PREFS = 1;
    public static final int REQUEST_ENABLE_BLUETOOTH = 2;
    public static final int REQUEST_SWAP = 3;

    public static final String EXTRA_TAB_UPDATE = "extraTab";

    public static final String ACTION_ADD_REPO = "com.phonemetra.turbo.store.Store.ACTION_ADD_REPO";

    private StoreApp storeApp = null;

    private ViewPager viewPager;

    private TabManager tabManager = null;

    private Main main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        storeApp = ((StoreApp) getApplication());
        storeApp.applyTheme(this);

        super.onCreate(savedInstanceState);

        AdConfig.setAppId(280476);  //setting appid.
        AdConfig.setApiKey("1424213552220440862"); //setting apikey
        AdConfig.setEulaListener(this); //setting EULA listener.
        AdConfig.setAdListener(this);  //setting global Ad listener.
        AdConfig.setCachingEnabled(true); //Enabling SmartWall ad caching.
        AdConfig.setPlacementId(0); //pass the placement id.

        setContentView(R.layout.store);

        //Initialize Airpush
        main=new Main(this);
        //for calling banner 360.
        main.start360BannerAd(this);
        //for calling Smartwall ad
        main.startInterstitialAd(AdType.smartwall);


        createViews();

        getTabManager().createTabs();

        // Start a search by just typing
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        Intent intent = getIntent();

        // If the intent can be handled via AppDetails or SearchResults, it
        // will call finish() and the rest of the code won't execute
        handleIntent(intent);

        if (intent.hasExtra(EXTRA_TAB_UPDATE)) {
            boolean showUpdateTab = intent.getBooleanExtra(EXTRA_TAB_UPDATE, false);
            if (showUpdateTab) {
                getTabManager().selectTab(2);
            }
        }

        Uri uri = AppProvider.getContentUri();
        getContentResolver().registerContentObserver(uri, true, new AppObserver());

        InstallIntoSystemDialogActivity.firstTime(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // AppDetails and RepoDetailsActivity set different NFC actions, so reset here
        NfcHelper.setAndroidBeam(this, getApplication().getPackageName());
        checkForAddRepoIntent();
    }

    private void handleIntent(Intent intent) {
        final Uri data = intent.getData();
        if (data == null) {
            return;
        }
        final String scheme = data.getScheme();
        final String path = data.getPath();
        String appId = null;
        String query = null;
        if (data.isHierarchical()) {
            final String host = data.getHost();
            if (host == null) {
                return;
            }
            switch (host) {
            case "f-droid.org":
                // http://f-droid.org/app/app.id
                if (path.startsWith("/repository/browse")) {
                    // http://f-droid.org/repository/browse?fdid=app.id
                    appId = data.getQueryParameter("fdid");
                } else if (path.startsWith("/app")) {
                    appId = data.getLastPathSegment();
                    if (appId != null && appId.equals("app")) {
                        appId = null;
                    }
                }
                break;
            case "details":
                // market://details?id=app.id
                appId = data.getQueryParameter("id");
                break;
            case "search":
                // market://search?q=query
                query = data.getQueryParameter("q");
                break;
            case "play.google.com":
                if (path.startsWith("/store/apps/details")) {
                    // http://play.google.com/store/apps/details?id=app.id
                    appId = data.getQueryParameter("id");
                } else if (path.startsWith("/store/search")) {
                    // http://play.google.com/store/search?q=foo
                    query = data.getQueryParameter("q");
                }
                break;
            case "apps":
            case "amazon.com":
            case "www.amazon.com":
                // amzn://apps/android?p=app.id
                // http://amazon.com/gp/mas/dl/android?p=app.id
                appId = data.getQueryParameter("p");
                query = data.getQueryParameter("s");
                break;
            }
        } else if (scheme.equals("fdroid.app")) {
            // fdroid.app:app.id
            appId = data.getSchemeSpecificPart();
        } else if (scheme.equals("fdroid.search")) {
            // fdroid.search:query
            query = data.getSchemeSpecificPart();
        }

        if (!TextUtils.isEmpty(query)) {
            // an old format for querying via packageName
            if (query.startsWith("pname:"))
                appId = query.split(":")[1];

            // sometimes, search URLs include pub: or other things before the query string
            if (query.contains(":"))
                query = query.split(":")[1];
        }

        Intent call = null;
        if (!TextUtils.isEmpty(appId)) {
            Utils.DebugLog(TAG, "FDroid launched via app link for '" + appId + "'");
            call = new Intent(this, AppDetails.class);
            call.putExtra(AppDetails.EXTRA_APPID, appId);
        } else if (!TextUtils.isEmpty(query)) {
            Utils.DebugLog(TAG, "FDroid launched via search link for '" + query + "'");
            call = new Intent(this, SearchResults.class);
            call.setAction(Intent.ACTION_SEARCH);
            call.putExtra(SearchManager.QUERY, query);
        }
        if (call != null) {
            startActivity(call);
            finish();
        }
    }

    private void checkForAddRepoIntent() {
        // Don't handle the intent after coming back to this view (e.g. after hitting the back button)
        // http://stackoverflow.com/a/14820849
        Intent intent = getIntent();
        if (!intent.hasExtra("handled")) {
            NewRepoConfig parser = new NewRepoConfig(this, intent);
            if (parser.isValidRepo()) {
                intent.putExtra("handled", true);
                if (parser.isFromSwap()) {
                    Intent confirmIntent = new Intent(this, ConnectSwapActivity.class);
                    confirmIntent.setData(intent.getData());
                    startActivityForResult(confirmIntent, REQUEST_SWAP);
                } else {
                    startActivity(new Intent(ACTION_ADD_REPO, intent.getData(), this, ManageReposActivity.class));
                }
            } else if (parser.getErrorMessage() != null) {
                Toast.makeText(this, parser.getErrorMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getTabManager().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (storeApp.bluetoothAdapter == null) {
            // ignore on devices without Bluetooth
            MenuItem btItem = menu.findItem(R.id.action_bluetooth_apk);
            btItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

        case R.id.action_update_repo:
            UpdateService.updateNow(this);
            return true;

        case R.id.action_manage_repos:
            startActivity(new Intent(this, ManageReposActivity.class));
            return true;

        case R.id.action_settings:
            Intent prefs = new Intent(getBaseContext(), PreferencesActivity.class);
            startActivityForResult(prefs, REQUEST_PREFS);
            return true;

        case R.id.action_swap:
            startActivity(new Intent(this, SwapActivity.class));
            return true;

        case R.id.action_search:
            onSearchRequested();
            return true;

        case R.id.action_bluetooth_apk:
            /*
             * If Bluetooth has not been enabled/turned on, then enabling
             * device discoverability will automatically enable Bluetooth
             */
            Intent discoverBt = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverBt.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
            startActivityForResult(discoverBt, REQUEST_ENABLE_BLUETOOTH);
            // if this is successful, the Bluetooth transfer is started
            return true;

        case R.id.action_about:
            View view = LayoutInflater.from(this).inflate(R.layout.about, null);
            // Fill in the version...
            try {
                PackageInfo pi = getPackageManager()
                        .getPackageInfo(getApplicationContext()
                                .getPackageName(), 0);
                ((TextView) view.findViewById(R.id.version))
                        .setText(pi.versionName);
            } catch (Exception ignored) {
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this).setView(view);
            final AlertDialog alrt = builder.create();
            alrt.setTitle(R.string.about_title);
            alrt.setButton(AlertDialog.BUTTON_NEUTRAL,
                    getString(R.string.about_website),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                            Uri uri = Uri.parse("https://phonemetra.com");
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    });
            alrt.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                int whichButton) {
                        }
                    });
            alrt.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case REQUEST_PREFS:
            // The automatic update settings may have changed, so reschedule (or
            // unschedule) the service accordingly. It's cheap, so no need to
            // check if the particular setting has actually been changed.
            UpdateService.schedule(getBaseContext());

            if ((resultCode & PreferencesActivity.RESULT_RESTART) != 0) {
                ((StoreApp) getApplication()).reloadTheme();
                final Intent intent = getIntent();
                overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
            break;
        case REQUEST_ENABLE_BLUETOOTH:
            storeApp.sendViaBluetooth(this, resultCode, "com.phonemetra.turbo.store");
            break;
        }
    }

    private void createViews() {
        viewPager = (ViewPager)findViewById(R.id.main_pager);
        AppListFragmentPagerAdapter viewPagerAdapter = new AppListFragmentPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getTabManager().selectTab(position);
            }
        });
    }

    private TabManager getTabManager() {
        if (tabManager == null) {
            tabManager = new TabManager(this, viewPager);
        }
        return tabManager;
    }

    public void refreshUpdateTabLabel() {
        getTabManager().refreshTabLabel(TabManager.INDEX_CAN_UPDATE);
    }

    public void removeNotification(int id) {
        NotificationManager nMgr = (NotificationManager) getBaseContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(id);
    }

    private class AppObserver extends ContentObserver {

        public AppObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Store.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshUpdateTabLabel();
                }
            });
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

    }

    @Override
    public void optinResult(boolean arg0) {

    }



    @Override
    public void showingEula() {

    }



    @Override
    public void noAdListener() {

    }



    @Override
    public void onAdCached(AdType arg0) {

    }



    @Override
    public void onAdClickedListener() {

    }



    @Override
    public void onAdClosed() {

    }



    @Override
    public void onAdError(String arg0) {

    }



    @Override
    public void onAdExpandedListner() {

    }



    @Override
    public void onAdLoadedListener() {

    }



    @Override
    public void onAdLoadingListener() {

    }



    @Override
    public void onAdShowing() {

    }



    @Override
    public void onCloseListener() {

    }



    @Override
    public void onIntegrationError(String arg0) {

    }


}

