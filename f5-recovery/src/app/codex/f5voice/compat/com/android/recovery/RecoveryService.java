package app.codex.f5voice.compat.com.android.recovery;
import android.app.*;import android.content.*;import android.os.*;import android.provider.Settings;import android.util.Log;
public final class RecoveryService extends Service {
 private final Handler main=new Handler();private RecoveryPolicy.Session session;private SettingsRepair repair;private String status;private long nextAttempt;private int failures,generation;private boolean registered;
 private final BroadcastReceiver power=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
  if(Intent.ACTION_SCREEN_OFF.equals(i.getAction())){session.power(session.boot,false);generation++;nextAttempt=0;save();}
  else if(Intent.ACTION_SCREEN_ON.equals(i.getAction())){session.power(Integer.toString(Settings.Global.getInt(getContentResolver(),"boot_count",-1)),true);generation++;nextAttempt=SystemClock.elapsedRealtime()+1800;failures=0;save();show("已唤醒，正在恢复 VoxKey");}
  main.removeCallbacks(tick);main.post(tick);
 }};
 private final Runnable tick=new Runnable(){public void run(){
  if(!RecoveryState.enabled(RecoveryService.this)){stopSelf();return;}
  boolean on=((PowerManager)getSystemService(POWER_SERVICE)).isInteractive();String boot=Integer.toString(Settings.Global.getInt(getContentResolver(),"boot_count",-1));
  boolean changed=!session.boot.equals(boot)||session.interactive!=on;boolean wake=session.power(boot,on);
  if(changed){generation++;save();}if(wake){nextAttempt=SystemClock.elapsedRealtime()+1800;failures=0;show("已唤醒，正在恢复 VoxKey");}
  if(on&&((UserManager)getSystemService(USER_SERVICE)).isUserUnlocked()&&session.pending&&SystemClock.elapsedRealtime()>=nextAttempt){
   try{repair.available();if(!session.imeDone){repair.ime();session.imeDone=true;save();}repair.accessibility();session.complete();save();failures=0;show("已恢复 VoxKey · 等待下次开机或唤醒");Log.i("VoxKeyRecovery","restored boot/wake settings");followUp(generation,3000);followUp(generation,9000);}
   catch(Exception e){nextAttempt=SystemClock.elapsedRealtime()+Math.min(60000,3000L<<Math.min(failures++,4));show("待恢复："+e.getMessage());Log.w("VoxKeyRecovery","restore pending "+e.getClass().getSimpleName());}
  }
  main.postDelayed(this,2000);
 }};
 private void followUp(int ticket,long delay){main.postDelayed(()->{if(ticket!=generation||!RecoveryState.enabled(this)||!((PowerManager)getSystemService(POWER_SERVICE)).isInteractive())return;try{repair.accessibility();}catch(Exception e){show("语音键服务待恢复，请打开助手查看");}},delay);}
 private void save(){RecoveryState.prefs(this).edit().putString("boot",session.boot).putBoolean("interactive",session.interactive).putBoolean("pending",session.pending).putBoolean("imeDone",session.imeDone).apply();}
 private Notification notice(String text){PendingIntent p=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);return new Notification.Builder(this,"recovery").setSmallIcon(getResources().getIdentifier("icon","drawable",getPackageName())).setContentTitle("VoxKey 恢复助手").setContentText(text).setOngoing(true).setContentIntent(p).build();}
 private void show(String text){if(text.equals(status))return;status=text;RecoveryState.status(this,text);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(5691,notice(text));}
 @Override public void onCreate(){super.onCreate();NotificationManager n=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);n.createNotificationChannel(new NotificationChannel("recovery","输入法自动恢复",NotificationManager.IMPORTANCE_MIN));startForeground(5691,notice("正在检查恢复状态"));android.content.SharedPreferences p=RecoveryState.prefs(this);session=new RecoveryPolicy.Session(p.getString("boot",""),p.getBoolean("interactive",false),p.getBoolean("pending",true));session.imeDone=p.getBoolean("imeDone",false);repair=new SettingsRepair(this);
  // A restarted helper may have missed SCREEN_OFF/ON. A normal manual IME switch leaves KEY registered;
  // the combined missing-KEY + different-IME signature identifies the vendor cleanup needing repair.
  if(RecoveryPolicy.cleanupSignature(repair.get(Settings.Secure.DEFAULT_INPUT_METHOD),repair.get(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES))){session.manual();save();}
  IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_SCREEN_ON);f.addAction(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_USER_UNLOCKED);registerReceiver(power,f);registered=true;main.post(tick);}
 @Override public int onStartCommand(Intent i,int flags,int id){if(!RecoveryState.enabled(this)){stopSelf();return START_NOT_STICKY;}if(i!=null&&"restore".equals(i.getAction())){session.manual();generation++;nextAttempt=0;save();main.removeCallbacks(tick);main.post(tick);}return START_STICKY;}
 @Override public IBinder onBind(Intent i){return null;}
 @Override public void onDestroy(){main.removeCallbacksAndMessages(null);if(registered)unregisterReceiver(power);super.onDestroy();}
}
