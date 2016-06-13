package com.phonemetra.turbo.store;

import android.annotation.TargetApi;
import android.os.Build;
import android.test.ActivityInstrumentationTestCase2;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class StoreTest extends ActivityInstrumentationTestCase2<Store> {

    public StoreTest() {
        super("com.phonemetra.turbo.store", Store.class);
    }

}
