package com.example.klb_pda.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.example.klb_pda.Listdata.Barcode_listData;
import com.example.klb_pda.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class Barcode_adapter extends ArrayAdapter<Barcode_listData> {
    private final Context context;
    int resource;
    List<Barcode_listData> objects;

    public Barcode_adapter(Context context, int resource, List<Barcode_listData> objects) {
        //super((Context) context, resource, objects);
        super((Context) context, resource, objects);
        this.context = (Context) context;
        this.resource = resource;
        this.objects = objects;
    }

    public class ViewHolder {
        TextView dialogitem01,dialogitem02,dialogitem03,TV_dialog_matemID,TV_dialog_matem,TV_dialog_MVL_ID;
        CheckedTextView simpleCheckedTextView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(this.resource,null);

            holder.dialogitem01 = (TextView) convertView.findViewById(R.id.qr210_dialog01_item01);
            holder.dialogitem02 = (TextView) convertView.findViewById(R.id.qr210_dialog01_item02);
            holder.dialogitem03 = (TextView) convertView.findViewById(R.id.qr210_dialog01_item03);
            holder.simpleCheckedTextView = convertView.findViewById(R.id.simpleCheckedTextView);
            holder.TV_dialog_matemID = (TextView) convertView.findViewById(R.id.TV_dialog_matemID);
            holder.TV_dialog_matem = (TextView) convertView.findViewById(R.id.TV_dialog_matem);
            holder.TV_dialog_MVL_ID = (TextView) convertView.findViewById(R.id.TV_dialog_MVL_ID);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        Barcode_listData  dataList = this.objects.get(position);
        holder.dialogitem01.setText(String.valueOf(dataList.getDialogitem01()));
        holder.dialogitem02.setText(String.valueOf(dataList.getDialogitem02()));
        holder.dialogitem03.setText(String.valueOf(dataList.getDialogitem03()));
        holder.TV_dialog_matemID.setText(String.valueOf(dataList.getTV_dialog_matemID()));
        holder.TV_dialog_matem.setText(String.valueOf(dataList.getTV_dialog_matem()));
        holder.TV_dialog_MVL_ID.setText(String.valueOf(dataList.getTV_dialog_MVL_ID()));

        holder.simpleCheckedTextView.setText(null);
        //holder.simpleCheckedTextView.setChecked(false);
        if (dataList.isDialogitem04()) {
            holder.simpleCheckedTextView.setCheckMarkDrawable(R.drawable.ic_check_circle_pink_400_24dp);
            holder.simpleCheckedTextView.setChecked(true);
            //convertView.setBackgroundColor(context.getColor(R.color.ListSelector));
        } else {
            holder.simpleCheckedTextView.setCheckMarkDrawable(null);
            holder.simpleCheckedTextView.setChecked(false);
            //convertView.setBackgroundColor(context.getColor(R.color.background));
        }

        //return super.getView(position, convertView, parent);
        return convertView;
    }
}
