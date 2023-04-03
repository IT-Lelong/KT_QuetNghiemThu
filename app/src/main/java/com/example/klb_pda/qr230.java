package com.example.klb_pda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.klb_pda.Adapter.Barcode_adapter;
import com.example.klb_pda.Listdata.Barcode_listData;
import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.ScannerConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

//public class qr230 extends AppCompatActivity implements EMDKManager.EMDKListener, StatusListener, DataListener {
public class qr230 extends AppCompatActivity {

    String ID, g_server;
    TextView head1;
    ListView list01;
    Button btnupload, btnclear;
    UIHandler uiHandler;
    JSONObject ujsonobject;
    JSONArray ujsonArray;
    ListView dialoglist01;
    qr230DB db = null;
    Barcode_adapter barcodeAdapter;
    ArrayList<Barcode_listData> barcodeListData;
    private CheckAppUpdate checkAppUpdate = null;
    DecimalFormat decimalFormat;

    //------------------------掃描機 Máy quét (S)----------------------------------//
    // Receive QR code and bar code data action and extra
    public static final String BARCODEPORT_RECEIVEDDATA_ACTION = "com.android.serial.BARCODEPORT_RECEIVEDDATA_ACTION";
    public static final String BARCODEPORT_RECEIVEDDATA_EXTRA_DATA = "DATA";

    // Simulate scanning keys
    static public final String ACTION_KEYEVENT_KEYCODE_SCAN_L_DOWN = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_L_DOWN";
    static public final String ACTION_KEYEVENT_KEYCODE_SCAN_L_UP = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_L_UP";
    static public final String ACTION_KEYEVENT_KEYCODE_SCAN_R_DOWN = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_R_DOWN";
    static public final String ACTION_KEYEVENT_KEYCODE_SCAN_R_UP = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_R_UP";
    public static final String ACTION_KEYEVENT_SCAN_F_UP = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_F_UP";
    public static final String ACTION_KEYEVENT_SCAN_F_DOWN = "com.android.action.keyevent.KEYCODE_KEYCODE_SCAN_F_DOWN";

    // service for 4710
    static final String PACKAGE_NAME = "com.zebra.scanner";
    static final String SERVICE_NAME = "com.zebra.scanner.ScannerJsbService";
    // service for 6703
//    static final String PACKAGE_NAME = "com.emdoor.scan6703";
//    static final String SERVICE_NAME = "com.emdoor.scan6703.scanner.ScannerService";

    // switch scanner enable state
    static final String ACTION_CHANGE_STATE_ENABLE = "com.zebra.action.CHANGE_STATE_ENABLE";
    static final String ACTION_CHANGE_STATE_DISABLE = "com.zebra.action.CHANGE_STATE_DISABLE";

