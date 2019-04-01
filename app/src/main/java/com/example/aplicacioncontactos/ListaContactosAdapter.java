package com.example.aplicacioncontactos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListaContactosAdapter extends BaseAdapter {
    private Context context;
    private int layout;
    private ArrayList<Model> recordList;

    public ListaContactosAdapter(Context context, int layout, ArrayList<Model> recordList) {
        this.context = context;
        this.layout = layout;
        this.recordList = recordList;
    }

    @Override
    public int getCount() {
        return recordList.size();
    }

    @Override
    public Object getItem(int i) {
        return recordList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class ViewHolder{
        ImageView imageView;
        TextView txtName, txtAge, txtPhone;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        View row = view;
        ViewHolder holder = new ViewHolder();

        if (row==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layout, null);
            holder.txtName = row.findViewById(R.id.txtNombre);
            holder.txtAge = row.findViewById(R.id.txtEdad);
            holder.txtPhone = row.findViewById(R.id.txtTelefono);
            holder.imageView = row.findViewById(R.id.imgIcon);
            row.setTag(holder);
        }
        else {
            holder = (ViewHolder)row.getTag();
        }

        Model model = recordList.get(i);

        holder.txtName.setText(model.getNombre());
        holder.txtAge.setText(model.getEdad());
        holder.txtPhone.setText(model.getTelefono());

        byte[] recordImage = model.getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(recordImage, 0, recordImage.length);
        holder.imageView.setImageBitmap(bitmap);

        return row;
    }
}
