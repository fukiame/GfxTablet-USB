package at.bitfire.gfxtablet;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String
        KEY_CANVAS_GRID = "grid_canvas_preference",
        KEY_KEEP_DISPLAY_ACTIVE = "keep_display_active_preference",
        KEY_TEMPLATE_IMAGE = "key_template_image";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_settings);
    }

}
