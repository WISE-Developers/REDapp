package ca.redapp.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class RPreferences {

    private Preferences prefs;

    private final List<PreferenceMigration> migrations = Arrays.asList(new PreferenceMigration[] {
            new PreferenceMigration("ensemble", "http://dd.weatheroffice.ec.gc.ca/ensemble/naefs/xml/", "https://dd.weather.gc.ca/ensemble/naefs/xml/"),
            new PreferenceMigration("current", "http://www.weatheroffice.gc.ca/rss/city/", "https://weather.gc.ca/rss/city/")
    });
    
    public RPreferences(Preferences prefs) {
        this.prefs = prefs;
        
        //get a list of stored keys in the preferences
        List<String> keys;
        try {
            String[] temp = prefs.keys();
            if (temp == null)
                keys = new ArrayList<>();
            else
                keys = Arrays.asList(temp);
        }
        catch (BackingStoreException e) {
            keys = new ArrayList<>();
        }
        //perform the migration
        for (PreferenceMigration migration : migrations) {
            //only check the migration if the key exists
            if (keys.contains(migration.key)) {
                String value = prefs.get(migration.key, "");
                if (value.equals(migration.originalValue)) {
                    prefs.put(migration.key, migration.newValue);
                }
            }
        }
    }
    
    public void remove(String key) {
        prefs.remove(key);
    }
    
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }
    
    public String getString(String key, String defaultValue) {
        return prefs.get(key, defaultValue);
    }
    
    public double getDouble(String key, double defaultValue) {
        return prefs.getDouble(key, defaultValue);
    }
    
    public void putInt(String key, int value) {
        prefs.putInt(key, value);
    }
    
    public void putBoolean(String key, boolean value) {
        prefs.putBoolean(key, value);
    }
    
    public void putString(String key, String value) {
        prefs.put(key, value);
    }
    
    public void putDouble(String key, double value) {
        prefs.putDouble(key, value);
    }
    
    /**
     * A migration that should occur when a setting may have a value
     * that is no longer valid.
     */
    private class PreferenceMigration {
        /**
         * The key to look for in the preferences object.
         */
        public String key;
        
        /**
         * The original value that, if stored in the key,
         * should be replaced.
         */
        public String originalValue;
        
        /**
         * The new value to use for the key.
         */
        public String newValue;
        
        public PreferenceMigration(String key, String originalValue, String newValue) {
            this.key = key;
            this.originalValue = originalValue;
            this.newValue = newValue;
        }
    }
}
