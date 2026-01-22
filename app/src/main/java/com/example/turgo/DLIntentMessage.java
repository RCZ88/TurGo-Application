package com.example.turgo;

public enum DLIntentMessage {
    EXTRA_LOADING_INPUT("loading_input"),
    EXTRA_IS_FRAGMENT("is_fragment"),
    EXTRA_TARGET_FRAGMENT("target_fragment"),
    EXTRA_TARGET_ACTIVITY("target_activity"),
    EXTRA_TARGET_USER("full_user_object"),
    EXTRA_TARGET_BOTTOM_NAV("bottom_nav");
    private final String intentMessage;
     DLIntentMessage(String intentMessage){
        this.intentMessage = intentMessage;
    }
    public String getIntentMessage(){
         return intentMessage;
    }

}
