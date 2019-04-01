package com.example.aplicacioncontactos;

import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ListView mListView;
    ArrayList<Model> mList;
    ListaContactosAdapter mAdapter = null;

    ImageView imageViewIcon;

    byte[] ImagenDefaul = (byte[]) Base64.decode(String.valueOf(R.drawable.contacto), Base64.DEFAULT);
    final int REQUEST_CODE_GALLERY = 999;

    private static boolean activador = true;

    public static SQLiteHelper mSQLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("Contactos");

        mListView = findViewById(R.id.listView);
        mList = new ArrayList<>();
        mAdapter = new ListaContactosAdapter(this, R.layout.row, mList);
        mListView.setAdapter(mAdapter);
        //condicion para que solo una vez se ejecute la creacion de base de datos, tabla,
        //y usuarios por defecto
        if(activador == true) {
            //creando base de datos
            mSQLiteHelper = new SQLiteHelper(this, "RECORDDB.sqlite", null, 1);

            //creando una tabla en la base de datos
            mSQLiteHelper.queryData("CREATE TABLE IF NOT EXISTS RECORD(id INTEGER PRIMARY KEY AUTOINCREMENT, nombre VARCHAR, edad VARCHAR, telefono VARCHAR, image BLOB)");
            //creadno dos usuaruios por defecto en la apliacion
            mSQLiteHelper.insertData("Guillermo Luna", "27", "2369234023", ImagenDefaul);
            mSQLiteHelper.insertData("Brenda Luna", "31", "2222435230", ImagenDefaul);
            activador = false;
        }

        FloatingActionButton creaContacto = findViewById(R.id.add);
        //Metodo para enviar a crear nuevo contacto
        creaContacto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, addContacto.class);
                //intent.putExtra("parametro", "string");
                startActivity(intent);
            }
        });

        //obtener todos los datos de sqlite
        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM RECORD");
        mList.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String nombre = cursor.getString(1);
            String edad = cursor.getString(2);
            String telefono = cursor.getString(3);
            byte[] image  = cursor.getBlob(4);
            //agregar a la lista
            mList.add(new Model(id, nombre, edad, telefono, image));
        }
        mAdapter.notifyDataSetChanged();
        if (mList.size()==0){
            //si no hay registro en la tabla de la base de datos, lo que significa que la vista de lista está vacía
            Toast.makeText(this, "Ningún se encontraron contactos...", Toast.LENGTH_SHORT).show();
        }

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> adapterView, View view, final int position, long l) {
                //Cuadro de diálogo de alerta para mostrar las opciones de actualización y eliminación.
                final CharSequence[] items = {"Editar", "Eliminar"};

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

                dialog.setTitle("Elige una acción");
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0){
                            //actualizar
                            Cursor c = mSQLiteHelper.getData("SELECT id FROM RECORD");
                            ArrayList<Integer> arrID = new ArrayList<Integer>();
                            while (c.moveToNext()){
                                arrID.add(c.getInt(0));
                            }
                            //Mostrar diálogo de actualización
                            showDialogUpdate(MainActivity.this, arrID.get(position));
                        }
                        if (i==1){
                            //eliminar
                            Cursor c = mSQLiteHelper.getData("SELECT id FROM RECORD");
                            ArrayList<Integer> arrID = new ArrayList<Integer>();
                            while (c.moveToNext()){
                                arrID.add(c.getInt(0));
                            }
                            showDialogDelete(arrID.get(position));
                        }
                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    private void showDialogDelete(final int idRecord) {
        AlertDialog.Builder dialogDelete = new AlertDialog.Builder(MainActivity.this);
        dialogDelete.setTitle("Advertencia!!");
        dialogDelete.setMessage("¿Estás seguro de eliminar?");
        dialogDelete.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    mSQLiteHelper.deleteData(idRecord);
                    Toast.makeText(MainActivity.this, "Contacto eliminado correctamente", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Log.e("error", e.getMessage());
                }
                updateRecordList();
            }
        });
        dialogDelete.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialogDelete.show();
    }

    private void showDialogUpdate(Activity activity, final int position){
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.editar_dialog);
        dialog.setTitle("Editar");

        imageViewIcon = dialog.findViewById(R.id.imageViewRecord);
        final EditText edtNombre = dialog.findViewById(R.id.edtNombre);
        final EditText edtEdad = dialog.findViewById(R.id.edtEdad);
        final EditText edtTelefono = dialog.findViewById(R.id.edtTelefono);
        Button btnUpdate = dialog.findViewById(R.id.btnUpdate);

        //obtener datos de la fila pulsada desde sqlite
        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM RECORD WHERE id="+position);
        mList.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String nombre = cursor.getString(1);
            edtNombre.setText(nombre);
            String edad = cursor.getString(2);
            edtEdad.setText(edad);
            String telefono = cursor.getString(3);
            edtTelefono.setText(telefono);
            byte[] image  = cursor.getBlob(4);
            //establecer imagen obtenida de sqlite
            imageViewIcon.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            //add to list
            mList.add(new Model(id, nombre, edad, telefono, image));
        }

        //establecer ancho de diálogo
        int width = (int)(activity.getResources().getDisplayMetrics().widthPixels*0.95);
        //establecer altura de diálogo
        int height = (int)(activity.getResources().getDisplayMetrics().heightPixels*0.7);
        dialog.getWindow().setLayout(width,height);
        dialog.show();

        //en el cuadro de diálogo de actualización, haga clic en la vista de imagen para actualizar la imagen
        imageViewIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check external storage permission
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        888
                );
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mSQLiteHelper.updateData(
                            edtNombre.getText().toString().trim(),
                            edtEdad.getText().toString().trim(),
                            edtTelefono.getText().toString().trim(),
                            MainActivity.imageViewToByte(imageViewIcon),
                            position
                    );
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Datos Acatualizados correctamente", Toast.LENGTH_SHORT).show();
                }
                catch (Exception error){
                    Log.e("Error al actualizar", error.getMessage());
                }
                updateRecordList();
            }
        });

    }

    private void updateRecordList() {
        //obtener todos los datos de sqlite
        Cursor cursor = mSQLiteHelper.getData("SELECT * FROM RECORD");
        mList.clear();
        while (cursor.moveToNext()){
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String age = cursor.getString(2);
            String phone = cursor.getString(3);
            byte[] image = cursor.getBlob(4);

            mList.add(new Model(id,name,age,phone,image));
        }
        mAdapter.notifyDataSetChanged();
    }


    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 888){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //gallery intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, 888);
            }
            else {
                Toast.makeText(this, "\n" + "No tiene permiso para acceder a la ubicación del archivo", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 888 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON) //enable image guidlines
                    .setAspectRatio(1, 1)// image will be square
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                //set image choosed from gallery to image view
                imageViewIcon.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
