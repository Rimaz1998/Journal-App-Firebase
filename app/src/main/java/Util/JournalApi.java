package Util;

import android.app.Application;

//create a singleton class to use the user information across the application
public class JournalApi extends Application {
    private  String username;
    private String userId;
    private static JournalApi apiInstance;

    //return an instance if not already available
    public static JournalApi getApiInstance(){
        if (apiInstance == null)
            apiInstance = new JournalApi();
            return apiInstance;
    }

    public JournalApi() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
