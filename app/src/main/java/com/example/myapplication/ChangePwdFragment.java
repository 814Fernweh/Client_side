package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.data.EmployeeResonseBean;
import com.example.myapplication.tools.CommonTool;
import com.example.myapplication.tools.Constants;
import com.example.myapplication.tools.HttpTool;
import com.example.myapplication.tools.UrlConfig;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lzy.okgo.model.HttpParams;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePwdFragment extends Fragment {

    String password;
    Integer eID;
    private EditText edit_init_pwd,edit_change_pwd00,edit_change_pwd11;
    private Button btn_commitpwd,btn_goBack;
    private OnPwdButtonClick onPwdButtonClick;    //2、定义接口成员变量

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = View.inflate(getActivity(),R.layout.change_pwd,null);

        password= getActivity().getIntent().getStringExtra("password");
        eID= getActivity().getIntent().getIntExtra("eID",0);

        //原密码的输入框
        edit_init_pwd = view.findViewById(R.id.edit_init_pwd);
        //新密码输入框
        edit_change_pwd00 = view.findViewById(R.id.edit_change_pwd00);
        //确认密码输入框
        edit_change_pwd11 = view.findViewById(R.id.edit_change_pwd11);
        // 提交修改 按钮
        btn_commitpwd = view.findViewById(R.id.btn_commitpwd);
        // 返回个人主页按钮
        btn_goBack=view.findViewById(R.id.btn_goBack);



        //事件处理的监听器
        btn_commitpwd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                //view.getid()就是获取当前点击的view的id，这个id是你在xml布局文件设置的id
                if(view.getId()==R.id.btn_commitpwd){
                    // 得到输入框中的数据
                    String inilPWD =  edit_init_pwd.getText().toString().trim();
                    String newPWD = edit_change_pwd00.getText().toString().trim();
                    String confirmPWD = edit_change_pwd11.getText().toString().trim();
                    // 提示用户输入所有信息
                    if (TextUtils.isEmpty(inilPWD)) {
                        Toast.makeText(getActivity(),"请输入原密码",Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(newPWD)) {
                        Toast.makeText(getActivity(),"请输入新密码",Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(confirmPWD)) {
                        Toast.makeText(getActivity(),"请第二次输入新密码",Toast.LENGTH_SHORT).show();
                    }
                    if(!inilPWD.equals(password)){
                        Toast.makeText(getActivity(),"请正确输入原密码",Toast.LENGTH_SHORT).show();
                    }
                    if(!newPWD.equals(confirmPWD)){
                        Toast.makeText(getActivity(),"两次输入密码不一致",Toast.LENGTH_SHORT).show();

                    }
                    // 原密码对了  两次密码也一致了
                    if(inilPWD.equals(password) && newPWD.equals(confirmPWD)){
                        sendNewPWDWithOkhttp(eID,newPWD);
                    }

                }
            }
        });

        btn_goBack.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                //4、如果接口成员变量不为空，则调用接口变量的方法。
                if (onPwdButtonClick != null) {
                    onPwdButtonClick.onClick(btn_goBack);
                }
            }
        });


        return view;
    }


    //  https://blog.csdn.net/bfboys/article/details/53193034
    //定义接口变量的get方法
    public OnPwdButtonClick getOnButtonClick() {
        return onPwdButtonClick;
    }
    //定义接口变量的set方法
    public  void setOnPwdButtonClick(OnPwdButtonClick onPwdButtonClick) {
        this.onPwdButtonClick = onPwdButtonClick;
    }
    //1、定义接口
    public interface OnPwdButtonClick{
        public void onClick(View view);
    }


    public void sendNewPWDWithOkhttp(Integer eID,String newpwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String BaseURL = "http://192.168.1.104:8081/employee/changePwd";
                JsonObject data = new JsonObject();
                data.addProperty("eID", eID);
                data.addProperty("newpwd",newpwd);
                //   System.out.println(data.toString());

                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = new FormBody.Builder().add("ChangePwdData", data.toString()).build();
                    Request request = new Request.Builder().url(BaseURL).post(requestBody).build();
                    Response response = client.newCall(request).execute();  // 发送请求 获取服务器返回的数据
                    String responseData = response.body().string();   // 对了
                    //string转map
                    Gson gson = new Gson();
                    Map<String, Object> map = new HashMap<String, Object>();
                    // map的key是日期 value是具体的record记录  没有的话就是null
                    // 服务器端 传回来的week数据  把上面的map转成json格式了
                    map = gson.fromJson(responseData, map.getClass());// 对了
                    System.out.println("map的值为:"+map);
                    String msg= (String) map.get("pwdMsg");
                    System.out.println("map的值为:"+msg);  // msg已经成功取到相应的提示用户信息 attendance success
                    // 子线程 中显示toast
                    Looper.prepare();
                    Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (Exception e) {
                    e.printStackTrace();
                }



            }

        }).start();
    }
}
