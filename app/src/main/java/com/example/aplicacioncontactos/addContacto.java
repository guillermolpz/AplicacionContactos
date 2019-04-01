package com.example.aplicacioncontactos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;

public class addContacto extends AppCompatActivity {

    EditText mEdtNombre, mEdtEdad, mEdtTelefono;
    Button mBtnAdd, mBtnList;
    ImageView mImageView;

    final int REQUEST_CODE_GALLERY = 999;

    //public static SQLiteHelper mSQLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_contacto);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Agregar Contacto");

        mEdtNombre = findViewById(R.id.edtNombre);
        mEdtEdad = findViewById(R.id.edtEdad);
        mEdtTelefono = findViewById(R.id.edtTelefono);
        mBtnAdd = findViewById(R.id.btnAdd);

        mImageView = findViewById(R.id.imageView);

        //Seleccione la imagen haciendo clic sobre imageview
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //lea el permiso de almacenamiento externo para seleccionar la imagen de
                //permiso de tiempo de ejecución para dispositivos Android 6.0 y superiores
                ActivityCompat.requestPermissions(
                        addContacto.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_GALLERY
                );
            }
        });

        //añadir registro a sqlite
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MainActivity.mSQLiteHelper.insertData(
                            mEdtNombre.getText().toString().trim(),
                            mEdtEdad.getText().toString().trim(),
                            mEdtTelefono.getText().toString().trim(),
                            imageViewToByte(mImageView)
                    );
                    Toast.makeText(addContacto.this, "Agregado correctamente", Toast.LENGTH_SHORT).show();
                    //reset views
                    mEdtNombre.setText("");
                    mEdtEdad.setText("");
                    mEdtTelefono.setText("");
                    mImageView.setImageResource(R.drawable.addphoto);
                    //Se utiliza esta forma de retorno para actualizar la lista de contactos al volver y visualizarlo
                    Intent intent = new Intent(addContacto.this, MainActivity.class);
                    startActivity(intent);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Mostrar lista de registros
        /*mBtnList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start recordlist activity
                startActivity(new Intent(MainActivity.this, ListaRegistrosActivity.class));
            }
        });*/
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
        if (requestCode == REQUEST_CODE_GALLERY){
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED){
                //gallery intent
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, REQUEST_CODE_GALLERY);
            }else{
                Toast.makeText(this, "No tiene permiso para acceder a la ubicación del archivo", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK){
            Uri imagenUri = data.getData();
            CropImage.activity(imagenUri)
                    .setGuidelines(CropImageView.Guidelines.ON)//enable image guidelines
                    .setAspectRatio(1,1)//image vill be square
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                //configurar imagen elegida de la galería a vista de imagen
                mImageView.setImageURI(resultUri);
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception error = result.getError();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
