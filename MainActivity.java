package com.mycompany.usbmanagertest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.MainThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.view.View.OnClickListener;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import android.os.Handler;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
//import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;


public class MainActivity extends AppCompatActivity implements BlankFragment.OnFragmentInteractionListener{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        //Log.d(logTag, "Resent oncreate");
        usbConnection();
        Buttonresponse();
        Stopresponse();
        stateMachineLauncher();
        GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.addSeries(series);
        GraphView dacvsdata = (GraphView) findViewById(R.id.DATAvsDAC);
        options = graph.getViewport();
        options.setXAxisBoundsManual(true);
        options.setYAxisBoundsManual(true);
        options.setMinX(0);
        options.setMaxX(1000);
        options.setMaxY(5);
        options.setMinY(0);
        options.setScalable(true);

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (MS)");
        graph.getGridLabelRenderer().setVerticalAxisTitle("DAC (Volts)");
        graph.getGridLabelRenderer().setLabelVerticalWidth(100);
        graph.getGridLabelRenderer().setPadding(40);
        graph.setTitle("Dac vs Time");
        optionsdac= dacvsdata.getViewport();
        optionsdac.setXAxisBoundsManual(true);
        optionsdac.setYAxisBoundsManual(true);
        optionsdac.setMinX(0);
        optionsdac.setMaxX(5);
        optionsdac.setMaxY(5);
        optionsdac.setMinY(-5);
        optionsdac.setScalable(true);
        // data versus dac
        dacvsdata.addSeries(seriesDAC);
        for(int i=0;i<1000;i++){
            double current_val=i*.005;
            seriesDAC.appendData(new DataPoint(current_val, -100), false, 1000);
        }
        dacvsdata.getGridLabelRenderer().setHorizontalAxisTitle("DAC (Volts)");
        dacvsdata.getGridLabelRenderer().setVerticalAxisTitle("DATA (Volts)");
        dacvsdata.getGridLabelRenderer().setLabelVerticalWidth(100);
        dacvsdata.getGridLabelRenderer().setPadding(40);
        dacvsdata.setTitle("Data vs DAC");
        GraphView datavstime = (GraphView) findViewById(R.id.DATAVSTIME);
        optionsdata=datavstime.getViewport();
        optionsdata.setXAxisBoundsManual(true);
        optionsdata.setYAxisBoundsManual(true);
        optionsdata.setMinX(0);
        optionsdata.setMaxX(100);
        optionsdata.setMaxY(5);
        optionsdata.setMinY(-5);
        //optionsdata.setScalable(true);
        datavstime.addSeries(seriesDATA);
        datavstime.getGridLabelRenderer().setHorizontalAxisTitle("time (ms)");
        datavstime.getGridLabelRenderer().setVerticalAxisTitle("DATA (Volts)");
        datavstime.getGridLabelRenderer().setLabelVerticalWidth(100);
        datavstime.getGridLabelRenderer().setPadding(40);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void onFragmentInteraction(Uri uri){
        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag("fragmentID");
    };

    protected void onStart() {
        mStop = false;
        if (mStopped){
            //enumerate();
            //Trigger enumerate in separate thread instead of here
            //difference is, now the data transfer is active in the "stream" phase, and the setup phase is separate as well
        }
        super.onStart();
    }
    protected void onStop() {
        mStop = true;
        super.onStop();
        unregisterReceiver(mUsbAttachReceiver);
        unregisterReceiver(mUsbDetachReceiver);
        unregisterReceiver(mUsbReceiver);
        unregisterReceiver(mTextReceiver);
        unregisterReceiver(mGraphReciever);
    }

