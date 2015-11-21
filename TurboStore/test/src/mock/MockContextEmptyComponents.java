package mock;

/**
 * As more components are required to test different parts of Turbo Store, we can
 * create them and add them here (and accessors to the parent class).
 */
public class MockContextEmptyComponents extends MockContextSwappableComponents {

    public MockContextEmptyComponents() {
        setPackageManager(new MockEmptyPackageManager());
        setResources(new MockEmptyResources());
    }

}
