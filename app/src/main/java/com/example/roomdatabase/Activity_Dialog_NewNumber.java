package com.example.roomdatabase;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class Activity_Dialog_NewNumber extends AppCompatActivity {

    //Элементы экрана
    EditText et_Name, et_Surname, et_Number;
    Button btn_Confirm, btn_Cancel;
    ImageButton btn_SelectImage;

    //Объект базы данных
    AppDatabase dataBase;

    Bitmap bitmap;

    //Байт массив для изображения
    byte[] imageArray;

    //Переменная для определения действия, добавляем новый номер или изменяем существующий
    boolean edit=false;

    //Локальные переменные для сообщения между методами
    String ed_name;
    String ed_Surname;
    String ed_Number;
    long id;
    byte[] ed_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_newnumber);

        try {
            //Присваиваем локальным переменным значения из Extra
            boolean ed=getIntent().getBooleanExtra("edit", false);
            long ed_id=getIntent().getLongExtra("id", 1);
            ed_name=getIntent().getStringExtra("name");
            ed_Surname=getIntent().getStringExtra("surname");
            ed_Number=getIntent().getStringExtra("number");
            ed_image=getIntent().getByteArrayExtra("image");
            edit=ed;
            id=ed_id;
        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(), "ERROR IN DIALOG INITIALIZE ELEMENTS", Toast.LENGTH_SHORT).show();
        }




        //Инициализируем базу данных
        dataBase = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database").allowMainThreadQueries().build();

        View.OnClickListener oclBtn=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Получаем значения из полей ввода
                String name=et_Name.getText().toString();
                String surname=et_Surname.getText().toString();
                String number=et_Number.getText().toString();
                switch (v.getId()){
                    case R.id.btn_Confirm:
                        //Проверяем на то что нам надо сделать, обновить запись или внести новую
                        if(edit==false){
                            dataBase.numberDao().insert(new Number(name, surname, number, imageArray)); }
                        else {
                            try {
                                dataBase.numberDao().updateByid(name, surname, number, imageArray, id);
                            }
                            catch (Exception e){
                                Toast.makeText(getApplicationContext(), "ERROR IN DIALOG CONFIRM BUTTON", Toast.LENGTH_SHORT).show();
                            }
                             }
                        finish();
                        break;
                    case R.id.btn_Cancel:
                        finish();
                        break;
                    case R.id.btn_SelectImage:
                        //Обращаемся к активности выбора фото из галереи
                        Intent photoPickerIntent =new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, 1);
                        break;
                }
            }
        };

        btn_Confirm=findViewById(R.id.btn_Confirm);
        btn_Confirm.setOnClickListener(oclBtn);
        btn_Cancel=findViewById(R.id.btn_Cancel);
        btn_Cancel.setOnClickListener(oclBtn);
        btn_SelectImage=findViewById(R.id.btn_SelectImage);
        btn_SelectImage.setOnClickListener(oclBtn);


        et_Name=findViewById(R.id.et_Name);
        et_Surname=findViewById(R.id.et_Surname);
        et_Number=findViewById(R.id.et_Number);

        /*
        Присваиваем полям ввода существующие значения, и изображение
        Этот блок находится в try-catch т.к. из-за того что в элементе
        пустой байт массив изображения и мы обращаемся к пустому массиву,
        в следствии чего приложение крашится
         */
        try{
            if(edit){
                setTitle("Изменение номера");
                et_Name.setText(ed_name);
                et_Surname.setText(ed_Surname);
                et_Number.setText(ed_Number);
                byte[] image=ed_image;
                imageArray=ed_image;
                //Декодируем байт массив и устанавливаем полученное изображение на кнопку выбора иконки
                Bitmap bmp= BitmapFactory.decodeByteArray(image, 0, image.length);
                btn_SelectImage.setImageBitmap(bmp);

            }
            else {
                setTitle("Новый номер");
            }
        }
        catch (Exception e){
            //Выполняется когда передаётся пустой байт массив
            Toast.makeText(getApplicationContext(), "ERROR: IMAGE NOT FOUND", Toast.LENGTH_SHORT).show();
        }

    }


    //Тут мы обрабатываем получение изображения из памяти устройства
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case 1:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    try {
                        //Получаем bitmap нужного изображения
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //Устанавливаем кнопке выбранное изображение
                    btn_SelectImage.setImageBitmap(bitmap);
                    //Поток преобразования изображения в байт-массив
                    ByteArrayOutputStream streamImage = new ByteArrayOutputStream();
                    //Переводим bitmap в нужный нам формат
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, streamImage);
                    //Переводим полученный bitmap в байт-массив, и присваиваем результат к локальному массиву изображения
                    imageArray=streamImage.toByteArray();
                }
        }
    }

}
