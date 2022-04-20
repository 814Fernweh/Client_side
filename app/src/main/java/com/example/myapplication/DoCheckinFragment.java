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

    //get tele and eId from MainActivity
    String telephone;
    Integer eID;

    private TextView textMapInfo;
    private LocationClient mLocationClient;
    private BDLocation bdLocation;

    private int grantedPermissionNum = 0;

    private Camera camera;
    private SurfaceHolder sh;
    private boolean isPreview = false;     //Defining non-preview states

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(),R.layout.fragment_checkin,null);

        // 解决intent无效问题   https://blog.csdn.net/weixin_45068278/article/details/117676637
        telephone= getActivity().getIntent().getStringExtra("telephone");
        eID= getActivity().getIntent().getIntExtra("eID",0);

        mapDataInit();

        textMapInfo = (TextView)view.findViewById(R.id.textMapInfo);
        //Get the SurfaceView component to display the camera preview
        SurfaceView sv = (SurfaceView) view.findViewById(R.id.surfaceView);
        sh = sv.getHolder();
        sh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Button preview =  view.findViewById(R.id.preview);                  //preview
        Button takePicture =  view.findViewById(R.id.btnTakePhoto);// take photo

        preview.setOnClickListener(new View.OnClickListener() {         //Implementing the camera preview function
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
        if (camera != null) {            //If the camera is not empty
            camera.stopPreview();
            camera.release();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mLocationClient.stop();
        super.onDestroy();
    }



    /**
     * Get the front camera
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
                    camera.setDisplayOrientation(90);  // rotate
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        return camera;
    }

    private void resetCamera() {        //Create resetCamera() method for re-preview function
        if (!isPreview) {
            camera.startPreview();
            isPreview = true;
        }
    }

    public void previewOnClick(View v) {
        // If the camera is in non-preview mode, turn on the camera
        if (!isPreview) {
            camera = getFrontCamera();                //open the front camera
            isPreview = true;
        }
        try {
            camera.setPreviewDisplay(sh);
            Camera.Parameters parameters = camera.getParameters();  //Get camera parameters
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.set("jpeg-quality", 80);   //Set the quality of the image
            camera.setParameters(parameters);      //Resetting the camera parameters
            camera.startPreview();
            camera.autoFocus(null);                 //Setting the autofocus
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void takePictureonClick(View v) {
        if (camera != null) {
            camera.takePicture(null, null, cameraCallBack);
        }
    }

    // Save photos to the system gallery
    final Camera.PictureCallback cameraCallBack = new Camera.PictureCallback() {  //Photo callback function
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // Create a bitmap from the data obtained from the photo
            final Bitmap bm = BitmapFactory.decodeByteArray(data, 0,
                    data.length);
            camera.stopPreview();
            isPreview = false;
            //获取sd卡根目录
            File appDir = new File(Environment.getExternalStorageDirectory(), "/DCIM/Camera/");
            if (!appDir.exists()) {                   //If the directory does not exist
                appDir.mkdir();                        //create
            }
            //将获取的当前系统时间设置为照片名称
            String fileName = System.currentTimeMillis() + ".jpg";
            File file = new File(appDir, fileName);
            try {  //保存拍到的图片
                FileOutputStream fos = new FileOutputStream(file); //Create a file output stream object
                //compress image content into JPEG format for output stream object
                bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                //Write out all the data in the buffer to the output stream
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Inserting photos into the system gallery
            try {
                MediaStore.Images.Media.insertImage(getActivity().getContentResolver(),
                        file.getAbsolutePath(), fileName, null);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);


            intent.setData(uri);
            getActivity().sendBroadcast(intent);
            resetCamera();
            upload(file.getAbsolutePath());
        }
    };


    public byte[] readPicture(String jpegFile){

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
                String BaseURL = "http://192.168.1.107:8081/attendance/getData";
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
                    RequestBody requestBody = new FormBody.Builder().add("JsonData", data.toString()).build();
                    Request request = new Request.Builder().url(BaseURL).post(requestBody).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();

                    Gson gson = new Gson();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map = gson.fromJson(responseData, map.getClass());
//                    System.out.println("map的值为:"+map);
                    String msg= (String) map.get("msg");
              //      System.out.println("map的值为:"+msg);

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
     * Timed position update (every 5s)
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        //Show detailed address information
        option.setIsNeedAddress(true);
        //  option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
        return;
    }

    /**
     *  start location
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
     * Asynchronous tasks for obtaining location information
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
     * Request user rights
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
                                Toast.makeText(mContext, "Location services require you to agree to the relevant permissions", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

}

