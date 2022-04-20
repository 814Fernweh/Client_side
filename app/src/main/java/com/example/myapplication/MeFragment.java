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
        // Change Password button
        // Click on it to jump to the new ChangePwdFragment page
        // to change your password
        btn_changepwd.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (onButtonClick != null) {
                    onButtonClick.onClick(btn_changepwd);
                }
            }
         });

        // logout   https://blog.csdn.net/dieni1979/article/details/101202906
        btn_logout.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                showdialog(v);
            }
        });

        return view;
    }


    public OnButtonClick getOnButtonClick() {
        return onButtonClick;
    }
    public  void setOnButtonClick(OnButtonClick onButtonClick) {
        this.onButtonClick = onButtonClick;
    }
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

            text_eid.setText(s.get(0));
            text_name.setText(s.get(1));
            text_age.setText(s.get(2));
            if(s.get(3).equals("1")){
                text_gender.setText("female");
            }

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

                 AlertDialog.Builder alertdialogbuilder=new AlertDialog.Builder(getActivity());
                 alertdialogbuilder.setMessage("Are you sure you want to exit the program?");
                 alertdialogbuilder.setPositiveButton("Yes",click1);
                 alertdialogbuilder.setNegativeButton("No",click2);
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


