package io.rapidpro.surveyor.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Patterns;
import android.widget.Toast;

import io.rapidpro.surveyor.R;
import io.rapidpro.surveyor.SurveyorApplication;
import io.rapidpro.surveyor.SurveyorPreferences;
import io.rapidpro.surveyor.activity.BaseActivity;

/**
 * Fragment to show our settings
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference pref = findPreference(SurveyorPreferences.HOST);
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!Patterns.WEB_URL.matcher((String) newValue).matches()) {
                    Toast.makeText(getActivity(), getString(R.string.error_invalid_host), Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getSurveyor().getPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getSurveyor().getPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SurveyorPreferences.HOST)) {
            getSurveyor().onTembaHostChange();
        }
    }

    private SurveyorApplication getSurveyor() {
       return ((BaseActivity) getActivity()).getSurveyor();
    }
}
