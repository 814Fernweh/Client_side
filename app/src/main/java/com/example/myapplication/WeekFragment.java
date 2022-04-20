package com.example.myapplication;
// https://blog.csdn.net/weixin_34232744/article/details/89614717  listview
// https://bbs.csdn.net/topics/392036935  问题  还没用过
import android.annotation.SuppressLint;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.data.EmployeeResonseBean;
import com.example.myapplication.data.RecordBean;
import com.example.myapplication.data.RecordResonseBean;
import com.example.myapplication.tools.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import org.json.JSONObject;

import java.util.ArrayList;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.text.ParseException;


import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeekFragment extends Fragment {

    Integer eID;
    Handler handler;
    ListView listView;
    SimpleAdapter adapter;

    List<Map<String, Object>> listitem= new ArrayList<Map<String, Object>>(); //存储数据的数组列表

    public WeekFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.week, container, false);

        eID= getActivity().getIntent().getIntExtra("eID",0);
        sendSearchWeekRequestWithOkhttp(eID,1);

        handler=new Handler(){
            @SuppressLint("HandlerLeak")
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        adapter = new SimpleAdapter(getActivity()
                                , listitem
                                , R.layout.week_detail
                                ,new String[]{"checkdate","checkarrive", "checkleave"}
                                , new int[]{R.id.tv_expense_abc,R.id.tv_expense_category, R.id.tv_expense_money});

                        listView = (ListView) v.findViewById(R.id.lv_expense);
                        listView.setAdapter(adapter);

                        break;
                    default:
                        break;

                }
            }
        };

        return v;
    }


    public static String getPastDate(int past,Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - past);
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String result = sdf.format(today);
        return result;
    }

    // 传入eid 和week=1 or month=2 作为参数flag去找考勤记录  如果是week 就返回当前日期所在周的
    // month 就返回本月的
    public void sendSearchWeekRequestWithOkhttp(Integer eID,Integer flag) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String BaseURL = "http://192.168.1.107:8081/attendance/clientSearchWeekData";
                JsonObject data = new JsonObject();
                data.addProperty("eID", eID);
                data.addProperty("flag",flag);

                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
                    //   RequestBody requestBody = RequestBody.create(JSONType, String.valueOf(data));
                    RequestBody requestBody = new FormBody.Builder().add("SearchWeekData", data.toString()).build();
                    Request request = new Request.Builder().url(BaseURL).post(requestBody).build();
                    Response response = client.newCall(request).execute();  // 发送请求 获取服务器返回的数据
                    String responseData = response.body().string();   // 对了
                    Gson gson = new Gson();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map = gson.fromJson(responseData, map.getClass());// 对了

             //       System.out.println("map的值为:"+map);

                    // 这周的日期存放在weekDaysList中
                    ArrayList<String> pastDaysList = new ArrayList<>();
                    try {
                        Date today = new Date();
                        //这里传来的时间是个string类型的，所以要先转为date类型的。
                        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
                        String date_str = sdf.format(today);
                        Date date =sdf.parse(date_str);
                        for (int i = 6; i >= 0; i--) {
                            pastDaysList.add(getPastDate(i,date));
                        }

                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                   // 依次取出所有的record值 包括null的 放在一个数组中
                    List<String> allweek=new ArrayList<>();
                    for(int j=0;j<7;j++){

                        // 11 18
                        int beginIndex,endIndex;
                        String day0A= (String) map.get(pastDaysList.get(j)+",arrive");
                        String day0L= (String) map.get(pastDaysList.get(j)+",leave");
                        if(!day0A.contains("no")){
                            beginIndex = day0A.indexOf("T")+1;
                            endIndex = day0A.lastIndexOf(".");
                            day0A=day0A.substring(beginIndex,endIndex);
                        }
                        if(!day0L.contains("no")){
                            beginIndex = day0L.indexOf("T")+1;
                            endIndex = day0L.lastIndexOf(".");
                            day0L=day0L.substring(beginIndex,endIndex);
                        }
                        allweek.add(day0A);
                        allweek.add(day0L);
                    }
                    // 0是arrive 1是leave 偶数是arrive 奇数是leave 按日期顺序加入list中
                    // allweek里面按时间顺序和arrive leave顺序存好了所有时间数据（包括日期） 没有的就是no arrive
                    System.out.println("day0的值为:"+allweek);

                    // 把里面的日期去掉 只留下时间String格式的！！！  然后就可以放到map里面
                    for (int i = 0; i < 14; i+=2){
                        Map<String, Object> listmap = new HashMap<String, Object>();
                        listmap.put("checkdate", pastDaysList.get(i/2));
                        listmap.put("checkarrive", allweek.get(i));
                        listmap.put("checkleave", allweek.get(i+1));

                        listitem.add(listmap);
                    }

                    Message message=new Message();
                    message.what=1;
                    handler.sendMessage(message);
                    System.out.println("listiem的值为:"+listitem);


                } catch (Exception e) {
                    e.printStackTrace();
                }



            }

        }).start();
    }

}