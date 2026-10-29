package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;import android.content.pm.PackageManager;import android.provider.Settings;import android.view.inputmethod.*;
final class SetupState {
 final boolean mic,ime,selected,service,capture;
 SetupState(Context c){
  mic=c.checkSelfPermission("android.permission.RECORD_AUDIO")==PackageManager.PERMISSION_GRANTED;
  boolean enabled=false;for(InputMethodInfo info:((InputMethodManager)c.getSystemService(Context.INPUT_METHOD_SERVICE)).getEnabledInputMethodList())if(c.getPackageName().equals(info.getPackageName()))enabled=true;ime=enabled;
  selected=RecoveryPolicy.same(RecoveryPolicy.IME,Settings.Secure.getString(c.getContentResolver(),Settings.Secure.DEFAULT_INPUT_METHOD));
  service=Settings.Secure.getInt(c.getContentResolver(),Settings.Secure.ACCESSIBILITY_ENABLED,0)==1&&RecoveryPolicy.contains(Settings.Secure.getString(c.getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES),RecoveryPolicy.KEY,false);
  capture=DeviceProfile.sonyBrand()?DeviceProfile.sony()&&SonyImeBridge.ready():CaptureSettings.enabled(c);
 }
 boolean ready(){return SetupPolicy.next(mic,ime,selected,service,capture)==5;}
}
