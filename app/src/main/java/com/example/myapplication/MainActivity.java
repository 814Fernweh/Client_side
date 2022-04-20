package com.example.myapplication;
// https://www.jianshu.com/p/e4b5275b57c3  导航栏底部

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.fragment.app.FragmentActivity;
import com.example.myapplication.MeFragment;

// extends
public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private TextView bottom_bar_text_1;
    private ImageView bottom_bar_image_1;
    private TextView bottom_bar_text_2;
    private ImageView bottom_bar_image_2;
    private TextView bottom_bar_text_3;
    private ImageView bottom_bar_image_3;

    private LinearLayout main_bottom_bar;
    private RelativeLayout bottom_bar_1_btn;
    private RelativeLayout bottom_bar_2_btn;
    private RelativeLayout bottom_bar_3_btn;

    public String telephone;
    public Integer eID;
    Bundle bundle;
    MeFragment meFragment=new MeFragment();
    ChangePwdFragment changePwdFragment=new ChangePwdFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bottom_bar);
        //  The default page is the Attendance and Clocking page
        setMain();

        // 自己加的 用户名 密码 必须是getIntent
        Intent intent=getIntent();
        telephone=intent.getStringExtra("telephone");
        eID=intent.getIntExtra("eID",0);
        String password=intent.getStringExtra("password");
        Integer age=intent.getIntExtra("age",0);
        String name=intent.getStringExtra("name");
        Integer gender=intent.getIntExtra("gender",0);
        Integer did=intent.getIntExtra("did",0);

        // https://blog.csdn.net/chenliguan/article/details/53906934
        bundle = new Bundle();
        bundle.putString("clientTele", telephone);
        bundle.putInt("clientEID", eID);

        // Photo and text corresponding to each button
        bottom_bar_text_1=findViewById(R.id.bottom_bar_text_1);
        bottom_bar_image_1=findViewById(R.id.bottom_bar_image_1);
        bottom_bar_text_2=findViewById(R.id.bottom_bar_text_2);
        bottom_bar_image_2=findViewById(R.id.bottom_bar_image_2);
        bottom_bar_text_3=findViewById(R.id.bottom_bar_text_3);
        bottom_bar_image_3=findViewById(R.id.bottom_bar_image_3);


        main_bottom_bar=findViewById(R.id.main_bottom_bar);
        bottom_bar_1_btn=findViewById(R.id.bottom_bar_1_btn);
        bottom_bar_2_btn=findViewById(R.id.bottom_bar_2_btn);
        bottom_bar_3_btn=findViewById(R.id.bottom_bar_3_btn);
        //Add click events to each of the three buttons
        bottom_bar_1_btn.setOnClickListener(this);
        bottom_bar_2_btn.setOnClickListener(this);
        bottom_bar_3_btn.setOnClickListener(this);


        // 无法从静态上下文中引用非静态 方法  new 一个实例 对方法进行调用
        // https://blog.csdn.net/bfboys/article/details/53193034
        //2、调用对象的set方法，回传接口对象
        //  !!!!! 这里的new fragment得和下面点击按钮的mefragment是同一个
        meFragment.setOnButtonClick(new MeFragment.OnButtonClick() {
            //3、实现接口对象的方法，
            @Override
            public void onClick(View view) {
                //switch to twoFragment
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_body,changePwdFragment)
                        .commit();
            }
        });


        changePwdFragment.setOnPwdButtonClick(new ChangePwdFragment.OnPwdButtonClick() {
            //3、实现接口对象的方法，
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_body,meFragment)
                        .commit();
            }
        });
    }

    // click button, switch fragment
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_bar_1_btn:
                //add
                getSupportFragmentManager().beginTransaction().replace(R.id.main_body,new DoCheckinFragment()).commit();
                new DoCheckinFragment().setArguments(bundle);
                setSelectStatus(0);
                break;
            case R.id.bottom_bar_2_btn:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_body,new SearchFragment()).commit();
                new SearchFragment().setArguments(bundle);
                setSelectStatus(1);
                break;
            case R.id.bottom_bar_3_btn:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_body,meFragment).commit();
                new MeFragment().setArguments(bundle);
                setSelectStatus(2);
                break;
        }
    }

    // Response after clicking on the bottom navigation bar
    private void setSelectStatus(int index) {
        switch (index){
            case 0:
                bottom_bar_text_1.setTextColor(Color.parseColor("#0097F7")); // 蓝色
                //其他的文本颜色不变
                bottom_bar_text_2.setTextColor(Color.parseColor("#666666"));
                bottom_bar_text_3.setTextColor(Color.parseColor("#666666"));
                //图片也不变
                bottom_bar_image_2.setImageResource(R.drawable.statistic);
                bottom_bar_image_3.setImageResource(R.drawable.me);
                break;
            case 1:

                bottom_bar_text_2.setTextColor(Color.parseColor("#0097F7"));
                bottom_bar_text_1.setTextColor(Color.parseColor("#666666"));
                bottom_bar_text_3.setTextColor(Color.parseColor("#666666"));
                bottom_bar_image_1.setImageResource(R.drawable.checkin);
                bottom_bar_image_3.setImageResource(R.drawable.me);
                break;

            case 2:
                bottom_bar_text_3.setTextColor(Color.parseColor("#0097F7"));
                bottom_bar_text_1.setTextColor(Color.parseColor("#666666"));
                bottom_bar_text_2.setTextColor(Color.parseColor("#666666"));
                bottom_bar_image_1.setImageResource(R.drawable.checkin);
                bottom_bar_image_2.setImageResource(R.drawable.statistic);
                break;
        }
    }



    //add fragment
    //Used to open the initial page
    private void setMain() {
        this.getSupportFragmentManager().beginTransaction().add(R.id.main_body,new DoCheckinFragment()).commit();
    }




}
