package com.example.myapplication.tools;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.example.myapplication.data.BaseBean;
import com.example.myapplication.data.BaseListBean;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpTool {

    // 将共用的网络操作写在一个类里 提供一个静态方法 当想要发起网络请求的时候，只需要简单调用这个static方法
    public static void postObject(final String url, HttpParams params, final Class classOfT, final HttpListener listener) {
        CommonTool.showLog(url + "请求参数==" + getParam(params));
        OkGo.<String>post(UrlConfig.URL + url)
                .params(params)
                .execute(new StringCallback() {

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        listener.onFailed("网络链接失败,请检查网络！");
                    }

                    @Override
                    public void onSuccess(Response<String> response) {
                        BaseBean appResObj = null;
                        CommonTool.showLog("接口" + url + "===" + response.body());
                        try {
                            appResObj = (BaseBean) GsonUtils.json2Object(response.body(), classOfT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (listener != null) {
                            if (appResObj == null) {
                                listener.onFailed("服务器忙请稍后再试");
                                return;
                            }
                            if (appResObj.code == 0) {
                                listener.onComplected(appResObj);
                            } else {
                                listener.onFailed(appResObj.msg);
                            }
                        }


                    }


                });
    }




    private static String getParam(HttpParams params) {
        String s = "";
        for (String str : params.urlParamsMap.keySet()) {
            s += "&" + str + "=" + params.urlParamsMap.get(str).get(0);
        }
        return s;
    }

    public interface HttpListener {
        void onComplected(Object... result);

        void onFailed(String msg);

    }
    // 线程的方式 实现okhttp
    // okhttp3.Callback回调接口函数
//    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
//        OkHttpClient client=new OkHttpClient();
//        Request request=new Request.Builder().url(address).build();
//        client.newCall(request).enqueue(callback);  // enqueue 内部已经开好子线程了 在子线程中执行http请求 并将最后的结果回调到callback中
//
//
//    }

}


