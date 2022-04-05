package com.example.myapplication;
// 个人信息修改 ui页面https://blog.csdn.net/qq_39827390/article/details/106623770
// 显示的内容写好了     可以更改的就只有密码和手机号
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.baidu.location.BDLocation;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MeFragment extends Fragment  {
    //从MainActivity里面得到手机号和eID
    String telephone,password,name;
    Integer eID,age,gender,did;
    private TextView text_eid,text_age,text_name,text_gender,text_did,text_tele;
    private Button btn_changepwd,btn_logout;
    private OnButtonClick onButtonClick;    //2、定义接口成员变量
    public MeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = View.inflate(getActivity(),R.layout.me,null);
        // 解决intent无效问题   https://blog.csdn.net/weixin_45068278/article/details/117676637
        telephone= getActivity().getIntent().getStringExtra("telephone");
        eID= getActivity().getIntent().getIntExtra("eID",0);
        password=getActivity().getIntent().getStringExtra("password");
        age=getActivity().getIntent().getIntExtra("age",0);
        name=getActivity().getIntent().getStringExtra("name");
        gender=getActivity().getIntent().getIntExtra("gender",0);
        did=getActivity().getIntent().getIntExtra("did",0);
        // 拿过来的intent都是对的

        text_eid = view.findViewById(R.id.text_eid);
        text_age = view.findViewById(R.id.text_age);
        text_name = view.findViewById(R.id.text_name);
        text_gender = view.findViewById(R.id.text_gender);
        text_did = view.findViewById(R.id.text_did);
        text_tele = view.findViewById(R.id.text_tele);

        btn_changepwd=(Button)view.findViewById(R.id.btn_changepwd);
        btn_logout=(Button)view.findViewById(R.id.btn_logout);
        new MeAsyncTask().execute();

     //     https://blog.csdn.net/bfboys/article/details/53193034
        // 修改密码按钮 点击后跳转到ChangePwdFragment  新的页面去修改密码
        btn_changepwd.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                //4、如果接口成员变量不为空，则调用接口变量的方法。
                if (onButtonClick != null) {
                    onButtonClick.onClick(btn_changepwd);
                }
            }
         });

        // 退出登录按钮   https://blog.csdn.net/dieni1979/article/details/101202906
        btn_logout.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                //4、如果接口成员变量不为空，则调用接口变量的方法。
                showdialog(v);
            }
        });


        return view;
    }

    //定义接口变量的get方法
    public OnButtonClick getOnButtonClick() {
        return onButtonClick;
    }
    //定义接口变量的set方法
    public  void setOnButtonClick(OnButtonClick onButtonClick) {
        this.onButtonClick = onButtonClick;
    }
    //1、定义接口
    public interface OnButtonClick{
        public void onClick(View view);
    }


    class MeAsyncTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected  List<String> doInBackground(Void... Params) {
            List<String> s=new ArrayList<String>();
            s.add(eID.toString());
            s.add(name);
            s.add(age.toString());
            s.add(gender.toString());
            s.add(did.toString());
            s.add(telephone);
            return s;

        }

        @Override
        protected void onPostExecute(List<String> s) {
            //   Toast.makeText(mContext, "地理位置已更新", Toast.LENGTH_SHORT).show();

            text_eid.setText(s.get(0));
            text_name.setText(s.get(1));
            text_age.setText(s.get(2));
            if(s.get(3).equals("1")){
                text_gender.setText("female");
            }
            // 是0还是2？？？？？
            if(s.get(3).equals("0")){
                text_gender.setText("male");
            }
            if(s.get(4).equals("1")){
                text_did.setText("market");
            }
            text_tele.setText(s.get(5));

        }
    }


    public void showdialog(View view) {
                 //定义一个新的对话框对象
                 AlertDialog.Builder alertdialogbuilder=new AlertDialog.Builder(getActivity());
                 //设置对话框提示内容
                 alertdialogbuilder.setMessage("确定要退出程序吗？");
                 //定义对话框2个按钮标题及接受事件的函数
                 alertdialogbuilder.setPositiveButton("Yes",click1);
                 alertdialogbuilder.setNegativeButton("No",click2);
                 //创建并显示对话框
                 AlertDialog alertdialog1=alertdialogbuilder.create();
                 alertdialog1.show();

    }

    private DialogInterface.OnClickListener click1=new DialogInterface.OnClickListener(){
        //使用该标记是为了增强程序在编译时候的检查，如果该方法并不是一个覆盖父类的方法，在编译时编译器就会报告错误。
        @Override
        public void onClick(DialogInterface arg0,int arg1)
        {
                     //当按钮click1被按下时执行结束进程
           android.os.Process.killProcess(android.os.Process.myPid());
        }
    };
    private DialogInterface.OnClickListener click2=new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface arg0,int arg1)
        {
            //当按钮click2被按下时则取消操作
            arg0.cancel();
        }
    };


}