    //private Handler handler = new Handler();
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra(BARCODEPORT_RECEIVEDDATA_EXTRA_DATA);   // get data from intent
            if (data != null && data.length() > 0) {
                if (head1.length() > 0) {
                    updatedetail(data.trim());
                } else {
                    uiHandler.sendEmptyMessage(1);
                    updateData(data.trim());
                }
            }
        }
    };

    //------------------------掃描機 Máy quét (E)----------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr230);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        db = new qr230DB(this);
        db.open();
        Bundle getbundle = getIntent().getExtras();
        ID = getbundle.getString("ID");
        g_server = getbundle.getString("SERVER");
        head1 = (TextView) findViewById(R.id.qr230_head1);
        list01 = (ListView) findViewById(R.id.qr230_list01);
        btnupload = (Button) findViewById(R.id.qr230_btnupload);
        btnclear = (Button) findViewById(R.id.qr230_btnclear);
        btnupload.setOnClickListener(btnuploadlistener);
        btnclear.setOnClickListener(btnclearlistener);
        list01.setOnItemClickListener(lsit01listener);

        uiHandler = new UIHandler();
        uiHandler.sendEmptyMessage(3);
        uiHandler.sendEmptyMessage(2);

        Locale locale = new Locale("en", "EN");
        String pattern = "###,###,###.##";
        decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        decimalFormat.applyPattern(pattern);

        //------------------------掃描機 Máy quét (S)----------------------------------//
        // Register QR code and bar code data receiver.
        IntentFilter filter = new IntentFilter(BARCODEPORT_RECEIVEDDATA_ACTION);
        registerReceiver(receiver, filter);
        //------------------------掃描機 Máy quét (E)----------------------------------//

    }

    //取得調撥單內容
    private void updateData(final String dataStr) {
        Thread scan = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = getdata(dataStr);
                    if (result.equals("FALSE")) {
                        qr230.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                                builder.setTitle("ERROR");
                                builder.setMessage(getString(R.string.qr230_msg01));
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.show();
                            }
                        });
                    } else {
                        ujsonobject = new JSONObject(result);
                        if (ujsonobject.getJSONArray("detail1").length() == 0) {
                            qr230.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                                    builder.setTitle("ERROR");
                                    builder.setMessage(getString(R.string.qr230_msg02));
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {

                                        }
                                    });
                                    builder.show();
                                }
                            });
                        } else {
                            head1.setText(dataStr);
                            db.insHead(dataStr);
                            uiHandler.sendEmptyMessage(0);
                        }
                    }
                } catch (Exception e) {
                    qr230.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                            builder.setTitle("ERROR");
                            builder.setMessage(getString(R.string.qr230_msg01));
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                            builder.show();
                        }
                    });
                }

            }
        });
        scan.start();
    }

    //掃描後更新資料
    private void updatedetail(String datastr) {
        try {
            if (datastr.startsWith("new")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //重印、拆單標籤格式 new_料號_批號_數量
                        String qr01 = "", qr02 = "";
                        Double qr03 = 0.0;

                        int index1 = datastr.indexOf("_");
                        int index2 = datastr.indexOf("_", index1 + 1);
                        int index3 = datastr.indexOf("_", index2 + 1);
                        int index4 = datastr.indexOf("_", index3 + 1);  //預備給 測試料號會多出 T_

                        //料號
                        qr01 = datastr.substring(4, index2);
                        if (qr01.equals("T")) {
                            qr01 = datastr.substring(4, index3).trim();
                            qr02 = datastr.substring(index3 + 1, index4);
                            qr03 = Double.valueOf(datastr.substring(index4 + 1).trim());
                        } else {
                            //批號
                            qr02 = datastr.substring(index2 + 1, index3);
                            //數量
                            qr03 = Double.valueOf(datastr.substring(index3 + 1));
                        }

                        String res = getDonVi("http://172.16.40.20/" + g_server + "/PDA_QR230/sosanh_donvi.php?ima01=" + qr01 + "&qty=" + qr03);
                        Double g_Soluong = 0.0;
                        if (res.length() > 0) {
                            try {
                                JSONArray jsonarray = new JSONArray(res);
                                JSONObject jsonObject = jsonarray.getJSONObject(0);
                                String g_res1 = jsonObject.getString("IMA25"); //庫存單位 Đơn vị tồn kho
                                String g_res2 = jsonObject.getString("IMA63"); //發料單位 Đơn vị phát liệu
                                String g_res3 = jsonObject.getString("SMD04"); //來源單位數量 Số lượng đơn vị gốc
                                String g_res4 = jsonObject.getString("SMD02"); //來源單位 Đơn vị gốc
                                String g_res5 = jsonObject.getString("SMD06"); //目的單位數量 Số lượng đơn vị đích
                                String g_res6 = jsonObject.getString("SMD03"); //目的單位 Đơn vị đích
                                String g_res7 = jsonObject.getString("IMA63_FAC"); //轉換率 Tỷ lệ chuyển đổi phát liệu
                                g_Soluong = jsonObject.getDouble("SL"); //已換算數量 SL đã quy đổi
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        scan(datastr.trim(), qr01, qr02, g_Soluong);
                    }
                });
                api.start();

            } else if (datastr.substring(0, 5).equals("BC525") || datastr.substring(0, 5).equals("BC527") || datastr.substring(0, 5).equals("BB525") || datastr.substring(0, 5).equals("BB527")) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //極版標籤 BC527-2101000198_1_07030333C_29568
                        String qr01 = "", qr02 = "";
                        Double qr03 = 0.0;

                        int index1 = datastr.indexOf("_");
                        int index2 = datastr.indexOf("_", index1 + 1);
                        int index3 = datastr.indexOf("_", index2 + 1);
                        int index4 = datastr.indexOf("_", index3 + 1); //預備給 測試料號會多出 T_
                        //取得料號
                        qr01 = datastr.substring(index2 + 1, index3);
                        //取得批號
                        qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr + "&kind=" + 2);
                        if (qr01.equals("T")) {
                            qr01 = datastr.substring(index2 + 1, index4);
                            qr03 = Double.valueOf(datastr.substring(index4 + 1));
                        } else {
                            //取得數量
                            qr03 = Double.valueOf(datastr.substring(index3 + 1));
                        }
                        scan(datastr.trim(), qr01, qr02, qr03);
                    }
                });
                api.start();
            } else if (datastr.substring(0, 5).equals("CC511") || datastr.substring(0, 5).equals("CC510") || datastr.substring(0, 5).equals("CC512") ||
                    datastr.substring(0, 5).equals("CC513") || datastr.substring(0, 5).equals("CC514") || datastr.substring(0, 5).equals("CC515") ||
                    datastr.substring(0, 5).equals("CC532") || datastr.substring(0, 5).equals("CC514") ||
                    datastr.substring(0, 5).equals("CC518") || datastr.substring(0, 8).equals("OLDSTAMP") || datastr.substring(0, 8).equals("THUNGHIEM")
            ) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //廣泰標籤 CC512-2010000697-108(CC5A2_KD91-1_NULL)_(03020489V_nap_WPX7A-BA)_20201026007002
                        //特殊標籤 CC514-2207000073-1492_CC514-2206000253-236(CC5A4_KD91-2_NULL)_(03010374D_vỏ bình ắc quy_WP8.5-12)_20220711059001
                        Double qr03 = 0.0;

                        int index1 = datastr.indexOf("-", 6);
                        int index2 = datastr.indexOf("("); //第一個(位置
                        int index3 = datastr.indexOf("(", index2 + 1); //第二個(位置
                        int index4 = datastr.indexOf("_", index3); //料號後的_位置
                        //取得料號
                        String qr01 = datastr.substring(index3 + 1, index4).trim();
                        //取得批號
                        String qr02 = getdatecode("http://172.16.40.20/" + g_server + "/QR220/get_datecode.php?code=" + datastr.trim() + "&kind=" + 1);
                        //取得數量
                        if (index2 > 32) {
                            String g_qr03_datastr = datastr.substring(0, index2);
                            Integer g_len_g_qr03_datastr = g_qr03_datastr.length();
                            Integer l_sl1 = 0, l_sl2 = 0, l_sl3 = 0, k = 0;

                            for (int i = 0; i <= g_len_g_qr03_datastr; i++) {
                                String l_code = "";
                                String j = "";
                                if (i < g_len_g_qr03_datastr) {
                                    j = g_qr03_datastr.substring(i, i + 1);
                                }

                                if (j.equals("_") || i == g_len_g_qr03_datastr) {
                                    if (i == g_len_g_qr03_datastr) {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        l_sl3 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                    } else {
                                        l_code = g_qr03_datastr.substring(k, i);
                                        if (l_sl1 == 0) {
                                            l_sl1 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        } else {
                                            l_sl2 = Integer.valueOf(l_code.substring(17, l_code.length()));
                                        }
                                    }
                                    k = i + 1;
                                }
                            }

                            qr03 = Double.valueOf(l_sl1 + l_sl2 + l_sl3);
                        } else {
                            qr03 = Double.valueOf(datastr.substring(index1 + 1, index2).trim());
                        }

                        String res = getDonVi("http://172.16.40.20/" + g_server + "/PDA_QR230/sosanh_donvi.php?ima01=" + qr01 + "&qty=" + qr03);
                        Double g_Soluong = 0.0;
                        if (res.length() > 0) {
                            try {
                                JSONArray jsonarray = new JSONArray(res);
                                JSONObject jsonObject = jsonarray.getJSONObject(0);
                                String g_res1 = jsonObject.getString("IMA25"); //庫存單位 Đơn vị tồn kho
                                String g_res2 = jsonObject.getString("IMA63"); //發料單位 Đơn vị phát liệu
                                String g_res3 = jsonObject.getString("SMD04"); //來源單位數量 Số lượng đơn vị gốc
                                String g_res4 = jsonObject.getString("SMD02"); //來源單位 Đơn vị gốc
                                String g_res5 = jsonObject.getString("SMD06"); //目的單位數量 Số lượng đơn vị đích
                                String g_res6 = jsonObject.getString("SMD03"); //目的單位 Đơn vị đích
                                g_Soluong = jsonObject.getDouble("SL"); //已換算數量 SL đã quy đổi
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        scan(datastr.trim(), qr01, qr02, g_Soluong);
                    }
                });
                api.start();
            } else if (datastr.startsWith("-", 5)) {
                Thread api = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        //供應商條碼 BB421-2101000169_2_07100071A_20211007_792
                        int index1 = datastr.indexOf("_");
                        int index2 = datastr.indexOf("_", index1 + 1);
                        int index3 = datastr.indexOf("_", index2 + 1);
                        int index4 = datastr.indexOf("_", index3 + 1);
                        //取得料號
                        String qr01 = datastr.substring(index2 + 1, index3).trim();
                        //取得批號
                        String qr02 = datastr.substring(index3 + 1, index4);
                        //取得數量
                        Double qr03 = Double.valueOf(datastr.substring(index4 + 1).trim());

                        String res = getDonVi("http://172.16.40.20/" + g_server + "/PDA_QR230/sosanh_donvi.php?ima01=" + qr01 + "&qty=" + qr03);
                        Double g_Soluong = 0.0;
                        if (res.length() > 0) {
                            try {
                                JSONArray jsonarray = new JSONArray(res);
                                JSONObject jsonObject = jsonarray.getJSONObject(0);
                                String g_res1 = jsonObject.getString("IMA25"); //庫存單位 Đơn vị tồn kho
                                String g_res2 = jsonObject.getString("IMA63"); //發料單位 Đơn vị phát liệu
                                String g_res3 = jsonObject.getString("SMD04"); //來源單位數量 Số lượng đơn vị gốc
                                String g_res4 = jsonObject.getString("SMD02"); //來源單位 Đơn vị gốc
                                String g_res5 = jsonObject.getString("SMD06"); //目的單位數量 Số lượng đơn vị đích
                                String g_res6 = jsonObject.getString("SMD03"); //目的單位 Đơn vị đích
                                String g_res7 = jsonObject.getString("IMA63_FAC"); //轉換率 Tỷ lệ chuyển đổi phát liệu
                                g_Soluong = jsonObject.getDouble("SL"); //已換算數量 SL đã quy đổi
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        scan(datastr.trim(), qr01, qr02, g_Soluong);
                    }
                });
                api.start();
            } else {
                qr230.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg02));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    private void scan(String xqr230b_02, String xqr230b_03, String xqr230b_04, Double xqr230b_05) {
        try {
            String result = db.scan(xqr230b_02, xqr230b_03, xqr230b_04, xqr230b_05);
            if (result.equals("FALSE")) {
                qr230.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg03));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("NORECORD")) {
                qr230.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg04));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("OVERQTY")) {
                qr230.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg05));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else if (result.equals("NORECORD2")) {
                qr230.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                        builder.setTitle("ERROR");
                        builder.setMessage(getString(R.string.qr210_msg14));
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
            } else {
                uiHandler.sendEmptyMessage(2);
            }
        } catch (Exception e) {

        }
    }

    private View.OnClickListener btnuploadlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (head1.getText().length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.qr210_msg08));
                builder.setMessage(getString(R.string.qr210_msg09));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String result = upload();
                                if (result.equals("TRUE")) {
                                    qr230.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                                            builder.setTitle(getString(R.string.qr210_msg08));
                                            builder.setMessage(getString(R.string.qr210_msg10));
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    qr230_mail("http://172.16.40.20/" + g_server + "/PDA_QR230/mail.php?ID=" + ID);
                                                }
                                            });
                                            builder.show();
                                            uiHandler.sendEmptyMessage(1);
                                        }
                                    });
                                } else {
                                    qr230.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                                            builder.setTitle(getString(R.string.qr210_msg08));
                                            builder.setMessage(getString(R.string.qr210_msg11));
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {

                                                }
                                            });
                                            builder.show();
                                        }
                                    });
                                }

                            }
                        });
                        thread.start();

                    }
                });
                builder.show();
            } else {
                Toast.makeText(qr230.this, R.string.qr210_msg16, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener btnclearlistener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            checkAppUpdate = new CheckAppUpdate(getApplicationContext(), g_server);
            checkAppUpdate.checkVersion();
            if (head1.getText().length() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(qr230.this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.qr210_msg12));
                builder.setMessage(getString(R.string.qr210_msg13));
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        db.close();
                        db.open();
                        uiHandler.sendEmptyMessage(1);
                    }
                });
                builder.show();
            } else {
                Toast.makeText(qr230.this, R.string.qr210_msg16, Toast.LENGTH_SHORT).show();
            }
        }
    };


    private AdapterView.OnItemClickListener lsit01listener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Button dialogbtn01, btn_select_all, btn_cancel_all;

            TextView item01 = (TextView) view.findViewById(R.id.qr210_view01_item01); //STT
            TextView item02 = (TextView) view.findViewById(R.id.qr210_view01_item02); //MVL
            String qr230_01 = item01.getText().toString();
            String qr230_02 = item02.getText().toString();
            final Dialog dialog = new Dialog(qr230.this);
            dialog.setContentView(R.layout.activity_qr210_dialog01);

            dialogbtn01 = (Button) dialog.findViewById(R.id.qr210_dialog01_btn01);
            btn_select_all = dialog.findViewById(R.id.btn_select_all);
            btn_cancel_all = dialog.findViewById(R.id.btn_cancel_all);
            dialoglist01 = (ListView) dialog.findViewById(R.id.qr210_dialog01_list01);


            barcodeListData = new ArrayList<>();
            barcodeAdapter = new Barcode_adapter(getApplicationContext(),
                    R.layout.activity_qr210_dialog01_view,
                    barcodeListData);
            dialoglist01.setAdapter(barcodeAdapter);
            Cursor cursor = db.getdialogdetail(qr230_01);
            UpdateAdapterdialog(cursor);


            dialogbtn01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uiHandler.sendEmptyMessage(2);
                    dialog.dismiss();

                }
            });

            btn_select_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //STT , MVL , Số Lô
                    db.updCheckALL(qr230_01, qr230_02, true);
                    Cursor cursor = db.getdialogdetail(qr230_01);
                    UpdateAdapterdialog(cursor);
                }
            });

            btn_cancel_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //STT , MVL , Số Lô
                    db.updCheckALL(qr230_01, qr230_02, false);
                    Cursor cursor = db.getdialogdetail(qr230_01);
                    UpdateAdapterdialog(cursor);
                }
            });


            //點選刪除
            dialoglist01.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    CheckedTextView simpleCheckedTextView = view.findViewById(R.id.simpleCheckedTextView);
                    TextView dialogitem01 = (TextView) view.findViewById(R.id.TV_dialog_MVL_ID);    //料號項目
                    TextView dialogitem02 = (TextView) view.findViewById(R.id.TV_dialog_matemID);   //標籤項目
                    TextView dialogitem03 = (TextView) view.findViewById(R.id.qr210_dialog01_item03);  //數量
                    String g_dialogitem01 = dialogitem01.getText().toString();
                    String g_dialogitem02 = dialogitem02.getText().toString();
                    String g_dialogitem03 = dialogitem03.getText().toString();

                    if (simpleCheckedTextView.isChecked()) {
                        db.updCheckBox(g_dialogitem01, g_dialogitem02, g_dialogitem03, false);
                        Cursor cursor = db.getdialogdetail(qr230_01);
                        UpdateAdapterdialog(cursor);
                    } else {
                        db.updCheckBox(g_dialogitem01, g_dialogitem02, g_dialogitem03, true);
                        Cursor cursor = db.getdialogdetail(qr230_01);
                        UpdateAdapterdialog(cursor);
                    }
                }
            });
            dialog.show();
        }
    };

    public String getdata(String dataStr) {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR230/getdata.php?sfp01=" + dataStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {

            return "FALSE";
        }
    }

    //取得批號
    private String getdatecode(String apiUrl) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String jsonstring = reader.readLine();
            reader.close();
            if (!jsonstring.equals("FALSE")) {
                return jsonstring;
            } else {
                return "NULL";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    private String getDonVi(String s) {
        try {
            HttpURLConnection conn = null;
            URL url = new URL(s);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String jsonstring = reader.readLine();
            reader.close();
            if (!jsonstring.equals("FALSE")) {
                if (jsonstring.contains("error:100")) {
                    return "E100";
                } else {
                    return jsonstring;
                }
            } else {
                return "FALSE";
            }
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String upload() {
        try {
            URL url = new URL("http://172.16.40.20/" + g_server + "/PDA_QR230/upload.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(999999);
            conn.setReadTimeout(999999);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            Cursor c = db.getall();
            Cursor c1 = db.getallb();
            JSONArray jarray = cur2Json(c,"qr230_table");
            JSONArray jarray1 = cur2Json(c1,"qr230b_table");
            JSONObject jobejct = new JSONObject();
            jobejct.put("QR_IMN07", ID);
            jobejct.put("QR_IMN01", head1.getText());
            jobejct.put("detail", jarray);
            jobejct.put("detail2", jarray1);
            writer.write(jobejct.toString().getBytes("UTF-8"));
            writer.flush();
            writer.close();
            os.close();
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String result = reader.readLine();
            reader.close();
            return result;
        } catch (Exception e) {

            return "FALSE";
        }
    }

    private class UIHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //資料新增
                case 0:
                    try {
                        db.append(ujsonobject);
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;
                //資料清除
                case 1:
                    try {
                        db.close();
                        db.open();
                        head1.setText("");
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;
                //掃描標籤
                case 2:
                    try {
                        Cursor getdetail = db.getdetail();
                        UpdateAdapter(getdetail);
                    } catch (Exception e) {

                    }
                    break;

                //取發料單號
                case 3:
                    try {
                        Cursor getHead = db.getallc();
                        UpdateHead(getHead);
                    } catch (Exception e) {

                    }
                    break;

            }
        }
    }

    private void UpdateHead(Cursor getHead) {
        try {
            if (getHead != null && getHead.getCount() >= 0) {
                getHead.moveToFirst();
                String g_head = getHead.getString(0);
                head1.setText(g_head);
            }
        } catch (Exception e) {
        }
    }

    public void UpdateAdapter(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.activity_qr210_view01, cursor,
                        new String[]{"qr230_01", "qr230_02", "qr230_03", "qr230_05", "qr230_06", "qr230_09", "qr230_10", "qr230_11", "qr230_11"},
                        new int[]{R.id.qr210_view01_item01, R.id.qr210_view01_item02, R.id.qr210_view01_item03,
                                R.id.qr210_view01_item04, R.id.qr210_view01_item05, R.id.qr210_view01_item06,
                                R.id.qr210_view01_item07, R.id.qr210_view01_item08, R.id.qr210_view01_item09}, 0);

                adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                    @Override
                    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                        TextView textView = (TextView) view;
                        if (columnIndex == 5) {
                            textView.setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                            return true;
                        }
                        if (columnIndex == 6) {
                            textView.setText(String.valueOf(decimalFormat.format(cursor.getDouble(columnIndex))));
                            return true;
                        }
                        return false;
                    }
                });
                list01.setAdapter(adapter);
            }
        } catch (Exception e) {
            String x = e.toString();
        } finally {

        }
    }

    public void UpdateAdapterdialog(Cursor cursor) {
        try {
            if (cursor != null && cursor.getCount() >= 0) {
                /*SimpleCursorAdapter adapter=new SimpleCursorAdapter(qr230.this,R.layout.activity_qr210_dialog01_view,cursor,
                        new String[]{"rownum","qr230b_04","qr230b_05"},
                        new int[]{R.id.qr210_dialog01_item01,R.id.qr210_dialog01_item02,R.id.qr210_dialog01_item03},0);
                dialoglist01.setAdapter(adapter);*/

                barcodeListData.clear();
                cursor.moveToFirst();
                int k = cursor.getCount();
                for (int i2 = 1; i2 <= k; i2++) {
                    barcodeListData.add(
                            new Barcode_listData(Integer.parseInt(cursor.getString(1)),
                                    cursor.getString(2),
                                    Double.parseDouble(cursor.getString(3)),
                                    Boolean.parseBoolean(cursor.getString(4)),
                                    cursor.getString(6),
                                    cursor.getString(5),
                                    cursor.getString(7)));
                    cursor.moveToNext();
                }
                barcodeAdapter.notifyDataSetChanged();

            }
        } catch (Exception e) {

        } finally {

        }
    }

    //Cursor 轉 Json
    public JSONArray cur2Json(Cursor cursor,String name_table) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumn; i++) {
                if (cursor.getColumnName(i) != null) {
                    try {
                        //GET DOUBLE cột số thâp phân
                        if (name_table.equals("qr230_table"))
                        {
                            if(i== 3 || i==4|| i== 5)
                            {
                                rowObject.put(cursor.getColumnName(i),cursor.getDouble(i));
                            }
                            else {
                                rowObject.put(cursor.getColumnName(i),cursor.getString(i));
                            }
                        }
                        else
                        {
                            if(i== 4)
                            {
                                rowObject.put(cursor.getColumnName(i),cursor.getDouble(i));
                            }
                            else {
                                rowObject.put(cursor.getColumnName(i),cursor.getString(i));
                            }
                        }
                        //rowObject.put(cursor.getColumnName(i),
                                //cursor.getString(i));
                    } catch (Exception e) {
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;

    }

    //發送mail
    private void qr230_mail(String apiurl) {
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
                } catch (Exception e) {
                }
            }
        }).start();
    }


}