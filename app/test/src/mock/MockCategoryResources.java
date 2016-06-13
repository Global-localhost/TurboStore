package mock;

import android.content.Context;

import com.phonemetra.turbo.store.R;

public class MockCategoryResources extends MockStoreResources {

    public MockCategoryResources(Context getStringDelegatingContext) {
        super(getStringDelegatingContext);
    }

    @Override
    public String getString(int id) {
        if (id == R.string.category_all) {
            return "All";
        } else if (id == R.string.category_recentlyupdated) {
            return "Recently Updated";
        } else if (id == R.string.category_whatsnew) {
            return "Whats New";
        } else {
            return "";
        }
}

}
