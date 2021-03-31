package at.bitfire.gfxtablet

import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class SettingsActivity : FragmentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_settings)
    }

    companion object
    {
        const val KEY_PREF_STYLUS_ONLY    = "stylus_only_preference"
        const val KEY_CANVAS_GRID         = "grid_canvas_preference"
        const val KEY_KEEP_DISPLAY_ACTIVE = "keep_display_active_preference"
        const val KEY_TEMPLATE_IMAGE      = "key_template_image"
    }
}
