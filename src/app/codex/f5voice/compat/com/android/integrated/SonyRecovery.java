package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;import android.app.job.*;import android.os.Build;
final class SonyRecovery {
 static final int JOB=7519;
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("sony-recovery",0);}
 static boolean enabled(Context c){return DeviceProfile.sony()&&prefs(c).getBoolean("enabled",false);}
 static void enable(Context c){if(!DeviceProfile.sony()){status(c,"此设备无需索尼辅助");return;}prefs(c).edit().putBoolean("enabled",true).commit();SonyImeBridge.enabled(true);schedule(c);start(c);}
 static void start(Context c){if(!enabled(c))return;try{c.startForegroundService(new Intent(c,SonyRecoveryService.class));}catch(RuntimeException e){status(c,"等待系统允许启动；打开辅助状态可重试");}}
 static void pause(Context c){prefs(c).edit().putBoolean("enabled",false).commit();((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOB);SonyImeBridge.enabled(false);c.stopService(new Intent(c,SonyRecoveryService.class));status(c,"已暂停，语音键保留索尼助手");}
 static void schedule(Context c){JobScheduler j=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);j.schedule(new JobInfo.Builder(JOB,new ComponentName(c,SonyRecoveryJob.class)).setPeriodic(15*60*1000).setPersisted(true).build());}
 static void status(Context c,String text){if(!text.equals(prefs(c).getString("status","")))prefs(c).edit().putString("status",text).apply();}
 static String status(Context c){return prefs(c).getString("status","尚未启用自动恢复");}
}
