package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;
public final class SonyRecoveryReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(SonyRecovery.enabled(c)){SonyRecovery.schedule(c);SonyRecovery.start(c);}}
}
