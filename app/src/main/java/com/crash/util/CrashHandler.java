package com.crash.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dory on 2018/2/8.
 * 全局捕获异常
 * 当程序发生Uncaught异常的时候,有该类来接管程序,并记录错误日志
 */

public class CrashHandler implements UncaughtExceptionHandler {
    public static String TAG = "CrashHandler";
    // 系统默认的UncaughtException处理类
    private UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler instance = new CrashHandler();
    private Context mContext;

    // 用来存储设备信息和异常信息
    private Map<String, String> info = new HashMap<>();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return instance;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该重写的方法来处理
     */
    public void uncaughtException(Thread thread, Throwable ex) {
        handleException(ex);
        mDefaultHandler.uncaughtException(thread, ex);
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常信息
     * @return true 如果处理了该异常信息;否则返回false.
     */
    public boolean handleException(Throwable ex) {
        if (ex == null)
            return false;
        new Thread() {
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "很抱歉,程序出现异常,即将退出", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        saveCrashInfo2File(ex);
        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = mContext.getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(),
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get("").toString());
                Log.d(TAG, field.getName() + ":" + field.get(""));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key).append("=").append(value).append("\r\n");
        }
        sb.append("\r\n------------------------------------------------------------------------------------------------------------------\r\n");
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// 记得关闭
        String result = writer.toString();
        sb.append(result);
        // 保存文件
        String time = format.format(new Date());
        String fileName = "crash-" + time + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "crash");
                Log.i(TAG, "dir:" + dir.toString());
                if (!dir.exists())
                    dir.mkdir();
                FileOutputStream fos = new FileOutputStream(new File(dir, fileName));
                fos.write(sb.toString().getBytes());
                fos.close();
                return fileName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "saveCrashInfo2File: error1");
            Log.e(TAG, "saveCrashInfo2File: error2");
            Log.e(TAG, "saveCrashInfo2File: error3");
        }
        return null;
    }
}