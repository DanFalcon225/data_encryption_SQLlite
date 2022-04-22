package com.example.daniilss18019262_cs6002_app1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity{

    private mySQLLiteHandle dbHandle;
    private SQLiteDatabase sqLiteDatabase;

    private EditText editTextKey;
    private EditText editTextPassword;

    private TextView textViewRetrievePassword;
    private TextView textViewStoredPassword;

    private Entity entity = new Entity("password"); //Key for Facebook Conceal Method

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mapping variables with the designed canvas
        editTextKey = findViewById(R.id.editTextKey);
        editTextPassword = findViewById(R.id.editTextPwd);

        textViewRetrievePassword = findViewById(R.id.textViewActualPwd);
        textViewStoredPassword = findViewById(R.id.textViewEncryptedPwd);


        try{

            //creating database
            dbHandle = new mySQLLiteHandle(this, "PassWordStorage", null, 1);
            sqLiteDatabase = dbHandle.getWritableDatabase();
            sqLiteDatabase.execSQL("CREATE TABLE PasswordTable(Keyword TEXT, Password TEXT)");

        } catch (Exception e){
            e.printStackTrace();
        }


    }


    public void WriteDatabase(View view){

        Crypto crypto = new Crypto(new SharedPrefsBackedKeyChain(this, CryptoConfig.KEY_256), new SystemNativeCryptoLibrary(), CryptoConfig.KEY_256);

        byte[] cipherText = null;

        try {
            cipherText = crypto.encrypt(editTextPassword.getText().toString().getBytes(), entity);
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ContentValues contentValues = new ContentValues();

        contentValues.put("Keyword", editTextKey.getText().toString());

        String finalString = null;
        finalString = new String(cipherText, StandardCharsets.ISO_8859_1);
        contentValues.put("Password", finalString);

        sqLiteDatabase.insert("PasswordTable", null, contentValues);
        sqLiteDatabase.update("PasswordTable", contentValues,null, null);
    }

    public void RetrievePassword(View view){

        String query = "SELECT Password from PasswordTable WHERE Keyword = " + "\"" + editTextKey.getText().toString() + "\"";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        String finalString = cursor.getString(0);

        try {
            byte[] cipherText = finalString.getBytes(StandardCharsets.ISO_8859_1);

            Crypto crypto = new Crypto(new SharedPrefsBackedKeyChain(this, CryptoConfig.KEY_256), new SystemNativeCryptoLibrary(), CryptoConfig.KEY_256);

            finalString = new String(crypto.decrypt(cipherText, entity));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (CryptoInitializationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        textViewRetrievePassword.setText(finalString);

    }

    public void EncryptedPassword(View view){

        String query = "SELECT Password from PasswordTable WHERE Keyword = " + "\"" + editTextKey.getText().toString() + "\"";
        Cursor cursor = sqLiteDatabase.rawQuery(query, null);
        cursor.moveToFirst();
        textViewStoredPassword.setText(cursor.getString(0));

    }
}