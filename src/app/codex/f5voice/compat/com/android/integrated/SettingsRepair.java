package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;import android.content.pm.*;import android.provider.Settings;
/** Fixed targets and fixed secure settings; never reads input contents or credentials. */
final class SettingsRepair {
 private final Context c;private final ContentResolver resolver;
 SettingsRepair(Context c){this.c=c;resolver=c.getContentResolver();}
 void available()throws Exception{
  if(c.checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")!=PackageManager.PERMISSION_GRANTED)throw new SecurityException("请通过安装脚本授予恢复设置权限");
  ServiceInfo ime=c.getPackageManager().getServiceInfo(ComponentName.unflattenFromString(RecoveryPolicy.IME),0);
  ServiceInfo key=c.getPackageManager().getServiceInfo(ComponentName.unflattenFromString(RecoveryPolicy.KEY),0);
  if(!ime.enabled||!key.enabled||!ime.applicationInfo.enabled)throw new IllegalStateException("请先安装并启用 VoxKey");
 }
 String get(String key){return Settings.Secure.getString(resolver,key);}
 private void put(String key,String value){if(!value.equals(get(key))&&!Settings.Secure.putString(resolver,key,value))throw new IllegalStateException("设置未保存："+key);}
 private void mergeSetting(String key,String target,boolean subtype){
  String seen=get(key);
  for(int attempt=0;attempt<3;attempt++){
   String latest=get(key);String merged=RecoveryPolicy.merge(seen,latest,target,subtype);
   String before=get(key);merged=RecoveryPolicy.merge(merged,before,target,subtype);
   if(!merged.equals(before)&&!Settings.Secure.putString(resolver,key,merged))throw new IllegalStateException("设置未保存："+key);
   String after=get(key);if(RecoveryPolicy.includesAll(after,merged,subtype))return;
   seen=RecoveryPolicy.merge(merged,after,target,subtype);
  }
  throw new IllegalStateException("设置正在变化，稍后重试");
 }
 void accessibility()throws Exception{
  available();mergeSetting(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,RecoveryPolicy.KEY,false);
  if(Settings.Secure.getInt(resolver,Settings.Secure.ACCESSIBILITY_ENABLED,0)!=1&&!Settings.Secure.putInt(resolver,Settings.Secure.ACCESSIBILITY_ENABLED,1))throw new IllegalStateException("无障碍总开关未恢复");
  if(!RecoveryPolicy.contains(get(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),RecoveryPolicy.KEY,false))throw new IllegalStateException("语音键服务登记未恢复");
 }
 void ime()throws Exception{
  available();mergeSetting(Settings.Secure.ENABLED_INPUT_METHODS,RecoveryPolicy.IME,true);put(Settings.Secure.DEFAULT_INPUT_METHOD,RecoveryPolicy.IME);
  if(!RecoveryPolicy.same(get(Settings.Secure.DEFAULT_INPUT_METHOD),RecoveryPolicy.IME))throw new IllegalStateException("默认输入法未恢复");
 }
}
