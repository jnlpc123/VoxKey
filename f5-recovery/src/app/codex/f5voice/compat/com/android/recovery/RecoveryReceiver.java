package app.codex.f5voice.compat.com.android.recovery;
import android.content.*;
public final class RecoveryReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(RecoveryState.enabled(c)){RecoveryState.schedule(c);RecoveryState.start(c);}}
}
