package com.sanhui.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.sanhui.ui.websocket.WebSocket;


public class IntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        WebSocket webSocket =new WebSocket();
        NetworkInfo networkInfo=manager.getActiveNetworkInfo();

        if(networkInfo != null  && networkInfo.isAvailable()){
            Toast.makeText(MainActivity.mcontext,"网络连接成功",Toast.LENGTH_SHORT).show();
        }
        else{
            Dialog(MainActivity.mcontext);
        }


    }

    //断网提示
    private void Dialog(final Context context){

        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("提示");
        builder.setMessage("网络断开");
        builder.setCancelable(false);
        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivity(new Intent("android.net.wifi.PICK_WIFI_NETWORK"));
            }
        });
        builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog= builder.create();
        dialog.show();
    }






}
