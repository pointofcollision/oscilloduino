package com.mycompany.usbmanagertest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class ChannelSettingsActivity extends AppCompatActivity {
    Button button10;
    TextView textView8;
    EditText editText2;
    EditText editText3;
    EditText editText4;
    EditText editText5;
    EditText editText6;
    EditText editText7;
    EditText editText8;

    TextView TextInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_settings);
        Intent channel =getIntent();
        Bundle extras=channel.getExtras();
        editText2 = (EditText)findViewById(R.id.editText2);
        editText2.setFilters(new InputFilter[]{new InputFilterMinMax("0","5")});
        editText3 = (EditText)findViewById(R.id.editText3);
        editText3.setFilters(new InputFilter[]{new InputFilterMinMax("0","5")});
        editText4 = (EditText)findViewById(R.id.editText4);
        editText4.setFilters(new InputFilter[]{new InputFilterMinMax("0","5")});
        //init e, high e, low e in that order
        //next is scan rate then gain
        editText5 = (EditText)findViewById(R.id.editText5);
        //needs to be int value from 1 to 4883 ( we can package as a float and round
        editText5.setFilters(new InputFilter[]{new InputFilterMinMax("1","4883")});
        editText7 = (EditText)findViewById(R.id.editText7);
        editText7.setFilters(new InputFilter[]{new InputFilterMinMax("0","3")});
        Integer messageReceived=extras.getInt("channel");
        textView8=(TextView)findViewById(R.id.textView8);
        if (messageReceived==1) {
            textView8.setText("Channel One");
            //2 3 4 5 6 7 8 (edittexts)
            editText2.setText(Float.toString(extras.getFloat("Init E")));
            editText3.setText(Float.toString(extras.getFloat("High E")));
            editText4.setText(Float.toString(extras.getFloat("Low E")));
            editText5.setText(Integer.toString(Math.round(extras.getFloat("Scan Rate"))));
            editText7.setText(Integer.toString(Math.round(extras.getFloat("Gain"))));

        }
        if (messageReceived==2) {
            textView8.setText("Channel Two");
            //2 3 4 5 6 7 8 (edittexts)
            editText2.setText(Float.toString(extras.getFloat("Init E")));
            editText3.setText(Float.toString(extras.getFloat("High E")));
            editText4.setText(Float.toString(extras.getFloat("Low E")));
            editText5.setText(Integer.toString(Math.round(extras.getFloat("Scan Rate"))));
            editText7.setText(Integer.toString(Math.round(extras.getFloat("Gain"))));
        }
        //sample interval >> gain
        if (messageReceived==3) {
            textView8.setText("Channel Three");
            //2 3 4 5 6 7 8 (edittexts)
            editText2.setText(Float.toString(extras.getFloat("Init E")));
            editText3.setText(Float.toString(extras.getFloat("High E")));
            editText4.setText(Float.toString(extras.getFloat("Low E")));
            editText5.setText(Integer.toString(Math.round(extras.getFloat("Scan Rate"))));
            editText7.setText(Integer.toString(Math.round(extras.getFloat("Gain"))));
        }
        if (messageReceived==4) {
            textView8.setText("Channel Four");
            //2 3 4 5 6 7 8 (edittexts)
            editText2.setText(Float.toString(extras.getFloat("Init E")));
            editText3.setText(Float.toString(extras.getFloat("High E")));
            editText4.setText(Float.toString(extras.getFloat("Low E")));
            editText5.setText(Integer.toString(Math.round(extras.getFloat("Scan Rate"))));
            editText7.setText(Integer.toString(Math.round(extras.getFloat("Gain"))));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_channel_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void button10Response(View view){
        Intent channelupdates=new Intent();
        editText2 = (EditText)findViewById(R.id.editText2);
        editText3 = (EditText)findViewById(R.id.editText3);
        editText4 = (EditText)findViewById(R.id.editText4);
        editText5 = (EditText)findViewById(R.id.editText5);
        editText7 = (EditText)findViewById(R.id.editText7);
        Float Init_E=Float.valueOf(editText2.getText().toString());
        channelupdates.putExtra("Init E", Init_E);
        Float High_E = Float.valueOf(editText3.getText().toString());
        channelupdates.putExtra("High E", High_E);
        Float Low_E=Float.valueOf(editText4.getText().toString());
        channelupdates.putExtra("Low E", Low_E);
        Float Scan_Rate=Float.valueOf(editText5.getText().toString());
        channelupdates.putExtra("Scan Rate", Scan_Rate);
        Float Sample_Interval=Float.valueOf(editText7.getText().toString());
        channelupdates.putExtra("Gain", Sample_Interval);
        Integer channelNumber=getIntent().getExtras().getInt("channel");
        channelupdates.putExtra("Channel Number",channelNumber);
        setResult(SettingsPageOne.RESULT_OK, channelupdates);
        //Now we have all 7 values inputed, plus the one value for the channel. Now we need to send this
        //back to the second activity, which will in turn return it to the first activity
        finish();
    }
}
