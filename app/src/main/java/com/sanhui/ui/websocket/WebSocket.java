package com.sanhui.ui.websocket;


import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

import static com.sanhui.ui.MainActivity.serverURL;

public class WebSocket {
    private static final String TAG = "WebSocketUtil";

    public WebSocketClient mWebSocketClient=null;
    private Context mContext;

//    public String address=serverURL+"/socke0t";

    //连接socket服务器
    public void connectWebSocket(String address, TextView textView, Context context){
        this.mContext=context;

        try {
            initSocketClient(address,textView);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        new Thread(){
            @Override
            public void run() {

                    mWebSocketClient.connect();

            }
        }.start();
    }

    //初始化socket服务器
    private void initSocketClient(String address,TextView textView) throws URISyntaxException {

        if(mWebSocketClient==null){
            mWebSocketClient=new WebSocketClient(new URI(address)) {


                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    //判断是否连接
                    Log.d(TAG,"connection");
                }

                @Override
                public void onMessage(String message) {
                    //服务器下发的数据

                    Log.i(TAG,message);
                    textView.post(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(message);
                        }
                    });

                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.e(TAG,"error connection");
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG,"error1 connection");
                }
            };

        }

    }

    //断开连接
    public void closeConnectWebSocket() {
        try {
            if(mWebSocketClient!=null){
                mWebSocketClient.close();
                mWebSocketClient = null;
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            mWebSocketClient = null;
        }
    }

    //判断与服务器是否连接
    public Boolean isClosedConnectWebSocket(){
        boolean bool= mWebSocketClient.isClosed();
        return bool;
    }


    ////开启重连
    public void reconnectWs() {
        //mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "开启重连");
                    mWebSocketClient.reconnectBlocking();
                } catch (InterruptedException e) {
//                    Log.e(TAG, "error");
                    e.printStackTrace();
                }
            }
        }.start();
    }


    //给服务器发送消息
    public void sendSocketMsg(String msg) {
        if (mWebSocketClient!=null && mWebSocketClient.isOpen()){
            mWebSocketClient.send(msg); //通过mWebSocketClient发送数据
        }

    }









}
