package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;import android.os.*;import android.net.Uri;import java.util.*;import java.util.concurrent.*;
/** Local Binder only. No credentials in intents, logs, files, status messages or URLs. */
final class MigrationClient {
 private static final Handler MAIN=new Handler(Looper.getMainLooper());private static final ExecutorService WORK=Executors.newSingleThreadExecutor(r->new Thread(r,"VoxKey-migrate"));
 private static final Set<Runnable> listeners=new LinkedHashSet<>();static volatile boolean running;
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("migration",0);}
 static boolean legacy(Context c){try{c.getPackageManager().getPackageInfo("app.codex.f5voice",0);return true;}catch(Exception e){return false;}}
 static String summary(Context c){return prefs(c).getString("status",legacy(c)?"尚未迁移旧版设置":"无需迁移旧版设置");}
 static synchronized void remove(Runnable r){listeners.remove(r);}
 static synchronized void start(Context context,Runnable done){
  Context c=context.getApplicationContext();if(done!=null)listeners.add(done);if(running)return;
  VoiceSettings settings=new VoiceSettings(c);
  if(settings.migrationVerified()){
   prefs(c).edit().putBoolean("done",true).putBoolean("settingsVerified",true).putBoolean("keyVerified",settings.migrationKeyVerified()).putString("status",settings.migrationKeyVerified()?"设置已迁移并校验":"设置已迁移并校验").commit();finish();return;
  }

  if(!MigrationPolicy.shouldImport(prefs(c).getBoolean("done",false),settings.configured(),legacy(c))){
   if(!prefs(c).getBoolean("done",false))prefs(c).edit().putBoolean("done",true).putString("status",settings.configured()?"保留新版已有设置":"无需迁移旧版设置").commit();finish();return;
  }
  running=true;prefs(c).edit().putString("status","正在迁移…").apply();
  WORK.execute(()->{Bundle b=null;try{
   b=c.getContentResolver().call(Uri.parse("content://app.codex.f5voice.migration"),"export-v1",null,null);
   if(b==null||!MigrationPolicy.valid(b.getInt("schema",0),b.getBoolean("hasKey",false),b.getString("key")))throw new IllegalStateException("migration schema");
   int result=settings.importLegacy(b.getString("workspace",""),b.getString("key"),b.getBoolean("cloud",false));
   if(!prefs(c).edit().putBoolean("done",true).putBoolean("keyVerified",result==2).putBoolean("settingsVerified",result!=0).putString("status",result==0?"保留新版已有设置":result==2?"设置已迁移并校验":"设置已迁移并校验").commit())throw new IllegalStateException("migration status");
  }catch(Exception e){prefs(c).edit().putString("status","迁移未完成，请重试").apply();}
  finally{if(b!=null){b.remove("key");b.clear();}synchronized(MigrationClient.class){running=false;finish();}}
  });
 }
 private static void finish(){for(Runnable r:listeners)MAIN.post(r);listeners.clear();}
}
