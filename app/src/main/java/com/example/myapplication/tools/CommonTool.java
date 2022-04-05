package com.example.myapplication.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.common.MyApplication;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonTool {
    // Android开发中，经常需要将少量简单类型数据保存在本地，
    // 如：用户设置。这些需要保存的数据可能一两个字符串，像这样的数据一般选择使用SharedPreferences来保存
    // 数据存储池
    public static SharedPreferences sp;

    // logcat中显示test数据的 相当于log日志
    public static void showLog(String msg) {
        if (UrlConfig.LOG_FLAG && !TextUtils.isEmpty(msg)) {
            Log.e("test", msg);
        }
    }
    // 弹出提示框 显示提示信息
    public static void showToast(String text){
        if(!TextUtils.isEmpty(text)){
            Toast.makeText(MyApplication.getContext(),text, Toast.LENGTH_SHORT).show();
        }
    }
    // 时间格式转成String格式
    public static String timeToString(long time, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(new Date(time));
    }

    private static SharedPreferences getSp(){
        if (sp == null) {
            //第一个参数是preferece的名称(比如：MyPref),第二个参数是打开的方式（一般选择private方式
            sp =MyApplication.getContext().getSharedPreferences("sports", Context.MODE_PRIVATE);
        }
        return sp;
    }

    public static void spPutString(String key, String value) {
        //基于XML文件存储key-value键值对数据
        getSp().edit().putString(key,value).commit();
    }

    public static String spGetString(String key) {
        return getSp().getString(key,null);
    }
    /**
     * 设置对话框
     *
     * @param activity
     * @param dialog
     * @param position 位置 Gravity.CENTER等
     * @param animId   -1表示没有动画
     * @param scaleW   {0~1}对话框的宽度=屏幕宽度*scaleW -1表示包裹内容
     * @param scaleH   {0~1}对话框的高度=屏幕高度*scaleH -1表示包裹内容
     */
    public static void setDialog(Activity activity, Dialog dialog, int position, int animId, double scaleW, double scaleH) {

        Window window = dialog.getWindow();
        window.setGravity(position);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        layoutParams.dimAmount = 0.2f;
        window.setAttributes(layoutParams);

        // 此处可以设置dialog显示的位置
        if (animId != -1) {
            window.setWindowAnimations(animId);
            // 添加动画
        }
        dialog.show();
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth() * scaleW);
        // 设置宽度
        if (scaleH > 0) {
            lp.height = (int) (display.getHeight() * scaleH);
        }
        dialog.getWindow().setAttributes(lp);
        dialog.setCancelable(true);

    }
    public static void toPermission(Activity context) {
        String brand = Build.BRAND;
        if (TextUtils.isEmpty(brand)) return;
        if (brand.equalsIgnoreCase("Xiaomi")) {
            gotoMiuiPermission(context);
        } else if (brand.equalsIgnoreCase("HUAWEI")) {
            gotoHuaweiPermission(context);
        } else if (brand.equalsIgnoreCase("Meizu")) {
            gotoMeizuPermission(context);
        } else {
            context.startActivity(getAppDetailSettingIntent(context));
        }
    }

    public static int dp2px(double dp) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) MyApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        final float scale = dm.density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 跳转到miui的权限管理页面
     */
    public static void gotoMiuiPermission(Activity context) {
        try { // MIUI 8
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(localIntent);
        } catch (Exception e) {
            try { // MIUI 5/6/7
                Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                localIntent.putExtra("extra_pkgname", context.getPackageName());
                context.startActivity(localIntent);
            } catch (Exception e1) { // 否则跳转到应用详情
                context.startActivity(getAppDetailSettingIntent(context));
            }
        }
    }

    /**
     * 跳转到魅族的权限管理系统
     */
    public static void gotoMeizuPermission(Activity context) {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            context.startActivity(getAppDetailSettingIntent(context));
        }
    }

    /**
     * 华为的权限管理页面
     */
    public static void gotoHuaweiPermission(Activity context) {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            context.startActivity(getAppDetailSettingIntent(context));
        }

    }

    /**
     * 获取应用详情页面intent（如果找不到要跳转的界面，也可以先把用户引导到系统设置页面）
     *
     * @return
     */
    public static Intent getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        return localIntent;
    }

    /**
     * 获取文件的实际路径
     *
     * @param context
     * @param path
     * @return
     */
    public static String getRealFilePath(Context context, String path) {
        log("test", "真实位置之前=" + path);
        try {
            if (!path.startsWith("content") && !path.startsWith("file")) {
                path = URLEncoder.encode(path, "utf-8").replaceAll("\\+", "%20");
                log("test", "2真实位置之前=" + path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(path))
            return null;
        final Uri uri = Uri.parse(path);
        return getRealFilePath(context, uri);
    }

    /**
     * 获取文件的实际路径
     */
    public static String getRealFilePath(Context context, Uri uri) {
        if (uri == null)
            return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
            log("test", "真实位置1=" + data);
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
            log("test", "真实位置2=" + data);
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }

                }
                cursor.close();
            }
            if (data == null) {
                data = getImageAbsolutePath(context, uri);
            }
            log("test", "真实位置3=" + data);
        }
        return data;
    }
    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param context
     * @param imageUri
     * @author yaoxing
     * @date 2014-10-12
     */
    @TargetApi(19)
    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    //Android uri转真实路径   https://blog.csdn.net/androidzmm/article/details/82886392

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    public static void log(String tag, String content) {
        if (UrlConfig.LOG_FLAG) {
            Log.e(tag, content);
        }
    }
}
