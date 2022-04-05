package com.example.myapplication;
// 将activity 改为fragment http://www.javashuo.com/article/p-mrtegwdz-np.html
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.functions.Consumer;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DoCheckinFragment extends Fragment implements View.OnClickListener, BDLocationListener {

    private Context mContext;
    private RxPermissions rxPermissions;

    //从MainActivity里面得到手机号和eID
    String telephone;
    Integer eID;

    private TextView textMapInfo;
    private LocationClient mLocationClient;
    private BDLocation bdLocation;

    private int grantedPermissionNum = 0;

    private Camera camera;                                  //定义相机对象
    private SurfaceHolder sh;
    private boolean isPreview = false;                     //定义非预览状态

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(),R.layout.fragment_checkin,null);

        // 自己加的 用户名 密码
//        Intent intent=getIntent();
//        // intent 没起作用
//        telephone=intent.getStringExtra("telephone");
//        eID=intent.getIntExtra("eID",0);
        // 解决intent无效问题   https://blog.csdn.net/weixin_45068278/article/details/117676637
        telephone= getActivity().getIntent().getStringExtra("telephone");
        eID= getActivity().getIntent().getIntExtra("eID",0);


        mapDataInit();

        textMapInfo = (TextView)view.findViewById(R.id.textMapInfo);
        //获取SurfaceView组件，用于显示摄像头预览
        SurfaceView sv = (SurfaceView) view.findViewById(R.id.surfaceView);
        sh = sv.getHolder();              //获取SurfaceHolder对象
        //设置该SurfaceHolder自己不维护缓冲
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Button preview =  view.findViewById(R.id.preview);                  //获取“预览”按钮
        Button takePicture =  view.findViewById(R.id.btnTakePhoto);//获取“拍照”按钮

        preview.setOnClickListener(new View.OnClickListener() {         //实现摄像头预览功能
            @Override
            public void onClick(View v) {
                previewOnClick(v);
            }
        });

        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureonClick(v);
            }
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Main_btnStart: {
                checkUserAllPermissions();
                break;
            }
            default:break;
    }
}

    @Override
    public void onStart() {
        super.onStart();
        checkUserAllPermissions();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (grantedPermissionNum >= 4) {
            startLocation();
        }
    }

    @Override
    public void onPause() {
        if (camera != null) {                         //如果摄像头不为空
            camera.stopPreview();                     //停止预览
            camera.release();                          //释放资源
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mLocationClient.stop();
        super.onDestroy();
    }



    /**
     * 获取前置摄像头
     * @return
     */
    public Camera getFrontCamera() {
        camera = null;
        Camera.CameraInfo info = new Camera.CameraInfo();
        int cnt = Camera.getNumberOfCameras();
        for (int i = 0;i<cnt;i++) {
            Camera.getCameraInfo(i,info);

            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    camera = Camera.open(i);
                    camera.setDisplayOrientation(90);  // 旋转90度
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return camera;
    }

    private void resetCamera() {        //创建resetCamera()方法，实现重新预览功能
        if (!isPreview) {                //如果为非预览模式
            camera.startPreview();       //开启预览
            isPreview = true;
        }
    }

    public void previewOnClick(View v) {
        // 如果摄像头为非预览模式，则打开相机
        if (!isPreview) {
            camera = getFrontCamera();                //打开相机
            isPreview = true;                       //设置为预览状态
        }
        try {
            camera.setPreviewDisplay(sh);           //设置用于显示预览的SurfaceView
            Camera.Parameters parameters = camera.getParameters();  //获取相机参数
            parameters.setPictureFormat(PixelFormat.JPEG);    //指定图片为JPEG图片
            parameters.set("jpeg-quality", 80);   //设置图片的质量
            camera.setParameters(parameters);      //重新设置相机参数
            camera.startPreview();                  //开始预览
            camera.autoFocus(null);                 //设置自动对焦
        } catch (IOException e) {                   //输出异常信息
            e.printStackTrace();
        }
    }


    public void takePictureonClick(View v) {
        if (camera != null) {                          //相机不为空
            camera.takePicture(null, null, cameraCallBack);    //进行拍照
        }
    }

    //实现将照片保存到系统图库中
    final Camera.PictureCallback cameraCallBack = new Camera.PictureCallback() {  //照片回调函数
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 根据拍照所得的数据创建位图
            final Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
                    data.length);
            camera.stopPreview();                                          //停止预览
            isPreview = false;                                             //设置为非预览状态
            //获取sd卡根目录
            File appDir = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera/");
            if (!appDir.exists()) {                   //如果该目录不存在
                appDir.mkdir();                        //创建该目录
            }
            //将获取的当前系统时间设置为照片名称
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);  	//创建文件对象
            try {  //保存拍到的图片
                FileOutputStream fos = new FileOutputStream(file); //创建一个文件输出流对象
                //将图片内容压缩为JPEG格式输出到输出流对象中
                bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                //将缓冲区中的数据全部写出到输出流中
                fos.flush();
                fos.close();                            //关闭文件输出流对象
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //将照片插入到系统图库
            try {
                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //最后通知图库更新
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);


            intent.setData(uri);
            getActivity().sendBroadcast(intent);   //这个广播的目的就是更新图库
            //    Toast.makeText(DoCheckInActivity.this, "照片保存至：" + file, Toast.LENGTH_LONG).show();
            resetCamera();                                //调用重新预览resetCamera()方法
            upload(file.getAbsolutePath());
        }
    };


    public byte[] readPicture(String jpegFile){
        //     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        File jpeg=new File(jpegFile);
        long fileSize = jpeg.length();
        try {
            FileInputStream fi = new FileInputStream(jpeg);

            byte[] buffer = new byte[(int) fileSize];
            int offset = 0;
            int numRead = 0;
            while (true) {
                try {
                    if (!(offset < buffer.length
                            && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0)) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                offset += numRead;
            }
            // 确保所有数据均被读取
            if (offset != buffer.length) {
                try {
                    throw new IOException("Could not completely read file "             + jpeg.getName());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                fi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return buffer;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public void upload(String jpegFile) {

        sendRequestWithOkhttp(jpegFile);

    }

    public void sendRequestWithOkhttp(String jpegFile) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String BaseURL = "http://192.168.1.104:8081/attendance/getData";
                byte[] buffer=readPicture(jpegFile);
                byte[] encode = Base64.encode(buffer, Base64.DEFAULT);

                JsonObject data = new JsonObject();
                data.addProperty("jpegData", new String(encode));
                data.addProperty("Latitude",bdLocation.getLatitude());
                data.addProperty("Longitude",bdLocation.getLongitude());
                data.addProperty("LocType",bdLocation.getLocType());  // 用户自己选类型 还没写
                // 没传过来
                data.addProperty("eTele", telephone);
                data.addProperty("eID", eID);
                data.addProperty("type", 1);

                //   System.out.println(data.toString());

                try {
                    OkHttpClient client = new OkHttpClient();
                    MediaType JSONType = MediaType.parse("application/json; charset=utf-8");
                    //   RequestBody requestBody = RequestBody.create(JSONType, String.valueOf(data));
                    RequestBody requestBody = new FormBody.Builder().add("JsonData", data.toString()).build();
                    Request request = new Request.Builder().url(BaseURL).post(requestBody).build();
                    Response response = client.newCall(request).execute();  // 发送请求 获取服务器返回的数据
                    String responseData = response.body().string();
                    //string转map
                    Gson gson = new Gson();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map = gson.fromJson(responseData, map.getClass());
                    System.out.println("map的值为:"+map);
                    String msg= (String) map.get("msg");
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



    private void mapDataInit() {
        mContext = getActivity();
        mLocationClient = new LocationClient(mContext);
        rxPermissions = new RxPermissions(getActivity());
    }

    /**
     * 定时更新位置（每隔5s）
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        //显示详细地址信息
        option.setIsNeedAddress(true);
        //  option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
        return;
    }

    /**
     * 开始定位
     */
    private void startLocation() {
        initLocation();
        mLocationClient.registerLocationListener(this);
        mLocationClient.start();
    }


    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        new MyLocationAsyncTask().execute(bdLocation);
    }

    /**
     * 获取定位信息的异步任务
     */
    class MyLocationAsyncTask extends AsyncTask<BDLocation, Void, String> {
        @Override
        protected String doInBackground(BDLocation... bdLocations) {
            bdLocation = bdLocations[0];
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("latitude：").append(bdLocation.getLatitude()).append("\n");
            currentPosition.append("longitude：").append(bdLocation.getLongitude()).append("\n");
            currentPosition.append("method：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                currentPosition.append("GPS\n");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                currentPosition.append("WI-FI\n");
            }
            currentPosition.append("Address：").append(bdLocation.getAddrStr());
            System.out.println(currentPosition.toString());
            return currentPosition.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(mContext, "Location updated", Toast.LENGTH_SHORT).show();
            textMapInfo.setText(s);
        }
    }

    /**
     * 申请用户权限
     */
    private void checkUserAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RxPermissions rxPermissions = new RxPermissions(getActivity());
            rxPermissions
                    .requestEach(Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_PHONE_STATE)
                    .subscribe(new Consumer<Permission>() {
                        @Override
                        public void accept(Permission permission) throws Exception {
                            if (permission.granted) {
                                grantedPermissionNum++;
                            } else if (permission.shouldShowRequestPermissionRationale) {
                            } else {
                                Toast.makeText(mContext, "定位服务需要您同意相关权限", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

}

