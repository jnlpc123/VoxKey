package app.codex.f5voice;
import android.app.*;import android.content.*;import android.os.*;import java.util.concurrent.*;
public final class SonyRecoveryService extends Service {
 private ScheduledExecutorService worker;private volatile LocalAdb adb;private volatile boolean stopping;private int failures;private String lastStatus;
 private Notification notification(String text){Intent i=new Intent(this,SonyRecoveryActivity.class);PendingIntent p=PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);return new Notification.Builder(this,"sony-recovery").setSmallIcon(getResources().getIdentifier("notification_mic","drawable",getPackageName())).setContentTitle("VoxKey 语音键辅助").setContentText(text).setContentIntent(p).setOngoing(true).build();}
 private void status(String text){if(text.equals(lastStatus))return;lastStatus=text;SonyRecovery.status(this,text);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(7519,notification(text));}
 @Override public void onCreate(){super.onCreate();NotificationManager m=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);m.createNotificationChannel(new NotificationChannel("sony-recovery","语音键辅助状态",NotificationManager.IMPORTANCE_MIN));startForeground(7519,notification("正在检查辅助连接"));worker=Executors.newSingleThreadScheduledExecutor(r->new Thread(r,"VoxKey-Sony-Recovery"));worker.execute(this::check);}
 private void check(){if(stopping)return;if(!SonyRecovery.enabled(this)){stopSelf();return;}long delay=5000;
  try{
   if(SonyImeBridge.ready()){failures=0;status("辅助已连接 · 自动恢复已开启");}
   else{status("正在连接本机 ADB；首次请在电视允许调试");LocalAdb client=new LocalAdb();adb=client;
    try{client.connect(this);if(!stopping&&SonyRecovery.enabled(this)){status("正在恢复辅助程序");client.deploy(this);}}finally{client.close();adb=null;}
    delay=RecoveryPolicy.delay(failures++);
   }
  }catch(Exception e){if(!stopping){status(e instanceof java.net.ConnectException?"本机调试端口未开启 · 将自动重试":"连接待恢复 · 请检查电视的 USB 调试授权");Diag.log("sony_recovery_retry "+e.getClass().getSimpleName()+" "+e.getMessage());}delay=RecoveryPolicy.delay(failures++);}
  if(!stopping)try{worker.schedule(this::check,delay,TimeUnit.MILLISECONDS);}catch(RejectedExecutionException ignored){}
 }
 @Override public int onStartCommand(Intent i,int flags,int id){return SonyRecovery.enabled(this)?START_STICKY:START_NOT_STICKY;}
 @Override public IBinder onBind(Intent i){return null;}
 @Override public void onDestroy(){stopping=true;LocalAdb client=adb;if(client!=null)client.close();if(worker!=null)worker.shutdownNow();super.onDestroy();}
}
