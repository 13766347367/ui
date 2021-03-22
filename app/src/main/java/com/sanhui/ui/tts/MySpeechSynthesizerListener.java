package com.sanhui.ui.tts;

import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;

public class MySpeechSynthesizerListener implements SpeechSynthesizerListener {
    @Override
    public void onSynthesizeStart(String s) {
        // 监听到合成开始，在此添加相关操作
        Log.e("info", "监听到合成开始");
    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i, int i1) {
        Log.e("info", "监听到有合成数据到达");

    }


    @Override
    public void onSynthesizeFinish(String s) {
        // 监听到合成结束，在此添加相关操作
        Log.e("info", "监听到合成结束");
    }

    @Override
    public void onSpeechStart(String s) {
        // 监听到合成并播放开始，在此添加相关操作
        Log.e("info", "监听到合成并播放开始");
    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {
        // 监听到播放进度有变化，在此添加相关操作
        Log.e("info", "监听到播放进度有变化");
    }

    @Override
    public void onSpeechFinish(String s) {
        // 监听到播放结束，在此添加相关操作
        Log.e("info", "监听到播放结束");
    }

    @Override
    public void onError(String s, SpeechError speechError) {
        // 监听到出错，在此添加相关操作
        Log.e("info", "监听到出错   "+speechError.code);
    }

}
