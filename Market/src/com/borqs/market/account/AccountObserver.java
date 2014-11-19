package com.borqs.market.account;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class AccountObserver {
    private static final  HashMap<String,WeakReference<AccountListener>> listeners
            = new HashMap<String,WeakReference<AccountListener>>();
    
    public static void registerAccountListener(String key,AccountListener listener) {
        
        synchronized(listeners) {
            WeakReference<AccountListener> ref = listeners.get(key);
            
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            listeners.put(key, new WeakReference<AccountListener>(listener));
        }
    }
    
    public static void unRegisterAccountListener(String key) {
        
        synchronized(listeners) {
            WeakReference<AccountListener> ref = listeners.get(key);
            
            if(ref != null && ref.get() != null) {
                ref.clear();
            }
            listeners.remove(key);
        }
    }
    
    public static void login() {
        synchronized(listeners) {
            Set<String> keySets = listeners.keySet();
            Iterator<String> iterator = keySets.iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                WeakReference<AccountListener> ref = listeners.get(key);
                if(ref != null && ref.get() != null) {
                    AccountListener listener =  ref.get();
                    listener.onLogin();
                }
            }
        }
    }
    
    public static void loging() {
        synchronized(listeners) {
            Set<String> keySets = listeners.keySet();
            Iterator<String> iterator = keySets.iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                WeakReference<AccountListener> ref = listeners.get(key);
                if(ref != null && ref.get() != null) {
                    AccountListener listener =  ref.get();
                    listener.onLoging();
                }
            }
        }
    }
    
    public static void logout() {
        synchronized(listeners) {
            Set<String> keySets = listeners.keySet();
            Iterator<String> iterator = keySets.iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                WeakReference<AccountListener> ref = listeners.get(key);
                if(ref != null && ref.get() != null) {
                    AccountListener listener =  ref.get();
                    listener.onLogout();
                }
            }
        }
    }
    
    public static void cancleLogin() {
        synchronized(listeners) {
            Set<String> keySets = listeners.keySet();
            Iterator<String> iterator = keySets.iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                WeakReference<AccountListener> ref = listeners.get(key);
                if(ref != null && ref.get() != null) {
                    AccountListener listener =  ref.get();
                    listener.onCancelLogin();
                }
            }
        }
    }
}
