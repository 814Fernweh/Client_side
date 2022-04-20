package com.example.myapplication;

import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.HashMap;
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
    private OnPwdButtonClick onPwdButtonClick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = View.inflate(getActivity(),R.layout.change_pwd,null);

        password= getActivity().getIntent().getStringExtra("password");
        eID= getActivity().getIntent().getIntExtra("eID",0);


        edit_init_pwd = view.findViewById(R.id.edit_init_pwd);
        edit_change_pwd00 = view.findViewById(R.id.edit_change_pwd00);
        edit_change_pwd11 = view.findViewById(R.id.edit_change_pwd11);
        btn_commitpwd = view.findViewById(R.id.btn_commitpwd);
        btn_goBack=view.findViewById(R.id.btn_goBack);

        //Listener for event handling
        btn_commitpwd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                //view.getid() is to get the id of the currently clicked view,
                // this id is the id you set in the xml layout file
                if(view.getId()==R.id.btn_commitpwd){
                    // Get the data in the input box
                    String inilPWD =  edit_init_pwd.getText().toString().trim();
                    String newPWD = edit_change_pwd00.getText().toString().trim();
                    String confirmPWD = edit_change_pwd11.getText().toString().trim();
                    // Prompt the user for all information
                    if (TextUtils.isEmpty(inilPWD)) {
                        Toast.makeText(getActivity(),"Please enter the original password",Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(newPWD)) {
                        Toast.makeText(getActivity(),"Please enter the new password",Toast.LENGTH_SHORT).show();
                    }
                    if (TextUtils.isEmpty(confirmPWD)) {
                        Toast.makeText(getActivity(),"Please confirm the password",Toast.LENGTH_SHORT).show();
                    }
                    if(!inilPWD.equals(password)){
                        Toast.makeText(getActivity(),"Please enter the original password correctly",Toast.LENGTH_SHORT).show();
                    }
                    if(!newPWD.equals(confirmPWD)){
                        Toast.makeText(getActivity(),"Two times to enter the password does not match",Toast.LENGTH_SHORT).show();

                    }
                    // The original password is correct and the two passwords are the same
                    if(inilPWD.equals(password) && newPWD.equals(confirmPWD)){
                        sendNewPWDWithOkhttp(eID,newPWD);
                    }

                }
            }
        });

        btn_goBack.setOnClickListener(new View.OnClickListener()  {
            @Override
            public void onClick(View v) {
                if (onPwdButtonClick != null) {
                    onPwdButtonClick.onClick(btn_goBack);
                }
            }
        });

        return view;
    }


    //  https://blog.csdn.net/bfboys/article/details/53193034
    //Defining get methods for interface variables
    public OnPwdButtonClick getOnButtonClick() {
        return onPwdButtonClick;
    }

    public  void setOnPwdButtonClick(OnPwdButtonClick onPwdButtonClick) {
        this.onPwdButtonClick = onPwdButtonClick;
    }
    public interface OnPwdButtonClick{
        public void onClick(View view);
    }


    public void sendNewPWDWithOkhttp(Integer eID,String newpwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String BaseURL = "http://192.168.1.107:8081/employee/changePwd";
                JsonObject data = new JsonObject();
                data.addProperty("eID", eID);
                data.addProperty("newpwd",newpwd);


                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
                    RequestBody requestBody = new FormBody.Builder().add("ChangePwdData", data.toString()).build();
                    Request request = new Request.Builder().url(BaseURL).post(requestBody).build();
                    Response response = client.newCall(request).execute();  // Send request Get the data returned by the server
                    String responseData = response.body().string();
                    //string转map
                    Gson gson = new Gson();
                    Map<String, Object> map = new HashMap<String, Object>();

                    // Server side The week data is sent back to the server and the map above is converted to json format.
                    map = gson.fromJson(responseData, map.getClass());// 对了

                    String msg= (String) map.get("pwdMsg");
                    // Sub-threads  show toast
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
