package com.mycompany.usbmanagertest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingsPageOne extends AppCompatActivity {
    TextView textView;
    Button button1;
    Button button4;
    Button button5;
    Button button3;
    Button button2;
    Button button6;
    Button button7;
    Button button8;
    Button button9;
    public static Float[]channelOneArrayB= {0.0f,0.0f,0.0f,1f,0f};
    public static Float[]channelTwoArrayB= {0.0f,0.0f,0.0f,1f,0f};
    public static Float[]channelThreeArrayB= {0.0f,0.0f,0.0f,1f,0f};
    public static Float[]channelFourArrayB= {0.0f,0.0f,0.0f,1f,0f};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page_one);
        textView=(TextView)findViewById(R.id.textView);
        button3=(Button)findViewById(R.id.button3);
        button2=(Button)findViewById(R.id.button2);
        button6=(Button)findViewById(R.id.button6);
        button7=(Button)findViewById(R.id.button7);
        button8=(Button)findViewById(R.id.button8);
        button2Response();
        button6Response();
        button7Response();
        button8Response();
        button9Response();



        if(MainActivity.channelOneOpen==true){
            button8.setText("Enabled");
        }
        else{
            button8.setText("Disabled");
        }
        if(MainActivity.channelTwoOpen==true){
            button7.setText("Enabled");
        }
        else{
            button7.setText("Disabled");
        }
        if(MainActivity.channelThreeOpen==true){
            button6.setText("Enabled");
        }
        else{
            button6.setText("Disabled");
        }
        if(MainActivity.channelFourOpen==true){
            button2.setText("Enabled");
        }
        else{
            button2.setText("Disabled");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings_page_one, menu);
        return true;
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent info) {

        if (requestCode == 2) {
            if(resultCode == Activity.RESULT_OK){
                Bundle pageOneExtras=info.getExtras();
                if(pageOneExtras.getInt("Channel Number")==1){
                    channelOneArrayB[0]=pageOneExtras.getFloat("Init E");
                    channelOneArrayB[1]=pageOneExtras.getFloat("High E");
                    channelOneArrayB[2]=pageOneExtras.getFloat("Low E");
                    channelOneArrayB[3]=pageOneExtras.getFloat("Scan Rate");
                    channelOneArrayB[4]=pageOneExtras.getFloat("Gain");
                }
                if(pageOneExtras.getInt("Channel Number")==2){
                    channelTwoArrayB[0]=pageOneExtras.getFloat("Init E");
                    channelTwoArrayB[1]=pageOneExtras.getFloat("High E");
                    channelTwoArrayB[2]=pageOneExtras.getFloat("Low E");
                    channelTwoArrayB[3]=pageOneExtras.getFloat("Scan Rate");
                    channelTwoArrayB[4]=pageOneExtras.getFloat("Gain");
                }
                if(pageOneExtras.getInt("Channel Number")==3){
                    channelThreeArrayB[0]=pageOneExtras.getFloat("Init E");
                    channelThreeArrayB[1]=pageOneExtras.getFloat("High E");
                    channelThreeArrayB[2]=pageOneExtras.getFloat("Low E");
                    channelThreeArrayB[3]=pageOneExtras.getFloat("Scan Rate");
                    channelThreeArrayB[4]=pageOneExtras.getFloat("Gain");
                }
                if(pageOneExtras.getInt("Channel Number")==4){
                    channelFourArrayB[0]=pageOneExtras.getFloat("Init E");
                    channelFourArrayB[1]=pageOneExtras.getFloat("High E");
                    channelFourArrayB[2]=pageOneExtras.getFloat("Low E");
                    channelFourArrayB[3]=pageOneExtras.getFloat("Scan Rate");
                    channelFourArrayB[4]=pageOneExtras.getFloat("Gain");
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
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
    public void button5Response(View view){
        button5 = (Button)findViewById(R.id.button5);


                Intent channelSettings= new Intent(this, ChannelSettingsActivity.class);
                Integer channelName1=1;
                channelSettings.putExtra("channel",channelName1);

                channelSettings.putExtra("Init E",channelOneArrayB[0]);
                channelSettings.putExtra("High E",channelOneArrayB[1]);
                channelSettings.putExtra("Low E",channelOneArrayB[2]);
                channelSettings.putExtra("Scan Rate",channelOneArrayB[3]);
                channelSettings.putExtra("Gain",channelOneArrayB[4]);
        startActivityForResult(channelSettings, 2);
    }
    public void button3Response(View view){
        button3 = (Button)findViewById(R.id.button3);


        Intent channelSettings= new Intent(this, ChannelSettingsActivity.class);
        Integer channelName4=4;
        channelSettings.putExtra("channel",channelName4);
        channelSettings.putExtra("Init E",channelFourArrayB[0]);
        channelSettings.putExtra("High E",channelFourArrayB[1]);
        channelSettings.putExtra("Low E",channelFourArrayB[2]);
        channelSettings.putExtra("Scan Rate",channelFourArrayB[3]);
        channelSettings.putExtra("Gain",channelFourArrayB[4]);
        startActivityForResult(channelSettings, 2);
    }
    public void button4Response(View view){
        button4 = (Button)findViewById(R.id.button4);


        Intent channelSettings= new Intent(this, ChannelSettingsActivity.class);
        Integer channelName2=2;
        channelSettings.putExtra("channel",channelName2);

        channelSettings.putExtra("Init E",channelTwoArrayB[0]);
        channelSettings.putExtra("High E",channelTwoArrayB[1]);
        channelSettings.putExtra("Low E",channelTwoArrayB[2]);
        channelSettings.putExtra("Scan Rate",channelTwoArrayB[3]);
        channelSettings.putExtra("Gain",channelTwoArrayB[4]);
        startActivityForResult(channelSettings,2);
    }
    public void button1Response(View view){
        button1 = (Button)findViewById(R.id.button);


        Intent channelSettings= new Intent(this, ChannelSettingsActivity.class);
        Integer channelName3=3;
        channelSettings.putExtra("channel",channelName3);
        channelSettings.putExtra("Init E",channelThreeArrayB[0]);
        channelSettings.putExtra("High E",channelThreeArrayB[1]);
        channelSettings.putExtra("Low E",channelThreeArrayB[2]);
        channelSettings.putExtra("Scan Rate",channelThreeArrayB[3]);
        channelSettings.putExtra("Gain",channelThreeArrayB[4]);
        startActivityForResult(channelSettings,2);
    }
    public void button8Response(){
        button8=(Button)findViewById(R.id.button8);
        button8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.channelOneOpen==true){
                    MainActivity.channelOneOpen=false;
                    button8.setText("Disabled");
                }
                else if (MainActivity.channelOneOpen==false){
                    MainActivity.channelOneOpen=true;
                    button8.setText("Enabled");
                }
            }
        });
    }
    public void button7Response(){
        button7=(Button)findViewById(R.id.button7);
        button7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.channelTwoOpen==true){
                    MainActivity.channelTwoOpen=false;
                    button7.setText("Disabled");
                }
                else if (MainActivity.channelTwoOpen==false){
                    MainActivity.channelTwoOpen=true;
                    button7.setText("Enabled");
                }
            }
        });
    }
    public void button6Response(){
        button6=(Button)findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.channelThreeOpen==true){
                    MainActivity.channelThreeOpen=false;
                    button6.setText("Disabled");
                }
                else if (MainActivity.channelThreeOpen==false){
                    MainActivity.channelThreeOpen=true;
                    button6.setText("Enabled");
                }
            }
        });
    }
    public void button2Response(){
        button2=(Button)findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.channelFourOpen==true){
                    MainActivity.channelFourOpen=false;
                    button2.setText("Disabled");
                }
                else if (MainActivity.channelFourOpen==false){
                    MainActivity.channelFourOpen=true;
                    button2.setText("Enabled");
                }
            }
        });
    }
    public void button9Response(){
        button9=(Button)findViewById(R.id.button9);
        button9.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent messageTest= new Intent();
                String editTextString= "Data Received";
                //assuming we want to send back data
                messageTest.putExtra("result",editTextString);
                messageTest.putExtra("Init E1",channelOneArrayB[0]);
                messageTest.putExtra("High E1",channelOneArrayB[1]);
                messageTest.putExtra("Low E1",channelOneArrayB[2]);
                messageTest.putExtra("Gain1",channelOneArrayB[4]);
                //
                messageTest.putExtra("Init E2",channelTwoArrayB[0]);
                messageTest.putExtra("High E2",channelTwoArrayB[1]);
                messageTest.putExtra("Low E2",channelTwoArrayB[2]);
                messageTest.putExtra("Scan Rate2",channelTwoArrayB[3]);
                messageTest.putExtra("Gain2",channelTwoArrayB[4]);;
                //
                messageTest.putExtra("Init E3",channelThreeArrayB[0]);
                messageTest.putExtra("High E3",channelThreeArrayB[1]);
                messageTest.putExtra("Low E3",channelThreeArrayB[2]);
                messageTest.putExtra("Scan Rate3",channelThreeArrayB[3]);
                messageTest.putExtra("Gain3",channelThreeArrayB[4]);
                //
                messageTest.putExtra("Init E4",channelThreeArrayB[0]);
                messageTest.putExtra("High E4",channelThreeArrayB[1]);
                messageTest.putExtra("Low E4",channelThreeArrayB[2]);
                messageTest.putExtra("Scan Rate4",channelThreeArrayB[3]);
                messageTest.putExtra("Gain4",channelThreeArrayB[4]);

                setResult(MainActivity.RESULT_OK,messageTest);
                finish();
            }
        });
    }
}
