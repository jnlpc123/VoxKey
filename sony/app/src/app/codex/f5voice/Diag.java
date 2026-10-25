package app.codex.f5voice;
import android.util.Log;
/** Never pass recognized text, PCM or input contents here. */
final class Diag {
 static void log(String event){Log.w("F5Voice",event);}
}
