package at.bitfire.gfxtablet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class CanvasActivity extends FragmentActivity implements View.OnSystemUiVisibilityChangeListener {
    private static final int RESULT_LOAD_IMAGE = 1;

    final Uri homepageUri = Uri.parse(("https://gfxtablet.bitfire.at"));

    NetworkClient netClient;

    SharedPreferences preferences;
    boolean fullScreen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (preferences.getBoolean("first_run", true)) {
            preferences.edit().putBoolean("first_run", false).apply();

            try {
                String cpuAbiProp = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("getprop ro.product.cpu.abi").getInputStream())).readLine();
                String CPU_ABI;
                if (cpuAbiProp.contains("x86_64")) {
                    CPU_ABI = "x86_64";
                } else if (cpuAbiProp.contains("x86")) {
                    CPU_ABI = "x86";
                } else if (cpuAbiProp.contains("arm64-v8a")) {
                    CPU_ABI = "arm64-v8a";
                } else if (cpuAbiProp.contains("armeabi-v7a")) {
                    CPU_ABI = "armeabi-v7a";
                } else {
                    CPU_ABI = "armeabi-v7a";
                }

                InputStream inputStream = this.getAssets().open("raw/" + CPU_ABI + "/daemon");
                //getFilesDir() 获得当前APP的安装路径 /data/data/包名/files 目录
                File file = new File(this.getApplicationContext().getFilesDir(), "daemon");
                if(!file.exists() || file.length() == 0) {
                    FileOutputStream fos = new FileOutputStream(file);//如果文件不存在，FileOutputStream会自动创建文件
                    int len;
                    byte[] buffer = new byte[1024];
                    while ((len = inputStream.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();//刷新缓存区
                    inputStream.close();
                    fos.close();
                }

                Runtime.getRuntime().exec("chmod +x " + this.getApplicationContext().getFilesDir() + "/daemon");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_canvas);

        // create network client in a separate thread
        netClient = new NetworkClient(PreferenceManager.getDefaultSharedPreferences(this));
        new Thread(netClient).start();
        new ConfigureNetworkingTask().execute();

        // notify CanvasView of the network client
        CanvasView canvas = findViewById(R.id.canvas);
        canvas.setNetworkClient(netClient);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (preferences.getBoolean(SettingsActivity.KEY_KEEP_DISPLAY_ACTIVE, true))
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        showTemplateImage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        netClient.getQueue().add(new NetEvent(NetEvent.Type.TYPE_DISCONNECT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_canvas, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (fullScreen)
            switchFullScreen(null);
        else
            super.onBackPressed();
    }

    public void showAbout(MenuItem item) {
        startActivity(new Intent(Intent.ACTION_VIEW, homepageUri));
    }

    public void showDonate(MenuItem item) {
        startActivity(new Intent(Intent.ACTION_VIEW, homepageUri.buildUpon().appendPath("donate").build()));
    }

    public void showSettings(MenuItem item) {
        startActivityForResult(new Intent(this, SettingsActivity.class), 0);
    }


    // full-screen methods

    public void switchFullScreen(MenuItem item) {
        final View decorView = getWindow().getDecorView();
        int uiFlags = decorView.getSystemUiVisibility();

        uiFlags ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        uiFlags ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        uiFlags ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setOnSystemUiVisibilityChangeListener(this);
        decorView.setSystemUiVisibility(uiFlags);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.i("GfxTablet", "System UI changed " + visibility);

        fullScreen = (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0;

        // show/hide action bar according to full-screen mode
        if (fullScreen) {
            Objects.requireNonNull(getActionBar()).hide();
            Toast.makeText(CanvasActivity.this, "Press Back button to leave full-screen mode.", Toast.LENGTH_LONG).show();
        } else
            Objects.requireNonNull(getActionBar()).show();
    }


    // template image logic

    private String getTemplateImagePath() {
        return preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null);
    }

    public void setTemplateImage(MenuItem item) {
        if (getTemplateImagePath() == null)
            selectTemplateImage(item);
        else {
            // template image already set, show popup
            PopupMenu popup = new PopupMenu(this, findViewById(R.id.menu_set_template_image));
            popup.getMenuInflater().inflate(R.menu.set_template_image, popup.getMenu());
            popup.show();
        }
    }

    public void selectTemplateImage(MenuItem item) {
        Intent i;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
        } else {
            i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    public void clearTemplateImage(MenuItem item) {
        preferences.edit().remove(SettingsActivity.KEY_TEMPLATE_IMAGE).apply();
        showTemplateImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                int flagsToPersist = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
                getContentResolver().takePersistableUriPermission(selectedImage, flagsToPersist);
                preferences.edit().putString(SettingsActivity.KEY_TEMPLATE_IMAGE, selectedImage.toString()).apply();
            } else {
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                try (Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null)) {
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);

                    preferences.edit().putString(SettingsActivity.KEY_TEMPLATE_IMAGE, picturePath).apply();
                    showTemplateImage();
                }
            }
        }
    }

    public void showTemplateImage() {
        ImageView template = findViewById(R.id.canvas_template);
        template.setImageDrawable(null);

        if (template.getVisibility() == View.VISIBLE) {
            String picturePath = preferences.getString(SettingsActivity.KEY_TEMPLATE_IMAGE, null);
            if (picturePath != null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Uri pictureUri = Uri.parse(picturePath);
                    template.setImageURI(pictureUri);
                } else {
                    try {
                        // TODO load bitmap efficiently, for intended view size and display resolution
                        // https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
                        final Drawable drawable = new BitmapDrawable(getResources(), picturePath);
                        template.setImageDrawable(drawable);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class ConfigureNetworkingTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return netClient.reconfigureNetworking();
        }

        protected void onPostExecute(Boolean success) {
            if (success)
                Toast.makeText(CanvasActivity.this, "Touch events will be sent to " + netClient.destAddress.getHostAddress() + ":" + NetworkClient.GFXTABLET_PORT, Toast.LENGTH_LONG).show();

            findViewById(R.id.canvas_template).setVisibility(success ? View.VISIBLE : View.GONE);
            findViewById(R.id.canvas).setVisibility(success ? View.VISIBLE : View.GONE);
            findViewById(R.id.canvas_message).setVisibility(success ? View.GONE : View.VISIBLE);
        }
    }

}
