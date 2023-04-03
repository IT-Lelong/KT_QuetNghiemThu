package com.example.klb_pda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

public class qr230DB {
    public SQLiteDatabase db = null;
    String DATABASE_NAME = "qr230DB.db";
    String TABLE_NAME = "qr230_table";
    String qr230_01 = "qr230_01"; //項次
    String qr230_02 = "qr230_02"; //料號
    String qr230_03 = "qr230_03"; //規格
    String qr230_04 = "qr230_04"; //應掃數量
    String qr230_05 = "qr230_05"; //已掃數量
    String qr230_06 = "qr230_06"; //已驗收量
    String qr230_07 = "qr230_07"; //發出倉庫
    String qr230_08 = "qr230_08"; //發出儲位
    String qr230_09 = "qr230_09"; //發出批號
    String qr230_10 = "qr230_10"; //物料名增
    String qr230_11 = "qr230_11"; //發料單位

    String TABLE_NAME2 = "qr230b_table";
    String qr230b_01 = "qr230b_01"; //項次
    String qr230b_02 = "qr230b_02"; //QRcode
    String qr230b_03 = "qr230b_03"; //料號
    String qr230b_04 = "qr230b_04"; //批號
    String qr230b_05 = "qr230b_05"; //數量
    String qr230b_06 = "qr230b_06"; //驗收狀況
    String qr230b_07 = "qr230b_07"; //明細項目

    String TABLE_NAME3 = "qr230c_table";
    String qr230c_01 = "qr230c_01"; //發料單號

    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            qr230_01 + " INTEGER," + qr230_02 + " TEXT," + qr230_03 + " TEXT," +
            qr230_04 + " DOUBLE," + qr230_05 + " DOUBLE," + qr230_06 + " DOUBLE," +
            qr230_07 + " TEXT," + qr230_08 + " TEXT," + qr230_09 + " TEXT," +
            qr230_10 + " TEXT," + qr230_11 + " TEXT," + " PRIMARY KEY(qr230_01))";

    String CREATE_TABLE2 = "CREATE TABLE " + TABLE_NAME2 + " (" +
            qr230b_01 + " INTEGER," + qr230b_02 + " TEXT," + qr230b_03 + " TEXT," +
            qr230b_04 + " TEXT," + qr230b_05 + " DOUBLE," + qr230b_06 + " TEXT," +
            qr230b_07 + " INTEGER)";

    String CREATE_TABLE3 = "CREATE TABLE " + TABLE_NAME3 + " (" + qr230c_01 + " TEXT)";

    private Context mCtx = null;

    public qr230DB(Context ctx) {
        this.mCtx = ctx;
    }

    public void open() throws SQLException {
        db = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        try {
            db.execSQL(CREATE_TABLE);
            db.execSQL(CREATE_TABLE2);
            db.execSQL(CREATE_TABLE3);
        } catch (Exception e) {

        }
    }

    public void close() {
        try {
            String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
            String DROP_TABLE2 = "DROP TABLE IF EXISTS " + TABLE_NAME2;
            String DROP_TABLE3 = "DROP TABLE IF EXISTS " + TABLE_NAME3;
            db.execSQL(DROP_TABLE);
            db.execSQL(DROP_TABLE2);
            db.execSQL(DROP_TABLE3);
            db.close();
        } catch (Exception e) {

        }
    }

    public void insHead(String dataStr) {
        if (dataStr.length() > 0){
            ContentValues args = new ContentValues();
            args.put(qr230c_01, dataStr.trim());
            db.insert(TABLE_NAME3, null, args);
        }

    }

