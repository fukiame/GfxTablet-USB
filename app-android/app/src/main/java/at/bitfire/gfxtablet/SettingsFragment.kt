package at.bitfire.gfxtablet

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsFragment : PreferenceFragmentCompat()
{
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<EditTextPreference>(SettingsActivity.KEY_PREF_HOST)?.setOnBindEditTextListener { it.setSingleLine() }
    }
}