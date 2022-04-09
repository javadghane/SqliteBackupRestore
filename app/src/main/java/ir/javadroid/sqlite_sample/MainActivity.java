package ir.javadroid.sqlite_sample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //نمونه ای از کلاس دیتابیس
    private MyDatabaseHelper dbHelper;

    //پوشه ذخیره سازی بک اپ - در پوشه دانلود دستگاه
    String backUpDir;
    //همان پوشه بالا به صورت فایل
    File backUpDirFolder;
    //آدرس دیتابیس اصلی برنامه در این متغیر است
    File localDbPath;

    //آداپتر لیست یوزرها در صفحه اصلی
    AdapterUser userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ایجاد یک نمونه از دیتابیس
        dbHelper = new MyDatabaseHelper(this);

        //پوشه مورد نظر در دایرکتوری دانلود
        backUpDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/sqlite_backup/";
        backUpDirFolder = new File(backUpDir);
        //درصورت نبودن پوشه آن پوشه ایجاد خواهد شد
        if (!backUpDirFolder.exists()) backUpDirFolder.mkdirs();
        //آدرس دیتابیس برنامه به دست می‌اید و در متغیر مربوطه ریخته میشود
        localDbPath = getDatabasePath(dbHelper.getDatabaseName());


        //دکمه ها و ادیت تکست های صفحه اصلی
        EditText edtId = findViewById(R.id.edtId);
        EditText edtName = findViewById(R.id.edtName);
        EditText edtAddress = findViewById(R.id.edtAddress);
        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnBackup = findViewById(R.id.btnBackUp);
        Button btnRestore = findViewById(R.id.btnRestore);

        btnBackup.setOnClickListener(view -> backUpDb());
        btnRestore.setOnClickListener(view -> restoreDb());

        btnAdd.setOnClickListener(view -> {
            //درج یک یوزر جدید
            dbHelper.insertUser(Integer.parseInt(edtId.getText().toString()), edtName.getText().toString(), edtAddress.getText().toString());
            //نمایش لیست یوزر ها در لیست
            showDataInList();
        });


        showDataInList();
    }

    void backUpDb() {
        try {
            //به جهت اطمینان اگر پوشه بک آپ ما ایجاد نشده بود آن را می‌سازد
            if (!backUpDirFolder.exists()) backUpDirFolder.mkdirs();
            //فایل دیتابیس داخلی برنامه ما در پوشه بک آپ کپی میشود
            copy(localDbPath, new File(backUpDirFolder, System.currentTimeMillis() + "_" + dbHelper.getDatabaseName()));
            //نمایش پیام موفقیت بک آپ
            Toast.makeText(this, "BackUpDone at:" + new File(backUpDirFolder, dbHelper.getDatabaseName()).getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void log(String msg) {
        android.util.Log.e("tag", msg);
    }

    void restoreDb() {
        //باز کردن فایل منیجر دستگاه برای انتخاب فایل بک آپ
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 123);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                //log(uri.toString());

                //کپی کردن فایل انتخاب شده به جای دیتابیس موجود
                copyFileFromUri(uri, localDbPath.getAbsolutePath());
                //نمایش پیام موفقیت
                Toast.makeText(this, "Restore Done.", Toast.LENGTH_LONG).show();
                //چون دیتابیس عوض شده آبجکت دیتابیس را مجدد میسازیم
                dbHelper = dbHelper.reopen(this);
                //نمایش دیتا در لیست صفحه اصلی
                showDataInList();

            }
        }
    }

    //کپی کردن فایل انتخاب شده از فایل منیجر به آدرس داده شده
    public void copyFileFromUri(Uri fileUri, String dest) {
        try {
            ContentResolver content = getContentResolver();
            InputStream inputStream = content.openInputStream(fileUri);
            OutputStream outputStream = new FileOutputStream(dest);
            log("Output Stream Opened successfully");
            byte[] buffer = new byte[1000];
            while ((inputStream.read(buffer, 0, buffer.length)) >= 0) {
                outputStream.write(buffer, 0, buffer.length);
            }
        } catch (Exception e) {
            log("Exception occurred " + e.getMessage());
        } finally {
            log("File Copied");
        }
    }


    //کپی کردن فایل از آدرس داده شده متغیر اول به آدرس متغیر دوم
    public void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    //گرفتن لیست یوزر ها از دیتابیس و نمایش در لیست صفحه اصلی
    void showDataInList() {
        ArrayList<ModelUser> userList = new ArrayList<>(dbHelper.getAllUsers());
        userAdapter = new AdapterUser(this, userList, new AdapterUser.delegate() {
            @Override
            public void onClick(ModelUser user, int position) {

            }

            @Override
            public void onDelete(ModelUser user, int position) {
                Toast.makeText(MainActivity.this, user.id + "Deleted.", Toast.LENGTH_SHORT).show();
                dbHelper.deleteUser(user.id);
                showDataInList();
            }
        });
        RecyclerView recData = findViewById(R.id.recyclerData);
        recData.setAdapter(userAdapter);
    }
}