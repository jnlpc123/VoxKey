package app.codex.f5voice.compat.com.android.recovery;
import android.content.*;import android.app.job.*;import android.os.Build;
final class RecoveryState {
 static final int JOB=5691;
 static Context device(Context c){return c.createDeviceProtectedStorageContext();}
 static SharedPreferences prefs(Context c){return device(c).getSharedPreferences("recovery",0);}
 static boolean supported(){return "DBF5".equals(Build.MODEL);}
 static boolean enabled(Context c){return supported()&&prefs(c).getBoolean("enabled",false);}
 static void start(Context c){if(!enabled(c))return;try{c.startForegroundService(new Intent(c,RecoveryService.class));}catch(RuntimeException e){status(c,"等待系统启动恢复服务");}}
 static void enable(Context c){if(!supported()){status(c,"此版本仅适配当贝 F5（DBF5）");return;}prefs(c).edit().putBoolean("enabled",true).putBoolean("pending",true).putBoolean("imeDone",false).commit();schedule(c);manual(c);}
 static void manual(Context c){if(!enabled(c))return;try{c.startForegroundService(new Intent(c,RecoveryService.class).setAction("restore"));}catch(RuntimeException e){status(c,"暂时无法启动恢复服务");}}
 static void disable(Context c){prefs(c).edit().putBoolean("enabled",false).commit();((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOB);c.stopService(new Intent(c,RecoveryService.class));status(c,"已停用，不再自动恢复；当前输入法保持不变");}
 static void schedule(Context c){((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).schedule(new JobInfo.Builder(JOB,new ComponentName(c,RecoveryJob.class)).setPeriodic(15*60*1000L).setPersisted(true).build());}
 static void status(Context c,String text){SharedPreferences p=prefs(c);if(!text.equals(p.getString("status","")))p.edit().putString("status",text).apply();}
}
