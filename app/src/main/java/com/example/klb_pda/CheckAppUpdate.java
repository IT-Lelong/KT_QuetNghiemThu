package com.example.klb_pda;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpEntity;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpResponse;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpGet;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class CheckAppUpdate {
    String g_server = "";
    private Context mCtxAPI = null;

    /**
     * Called when the activity is first created.
     */
    String newVerName = "";//新版本名稱
    int newVerCode = -1;//新版本號
    ProgressDialog pd = null;
    String UPDATE_SERVERAPK = "PDA_NGHIEMTHU_KT_T50C.apk";


    public CheckAppUpdate(Context ctx, String g_server) {
        this.g_server = g_server;
        this.mCtxAPI = ctx;
    }

    public void checkVersion() {
        new Asyn_getServerVer().execute("http://172.16.40.20/" + g_server + "/check_ver.php?app=PDA_NGHIEMTHU_KT_T50C");
    }

    private String docNoiDung_Tu_URL(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            // create a url object
            URL url = new URL(theUrl);
            // create a urlconnection object
            URLConnection urlConnection = url.openConnection();
            // wrap the urlconnection in a bufferedreader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            // read from the urlconnection via the bufferedreader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
                //content.append(line);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    private class Asyn_getServerVer extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            return docNoiDung_Tu_URL(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.length() > 0) {
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    newVerCode = Integer.valueOf(jsonObject.getString("QRG003"));
                    newVerName = jsonObject.getString("QRG005");
                    if (newVerCode > 0) {
                        int verCode = 0;
                        String verName ="";
                        try {
                            verCode = mCtxAPI.getPackageManager().getPackageInfo("com.example.klb_pda", 0).versionCode;
                            verName = mCtxAPI.getPackageManager().getPackageInfo("com.example.klb_pda", 0).versionName;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        if ((newVerCode > verCode) || (newVerCode == verCode && !newVerName.equals(verName))) {
                            doNewVersionUpdate();//更新版本
                        } else {
                            // notNewVersionUpdate();//提示已是最新版本
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 獲得版本號
     */
    public int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getPackageManager().getPackageInfo("com.example.klb_pda", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e("版本號獲取異常", e.getMessage());
        }
        return verCode;
    }

    /**
     * 獲得版本名稱
     */
    public String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo("com.example.klb_pda", 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("版本名稱獲取異常", e.getMessage());
        }
        return verName;
    }

    /**
     * 不更新版本
     */
    public void notNewVersionUpdate() {
        int verCode = this.getVerCode(mCtxAPI);
        String verName = this.getVerName(mCtxAPI);
        StringBuffer sb = new StringBuffer();
        sb.append(R.string.ver_curr_version);
        sb.append(verName);
        sb.append(" Code:");
        sb.append(verCode);
        sb.append("\n已是最新版本，無需更新");
        Dialog dialog = new android.app.AlertDialog.Builder(mCtxAPI)
                .setTitle("軟件更新")
                .setMessage(sb.toString())
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                }).create();
        dialog.show();
    }

    /**
     * 更新版本
     */
    public void doNewVersionUpdate() {
        int verCode = this.getVerCode(mCtxAPI);
        String verName = this.getVerName(mCtxAPI);
        StringBuffer sb = new StringBuffer();
        sb.append(mCtxAPI.getString(R.string.ver_curr_version));
        sb.append(verName);
        sb.append(" Code:");
        sb.append(verCode);
        sb.append("\n");
        sb.append(mCtxAPI.getString(R.string.ver_new_version));
        sb.append(newVerName);
        sb.append(" Code:");
        sb.append(newVerCode);
        sb.append("\n");
        sb.append(mCtxAPI.getString(R.string.ver_install));
        Dialog dialog = new android.app.AlertDialog.Builder(mCtxAPI)
                .setTitle(mCtxAPI.getString(R.string.ver_soft_update))
                .setMessage(sb.toString())
                .setPositiveButton(mCtxAPI.getString(R.string.ver_update), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        pd = new ProgressDialog(mCtxAPI);
                        pd.setTitle(mCtxAPI.getString(R.string.ver_downloading));
                        pd.setMessage(mCtxAPI.getString(R.string.ver_please_wait));
                        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        downFile("http://172.16.40.20/" + g_server + "/AndroidUpdateService/PDA_NGHIEMTHU_KT_T50C.apk");
                    }
                }).create();
                /*.setNegativeButton("暫不更新", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        //finish();
                        Toast.makeText(mCtxAPI, "Không cập nhật", Toast.LENGTH_SHORT).show();
                    }
                }).create();*/
        //顯示更新框
        dialog.show();
    }

    /*
     * 下載apk
     */
    public void downFile(final String url) {
        pd.show();
        new Thread() {
            public void run() {
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(url);
                HttpResponse response;
                try {
                    response = client.execute(get);
                    HttpEntity entity = response.getEntity();
                    long length = entity.getContentLength();
                    InputStream is = entity.getContent();
                    FileOutputStream fileOutputStream = null;
                    if (is != null) {
                        File file = new File(Environment.getExternalStorageDirectory() + "/Download", UPDATE_SERVERAPK);
                        fileOutputStream = new FileOutputStream(file);
                        byte[] b = new byte[1024];
                        int charb = -1;
                        int count = 0;
                        while ((charb = is.read(b)) != -1) {
                            fileOutputStream.write(b, 0, charb);
                            count += charb;
                        }
                    }
                    fileOutputStream.flush();
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    down();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pd.cancel();
            update();
        }
    };

    /**
     * 下載完成，通過handler將下載對話框取消
     */
    public void down() {
        new Thread() {
            public void run() {
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
            }
        }.start();
    }

    /**
     * 安裝應用
     */
    public void update() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        File apkFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + UPDATE_SERVERAPK);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }*/
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(mCtxAPI, BuildConfig.APPLICATION_ID + ".provider", apkFile);
        intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        mCtxAPI.startActivity(intent);
    }

}
