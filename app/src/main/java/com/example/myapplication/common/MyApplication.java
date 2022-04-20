package com.example.myapplication.common;

import android.app.Activity;
import android.app.Application;
import java.util.Stack;

public class MyApplication extends Application {
    private static  MyApplication context;
    //Managing activities through the stack
    private static Stack<Activity> activityStack;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
    }

    public static  MyApplication getContext() {
        return context;
    }
    /**
     * add Activity
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

}
