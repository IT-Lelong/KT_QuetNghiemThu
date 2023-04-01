package com.example.klb_pda.Listdata;

import java.io.Serializable;

public class Barcode_listData implements Serializable {
    int dialogitem01 ;
    String dialogitem02, TV_dialog_matem, TV_dialog_matemID, TV_dialog_MVL_ID;
    boolean dialogitem04;
    Double dialogitem03;

    public Barcode_listData(int dialogitem01, String dialogitem02, Double dialogitem03, boolean dialogitem04,
                            String TV_dialog_matemID, String TV_dialog_matem, String TV_dialog_MVL_ID) {
        this.dialogitem01 = dialogitem01;
        this.dialogitem02 = dialogitem02;
        this.dialogitem03 = dialogitem03;
        this.dialogitem04 = dialogitem04;
        this.TV_dialog_matem = TV_dialog_matem;
        this.TV_dialog_matemID = TV_dialog_matemID;
        this.TV_dialog_MVL_ID = TV_dialog_MVL_ID;
    }

    public int getDialogitem01() {
        return dialogitem01;
    }

    public void setDialogitem01(int dialogitem01) {
        this.dialogitem01 = dialogitem01;
    }

    public Double getDialogitem03() {
        return dialogitem03;
    }

    public void setDialogitem03(Double dialogitem03) {
        this.dialogitem03 = dialogitem03;
    }

    public String getDialogitem02() {
        return dialogitem02;
    }

    public void setDialogitem02(String dialogitem02) {
        this.dialogitem02 = dialogitem02;
    }

    public boolean isDialogitem04() {
        return dialogitem04;
    }

    public void setDialogitem04(boolean dialogitem04) {
        this.dialogitem04 = dialogitem04;
    }

    public String getTV_dialog_matem() {
        return TV_dialog_matem;
    }

    public void setTV_dialog_matem(String TV_dialog_matem) {
        this.TV_dialog_matem = TV_dialog_matem;
    }

    public String getTV_dialog_matemID() {
        return TV_dialog_matemID;
    }

    public void setTV_dialog_matemID(String TV_dialog_matemID) {
        this.TV_dialog_matemID = TV_dialog_matemID;
    }

    public String getTV_dialog_MVL_ID() {
        return TV_dialog_MVL_ID;
    }

    public void setTV_dialog_MVL_ID(String TV_dialog_MVL_ID) {
        this.TV_dialog_MVL_ID = TV_dialog_MVL_ID;
    }
}
