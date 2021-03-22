package com.sanhui.ui.serial;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;


import com.firefly.api.serialport.SerialPort;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import com.sanhui.ui.MainActivity;
import com.sanhui.ui.tts.TTSUtils;
import com.sanhui.ui.websocket.WebSocket;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class Serial  {

    public TextView Licensecode;
    public TextView Weightbriddge;
    public TextView data;
    public EditText deviceid;

    private SerialPort serialPort = null;
    private UsbSerialPort usbSerialPort;
    private UsbSerialPort usbSerialPort1;
    private SetSerialData msetSerialData = new SetSerialData();
    private WebSocket webSocket;
    private TTSUtils tts;

    private boolean plateok;
    private String result = null;
    private String hexresult = null;
    String license;
    String getweight;


    //获取控件
    public void getactivity(TextView mLicensecode, TextView mWeightbriddge, TextView mdata, EditText mdeviceid, WebSocket webSocketUtil) {
        usbSerialPort = MainActivity.mcontext.getUsbSerial();
        usbSerialPort1 = MainActivity.mcontext.getUsbSerial1();
        //初始化声音模块
        tts=new TTSUtils(MainActivity.mcontext);
//        tts.initData("初始化完毕");
        deviceid = mdeviceid;
        Licensecode = mLicensecode;
        Weightbriddge = mWeightbriddge;
        data = mdata;

        webSocket = webSocketUtil;
    }




    //打开串口
    public boolean OpenSerial(String path, int baudrate) {

        try {

            serialPort = new SerialPort(new File(path), baudrate, 0);

            //串口回调函数
            serialPort.setCallback(this::onDataReceived);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }




    //接收串口回传数据
    public void onDataReceived(byte[] bytes, int i) {

        hexresult = byteToStr(bytes, i);
        try {
            result = new String(bytes, 0, i, "GB2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(hexresult);
        System.out.println(result);

        msetSerialData.getweight(result);
        //判断是否为车牌数据
        if (result.length() == 7 && hexresult.length() == 16) {

            plateok = msetSerialData.isChineseChar(result.charAt(0));
            if (plateok) {
                license = result;
//                tts.initData(license);


                //车牌在显示屏显示
                try {
                    usbSerialPort.write(msetSerialData.licensebyte(hexresult), msetSerialData.licensebyte(hexresult).length);
                    usbSerialPort.write(msetSerialData.licensebyte(hexresult), msetSerialData.licensebyte(hexresult).length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //车牌发送终端显示
                Licensecode.post(new Runnable() {
                    @Override
                    public void run() {
                        Licensecode.setText(license);
                    }
                });



                //继电器信号
                relay(true);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                relay(false);

            }

         //判断是否为地磅
        } else if (result.length() == 17) {
            getweight = msetSerialData.setport;
            if (!getweight.equals("")) {

                //地磅在服务器显示
                try {
                    usbSerialPort.write(msetSerialData.weightbyte(getweight), msetSerialData.weightbyte(getweight).length);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //地磅在终端显示
                Weightbriddge.post(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        Weightbriddge.setText(getweight + "kg");
                    }
                });

            }

            //整理数据发送服务器
        }if (plateok) {
            if (msetSerialData.iweightdata1 != 0) {
                //时间戳
                long date = System.currentTimeMillis() / 1000;
                String printfdata = "{\"cid\":\"" + deviceid.getText() + "\",\"plate\":\"" + license + "\",\"weigh\":\"" + msetSerialData.iweightdata1 + "\",\"weighing_time\":\"" + date + "\"}";

                Log.d("printfdataok", printfdata);

                //发送服务器
                webSocket.sendSocketMsg(printfdata);


                license = null;
                msetSerialData.iweightdata1 = 0;
                plateok = false;
            }

        }

    }

    //字节转16进制字符串
    private static String byteToStr(byte[] b, int size) {
        String ret = "";
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }


    //继电器开关
    private void relay(boolean in) {
        String[] studyCodeStrArr = "0XA0 0X01 0X01 0XA2 ".replace("X", "x").split(" ");
        String[] studyCodeStrArr1 = "0XA0 0X01 0X00 0XA1".replace("X", "x").split(" ");
        if (in) {
            studyCodeStrArr1 = studyCodeStrArr;
        }
        try {
            sendToUsb(studyCodeStrArr1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //发送继电器指令
    private void sendToUsb(String[] hexString) throws Exception {
        byte[] bytes = new byte[hexString.length];

        for (int i = 0; i < hexString.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexString[i].substring(2), 16);
        }
        usbSerialPort1.write(bytes, bytes.length);
    }
}