    //for recieving data from the new activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String displayMessage= data.getStringExtra("result");
                txtInfo.setText(displayMessage);
            }
            if (data!=null) {
                Bundle extrasmain = data.getExtras();
                channelOneArray[0] = extrasmain.getFloat("Init E1");
                channelOneArray[1] = extrasmain.getFloat("High E1");
                channelOneArray[2] = extrasmain.getFloat("Low E1");
                channelOneArray[3] = extrasmain.getFloat("Scan Rate1");
                channelOneArray[4] = extrasmain.getFloat("Gain1");

                channelTwoArray[0] = extrasmain.getFloat("Init E2");
                channelTwoArray[1] = extrasmain.getFloat("High E2");
                channelTwoArray[2] = extrasmain.getFloat("Low E2");
                channelTwoArray[3] = extrasmain.getFloat("Scan Rate2");
                channelTwoArray[4] = extrasmain.getFloat("Gain2");

                channelThreeArray[0] = extrasmain.getFloat("Init E3");
                channelThreeArray[1] = extrasmain.getFloat("High E3");
                channelThreeArray[2] = extrasmain.getFloat("Low E3");
                channelThreeArray[3] = extrasmain.getFloat("Scan Rate3");
                channelThreeArray[4] = extrasmain.getFloat("Gain3");

                channelFourArray[0] = (extrasmain.getFloat("Init E4"));
                channelFourArray[1] = extrasmain.getFloat("High E4");
                channelFourArray[2] = extrasmain.getFloat("Low E4");
                channelFourArray[3] = extrasmain.getFloat("Scan Rate4");
                channelFourArray[4] = extrasmain.getFloat("Gain4");
            }
            
	 
	    
	    
	    if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("data.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data+"\n");
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    protected long[] configCVPacket(Float[] channel_array, int channel)
    {
    	long[] setCV={0,0,0,0,0,0};
        //for this we will be keeping the values in their int form, the final transform will be done
        //this will ultimately be an integer array only, since we are doing the error logging format
    	long init_vol = Math.round(channel_array[0]*(1024.0/5.0));
        Log.d(logTag,"init vol: "+String.valueOf((init_vol)));
	long high_vol =Math.round(channel_array[1]*(1024.0/5.0));
	long low_vol = Math.round(channel_array[2] * (1024.0 / 5.0));
        // the 1024/5 is the agreed upon conversion method
	long scan_rate = Math.round(channel_array[3]);
        //both scan rate and init game should be very close to the right values already
    long init_gain =Math.round(channel_array[4]);
	setCV[0]=channel;
        setCV[1]=init_gain;
        setCV[2]=init_vol;
        setCV[3]=high_vol;
        setCV[4]=low_vol;
        setCV[5]=scan_rate;
        return setCV;
    }



    protected void onResume(){
        //milliseconds=0;
       // millisecondsdata=0;
        super.onResume();
        GraphView dacvsdata = (GraphView) findViewById(R.id.DATAvsDAC);
       // Log.d(logTag,"right after result");
        options = ((GraphView) findViewById(R.id.graph)).getViewport();
        double voltspersec;
        double Time;
        double result;
        if(channelTwoOpen) {
            //Log.d(logTag, "in proper if volts setup");
            voltspersec = channelTwoArray[3] / 1000.0;
            //Log.d(logTag, "Value of volts per sec "+String.valueOf(voltspersec));
            Time = 2*(channelTwoArray[1]-channelTwoArray[2])/voltspersec;
            result=(Time*3000);
            //Log.d(logTag, "going for trasher");
            Log.d(logTag, String.valueOf(result));
            options.setMaxX(100);
        }
        else if(channelOneOpen){
            voltspersec = channelOneArray[3] / 1000.0;
            Time = 2 * (channelOneArray[1] - channelOneArray[2]) / voltspersec;
            result = (Time * 3000.0);
            //Log.d(logTag, "going for trasher");
            Log.d(logTag, String.valueOf(result));
            options.setMaxX(100);
        }

        IntentFilter intentReceiver= new IntentFilter(ACTION_TEST);
        IntentFilter attachReceiver= new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        IntentFilter detachReceiver= new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        IntentFilter usbReceiver = new IntentFilter(ACTION_USB_PERMISSION);
        IntentFilter graphReceiver = new IntentFilter(ACTION_GRAPH);
        mGraphReciever= new BroadcastReceiver(){
            @Override
            //will need to likely change this, because im not sure how many values we will be getting, likely just 10 or 1 at a time.
            public void onReceive(Context context, Intent intent){
                if (intent.getAction().equals(ACTION_GRAPH)){
                    //Bundle bundle = intent.getExtras();
                    //alright so this will be the hard part. We have an array whose front end will always be DATA in theory as long as the output stream is linear
                    byte[] myOriginalArray=intent.getByteArrayExtra("data points");
                    long[] mydataarray ={0,0,0,0,0};
                    //last one added is for gain
                    //still need to factor in the gain when calculating points, but i dont think the translation is wrong.
                    //need to pick apart the array we received to get this to be formatted right.
                    //first 2 bytes
                    double active_num=0;
                    long break_Position=0;
                    double position_count=0;
                    long slashRCount=0;
                    //only want to go up to end of 3rd
                    for(int i=3;i<myOriginalArray.length;i++){
                        //see a /r,split there
                        //judging by the type of stuff we have to do, we need to convert these multi
                        //byte numbers into single integers then do some simple math
                        //the converter for android makes this difficult to do with straight bytes
                        if(myOriginalArray[i]=='\r'){
                            slashRCount++;
                           // position_count=i+1;
                            //Log.d(logTag,"/r found");
                            if(slashRCount==2){
                              //  Log.d(logTag,"right position found");
                                //previous 1-3 bytes are the desired ones, find the size diff first
                                position_count=0;
                                for(int k=i-1;k>4;k--){
                                    long test=myOriginalArray[k]-48;

                                 //   Log.d(logTag, "Current place: " + String.valueOf(test));

                                  //  Log.d(logTag,"Current tens place"+String.valueOf(position_count));
                                    active_num=active_num+(test)*Math.pow(10,position_count);
                                 //   Log.d(logTag,"active num: "+ String.valueOf(active_num));
                                    position_count++;

                                }
                                mydataarray[0]=(long)(active_num);
                                //add to long map, this is frst of 4 values, msb for data
                                active_num=0;
                                position_count=0;
                                slashRCount=0;
                                break;
                            }
                        }

                    }
                    for(int i=3;i<myOriginalArray.length;i++){
                       // Log.d(logTag,"active num: "+ String.valueOf(active_num));
                        //see a /r,split there
                        //judging by the type of stuff we have to do, we need to convert these multi
                        //byte numbers into single integers then do some simple math
                        //the converter for android makes this difficult to do with straight bytes
                        if(myOriginalArray[i]=='\r'){
                          //  Log.d(logTag,"/r found");
                            slashRCount++;
                            if(slashRCount==3){
                                //previous 1-3 bytes are the desired ones, find the size diff first
                                position_count=0;
                             //   Log.d(logTag,"break position: "+ String.valueOf(break_Position));
                                for(int k=i-1;k>break_Position;k--){
                                    long test=myOriginalArray[k]-48;
                                    active_num=active_num+(test)*Math.pow(10,position_count);
                                //    Log.d(logTag,"active num: "+ String.valueOf(active_num));
                                    position_count++;
                                }
                                mydataarray[1]=(long)(active_num);
                                active_num=0;
                                position_count=0;
                                slashRCount=0;
                                //add to long map, this is second of 4 values, lsb for data
                                break;
                            }
                            break_Position=i;
                       //     Log.d(logTag,"break position: "+ String.valueOf(break_Position));
                        }

                    }
                    for(int i=3;i<myOriginalArray.length;i++){
                        // Log.d(logTag,"active num: "+ String.valueOf(active_num));
                        //see a /r,split there
                        //judging by the type of stuff we have to do, we need to convert these multi
                        //byte numbers into single integers then do some simple math
                        //the converter for android makes this difficult to do with straight bytes
                        if(myOriginalArray[i]=='\r'){
                            //  Log.d(logTag,"/r found");
                            slashRCount++;
                            if(slashRCount==5){
                                //previous 1-3 bytes are the desired ones, find the size diff first
                                position_count=0;
                                //   Log.d(logTag,"break position: "+ String.valueOf(break_Position));
                                for(int k=i-1;k>break_Position;k--){
                                    long test=myOriginalArray[k]-48;
                                    active_num=active_num+(test)*Math.pow(10,position_count);
                                    //    Log.d(logTag,"active num: "+ String.valueOf(active_num));
                                    position_count++;
                                }
                                mydataarray[4]=(long)(active_num);
                                active_num=0;
                                position_count=0;
                                slashRCount=0;
                                //add to long map, this is second of 4 values, lsb for data
                                break;
                            }
                            break_Position=i;
                            //     Log.d(logTag,"break position: "+ String.valueOf(break_Position));
                        }

                    }
                    for(int i=3;i<myOriginalArray.length;i++){
                        //see a /r,split there
                        //judging by the type of stuff we have to do, we need to convert these multi
                        //byte numbers into single integers then do some simple math
                        //the converter for android makes this difficult to do with straight bytes
                        if(myOriginalArray[i]=='\r'){
                            slashRCount++;
                            if(slashRCount==7){
                                //previous 1-3 bytes are the desired ones, find the size diff first
                                position_count=0;
                                for(int k=i-1;k>break_Position;k--){
                                    long test=myOriginalArray[k]-48;
                                    active_num=active_num+(test)*Math.pow(10,position_count);
                                    position_count++;
                                }
                                mydataarray[2]=(long)(active_num);
                                active_num=0;
                                position_count=0;
                                slashRCount=0;
                                //add to long map, this is third of 4 values, msb for dac
                                break;
                            }
                            break_Position=i;
                        }

                    }
                    for(int i=3;i<myOriginalArray.length;i++){
                        //see a /r,split there
                        //judging by the type of stuff we have to do, we need to convert these multi
                        //byte numbers into single integers then do some simple math
                        //the converter for android makes this difficult to do with straight bytes
                        if(myOriginalArray[i]=='\r'){
                            slashRCount++;
                            if(slashRCount==8){
                                //previous 1-3 bytes are the desired ones, find the size diff first
                                position_count=0;
                                for(int k=i-1;k>break_Position;k--){
                                    long test=myOriginalArray[k]-48;
                                    active_num=active_num+(test)*Math.pow(10,position_count);
                                    position_count++;
                                }
                                mydataarray[3]=(long)(active_num);
                                active_num=0;
                                position_count=0;
                                slashRCount=0;
                                break;
                            }
                            break_Position=i;
                        }
                    }
                    String fileData=String.valueOf("DATA (first two), DAC (second two)");
                    for (long l: mydataarray) {
                       // Log.d(logTag, "Table of longs translated: " + String.valueOf(l));
                        fileData=fileData+" ,"+String.valueOf(l);

                    }

                    writeToFile(fileData,context);
                    //variable byte array, we know it is long enough for one data point
                    //need to look through the array for our data points, trimming off everything before the next start
                    //of a DATA packet. so DATA (whatever length of numbers) DAC (whatever length of numbers) ,/r,-CUTOFF- DATA (next point)
                    //total bytes: bytes 6 and 7 are the data value, we know they are 1 millisecond
                    //if the value is too large (outside 5v range) it must be negative and we have to do
                    //some weird conversion stuff
                    //now we have the vaues we need, need to translate these. I
                    double val1int;
                    if  (mydataarray[0] >= 252) {
                        val1int = -(long)(1 + (255 - mydataarray[1]) + ((255 - mydataarray[0]) *Math.pow(2,8)));
                        if(mydataarray[4]==0){
                        val1int=val1int/((4.94*Math.pow(10,6)));
                        }
                        else if(mydataarray[4]==1){
                           val1int=val1int/((499.5*Math.pow(10,3)));
                        }
                        else if(mydataarray[4]==2){
                            val1int=val1int/((49.87*Math.pow(10,3)));
                        }
                        else if(mydataarray[4]==3){
                            val1int=val1int/((.9912*Math.pow(10,3)));
                        }
                    }
                    else {
                        val1int = mydataarray[1] + mydataarray[0]*(long)(Math.pow(2,8));
                        if(mydataarray[4]==0){
                            val1int=val1int/((long)(4.94*Math.pow(10,6)));
                        }
                        else if(mydataarray[4]==1){
                            val1int=val1int/((long)(499.5*Math.pow(10,3)));
                        }
                        else if(mydataarray[4]==2){
                            val1int=val1int/((long)(49.87*Math.pow(10,3)));
                        }
                        else if(mydataarray[4]==3){
                            val1int=val1int/((long)(.9912*Math.pow(10,3)));
                        }
                    }
                   // int val1int=(myOriginalArray[0]-48) << 8 | (myOriginalArray[1]-48 & 0xFF);
                    Log.d(logTag,"int found for data: "+ String.valueOf(val1int));
                    double val1intCON=1.0*val1int;
                    double val1=val1intCON*5.0/1024.0;
                    Log.d(logTag,"float found for data: "+ String.valueOf(val1));
                    //double dacval1int=(myOriginalArray[2]-48)<<8 | (myOriginalArray[3]-48 & 0xFF);
                    long dacval1int;
                    if  (mydataarray[2] >= 252) {
                        dacval1int = -(long)(1 + (255 - mydataarray[3]) + ((255 - mydataarray[2]) *Math.pow(2,8)));
                    }
                    else {
                        dacval1int = mydataarray[3] + mydataarray[2]*(long)(Math.pow(2,8));
                    }
                    Log.d(logTag,"int found for dac: "+ String.valueOf(dacval1int));
                    double dacval1=dacval1int*1.0;
                    dacval1=5.0*dacval1/1024.0;
                    Log.d(logTag, "int found for dac: " + String.valueOf(dacval1));
                        seriesDATA.appendData(new DataPoint(millisecondsdata,val1int),true,500);
                        series.appendData(new DataPoint(milliseconds,dacval1), true, 1000);
                      //  try {
                            //gonna try to update the sub class append data for this. It will insert the
                            //value at the right position in the list (just normal append for first 2 graphs,
                            //for this one it should ideally insert the point in the middle of the list
                            //if there is already a point at that spot, it adds this point at a small value past it
                            //(very small, .like .001)
                            seriesDAC.appendData(new DataPoint(dacval1, val1), false, 1000);
                            GraphView dacvsdata = (GraphView) findViewById(R.id.DATAvsDAC);
                            optionsdac=dacvsdata.getViewport();
                            optionsdac.setMaxY(seriesDAC.getHighestValueY());
                            optionsdac.setMinY(-2*seriesDAC.getHighestValueY());

                      //  }
                      //  catch (IllegalArgumentException e){
                           // GraphView dacvsdata = (GraphView) findViewById(R.id.DATAvsDAC);
                            //List<Series> usableSeries=dacvsdata.getSeries();
                           // for(int i=0; i<usableSeries.size();i++){
                                //if(i==)
                            //}
                            //Log.d(logTag,"caught error");
                       // }
                    millisecondsdata++;
                    milliseconds++;
                }
            }
        };
        this.registerReceiver(mGraphReciever, graphReceiver);
        mTextReceiver= new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent){
                if (intent.getAction().equals(ACTION_TEST)){
                    String messageReceived= intent.getStringExtra("message received");
                    txtInfo.setText(messageReceived);
                }
            }
        };
        this.registerReceiver(mTextReceiver,intentReceiver);
        mUsbAttachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    showDevices();

                }

            }
        };
        this.registerReceiver(mUsbAttachReceiver,attachReceiver);
        mUsbDetachReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    sDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (sDevice != null) {
                        // call your method that cleans up and closes communication with the device
                    }
                }

            }
        };
        this.registerReceiver(mUsbDetachReceiver,detachReceiver);
        mUsbReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        sDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (intent.getBooleanExtra(
                                UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (sDevice != null) {
                                // call method to set up device communication
                                //msgSend(sDevice);

                            }
                        } else {

                        }
                    }
                }
            }
        };
        this.registerReceiver(mUsbReceiver,usbReceiver);


    }
    public final static String testString="test_string";
    public final static String logTag="logged";
    //enumerated type for state machine below
    enum States{
        Idle, Setup, Stream
    };
    public void changeStates(States theState)
    {
        switch(theState){
            case Idle:
                Log.d(logTag, "Idling");
                dataBuffer=new byte[0];
                break;
                //test to see if device is attached
            case Setup:
                Log.d(logTag,"Setting up");
                dataBuffer=new byte[0];
                //if it is then set up the communications line
                if(mUsbManager.getDeviceList().values().iterator().hasNext()){
                sDevice=enumerate();}
                break;
            case Stream:
                Log.d(logTag, "Streaming");
                       //replace msgSend with actual body, assuming that looping stuff is present
                msgSend("STREAM");
                break;
                //will need actual code here obviously
            default: Log.d(logTag,"No choice selected");
                break;
        }
    }
    private ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private Handler uiHandler = new Handler();
    BroadcastReceiver mGraphReciever;
    BroadcastReceiver mTextReceiver;
    BroadcastReceiver mUsbAttachReceiver;
    BroadcastReceiver mUsbDetachReceiver;
    BroadcastReceiver mUsbReceiver;
    private boolean mStop=false;
    private boolean mStopped=true;
    private boolean startTransfer=false;
    Button btnDiscover;
    Button btnStop;
    Button optionsButton;
    private UsbEndpoint epIN = null;
    private UsbEndpoint epOUT = null;
    private UsbManager usbm = null;
    private UsbDeviceConnection conn =null;
    private UsbInterface inf_device = null;
    public byte[] dataBuffer=new byte[0];
    //public static ByteBuffer dataBuffer=ByteBuffer.wrap(new byte[0]);
    public static TextView txtInfo;
    public static int milliseconds=0;
    public static int millisecondsdata=0;
    public static boolean fragmentOne=false;
    public static boolean fragmentMenuOpened=false;
    public static boolean fragmentTwo=false;
    public static AtomicBoolean thread_safe_flag=new AtomicBoolean(false);
    private static AtomicBoolean commLoopFlag = new AtomicBoolean(false);
    private static AtomicBoolean setUpCompleteTrigger=new AtomicBoolean(true);
    public static AtomicBoolean thread_safe_flag0=new AtomicBoolean(false);
    public static boolean channelOneOpen=false;
    public static boolean channelTwoOpen=false;
    public static boolean channelThreeOpen=false;
    public static boolean channelFourOpen=false;
    public static String string_id="";

    //array needs 7 slots for the data from the inputs, maybe can merge the boolean with it to create a full array.
    //5th value is an integer, all others are floats
    public static Float[]channelOneArray= {0.0f,0.0f,1f,1.0f,1f};
    public static Float[]channelTwoArray= {0.0f,0.0f,1f,1.0f,1f};
    public static Float[]channelThreeArray= {0.0f,0.0f,1f,1.0f,1f};
    public static Float[]channelFourArray= {0.0f,0.0f,1f,1.0f,1f};
    public static int[]channelOneIntArray={1,1,1,1,1,1,1};
    //preceding flag safe for use in between threads
    private UsbManager mUsbManager;
    static Viewport options;
    static Viewport optionsdac;
    static Viewport optionsdata;
    //part of a test of using intents to pass data between activities and fragments
    public final static String desiredOutputTest="settings_page_one_test";
    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.mycompany.USB_PERMISSION";
    public final static String ACTION_TEST="com.mycompany.ACTION_TEST";
    public final static String ACTION_GRAPH="com.mycompany.ACTION_GRAPH";
    final static String DATA_EXTRA = "com.mycompany.intent.extra.DATA";
    public static UsbDevice sDevice = null;
    static PointsGraphSeries<DataPoint> series= new PointsGraphSeries<>();
    static PointsGraphSeries<DataPoint> seriesDAC= new PointsGraphSeries<>();

    static PointsGraphSeries<DataPoint> seriesDATA= new PointsGraphSeries<>();
    public void ActivityOneLaunch(View view){
        Intent Activityonelaunchintent= new Intent(this, SettingsPageOne.class);
        Log.d(logTag, "before launch");
        startActivityForResult(Activityonelaunchintent, 1);
    }
    public void stateMachineLauncher(){
        Thread MyThread= new Thread(Enum_Cases);
        MyThread.start();
    }
    public Integer sm_Update(Integer num){
        if(num==0){
            num=1;
            changeStates(States.Idle);
	    

        }
        else if(num==1){
            //current state is idle, need to see if we should change to setup
            if(mUsbManager.getDeviceList().values().iterator().hasNext()){
                num=2;
                changeStates(States.Setup);
            }
            else{
                num=1;
            }
        }
        else if(num==2){
             if(mUsbManager.getDeviceList().values().iterator().hasNext()){
                 if(!commLoopFlag.get())
                 {
                     commLoopFlag.compareAndSet(false,true);
                     Log.d(logTag, "starting comm loop");
                 }
                 if(startTransfer && setUpCompleteTrigger.get()){
                     Log.d(logTag,"About to test for msgSend");
                     if(string_id.equals("aMEASURE V2")) {
                         //Need to reset this to streamset to set the command data
                         //this will eventually send all data for all streams, where the data will be translated before data trasmittal is commenced
                         if(channelOneOpen){Log.d(logTag,"ch1 settings opened");
                            msgSend("setch1");
                         }
                         if(channelTwoOpen){
                             Log.d(logTag,"ch2 settings opened");
                             msgSend("setch2");
                         }
                         SystemClock.sleep(3000);
                         num = 3;
                         Log.d(logTag, "entering stream state");
                         changeStates(States.Stream);
                     }
                     else{
                         Log.d(logTag,"About to trigger msgSend");
                         msgSend("*idn?");
                     }
                 }
                 else{
                     num=2;
                     Log.d(logTag,"streaming could not progress");
                 }
             }
            else{
                 num=1;
                 changeStates(States.Idle);
             }
        }
        else if(num==3){
            if(!startTransfer||thread_safe_flag.get()||!mUsbManager.getDeviceList().values().iterator().hasNext()){
                Log.d(logTag,"stopping stream");
                msgSend("stop");
                startTransfer=false;
            thread_safe_flag.compareAndSet(true,false);
            Intent resetIntent=new Intent();
            resetIntent.setAction(ACTION_TEST);
            resetIntent.putExtra("message received", "Transfer reset");
            sendBroadcast(resetIntent);
                num=2;
                changeStates(States.Setup);
            }
        }
        return num;
    };
    Runnable Enum_Cases = new Runnable() {

        @Override
        public void run() {
            Integer currentState=0;
            while(true) {
                //need to set a variable
                //1 for idle, 2 for setup, 3 for stream.
                SystemClock.sleep(3000);
                currentState= sm_Update(currentState);
            }
        }
    };
    public UsbDevice enumerate(){
        usbm = (UsbManager) getSystemService(USB_SERVICE);
        HashMap<String, UsbDevice> devlist = usbm.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator();
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);

        while (deviter.hasNext()) {
            sDevice = deviter.next();
            Log.d(logTag, sDevice.getDeviceName() + "\n");

                if (!usbm.hasPermission(sDevice)){
                    usbm.requestPermission(sDevice, pi);}
                else {
                }
                break;

        }
        initDevice();
        return sDevice;
    }
    public void showDevices() {
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            sDevice = deviceIterator.next();
            //mUsbManager.requestPermission(sDevice, mPermissionIntent);
            //fairly sure preceding permissionrequest was redundant
            Log.d(logTag,"devices shown below");
            Log.d(logTag,sDevice.getDeviceName() + "\n");
            Log.d(logTag,sDevice.getDeviceId() + "\n");
            Log.d(logTag,sDevice.getDeviceProtocol() + "\n");
            Log.d(logTag,"Product ID: "+ sDevice.getProductId() + "\n");
            Log.d(logTag,"Vendor ID: "+sDevice.getVendorId() + "\n");

        }
    }

    private void usbConnection() {
        //IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        //registerReceiver(mUsbAttachReceiver, filter);
        //filter = new IntentFilter(UsbManager.ACTION_USB_DEVICE_DETACHED);
        //registerReceiver(mUsbDetachReceiver, filter);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
       // filter = new IntentFilter(ACTION_USB_PERMISSION);
       // registerReceiver(mUsbReceiver, filter);
        //showDevices();
    }

    public void Buttonresponse() {

        btnDiscover = (Button) findViewById(R.id.btnDiscover);
        txtInfo = (TextView) findViewById(R.id.txtInfo);
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            //We want it to be able to handle resuming transfer after it being paused, first press
            public void onClick(View v) {
                if (startTransfer == false) {
                    //So here if transfer has not yet started, nor stopped, it will start it
                    if (channelOneOpen||channelTwoOpen){
                    txtInfo.setText("Transfer Started");
                    thread_safe_flag.compareAndSet(true, false);
                    startTransfer = true;
                    txtInfo.setText("transfer started");}
                    else
                    {
                        txtInfo.setText("Channel is currently disabled");
                    }
                } else if (startTransfer && !thread_safe_flag.get()) {
                    //here if transfer has started but not yet stopped this will display
                    txtInfo.setText("Transfer has already started" + "\n");
                }
                //we need one more for when transfer has been resumed, after that last one
                //Now we need an if for when the transfer has stopped, but was allowed to start (no more manipulation of startTransfer we just use the atomicboolean from this point on
                else if (startTransfer && thread_safe_flag.get()) {
                    if (channelOneOpen||channelTwoOpen) {
                        txtInfo.setText("Transfer resumed" + "\n");
                        thread_safe_flag.compareAndSet(true, false);
                        //rerun msgSend for the loop stopping, change flag
                    }
                    else {
                        txtInfo.setText("Channel is currently disabled");
                    }
                }
            }
        });
    }
    public void Stopresponse(){
        btnStop=(Button) findViewById(R.id.buttonStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!thread_safe_flag.get()) {
                        thread_safe_flag.compareAndSet(false, true);
                        txtInfo.setText("Transfer halted" + "\n");

                } else if (thread_safe_flag.get()) {
                    txtInfo.setText("Transfer has already halted" + "\n");
                }
            }
        });


    }
    public byte[] int_to_byte(int[] integer){
        //use the integer array and cast each integer to 4 bytes
        byte[] return_value= new byte[integer.length+6];
        return_value[0]='C';
        return_value[1]='V';
        return_value[2]='\r';
        return_value[3]='\n';
        return_value[integer.length+4]='\r';
        return_value[integer.length+5]='\n';

        for (int m=0;m<(integer.length);m++){
            //just add 3 to the result
            return_value[m+4]=(byte)integer[m];


        }
        return return_value;
    };
    public byte[] twosComplementConversion(byte[] input){
        //Takes a byte input, sees if flip needed, also need to add 1
        // if first bit is zero, no flip, otherwise, flip and add 1
        byte[] output= new byte[input.length];

        for (int i=0; i<input.length;i++){
            if( (input[i]>>7&1)==0 ){
                output[i]=input[i];
            }
            else if ( (input[i]>>7&1)==1 ){
                output[i]=( (byte) ~input[i]);
                output[i]=addCarryFunction(output[i]);
            }
        }
        return output;
    };
    public byte addCarryFunction(byte input){
        byte output= input;
        boolean carry=true;
        for(int position=0;position<8;position++)
        {//if the digit is a 1, adding 1 makes zero, carry continues to next.
            //If the number is too big for 2's complement we will just default the whole value to zero at the end with an exception
            if (((output>>position) & 1)==1 && carry){
                carry=true;
                //need to set value of position to carry now
                output=(byte)(output & ~(1<<position));
        }
            else if (((output>>position) & 1)==0 && carry){
                carry=false;
                output=(byte)(output|(1<<position));
            }
        }
        return output;
    };
    protected byte[] cv_to_byte(long[] channel_settings){

        byte[] configured_settings= new byte[16];
        configured_settings[0]='C';
        configured_settings[1]='V';
        configured_settings[2]='\r';
        configured_settings[3]='\n';
        //we need the 10 data byes, along with CV + "\r\n" before it, and \r\n after the data as well
        //so now we have the 10 data bytes
        //first byte is the channel number, which is the first int of param
        configured_settings[4]=(byte)(channel_settings[0]);
        //then we have gain, also one byte (0-3 int)
        configured_settings[5]=(byte)(channel_settings[1]);
        //then we have the multi byte chunks, a bit more difficult
        //first init, then high, then low
        configured_settings[6]=(byte)(channel_settings[2]>>8 & 0xFF);
        configured_settings[7]=(byte)((channel_settings[2]) & 0xFF);

        Log.d(logTag,"init e msb byte translation"+String.valueOf(configured_settings[6]));
        System.out.println(Integer.toBinaryString(configured_settings[6] & 255 | 256).substring(1));
        Log.d(logTag,"init e lsb byte translation"+String.valueOf(configured_settings[7]));
        System.out.println(Integer.toBinaryString(configured_settings[7] & 255 | 256).substring(1));
        configured_settings[8]=(byte)(channel_settings[3]>>8 & 0xFF);
        configured_settings[9]=(byte)((channel_settings[3]) &0xFF);
        Log.d(logTag,"high e msb byte translation"+String.valueOf(configured_settings[6]));
        System.out.println(Integer.toBinaryString(configured_settings[8] & 255 | 256).substring(1));
        Log.d(logTag,"high e lsb byte translation"+String.valueOf(configured_settings[7]));
        System.out.println(Integer.toBinaryString(configured_settings[9] & 255 | 256).substring(1));
        configured_settings[10]=(byte)(channel_settings[4]>>8 & 0xFF);
        configured_settings[11]=(byte)((channel_settings[4]) &0xFF);
        // then scan rate, which needs a similar format to be transmitted correctly
        configured_settings[12]= (byte)(channel_settings[5]>>8 & 0xFF);
        configured_settings[13]= (byte)((channel_settings[5]) &0xFF);
        configured_settings[14]='\r';
        configured_settings[15]='\n';
        //
        return configured_settings;
    }
    public void msgSend(final String str) {
        class mLoop implements Runnable {
            String string=str;
            mLoop(String s) {  string = s; }
            public void run() {
                UsbDevice dev = sDevice;
                if (dev == null){
                    startTransfer=false;
                    return;}
                if (!thread_safe_flag.get()){
                    String string_used=str;
                    byte[] byte_example=new byte[0];
                    byte[] byte_example_send= new byte[10];
                    int length=0;
                    if(string_used.equals("setch1"))
                    {
                        long[] byte_int = configCVPacket(channelOneArray, 0);
                        //cant use int to byte here, because some ints need multiple bytes
                        byte_example = cv_to_byte(byte_int);
                        length = byte_example.length;
                        byte_example_send=byte_example;
                        //The output is configured, need to translate the input


                    }
                    else if(string_used.equals("setch2"))
                    {
                        long[] byte_int = configCVPacket(channelTwoArray, 1);
                        //cant use int to byte here, because some ints need multiple bytes
                        byte_example = cv_to_byte(byte_int);
                        length = byte_example.length;
                        byte_example_send=byte_example;
                        //The output is configured, need to translate the input


                    }
                    else if (string_used.equals("stop"))
                    {
                        String stop="STOP";
                        byte_example=stop.getBytes(StandardCharsets.UTF_8);
                        length=byte_example.length;
                        byte_example_send = new byte[length+2];
                        //Need to add '/r' and '/n' to the end of the byte array
                        for (int i=0;i<length;i++)
                        {
                            byte_example_send[i]=byte_example[i];
                        }
                        byte_example_send[4]='\r';
                        byte_example_send[5]='\n';
                        length=byte_example_send.length;
                    }
                    else if (string_used.equals("*idn?"))
                    {
                        String id="*idn?";
                        byte_example=id.getBytes(StandardCharsets.UTF_8);

                        length=byte_example.length;
                        byte_example_send = new byte[length+2];
                        //Need to add '/r' and '/n' to the end of the byte array
                        for (int i=0;i<length;i++)
                        {
                            byte_example_send[i]=byte_example[i];
                        }
                        byte_example_send[5]='\r';
                        byte_example_send[6]='\n';
                        length=byte_example_send.length;


                    }
                    else if (string_used.equals("STREAM"))
                    {
                        String id="STREAM";
                        byte_example=id.getBytes(StandardCharsets.UTF_8);

                        if(channelOneOpen && channelTwoOpen) {

                            byte_example_send = new byte[length + 2+5];
                            length = byte_example.length;
                            //Need to add '/r' and '/n' to the end of the byte array
                            for (int i = 0; i < length; i++) {
                                byte_example_send[i] = byte_example[i];
                            }
                            byte_example_send[6] = '\r';
                            byte_example_send[7] = '\n';
                            byte_example_send[8] = 'A';
                            byte_example_send[9] = 'L';
                            byte_example_send[10] = 'L';
                            byte_example_send[11] = '\r';
                            byte_example_send[12] = '\n';
                            length = byte_example_send.length;
                        }
                        else if(channelOneOpen) {
                            length = byte_example.length;
                            byte_example_send = new byte[length + 2+5];
                            //Need to add '/r' and '/n' to the end of the byte array
                            for (int i = 0; i < length; i++) {
                                byte_example_send[i] = byte_example[i];
                            }
                            byte_example_send[6] = '\r';
                            byte_example_send[7] = '\n';
                            byte_example_send[8] = 'c';
                            byte_example_send[9] = 'h';
                            byte_example_send[10] = '0';
                            byte_example_send[11] = '\r';
                            byte_example_send[12] = '\n';
                            length = byte_example_send.length;
                        }
                        else if(channelTwoOpen) {
                            length = byte_example.length;
                            byte_example_send = new byte[length + 2+5];
                            //Need to add '/r' and '/n' to the end of the byte array
                            for (int i = 0; i < length; i++) {
                                byte_example_send[i] = byte_example[i];
                            }
                            byte_example_send[6] = '\r';
                            byte_example_send[7] = '\n';
                            byte_example_send[8] = 'c';
                            byte_example_send[9] = 'h';
                            byte_example_send[10] = '1';
                            byte_example_send[11] = '\r';
                            byte_example_send[12] = '\n';
                            length = byte_example_send.length;
                        }
                    }
                    //breaks communication once stop transfer is switch from false/true
                        int ans_out = conn.bulkTransfer(epOUT, byte_example_send, 0,length, 0);
                    if(ans_out>0)
                    {
                        Log.d(logTag,"bytes transferred out: "+ ans_out);
                    }
                    else{
                        Log.d(logTag,"failure to transfer bytes out");
                    }

                }
                Log.d(logTag,"Thread halted");
            }
        }
        Thread t = new Thread(new mLoop(str));
        t.start();
        return;
    }
    public void prepAndSendData(){
       // Log.d(logTag,"called Prep and Send data");
    //wont be called unless the correct amount of data (at least one packet) is present in the dataBuffer
        byte[] currentBuffer=dataBuffer;
        byte[] desiredChunk;
        boolean check1=false;
        boolean check2=false;
        int position1=0;
        int position2=0;
        //now search through until find a stop point
        for(int i=0;i<currentBuffer.length;i++){
            if(currentBuffer[i]=='A' && i>2){
                if(currentBuffer[i-1]=='T'&&currentBuffer[i-2]=='A'&&currentBuffer[i-3]=='D'&&!check1){
                    check1=true;
                    position1=i;
                   // Log.d(logTag,"Check one (data) passed");
                   // Log.d(logTag,String.valueOf(i)+ " value of position");
                    //found DATA, next look for DAC
                }
            }
            else if(currentBuffer[i]=='C'&&check1){
                if(currentBuffer[i-1]=='A'&&currentBuffer[i-2]=='D'){
                    position2=i;
                    check2=true;
                    //Log.d(logTag,"Check 2 passed, position: "+ String.valueOf(i));
                    for(int j=i+1;j<currentBuffer.length;j++){
                        if(currentBuffer[j]=='D'&&check1&&check2){
                            //start of next array here, we want to break off part of dataBuffer here
                            //dataBuffer
                           // Log.d(logTag, "d for next data found at position: "+String.valueOf(j));
                            desiredChunk=new byte[j];
                            //now we need to use this, fill it with data from the ByteBuffer
                            //j bytes to put into it, starting at position 0
                            for(int k=0;k<desiredChunk.length;k++){
                                desiredChunk[k]=dataBuffer[k];
                            }
                            //the above part works, for the msot part. Only issue is reading even if there
                            //are not two data bytes after the DAC byte.

                        //the premise in this part is we need to read from the databuffer at position
                            //where the next 'D" starts untill the endof the buffer, the part before that D
                            //is cut out.
                            byte[] newDataBuffer=new byte[currentBuffer.length-desiredChunk.length];
                            //Log.d(logTag,"Size of dataBuffer,newDataBuffer, and desiredChunk in that order: "+ " "+
                            //        String.valueOf(dataBuffer.length)+" " + String.valueOf(newDataBuffer.length) +
                           //      " "+  String.valueOf(desiredChunk.length));
                            long size=currentBuffer.length-desiredChunk.length;
                           // Log.d(logTag,"size for iterations: " + String.valueOf(size));
                            for(int m=0;m<(size);m++){
                                newDataBuffer[m]=currentBuffer[m+j];
                            };
                            dataBuffer=newDataBuffer;
                          //  byte[] print_byte=dataBuffer;
                         //   Log.d(logTag,"Remaining bytes in dataBuffer: "+String.valueOf(dataBuffer.length));
                         //   for(byte b:dataBuffer){
                         //       Log.d(logTag,String.valueOf(b));
                        //    }
                            Intent intent = new Intent(ACTION_GRAPH);
                            //run some function now to parse the data, and then deliver what it returns
                            //send this whole byte array to intent receiver, let it do the rest
                            //of the work
                            //need to send the proper chunk of dataBuffer
                           // Log.d(logTag,"Data being sent to graph function: "+ new String(desiredChunk, StandardCharsets.UTF_8));
                          //  Log.d(logTag,"length: "+String.valueOf(desiredChunk.length));
                            intent.putExtra("data points", desiredChunk);
                            sendBroadcast(intent);
                            return;

                        }
                       // Log.d(logTag, "test passed, calling graph add");
                      //  desiredChunk=currentBuffer;
                        //now we need to use this, fill it with data from the ByteBuffer
                        //j bytes to put into it, starting at position 0
                      //  dataBuffer=ByteBuffer.wrap(new byte[0]);
                      //  Log.d(logTag,"Remaining bytes in dataBuffer: "+String.valueOf(dataBuffer.array().length));
                      //  Intent intent = new Intent(ACTION_GRAPH);
                        //run some function now to parse the data, and then deliver what it returns
                        //send this whole byte array to intent receiver, let it do the rest
                        //of the work
                        //need to send the proper chunk of dataBuffer
                      //  Log.d(logTag,"Data being sent to graph function: "+ new String(desiredChunk, StandardCharsets.UTF_8));
                      //  Log.d(logTag,"length: "+String.valueOf(desiredChunk.length));
                       // intent.putExtra("data points", desiredChunk);
                      //  sendBroadcast(intent);
                      //  return;
                        //look for start of next array, by the time DAC is seen then data should
                        //have already been seen so we can stop the loop here no point in running it more

                        //if end of this loop is reached, it means that there is exactly 1 data point inside the dataBuffer
                        //this is fine, just set dataBuffer to 0 and use the contents as data to transmit
                    }
                }
            }
        }
        //at the end calls graphing with a simplified format
    }
    public boolean parseData(byte[] data){
        if(dataBuffer.length==0){
        dataBuffer=data;
            if(dataBuffer.length<29) {
                return false;
                //bool is for whether or not it is safe to call graph add

            }
            else{
                return true;
            }
        }
        else {
            byte[] temp=dataBuffer;
            byte[] finalTemp=new byte[temp.length+data.length];
            for (int i = 0; i <temp.length+data.length;i++){
                if(i<temp.length){
                    //first we fill the array by putting in original stuff, new stuff added on to back.
                    //we need to read from the front though in the graph method
                    finalTemp[i]=temp[i];
                }
                else if(i>=temp.length){
                    //now we add the new data, need to account for the shift
                    finalTemp[i]=data[i-temp.length];
                }
            }
            dataBuffer=finalTemp;
            if(dataBuffer.length<29) {
                return false;
                //bool is for whether or not it is safe to call graph add

            }
            else{
                return true;
            }
        }
//may replace previous section, however it is seriously flawed based on the format i am receiveing
        //data in, it will only work on packets sliced at the correct sections and even then, it might construe
        //data where there is none
      //  int non_returnvals=0;
      //  for(byte b:data){
       //     if(b!='\r' && b!='\n'){
     //           non_returnvals++;
        //    }

     //   }
     //   byte[] results=new byte[non_returnvals];
       // int count=0;
       // for(int i=0;i<data.length;i++){
       //     if(data[i]!='\r' && data[i]!='\n'){
        //        results[count]=data[i];
       //         count++;
      //      }
     //   }
      //  byte[] data_result=new byte[4];
        //now we have a byte with the format "00DAC00DATA01"
        //however the bytes may be rotated somewhere along this line
        //to account for this, we have to use wrapping so to speak
       // try {
         //   while(results[2]!='T'){
         //       byte[]temp=new byte[results.length];
           //     for (int i=0;i<results.length;i++){
            //        if(i<results.length-1){
             //           temp[i]=results[i+1];
             //       }
             //       else{
              //          temp[i]=results[0];
              //      }
        //        }
         //       results=temp;
       //     }
     //       Log.d(logTag,"Updated string: "+new String(results,StandardCharsets.UTF_8));
            //may take out below portion
     //       data_result[0]=results[4];
     //       data_result[1]=results[5];
            //data values
            //now for dac values
     //       data_result[2]=results[11];
     //       data_result[3]=results[12];
     //   }
     //   catch(IndexOutOfBoundsException ex){
      //     return new byte[1];
      //  }
      //  return data_result;
    }

    private void initDevice(){
        UsbDevice dev = sDevice;
        if (dev == null){
            Log.d(logTag,"Device Was Null");
            startTransfer=false;
            return;}
        usbm = (UsbManager) getSystemService(USB_SERVICE);
        conn = usbm.openDevice(dev);
        inf_device = dev.getInterface(1);
        Log.d(logTag, "Interface count:" + dev.getInterfaceCount() + "\n");
        Log.d(logTag, "Insert device specifications here" + "\n");
        try {
            if (!conn.claimInterface(inf_device, true)) {
                startTransfer=false;
                return;
            }
        }
        catch(ArrayIndexOutOfBoundsException errorName) {
            startTransfer=false;
            //conn.releaseInterface(dev.getInterface(1));
            return;
        }
        conn.claimInterface(inf_device, true);
        conn.controlTransfer(0x21, 0x22, 0x1, 0, null, 0, 0);
        //magic line above, makes the communication work
       // conn.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
        //conn.controlTransfer(0x21, 32, 0, 0, new byte[] { (byte) 0x80,
           //     0x25, 0x00, 0x00, 0x00, 0x00, 0x08 }, 7, 0);
        Log.d(logTag,"Endpoint count: "+Integer.toString(inf_device.getEndpointCount()));
        for (int i = 0; i < inf_device.getEndpointCount(); i++) {

            Log.d(logTag,String.format("0x%02X", inf_device.getEndpoint(i).getAddress())+"\n");
            if(inf_device.getEndpoint(i).getType()==UsbConstants.USB_ENDPOINT_XFER_BULK) {
                Log.d(logTag,"bulk transfer endpoint found");
                if (inf_device.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN) {
                    epIN = inf_device.getEndpoint(i);
                    Log.d(logTag, "epIN found");
                } else if (inf_device.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_OUT) {

                    epOUT = inf_device.getEndpoint(i);
                    Log.d(logTag, "epOUT found");
                }
            }
        }
        setUpCompleteTrigger.compareAndSet(false,true);
        uiHandler.postDelayed(runnable, 100);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mainloop(sDevice);
            uiHandler.postDelayed(this, 0);                    //Start runnable again after 100ms
        }
    };
    public void mainloop(UsbDevice sDevice){
        if (conn!=null && epIN!=null) {
            if(startTransfer) {
                rwlock.writeLock().lock();
                byte[] inBuffer = new byte[500];
                int readableBytes=0;
                // Log.d(logTag, "calling bulkTransfer() in");
                int len = conn.bulkTransfer(epIN, inBuffer, 40/*inBuffer.length*/, 100);
                //constrain the scope so it makes sense is our best bet
                //each run through here we pull one set of values out, run it through, delete segment
                //Log.d(logTag, "complete loop.");
                if (len > 0) {
                   // Log.i(logTag, "data read!");
                    for(byte b: inBuffer) {
                        if(b!=inBuffer[499]) {
                      //      System.out.println(Integer.toBinaryString(b & 255 | 256).substring(1));
                           readableBytes++;
                        }
                        }
                    byte[] bufferstr=new byte[readableBytes];
                    for (int i=0;i<readableBytes;i++){
                    bufferstr[i]=inBuffer[i];

                    }
                   String responseMicroduino=new String(bufferstr,StandardCharsets.UTF_8);
                    int non_returnvals=0;
                    for(byte b:bufferstr){
                        if(b!='\r' && b!='\n'){
                            non_returnvals++;
                        }

                    }
                    byte[] results=new byte[non_returnvals];
                    int count=0;
                    for(int i=0;i<bufferstr.length;i++){
                        if(bufferstr[i]!='\r' && bufferstr[i]!='\n'){
                            results[count]=bufferstr[i];
                            count++;
                        }
                    }
                   // Log.d(logTag,"Found data: "+new String(results,StandardCharsets.UTF_8));

                   // Log.d(logTag,String.valueOf("length: " + bufferstr.length));
                    if(bufferstr.length>10){
                            if(responseMicroduino.substring(0,11).equals("aMEASURE V2")){
                            Log.d(logTag, "id received");
                            Intent intent = new Intent(ACTION_TEST);
                            string_id = responseMicroduino.substring(0, 11);
                            intent.putExtra("message received", responseMicroduino.substring(0, 11));
                                sendBroadcast(intent);
                        }
                    }
                    if(bufferstr.length>2) {
                        if (bufferstr[0]=='A'&&bufferstr[1]=='C' && bufferstr[2]=='K') {
                            Log.d(logTag, "ACK packet received");
                        }
                        else if(bufferstr.length>=7 &&bufferstr.length<40 && !responseMicroduino.substring(0,2).equals("aM")){

                            //Log.d(logTag, "Data found");
                           // byte[] data_dac=parseData(bufferstr);
                            //now what we need to do is parse data will add this buffer onto the END
                            //of the current buffer THEN IN THE GRAPH SECTION HANDLE PLACEMENT
                            if(parseData(bufferstr)) {
                                //test case, will use length of 1 if the packet is not valid
                                prepAndSendData();
                                //moved the data sending method to its own fnction for conciseness
                            }
                        }
                    }
                    //for(byte b: results){
                    //    Log.d(logTag,String.valueOf(b));
                   // }
                    //some comparison, only want to send it to the graph intent if its actually
                    //graph data
                    //reenable once i have catches for non graph data
                }
                else {
                    // Log.i(logTag, "zero data read!");
                }
                rwlock.writeLock().unlock();
            }
        }
        //startTransfer=false;
        //mStopped=true;
        //commLoopFlag.compareAndSet(true, false);
        //setUpCompleteTrigger.compareAndSet(true, false);
        //conn.releaseInterface(dev.getInterface(1));
    }


}








