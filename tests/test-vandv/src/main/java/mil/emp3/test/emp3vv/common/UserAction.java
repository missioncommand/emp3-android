package mil.emp3.test.emp3vv.common;

public interface UserAction {
    boolean actOn(String userAction);
    String[] getSupportedUserActions(); // Actions shown in specific buttons, maximum of four button can be populated, so
                                        // only the first four items from the array will be processed.
    String[] getMoreActions();          // If test needs more than four action buttons then pass them using this method.
                                        // They will become a menu under the fifth action button

    // A specific test can use the registers menu manager to recreate the test menu. This allows for dynamically changing
    // test menu without existing to drawer test level and allows us to control number of items on the drawer menu.
    void registerTestMenuManager(ITestMenuManager testMenuManager, int maxSupportedActions);
}
