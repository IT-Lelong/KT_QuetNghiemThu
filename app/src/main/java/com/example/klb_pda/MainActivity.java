package com.example.klb_pda;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Button btnlogin, btnback, btnlanguage;
    EditText editID, editPassword;
    CheckBox onlinecheck, SaveCheck;
    TextView tv_ver;
    String g_server = "PHP_KT";
    Locale locale;
    String ID, PASSWORD;
    String TABLE_NAME = "acc_table";
    String accID = "accID";
    String pass = "pass";
    private SQLiteDatabase db = null;
    private CheckAppUpdate checkAppUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLanguage();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        verifyStoragePermissions(MainActivity.this);

        checkAppUpdate = new CheckAppUpdate(this, g_server);
        checkAppUpdate.checkVersion();

        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + accID + " TEXT," + pass + " TEXT)";
        db = getApplicationContext().openOrCreateDatabase("Main.db", 0, null);
        try {
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
        }

        btnlogin = (Button) findViewById(R.id.btnlogin);
        btnback = (Button) findViewById(R.id.btnback);
        btnlanguage = (Button) findViewById(R.id.btnlanguage);
        editID = (EditText) findViewById(R.id.editID);
        editPassword = (EditText) findViewById(R.id.editPassword);
        onlinecheck = (CheckBox) findViewById(R.id.onlinecheck);
        SaveCheck = (CheckBox) findViewById(R.id.SaveCheck);
        tv_ver = (TextView) findViewById(R.id.tv_ver);
        btnlanguage.setOnClickListener(btnlanguageListener);
        btnlogin.setOnClickListener(btnloginListener);
        btnback.setOnClickListener(btnbackListener);

        Cursor c = db.rawQuery("SELECT accID,pass FROM " + TABLE_NAME + "", null);
        c.moveToFirst();
        Integer l_cn = c.getCount();
        if (l_cn > 0) {
            editID.setText(c.getString(0));
            editPassword.setText(c.getString(1));
            SaveCheck.setChecked(true);
        } else {
            editID.setText("");
            editPassword.setText("");
            SaveCheck.setChecked(false);
        }

        try {
            String verCode = String.valueOf(this.getPackageManager().getPackageInfo("com.example.klb_pda", 0).versionCode);
            String verName = this.getPackageManager().getPackageInfo("com.example.klb_pda", 0).versionName;
            tv_ver.setText("SV: "+ g_server +" VerCode: " + verCode + " VerName: " + verName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        editID.requestFocus();
    }

    // Storage Permissions (S)
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static final int REQUEST_WRITE_PERMISSION = 786;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "Permission granted", Toast.LENGTH_LONG).show();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    private boolean canReadWriteExternal() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
    }

    // Storage Permissions (E)

    @Override
    protected void onRestart() {
        super.onRestart();
        if (!SaveCheck.isChecked()) {
            editID.setText("");
            editPassword.setText("");
        }
    }

    private View.OnClickListener btnloginListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ID = editID.getText().toString();
            PASSWORD = editPassword.getText().toString();
            if (onlinecheck.isChecked()) {
                //離線登入
                if (ID.length() > 0) {
                    Intent login = new Intent();
                    login.setClass(MainActivity.this, Menu.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("ID", editID.getText().toString());
                    bundle.putString("SERVER", g_server);
                    login.putExtras(bundle);
                    startActivity(login);
                } else {
                    Toast alert = Toast.makeText(MainActivity.this, "請輸入帳號", Toast.LENGTH_LONG);
                    alert.show();
                }
            } else {
                login("http://172.16.40.20/" + g_server + "/login.php?ID=" + ID + "&PASSWORD=" + PASSWORD);
            }
        }
    };

    private View.OnClickListener btnbackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    };

    private void login(String apiurl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiurl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.connect();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String result = reader.readLine();
                    reader.close();
                    if (result.equals("pass")) {
                        if (SaveCheck.isChecked()) {
                            db.execSQL("DELETE FROM " + TABLE_NAME + "");
                            ContentValues args = new ContentValues();
                            args.put(accID, ID);
                            args.put(pass, PASSWORD);
                            db.insert(TABLE_NAME, null, args);
                        } else {
                            db.execSQL("DELETE FROM " + TABLE_NAME + "");
                        }

                        Intent login = new Intent();
                        login.setClass(MainActivity.this, Menu.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("ID", editID.getText().toString());
                        bundle.putString("SERVER", g_server);
                        login.putExtras(bundle);
                        startActivity(login);
                    } else if (result.equals("error")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast alert = Toast.makeText(MainActivity.this, getString(R.string.main_E02), Toast.LENGTH_LONG);
                                alert.show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast alert = Toast.makeText(MainActivity.this, getString(R.string.main_E03), Toast.LENGTH_LONG);
                                alert.show();
                            }
                        });
                    }
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast alert = Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG);
                            alert.show();
                        }
                    });
                }
            }
        }).start();
    }

    //切換語言按鈕事件
    private Button.OnClickListener btnlanguageListener = new Button.OnClickListener() {
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setSingleChoiceItems(new String[]{"中文", "Tiếng Việt"},
                    getSharedPreferences("Language", Context.MODE_PRIVATE).getInt("Language", 0),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {

                            SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("Language", i);
                            editor.apply();
                            dialogInterface.dismiss();

                            //重新載入APP
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    //設定顯示語言
    private void setLanguage() {
        SharedPreferences preferences = getSharedPreferences("Language", Context.MODE_PRIVATE);
        int language = preferences.getInt("Language", 0);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();
        switch (language) {
            case 0:
                locale = new Locale("zh");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;
            case 1:
                locale = new Locale("vi");
                Locale.setDefault(locale);
                configuration.setLocale(locale);
                break;

        }
        resources.updateConfiguration(configuration, displayMetrics);
    }
}