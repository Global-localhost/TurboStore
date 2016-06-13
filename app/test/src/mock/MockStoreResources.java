package mock;

import android.content.Context;
import android.test.mock.MockResources;

import com.phonemetra.turbo.store.R;

public class MockStoreResources extends MockResources {

    private Context getStringDelegatingContext;

    public MockStoreResources(Context getStringDelegatingContext) {
        this.getStringDelegatingContext = getStringDelegatingContext;
    }

    @Override
    public String getString(int id) {
        return getStringDelegatingContext.getString(id);
    }

    @Override
    public int getInteger(int id) {
        if (id == R.integer.phonemetra_repo_inuse) {
            return 1;
        //} else if (id == R.integer.phonemetra_archive_inuse) {
        //    return 0;
        } else if (id == R.integer.phonemetra_repo_priority) {
            return 10;
        //} else if (id == R.integer.phonemetra_archive_priority) {
        //    return 20;
        } else {
            return 0;
        }
}

}
