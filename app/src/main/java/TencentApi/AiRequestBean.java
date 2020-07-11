package TencentApi;

import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static TencentApi.TencentAIParamsHelper.md5;

public class AiRequestBean {

    public static final String BasicTalkUrl="https://api.ai.qq.com/fcgi-bin/nlp/nlp_textchat";

    public static final String ERROR = "error";
    private TreeMap<String, String> mParams;
    private String TAG = "AiRequestBean";
    private AiRequestBean() {
        mParams = new TreeMap<>();
        //时间戳
        String time_stamp = System.currentTimeMillis() / 1000 + "";
        //随机字符串
        String nonce_str = TencentAIParamsHelper.getRandomString(10);
        //appId
        String app_id = String.valueOf(TencentAIConstants.APP_ID_AI);
        //将通用参数设置进map中
        mParams.put("app_id", app_id);
        mParams.put("nonce_str", nonce_str);
        mParams.put("time_stamp", time_stamp);
    }

    /**
     * TreeMap生成鉴权信息
     */
    private String generateAppSign() throws UnsupportedEncodingException {
        Set<String> keySet = mParams.keySet();
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = mParams.get(key);
            sb.append("&").append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
        }
        sb.deleteCharAt(0);
        sb.append("&app_key=").append(TencentAIConstants.APP_KEY_AI);
        String sign = md5(sb.toString());
        return sign;
    }

    //发起请求
    public void request(String url, Callback callback) throws IOException {
        //生成签名加入到参数列表中
        String sign = generateAppSign().toUpperCase();
        Log.d(TAG, "sign:"+sign);
        mParams.put("sign", sign);
        //使用okhttp发起请求
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Iterator<String> iterator = mParams.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = mParams.get(key);
            builder.addFormDataPart(key, value);
        }
        RequestBody requestBody = builder.build();
        Log.d(TAG, "requestBody:"+requestBody.toString());
        Request request = new Request.Builder().header("Content-Type", "application/x-www-form-urlencoded")
                .url(url)
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(callback);
    }

    public static class Builder {
        private AiRequestBean targetBean;

        public Builder() {
            targetBean = new AiRequestBean();
        }

        public AiRequestBean build() {
            return targetBean;
        }

        public Builder addParam(String key, String value) {
            targetBean.mParams.put(key, value);
            return this;
        }
    }
}
