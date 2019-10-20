package cn.wandersnail.commons.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

/**
 * date: 2019/8/6 14:16
 * author: zengfansheng
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private File logDir;
    private DocumentFile logDirFile;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Callback callback;
    private final String appVerName;
    private final String packageName;
    private final String appName;
    private final Map<String, String> customInfoMap = new HashMap<>();
    private final Context context;

    /**
     * Android Q上不可用
     * 
     * @param context
     * @param logDir
     * @param callback
     */
    @Deprecated
    public CrashHandler(@NonNull Context context, @NonNull File logDir, Callback callback) {
        this(context, callback);
        this.logDir = logDir;
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }
    
    public CrashHandler(@NonNull Context context, @NonNull DocumentFile logDir, Callback callback) {
        this(context, callback);
        this.logDirFile = logDir;
    }
    
    private CrashHandler(@NonNull Context context, Callback callback) {
        this.callback = callback;
        this.context = context.getApplicationContext();
        packageName = context.getPackageName();
        String appName = "CrashLogs";
        String appVerName = "null";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
            appVerName = packageInfo.versionName;
            appName = context.getResources().getString(packageInfo.applicationInfo.labelRes);
        } catch (Exception ignore) {
        }
        this.appName = appName;
        this.appVerName = appVerName;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 添加自定义打印信息
     *
     * @param name  信息名称
     * @param value 信息值
     */
    public void addCustomInformation(String name, String value) {
        customInfoMap.put(name, value);
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (saveErrorLog(e)) {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        } else if (defaultHandler != null) {
            defaultHandler.uncaughtException(t, e);
        }
    }

    private boolean saveErrorLog(Throwable e) {
        String time = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(System.currentTimeMillis());
        String filename = String.format("crash_log_%s.txt", time);
        StringWriter sw = new StringWriter();
        OutputStream out = null;
        InputStream inputStream = null;
        try {
            PrintWriter pw = new PrintWriter(sw);
            pw.println("*********************************** CRASH START ***********************************");
            String crashTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).format(System.currentTimeMillis());
            pw.println("CRASH_TIME=" + crashTime);
            //获取手机的环境
            appendParams(pw, Arrays.asList("DEVICE", "MODEL", "SUPPORTED_ABIS", "REGION", "SOFT_VERSION", "BRAND"),
                    Build.class.getDeclaredFields());
            appendParams(pw, Arrays.asList("RELEASE", "SECURITY_PATCH", "CODENAME"), Build.VERSION.class.getDeclaredFields());
            pw.println("APP_VERSION=" + appVerName);
            pw.println("APP_NAME=" + appName);
            pw.println("APP_PACKAGE_NAME=" + packageName);
            for (Map.Entry<String, String> entry : customInfoMap.entrySet()) {
                pw.println(entry.getKey() + "=" + entry.getValue());
            }
            e.printStackTrace(pw);
            pw.println("*********************************** CRASH END ***********************************\n");
            String detailError = sw.toString();
            if (logDirFile == null) {
                logDirFile = DocumentFile.fromFile(logDir);
            }
            DocumentFile originFile = logDirFile.findFile(filename);
            DocumentFile logFile = logDirFile.createFile("text/plain", filename + ".tmp");            
            if (originFile != null) {
                inputStream = context.getContentResolver().openInputStream(originFile.getUri());
            }
            if (logFile != null) {
                out = context.getContentResolver().openOutputStream(logFile.getUri(), "rwt");
                if (out != null) {
                    if (inputStream != null) {
                        byte[] bytes = new byte[10240];
                        int len;
                        while ((len = inputStream.read(bytes)) != -1) {
                            out.write(bytes, 0, len);
                        }
                        originFile.delete();
                    }
                    out.write(detailError.getBytes());
                    out.close();
                    logFile.renameTo(filename);
                }
            }
            if (callback != null) {
                return callback.onSaved(detailError, e);
            } else {
                return false;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private void appendParams(PrintWriter pw, List<String> needInfos, Field[] fields) throws IllegalAccessException {
        for (Field field : fields) {
            field.setAccessible(true);
            if (needInfos.contains(field.getName().toUpperCase(Locale.ENGLISH))) {
                String value = "";
                Object o = field.get(null);
                if (o != null) {
                    if (o.getClass().isArray()) {
                        StringBuilder sb = new StringBuilder();
                        Object[] os = (Object[]) o;
                        for (int i = 0; i < os.length; i++) {
                            Object o1 = os[i];
                            if (i == 0) {
                                sb.append("[");
                            }
                            if (i == os.length - 1) {
                                sb.append(o1);
                                sb.append("]");
                            }
                            if (i != os.length - 1) {
                                sb.append(o1).append(",");
                            }
                        }
                        value = sb.toString();
                    } else {
                        value = o.toString();
                    }
                }
                pw.println(field.getName() + "=" + value);
            }
        }
    }

    public interface Callback {
        /**
         * 日志保存完毕
         *
         * @param detailError 详细错误信息
         * @param e           原始的异常信息
         * @return true：直接杀死进程；false：交给默认处理器
         */
        boolean onSaved(@NonNull String detailError, @NonNull Throwable e);
    }
}
