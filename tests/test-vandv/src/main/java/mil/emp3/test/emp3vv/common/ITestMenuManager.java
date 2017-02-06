package mil.emp3.test.emp3vv.common;

public interface ITestMenuManager {
    void recreateTestMenu(String []supportedUserActions, String[] moreUserActions);
    int getMaxSupportedActions();
}
