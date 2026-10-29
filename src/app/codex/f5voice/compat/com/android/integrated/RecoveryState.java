package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;import android.app.job.*;import android.os.Build;
final class RecoveryState {
 static final int JOB=5691;
 static Context device(Context c){return c.createDeviceProtectedStorageContext();}
 static SharedPreferences prefs(Context c){return device(c).getSharedPreferences("recovery",0);}
 static boolean supported(){return DevicePolicy.recoverySupported(Build.MANUFACTURER,Build.BRAND,Build.MODEL,Build.VERSION.SDK_INT);}
 static boolean enabled(Context c){return supported()&&prefs(c).getBoolean("enabled",false);}
 static void start(Context c){if(!enabled(c))return;try{c.startForegroundService(new Intent(c,RecoveryService.class));}catch(RuntimeException e){status(c,"等待系统启动恢复服务");}}
 static void enable(Context c){if(!supported()){status(c,"自动恢复需要 Android 8 及以上的当贝设备");return;}prefs(c).edit().putBoolean("enabled",true).putBoolean("pending",true).putBoolean("imeDone",false).commit();schedule(c);manual(c);}
 static void manual(Context c){if(!enabled(c))return;try{c.startForegroundService(new Intent(c,RecoveryService.class).setAction("restore"));}catch(RuntimeException e){status(c,"暂时无法启动恢复服务");}}
 static void disable(Context c){prefs(c).edit().putBoolean("enabled",false).commit();((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOB);c.stopService(new Intent(c,RecoveryService.class));status(c,"自动恢复已停用");}
 static void schedule(Context c){((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(new JobInfo.Builder(JOB,new ComponentName(c,RecoveryJob.class)).setPeriodic(15*60*1000L).setPersisted(true).build());}
 static void status(Context c,String text){SharedPreferences p=prefs(c);if(!text.equals(p.getString("status","")))p.edit().putString("status",text).apply();}
}
