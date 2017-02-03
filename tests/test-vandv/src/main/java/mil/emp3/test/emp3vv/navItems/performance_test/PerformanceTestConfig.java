package mil.emp3.test.emp3vv.navItems.performance_test;

/**
 * Performance test configuration parameters.
 */
public class PerformanceTestConfig {
    int trackCount;
    boolean changeAffiliation;
    boolean batchUpdates;

    public int getTrackCount() {
        return trackCount;
    }

    public void setTrackCount(int trackCount) {
        this.trackCount = trackCount;
    }

    public boolean isChangeAffiliation() {
        return changeAffiliation;
    }

    public void setChangeAffiliation(boolean changeAffiliation) {
        this.changeAffiliation = changeAffiliation;
    }

    public boolean isBatchUpdates() {
        return batchUpdates;
    }

    public void setBatchUpdates(boolean batchUpdates) {
        this.batchUpdates = batchUpdates;
    }
}
