package com.example.chatterbot;

import TencentApi.AiRequestBean;
import TencentApi.entity.ChatResponse;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import util.JsonParser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.aip.imageclassify.AipImageClassify;
import com.google.gson.Gson;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer mIat;
    private String TAG = "MainActivity";
    private EditText mResultText;
    static private SpeechSynthesizer mTts;
    public static String voicerLocal = "xiaoyan";
    StringBuilder stringBuilder;



    public static final String BAIDU_APP_ID = "19704345";
    public static final String BAIDU_API_KEY = "9kisG8lgKGnvYX5ACSGlrdUW";
    public static final String BAIDU_SECRET_KEY = "jtbRruoofmlK0jVR8p8coZBgr3PXkoEk";

    private ImageView imageView;
    private TextView textView;
    private Uri imageUri;
    public static final int TAKE_PHOTO = 1;

    private String userSpeak = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();

        mResultText = findViewById(R.id.result_text);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5f00533a");
        mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
        stringBuilder = new StringBuilder();

        findViewById(R.id.start_speak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stringBuilder.delete(0, stringBuilder.length());
                setVoiceRecParam();
                mIat.startListening(mRecognizerListener);
            }
        });

//        findViewById(R.id.start_dec_btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //PlantDect();//调用植物识别接口方法
//                AnimalDect();//调用动物识别接口方法
//            }
//        });
        findViewById(R.id.stop_speak).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIat.stopListening();
            }
        });



        imageView = findViewById(R.id.photo);
        textView = findViewById(R.id.result_txt);
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                takePicture();
            }
        });
        findViewById(R.id.start_dec_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //PlantDect();//调用植物识别接口方法
                AnimalDect();//调用动物识别接口方法
            }
        });

    }


    void PlantDect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                AipImageClassify client = new AipImageClassify(BAIDU_APP_ID, BAIDU_API_KEY, BAIDU_SECRET_KEY);
                client.setConnectionTimeoutInMillis(2000);
                client.setSocketTimeoutInMillis(60000);
                //String path = Environment.getExternalStorageDirectory()+ "/image/plant.jpg";
                String path = getExternalCacheDir()+ "/output_image.jpg";//
                Log.d("phone",path);
                JSONObject res = client.plantDetect(path, new HashMap<String, String>());
                try {
                    Log.d("TAG", res.toString(2));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    //动物识别
    void AnimalDect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                AipImageClassify client = new AipImageClassify(BAIDU_APP_ID, BAIDU_API_KEY, BAIDU_SECRET_KEY);
                client.setConnectionTimeoutInMillis(2000);
                client.setSocketTimeoutInMillis(60000);
                //String path = Environment.getExternalStorageDirectory()+ "/image/output_image.jpg";
                String path = getExternalCacheDir()+ "/output_image.jpg";
                Log.d("TAG22", path);
               JSONObject res = client.animalDetect(path, new HashMap<String, String>());
//                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//                JSONObject res = client.animalDetect(path, new HashMap<String, String>());
                try {
                    Log.d("TAG22", res.toString(2));
                    String result = res.getJSONArray("result").getJSONObject(0).getString("name");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText("识别结果："+result);
                            Log.d("TAG1",textView.getText().toString());
                            if(!(textView.getText().toString().equals(""))){
                                return;
                            }
                            mTts.startSpeaking(textView.getText().toString(), mTtsListener);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //图片处理
    public static Bitmap resizeBitmap(Bitmap bitmap, int w, int h){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scaleWidth = ((float)w)/width;
        float scaleHeight = ((float)h)/height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);

        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
        return resizeBitmap;
    }


    //调用相机，将图片显示回来
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("FILE",imageUri.toString());
        Log.d("FILE", "requestCode"+String.valueOf(requestCode));
        Log.d("FILE", "TAKE_PHOTO"+String.valueOf(TAKE_PHOTO));
        Log.d("FILE", "RESULT_OK"+String.valueOf(RESULT_OK));
        switch (requestCode){
            case TAKE_PHOTO:
                if(requestCode == 1){
                    Log.d("FILE","RESULT_FIRST_USER"+String.valueOf(RESULT_FIRST_USER));
                    Log.d("FILE","RESULT_OK"+String.valueOf(RESULT_OK));
                    try{
                        //将拍摄的照片显示出来
                        Log.d("imgUrl",imageUri.toString());
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                        bitmap = resizeBitmap(bitmap,800,800);
                        imageView.setImageBitmap(bitmap);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }

    }



    //创建
    void takePicture(){
        //创建File对象，用于存储拍照后的图片
        //.getAbsolutePath()
        File outputImage = new File(getExternalCacheDir(),"output_image.jpg");
        try{
            if(outputImage.exists()){
                outputImage.delete();
            }
            outputImage.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }
        if(Build.VERSION.SDK_INT < 24){
            imageUri = Uri.fromFile(outputImage);
        }else {
            imageUri = FileProvider.getUriForFile(MainActivity.this,"com.demo.chatterbot.fileprovider",outputImage);
        }

        //启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent,TAKE_PHOTO);
    }



    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    String robotSpeak = (String) msg.obj;
                    Log.d(TAG, "onResponse: " + robotSpeak);
                    mTts.startSpeaking(robotSpeak, mTtsListener);
                    mResultText.setText(mResultText.getText() + "\n机器人：" + robotSpeak);
                    break;
            }
        }
    };

    private String getLocalRecResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //识别通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "iat/common.jet"));
        tempBuffer.append(";");
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "iat/sms_16k.jet"));
        //识别8k资源-使用8k的时候请解开注释
        return tempBuffer.toString();
    }

    private void setVoiceRecParam() {
        mIat.setParameter(SpeechConstant.PARAMS, null);// 清空参数
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);// 设置引擎
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");// 设置返回结果格式
        mIat.setParameter(ResourceUtil.ASR_RES_PATH, getLocalRecResourcePath());// 设置本地识别资源
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");// 设置语音前端点:静音超时时间
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");// 设置语音后端点:后端点静音检测时间
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");// 设置音频保存路径
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "onInit:" + code);
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG, "onError:" + error);
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            stringBuilder.append(text);
            if (isLast) {
                userSpeak = "";
                userSpeak = stringBuilder.toString();
                mResultText.setText(mResultText.getText() + "\n我：" + userSpeak);
                Log.d("TAG================", userSpeak.substring(0,userSpeak.length()-1));
                if(userSpeak.substring(0,userSpeak.length()-1).equals("打开相机")){
                    takePicture();
                    AnimalDect();//调用动物识别接口方法
                    mTts.startSpeaking((String)textView.getText(), mTtsListener);
                }
                try {
                    new AiRequestBean.Builder().addParam("session", "10000").addParam("question", userSpeak).build().request(AiRequestBean.BasicTalkUrl,
                            new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {}
                                @Override
                                public void onResponse(Call call, Response response) throws IOException {
                                    ChatResponse chatResponse = new Gson().fromJson(response.body().string(), ChatResponse.class);
                                    String robotSpeak = chatResponse.data.answer;
                                    Message msg = new Message();
                                    msg.what = 1;
                                    msg.obj = robotSpeak;
                                    myHandler.sendMessage(msg);
                                }
                            });
                } catch (IOException e) {e.printStackTrace();}
            }
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {
        }

        @Override
        public void onSpeakPaused() {
        }

        @Override
        public void onSpeakResumed() {
        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {
        }

        @Override
        public void onCompleted(SpeechError speechError) {
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
        }
    };

    private void setSpeakParam() {
        mTts.setParameter(SpeechConstant.PARAMS, null);// 清空参数
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);//设置使用本地引擎
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());//设置发音人资源路径
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerLocal);//设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "50");//设置合成语速
        mTts.setParameter(SpeechConstant.PITCH, "50");//设置合成音调
        mTts.setParameter(SpeechConstant.VOLUME, "50");//设置合成音量
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");//设置播放器音频流类型
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");// 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");// 设置音频保存路径
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        String type = "tts";
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, type + "/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, type + "/" + MainActivity.voicerLocal + ".jet"));
        return tempBuffer.toString();
    }

    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
        }
    };

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET,
                            Manifest.permission.CAMERA}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
