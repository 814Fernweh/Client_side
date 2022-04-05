package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;


import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
//import android.support.v7.widget.Toolbar;

import com.example.myapplication.data.EmployeeResonseBean;
import com.example.myapplication.tools.CommonTool;
import com.example.myapplication.tools.Constants;
import com.example.myapplication.tools.HttpTool;
import com.example.myapplication.tools.UrlConfig;
import com.google.gson.Gson;
import com.lzy.okgo.model.HttpParams;

// 员工登录
public class LoginActivity extends AppCompatActivity {
    private TextView title;
    private EditText  telephoneEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox rememberPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取SharedPreference对象
        pref= PreferenceManager.getDefaultSharedPreferences(this);
        title = findViewById(R.id.title);
        //用户名的输入框
        telephoneEdit = findViewById(R.id.inputTele);
        //密码输入框
        passwordEdit = findViewById(R.id.inputPwd);
        //登录按钮
        rememberPass=(CheckBox)findViewById(R.id.remember_pass);
        loginBtn = findViewById(R.id.loginBtn);
        boolean isRemember=pref.getBoolean("remember_password",false);
        if(isRemember){
            // 将账号和密码设置到文本框中
            String account=pref.getString("account","");
            String password=pref.getString("password","");
            telephoneEdit.setText(account);
            passwordEdit.setText(password);
            rememberPass.setChecked(true);
        }
        //事件处理的监听器
        loginBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                //view.getid()就是获取当前点击的view的id，这个id是你在xml布局文件设置的id
                if(view.getId()==R.id.loginBtn){
                    String account =  telephoneEdit.getText().toString().trim();
                    String loginPwd = passwordEdit.getText().toString().trim();

                    if (TextUtils.isEmpty(account)) {
                        Toast.makeText(LoginActivity.this,"请输入用户名",Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(loginPwd)) {
                        Toast.makeText(LoginActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
                    }
                    HttpParams params=new HttpParams();
                    // 服务器端的对应名字
                    params.put("telephone",account);
                    params.put("password",loginPwd);
                  //  params.put("type",0);
                    HttpTool.postObject(UrlConfig.LOGIN_URL, params,EmployeeResonseBean.class, new HttpTool.HttpListener() {
                        @Override
                        public void onComplected(Object... result) {
                            EmployeeResonseBean bean= (EmployeeResonseBean) result[0];

                            Constants.employeeBean=bean.data;
                            if(bean.code==0){
                                editor=pref.edit();
                                //检查复选框是否被选中
                                if(rememberPass.isChecked()){
                                    editor.putBoolean("remember_password",true);
                                    editor.putString("account",account);
                                    editor.putString("password",loginPwd);
                                }
                                else {
                                    editor.clear();
                                }
                                editor.apply();
                                CommonTool.spPutString("isLogin","1");
                                CommonTool.spPutString("employeebean",new Gson().toJson( Constants.employeeBean));
                                Toast.makeText(LoginActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                                // 登录成功 启动主页面的活动
                          //     Intent intent=new Intent(LoginActivity.this,SearchActivity.class);
                               Intent intent=new Intent(LoginActivity.this, MainActivity.class);


                                // 传递当前用户的telephone、密码、工号、年龄、姓名、性别、工作部门的id
                                intent.putExtra("telephone",account);
                                intent.putExtra("password",loginPwd);
                                intent.putExtra("eID",bean.data.getEid());
                                intent.putExtra("age",bean.data.getAge());
                                intent.putExtra("name",bean.data.getName());
                                intent.putExtra("gender",bean.data.getGender());
                                intent.putExtra("did",bean.data.getdId());
                                System.out.println("LoginActivity,intent"+intent);
                            //    System.out.println("mainActivity,eID"+eID);
                                startActivity(intent);

                            }else{
                                CommonTool.showToast(bean.msg);
                            }
                        }
                        @Override
                        public void onFailed(String msg) {
                            CommonTool.showToast(msg);
                        }
                    });
                }
            }
        });

    }
}
