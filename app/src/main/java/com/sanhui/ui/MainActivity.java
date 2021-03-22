package com.sanhui.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firefly.api.FireflyApi;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.sanhui.ui.serial.Serial;
import com.sanhui.ui.websocket.WebSocket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements Thread.UncaughtExceptionHandler{

    private static final long HEART_BEAT_RATE =1000 ;
    EditText mSerialPath0;
    EditText mSerialPath1;
    EditText mSerialBaudrate0;
    EditText mSerialBaudrate1;

    EditText mserveraddress;
    EditText mdeviceid;
    Button login;

    TextView mLicensecode;
    TextView mWeightbriddge;
    TextView mHostdata;
    TextView mdata;

    private LineLoadingView lineLoadingView0;
    private LineLoadingView lineLoadingView1;
    private LineLoadingView lineLoadingView2;
    public static MainActivity mcontext;
    private SharedPreferences sharedPreferences;

    private Handler mHandler = new Handler();
    private WebSocket websocket=new WebSocket();
    private FireflyApi fireflyApi=new FireflyApi();
    private Serial mserial=new Serial();
    private UsbSerialPort usbSerialPort;
    private UsbSerialPort usbSerialPort1;

    private BroadcastReceiver receiver =new IntentReceiver();

    private boolean onclike=false;
    private boolean open=false;
    private  boolean isconnect=false;

    InputMethodManager imm;

    public static String serverURL = "ws://192.168.1.142:8020";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter=new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        this.registerReceiver(receiver,filter);

        mcontext = this;
        AudioManager audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        View decorView = mcontext.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(uiOptions);


        /////////////////////////////////////////////////////////
        mSerialPath0 = (EditText) findViewById(R.id.serial485);
        mSerialPath1 = (EditText) findViewById(R.id.serial232);
        mSerialBaudrate0 = (EditText) findViewById(R.id.serial485_baudrate);
        mSerialBaudrate1 = (EditText) findViewById(R.id.serial232_baudrate);
        //////////////////////////////////////////////////////////
        mserveraddress = (EditText) findViewById(R.id.server_address);
        mdeviceid = (EditText) findViewById(R.id.device_id);
        login=findViewById(R.id.login);
        //////////////////////////////////////////////////////
        mserveraddress = (EditText) findViewById(R.id.server_address);
        mdeviceid = (EditText) findViewById(R.id.device_id);
        mLicensecode = (TextView) findViewById(R.id.licensedata);
        mWeightbriddge = (TextView) findViewById(R.id.weightbriddge);
        mHostdata = (TextView) findViewById(R.id.hostdata);
        mdata = (TextView) findViewById(R.id.data);
        //////////////////////////////////////////////////////
        lineLoadingView0 = findViewById(R.id.music_loading0);
//        lineLoadingView1.startAnim();
        lineLoadingView1 = findViewById(R.id.music_loading1);
//        lineLoadingView2.startAnim();
        lineLoadingView2 = findViewById(R.id.music_loading2);

        sharedPreferences=getSharedPreferences("get",MODE_PRIVATE);

        initPermission();


        //开启看门狗
        boolean t= fireflyApi.watchDogEnable(true);
        Log.i("watchDogEnable",t+"");
//        initPermission();
        //初始化
        try {
            Init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //心跳包
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);

        //断网主动重启软件
        Thread.setDefaultUncaughtExceptionHandler(this);


    }



    public void Init() throws InterruptedException{


        mdeviceid.setText(sharedPreferences.getString("mdeviceid","SN001"));
        mserveraddress.setText(sharedPreferences.getString("mserveraddress",serverURL));



        editviewabale(false);

        initusb();

        Thread.sleep(500);


        websocket.connectWebSocket(serverURL + "/websocket/" + mdeviceid.getText().toString(), mHostdata, mcontext);
        lineLoadingView2.startAnim();



        Thread.sleep(500);


        mserial.getactivity(mLicensecode, mWeightbriddge, mdata, mdeviceid, websocket);



        open = mserial.OpenSerial(mSerialPath0.getText().toString(), Integer.valueOf(mSerialBaudrate0.getText().toString()).intValue());
        if (open) {
            lineLoadingView0.startAnim();
            open = false;
        } else {
            lineLoadingView0.stopAnim();
        }



        Thread.sleep(500);

        open = mserial.OpenSerial(mSerialPath1.getText().toString(), Integer.valueOf(mSerialBaudrate1.getText().toString()).intValue());
        if (open) {
            lineLoadingView1.startAnim();
            open = false;
        } else {
            lineLoadingView1.stopAnim();
        }

    }

    //按钮
    public void login(View V) throws  InterruptedException {

        if(onclike){
            onclike=false;
            login.setText("参数设置");
            SharedPreferences.Editor editor=sharedPreferences.edit();
            editor.putString("mdeviceid",mdeviceid.getText().toString());
            editor.putString("mserveraddress",mserveraddress.getText().toString());
            editor.commit();
            String data= mdeviceid.getText().toString() +" " +mserveraddress.getText().toString();
            Log.e("file",data);
            websocket.closeConnectWebSocket();
            Init();
        }else{
            login.setText("保存参数");
            onclike=true;
            editviewabale(true);
        }

    }


    //发送心跳包
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
//            Log.e("JWebSocketClientService", "心跳包检测websocket连接状态");
            if (websocket.mWebSocketClient != null) {
                if (websocket.isClosedConnectWebSocket()) {
                    mHandler.removeCallbacks(heartBeatRunnable);
                    lineLoadingView2.stopAnim();
                    isconnect = true;

                    websocket.reconnectWs();
                } else {
                    //业务逻辑 这里如果服务端需要心跳包为了防止断开 需要不断发送消息给服务端
//                    mWebSocketUtil.sendSocketMsg("heartBeat");
                    websocket.mWebSocketClient.sendPing();
                    if (isconnect) {
                        lineLoadingView2.startAnim();
                        isconnect = false;
                    }
                }
            } else {
                unregisterReceiver(receiver);
                //如果client已为空，重新初始化连接
                websocket.mWebSocketClient = null;
                websocket.connectWebSocket(serverURL, mHostdata, mcontext);
            }
            //看门狗喂狗
           fireflyApi.watchDogFeed();

            timedata();

            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);

        }
    };


    //时间
    public void timedata(){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        mdata.setText(formatter.format(curDate));
    }


    /**
     * android 6.0 以上需要动态申请麦克风权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE // demo使用
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }





    public UsbSerialPort getUsbSerial() {
        return usbSerialPort;
    }
    public UsbSerialPort getUsbSerial1() {
        return usbSerialPort1;
    }
    UsbManager manager;
    UsbDevice device0;
    UsbDevice device1;
    //初始化usb
    private void initusb() {
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceMap = manager.getDeviceList();

        Iterator<UsbDevice> deviceIterator = deviceMap.values().iterator();

        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d("USB", String.valueOf(device));
//            Log.d("USB", device.getDeviceName());
//            Log.d("USB", device.getProductName());
//            Log.d("usb", "vendorID--" + device.getVendorId() + "ProductId--" + device.getProductId());
//            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
//                return device; // 获取USBDevice
//            }
            String sub2 = device.getProductName().substring(7);
//            UsbDevice[mName=/dev/bus/usb/003/002,mVendorId=6790,mProductId=29987,mClass=255,mSubclass=0,mProtocol=0,mManufacturerName=null,mProductName=USB2.0-Ser!,mVersion=1.16,mSerialNumber=null,mConfigurations=[
//            UsbConfiguration[mId=1,mName=null,mAttributes=128,mMaxPower=48,mInterfaces=[
//            UsbInterface[mId=0,mAlternateSetting=0,mName=null,mClass=255,mSubclass=1,mProtocol=2,mEndpoints=[
//            UsbEndpoint[mAddress=130,mAttributes=2,mMaxPacketSize=32,mInterval=0]
//            UsbEndpoint[mAddress=2,mAttributes=2,mMaxPacketSize=32,mInterval=0]
//            UsbEndpoint[mAddress=129,mAttributes=3,mMaxPacketSize=8,mInterval=1]]]]

//            UsbDevice[mName=/dev/bus/usb/005/003,mVendorId=6790,mProductId=29987,mClass=255,mSubclass=0,mProtocol=0,mManufacturerName=null,mProductName=USB2.0-Serial,mVersion=1.16,mSerialNumber=null,mConfigurations=[
//            UsbConfiguration[mId=1,mName=null,mAttributes=128,mMaxPower=49,mInterfaces=[
//            UsbInterface[mId=0,mAlternateSetting=0,mName=null,mClass=255,mSubclass=1,mProtocol=2,mEndpoints=[
//            UsbEndpoint[mAddress=130,mAttributes=2,mMaxPacketSize=32,mInterval=0]
//            UsbEndpoint[mAddress=2,mAttributes=2,mMaxPacketSize=32,mInterval=0]
//            UsbEndpoint[mAddress=129,mAttributes=3,mMaxPacketSize=8,mInterval=1]]]]



            //判断usb
            if(sub2.equals("Ser!")){

                device0 = deviceMap.get(device.getDeviceName());

            }else if(sub2.equals("Serial")){
                device1 = deviceMap.get(device.getDeviceName());

            }else{

                Log.d("error","device.getDeviceName().substring(15, 16)=" +sub2);

            }
            
        }


        //USB\VID_1A86&PID_7523&REV_0263
        //USB\VID_1A86&PID_7523&REV_0263-+
        //打开usb串口
        if(device0  != null) {

            UsbDeviceConnection connection = manager.openDevice(device0);
            UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device0);

            if (connection == null) {
                // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            } else {
//                List<UsbSerialPort>  list=driver.getPorts();
//                Log.d("list", String.valueOf(list));
////                Log.d("list", String.valueOf(list.size()));
                usbSerialPort = driver.getPorts().get(0);
                try {
//                    Log.d("USB", "ok");
                    usbSerialPort.open(connection);
                    usbSerialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                } catch (IOException e) {
                    // Deal with error.
                    Log.d("usb", "error");
                }
            }
            device0=null;
        }
        if(device1  != null) {
            UsbDeviceConnection connection = manager.openDevice(device1);
            UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device1);

            if(connection == null) {

                // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)

            } else {
                usbSerialPort1 = driver.getPorts().get(0);
                try {
                    usbSerialPort1.open(connection);
                    usbSerialPort1.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                } catch (IOException e) {
                    // Deal with error.
                    Log.d("usb", "error");
                }
            }
            device1=null;
        }
    }



    //修改edittext的编辑权限-
    public void editviewabale(boolean able) {
        if (able) {
            mSerialPath0.setEnabled(true);
            mSerialPath1.setEnabled(true);
            mSerialBaudrate0.setEnabled(true);
            mSerialBaudrate1.setEnabled(true);
            mserveraddress.setEnabled(true);
            mdeviceid.setEnabled(true);

        } else {
            mSerialPath0.setEnabled(false);
            mSerialPath1.setEnabled(false);
            mSerialBaudrate0.setEnabled(false);
            mSerialBaudrate1.setEnabled(false);
            mserveraddress.setEnabled(false);
            mdeviceid.setEnabled(false);
        }
    }





    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                Log.e("uncaughtException", "error : ", e);
        Intent intent = new Intent(this, MainActivity.class);


        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
//        System.out.println("uncaughtException");

        //e.printStackTrace();
        //System.exit(0);
        //杀死该应用进程
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}