    public String append(JSONObject result) {
        try {
            JSONArray jarray1 = result.getJSONArray("detail1");
            JSONArray jarray2 = result.getJSONArray("detail2");
            for (int i = 0; i < jarray1.length(); i++) {
                JSONObject data = jarray1.getJSONObject(i);
                Integer xqr230_01 = data.getInt("SFS02");
                String xqr230_02 = data.getString("SFS04");
                String xqr230_03 = data.getString("TA_IMA021_1");
                Double xqr230_04 = data.getDouble("SFS05");
                Double xqr230_05 = data.getDouble("SFSUD07");
                Double xqr230_06 = data.getDouble("SFSUD08");
                String xqr230_07 = data.getString("SFS07");
                String xqr230_08 = data.getString("SFS08");
                String xqr230_09 = data.getString("SFS09");
                String xqr230_10 = data.getString("TA_IMA02_1");
                String xqr230_11 = data.getString("SFS06");

                ContentValues args = new ContentValues();
                args.put(qr230_01, xqr230_01);
                args.put(qr230_02, xqr230_02);
                args.put(qr230_03, xqr230_03);
                args.put(qr230_04, xqr230_04);
                args.put(qr230_05, xqr230_05);
                args.put(qr230_06, xqr230_06);
                args.put(qr230_07, xqr230_07);
                args.put(qr230_08, xqr230_08);
                args.put(qr230_09, xqr230_09);
                args.put(qr230_10, xqr230_10);
                args.put(qr230_11, xqr230_11);
                db.insert(TABLE_NAME, null, args);
            }
            for (int i = 0; i < jarray2.length(); i++) {
                JSONObject data = jarray2.getJSONObject(i);
                Integer xqr230b_01 = data.getInt("QR_IMN02");
                String xqr230b_02 = data.getString("QR_IMN03");
                String xqr230b_03 = data.getString("QR_IMN04");
                String xqr230b_04 = data.getString("QR_IMN05");
                Double xqr230b_05 = data.getDouble("QR_IMN06");
                String xqr230b_06 = data.getString("QR_IMN08");
                Integer xqr230b_07 = data.getInt("QR_IMN09");
                ContentValues args = new ContentValues();
                args.put(qr230b_01, xqr230b_01);
                args.put(qr230b_02, xqr230b_02.trim());
                args.put(qr230b_03, xqr230b_03);
                args.put(qr230b_04, xqr230b_04);
                args.put(qr230b_05, xqr230b_05);
                args.put(qr230b_06, xqr230b_06);
                args.put(qr230b_07, xqr230b_07);
                db.insert(TABLE_NAME2, null, args);
            }
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    //QR_code,MVL,Số Lô, Số Lượng
    public String scan(String xqr230b_02, String xqr230b_03, String xqr230b_04, Double xqr230b_05) {
        try {
            //確認是否有此品號
            Cursor c = db.rawQuery("SELECT COUNT(qr230_02) count FROM " + TABLE_NAME + " WHERE qr230_02='" + xqr230b_03 + "' ORDER BY qr230_01", null);
            c.moveToFirst();
            Integer tcount = c.getInt(0);
            c.close();
            if (tcount > 0) {
                //檢查掃描數量是否超過
                Cursor c1 = db.rawQuery("SELECT qr230_01,qr230_05,qr230_06 FROM " + TABLE_NAME +
                        " WHERE qr230_02='" + xqr230b_03 + "' AND qr230_05 > qr230_06 AND qr230_05 - qr230_06 >= " + xqr230b_05 + " ORDER BY qr230_01", null);
                if (c1.getCount() == 0) {
                    return "OVERQTY";
                }
                c1.moveToFirst();
                Integer tqr230_01 = c1.getInt(0); //項次
                Double tqr230_05 = c1.getDouble(1); //已掃數量
                Double tqr230_06 = c1.getDouble(2); //已驗收量
                if (tqr230_05 - tqr230_06 - xqr230b_05 >= 0) {
                    Cursor d = db.rawQuery("SELECT qr230b_07 FROM " + TABLE_NAME2 + " " +
                            "WHERE qr230b_02='" + xqr230b_02 + "' AND qr230b_06='false' AND qr230b_03= '" + xqr230b_03 + "' ", null);
                    d.moveToFirst();
                    if (!d.isNull(0)) {
                        String d_qr230b_07 = d.getString(0);
                        db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_06=qr230_06+" + xqr230b_05 + " WHERE qr230_01=" + tqr230_01);
                        db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr230b_06= 'true' " +
                                " WHERE qr230b_02 ='" + xqr230b_02 + "' AND qr230b_01 ='" + tqr230_01 + "' AND qr230b_07 = '" + d_qr230b_07 + "'  ");
                        return "TRUE";
                    } else {
                        return "NORECORD2";
                    }

                } else {
                    return "OVERQTY";
                }
            } else {
                return "NORECORD";
            }

        } catch (Exception e) {
            return "FALSE";
        }
    }

    public String delscan(String xqr230b_01, String xqr230b_04, Double xqr230b_05) {
        try {
            Cursor c = db.rawQuery("SELECT rowid FROM " + TABLE_NAME2 + " WHERE qr230b_01='" + xqr230b_01 + "' AND qr230b_04='" + xqr230b_04 + "' AND qr230b_05=" + xqr230b_05, null);
            c.moveToFirst();
            Integer id = c.getInt(0);
            db.execSQL("DELETE FROM " + TABLE_NAME2 + " WHERE rowid=" + id);
            db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_05=qr230_05-" + xqr230b_05 + " WHERE qr230_01='" + xqr230b_01 + "'");
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    public Cursor getall() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY qr230_01", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getallb() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME2 + " ORDER BY qr230b_01,qr230b_07", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getallc() {
        try {
            return db.rawQuery("SELECT * FROM " + TABLE_NAME3 , null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    public Cursor getdetail() {
        try {
            return db.rawQuery("SELECT rowid _id,qr230_01,qr230_02,qr230_03,cast(qr230_04 as string) qr230_04,cast(qr230_05 as string) qr230_05,cast(qr230_06 as string) qr230_06,qr230_07,qr230_08,qr230_09,qr230_10,qr230_11 " +
                    " FROM " + TABLE_NAME + " ORDER BY " + qr230_01 + " ASC", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }

    }

    public Cursor getdialogdetail(String xqr230b_01) {
        try {
            return db.rawQuery("SELECT rowid _id,(SELECT count(*) FROM qr230b_table b WHERE a.rowid >=b.rowid AND b.qr230b_01='" + xqr230b_01 + "' ) AS rownum," +
                    "qr230b_04,qr230b_05,qr230b_06,qr230b_02,qr230b_07,qr230b_01 FROM "
                    + TABLE_NAME2 + " a WHERE qr230b_01='" + xqr230b_01 + "' ORDER BY rownum ", null);
        } catch (Exception e) {
            Cursor c = null;
            return c;
        }
    }

    //Hạng mục vật liệu , Hạng mục tem , Số lượng
    public String updCheckBox(String dialogitem01, String dialogitem02, String g_dialogitem03, boolean b) {
        try {
            if (b == true) {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_06=qr230_06 + " + g_dialogitem03 + " WHERE qr230_01='" + dialogitem01 + "'");
                db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr230b_06= '" + b + "' WHERE qr230b_01 ='" + dialogitem01 + "' AND qr230b_07 = '" + dialogitem02 + "'  ");
            } else {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_06=qr230_06 - " + g_dialogitem03 + " WHERE qr230_01='" + dialogitem01 + "'");
                db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr230b_06= '" + b + "' WHERE qr230b_01 ='" + dialogitem01 + "' AND qr230b_07 = '" + dialogitem02 + "'");
            }
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }

    //STT , MVL , Số Lô
    public String updCheckALL(String qr230_01, String qr230_02, boolean b) {
        try {
            if (b == true) {
                //Tính tổng số lượng đã phát
                String[] sumQr230b_05 = new String[]{"sum(" + qr230b_05 + ")"};
                String selection = "qr230b_03 = ? AND qr230b_01 = ? ";
                String[] selectionArgs = new String[]{qr230_02, qr230_01};

                Cursor sum_cur = db.query(TABLE_NAME2, sumQr230b_05, selection, selectionArgs, null, null, null);
                sum_cur.moveToFirst();
                if (sum_cur.getCount() > 0) {
                    int g_sum_qr230b_05 = sum_cur.getInt(0);
                    db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_06= " + g_sum_qr230b_05 + " WHERE qr230_02='" + qr230_02 + "' AND qr230_01 = '" + qr230_01 + "' ");
                }
            } else {
                db.execSQL("UPDATE " + TABLE_NAME + " SET qr230_06 = 0  WHERE qr230_02='" + qr230_02 + "' AND qr230_01 = '" + qr230_01 + "' ");
            }
            db.execSQL("UPDATE " + TABLE_NAME2 + " SET qr230b_06= '" + b + "' WHERE qr230b_03 ='" + qr230_02 + "' AND qr230b_01 = '" + qr230_01 + "'  ");
            return "TRUE";
        } catch (Exception e) {
            return "FALSE";
        }
    }
}
