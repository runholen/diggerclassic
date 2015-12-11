package com.plasma.digger;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

public class SettingsActivity extends PreferenceActivity {

   @Override
   public void onCreate(Bundle savedInstanceState) {        
	   super.onCreate(savedInstanceState);        
	   addPreferencesFromResource(R.xml.preferences);
	   PreferenceScreen root = getPreferenceScreen();
	   ListPreference lp = (ListPreference)getPreference(root,"ed_autocompany");
   }

	private Preference getPreference(PreferenceGroup root, String key) {
		for (int t = 0; t < root.getPreferenceCount(); t++){
			Preference p = root.getPreference(t);
			if (p.getKey().equals(key)) return p;
			if (p instanceof PreferenceGroup){
				p = getPreference((PreferenceGroup)p, key);
				if (p != null) return p;
			}
		}
		return null;
	} 
}