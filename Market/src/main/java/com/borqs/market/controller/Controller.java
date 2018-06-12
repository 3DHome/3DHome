package com.borqs.market.controller;


public abstract class Controller {
    
    @SuppressWarnings("unused")
    private static final String TAG = Controller.class.getSimpleName();

    public Controller() {
        
    }
    
    public void dispose() {}
    
    abstract public boolean handleMessage(int what, Object data);

    public boolean handleMessage(int what) {
        return handleMessage(what, null);
    }
}
