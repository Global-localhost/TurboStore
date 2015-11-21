package com.phonemetra.turbo.store.mock;

import com.phonemetra.turbo.store.data.App;

public class MockApp extends App {

    public MockApp(String id) {
        this(id, "App " + id);
    }

    public MockApp(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
