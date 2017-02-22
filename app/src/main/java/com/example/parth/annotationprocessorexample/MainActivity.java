package com.example.parth.annotationprocessorexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.facebook.FacebookSdk;
import com.parthdave.annotations.FacebookLogin;
import com.parthdave.annotations.FacebookLoginBtn;

import org.json.JSONException;
import org.json.JSONObject;

@FacebookLogin
public class MainActivity extends AppCompatActivity {

    @FacebookLoginBtn
    TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTitle = (TextView) findViewById(R.id.tvTitle);
    }

    public void init(View view){
    }

    public void updateUI(){

    }

    public void parseFacebookData(JSONObject json){
        try {
            Toast.makeText(this,json.getString("first_name")+" "+json.getString("last_name"),Toast.LENGTH_LONG).show();
            System.out.println(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
