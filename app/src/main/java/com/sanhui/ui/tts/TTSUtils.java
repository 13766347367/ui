package com.sanhui.ui.tts;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.TtsMode;
import com.sanhui.ui.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.sanhui.ui.tts.IOfflineResourceConst.DEFAULT_SDK_TTS_MODE;

public class TTSUtils  implements IOfflineResourceConst{
    private Context mContext;
    private MySpeechSynthesizerListener listener = new MySpeechSynthesizerListener();
    private static final String TAG = "TTSUtils";
    private boolean isOnlineSDK = TtsMode.ONLINE.equals(DEFAULT_SDK_TTS_MODE);
    private static final String SAMPLE_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/baiduTTS/";
    private static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + TEXT_MODEL;

    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME = TEMP_DIR + "/" + VOICE_MALE_MODEL;

    protected String sn="9c7e2422-7d9f0292-0c22-00e7-7ece6-00";


    private SpeechSynthesizer mSpeechSynthesizer;

    public TTSUtils(Context context){
        this.mContext = context;
    }

    public void initData(String str) {
        boolean isSuccess;
        if (!isOnlineSDK) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                return;
            } else {
                System.out.println("离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }


        if (mSpeechSynthesizer == null) {
            mSpeechSynthesizer = SpeechSynthesizer.getInstance();
            // this 是Context的之类，如Activity
            mSpeechSynthesizer.setContext(mContext);
            //播报状态的接口 用于监听是否成功等等
            // 详情见 MySpeechSynthesizerListener 已经加上注释
            mSpeechSynthesizer.setSpeechSynthesizerListener(listener);
            //这里只是为了让Demo运行使用的APPID,请替换成自己的id。
            mSpeechSynthesizer.setAppId("23811126");
            //这里只是为了让Demo正常运行使用APIKey,请替换成自己的APIKey
            mSpeechSynthesizer.setApiKey("MVtTaaMdweNhKe43gGARuUAM",
                    "8EQRzQOI8OFybyddAezBzLEFwYCd8XQW");
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_AUTH_SN, sn);


            if (!isOnlineSDK) {
                // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
                // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);

                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
                // 该参数设置为TtsMode.MIX生效。
                // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
                // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
                // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
                // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

            }

            // 纯在线
//            mSpeechSynthesizer.auth(TtsMode.ONLINE);
            // 离在线混合
//            mSpeechSynthesizer.auth(TtsMode.OFFLINE);
            // 设置发声的人声音，设置在线发声音人：
            // 0 普通女声（默认）
            // 1 普通男声 2 特别男声
            // 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
            // 设置合成的音量，0-9 ，默认 5
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME,"5");
            //=============================配置 这里我都用的默认 所以不需要配置==========
            //=======================================================================
            // 设置合成的语速，0-9 ，默认 5
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "3");
            // 设置合成的语调，0-9 ，默认 5
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "3");
            //========================================================================
            //========================================================================
            // 初始化在线合成功能
//            mSpeechSynthesizer.initTts(TtsMode.ONLINE);
            //初始化离在线混合模式
//            mSpeechSynthesizer.initTts(TtsMode.OFFLINE);
        }
        //验证是否授权通过
        AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
        if (authInfo.isSuccess()) {
          int  result=  mSpeechSynthesizer.initTts(TtsMode.MIX);
            mSpeechSynthesizer.speak(str);
            Log.e("info", "授权成功"+"   "+result);
        } else {
            // 授权失败
            Log.e("info", authInfo.getTtsError().getDetailMessage());
        }
    }


    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                System.out.println("[ERROR] 文件不存在或者不可读取，请从demo的assets目录复制同名文件到："
                        + f.getAbsolutePath());

                return false;
            }
        }
        return true;
    }



}
