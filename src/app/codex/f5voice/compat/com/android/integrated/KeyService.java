package app.codex.f5voice.compat.com.android.integrated;
import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
public final class KeyService extends AccessibilityService {
 static final VoiceGate gate=new VoiceGate();
 @Override public void onServiceConnected(){RecoveryState.start(this);Diag.log("filter_connected");if(!new VoiceSettings(this).cloud())LocalRecognizer.shared().load(this,null);}
 @Override protected boolean onKeyEvent(KeyEvent e){
  if(!DeviceProfile.f5())return false;
  int action=gate.key(e.getKeyCode(),e.getAction()==KeyEvent.ACTION_DOWN,e.getRepeatCount(),e.getDeviceId()>0);
  if(action==VoiceGate.PASS)return false;
  Diag.log("owned_key action="+e.getAction()+" decision="+action+" generation="+gate.generation);
  VoiceIme ime=VoiceIme.instance;
  if(ime!=null){if(action==VoiceGate.START)ime.startVoice();else if(action==VoiceGate.STOP)ime.stopVoice();}
  return true;
 }
 @Override public void onAccessibilityEvent(AccessibilityEvent event){}
 @Override public void onInterrupt(){if(VoiceIme.instance!=null)VoiceIme.instance.cancelVoice();}
 @Override public void onDestroy(){if(VoiceIme.instance!=null)VoiceIme.instance.cancelVoice();gate.editor(false);super.onDestroy();}
}
