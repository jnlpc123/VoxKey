package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;
final class CaptureSettings {
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("setup",0);}
 static boolean enabled(Context c){return !DeviceProfile.sonyBrand()&&prefs(c).getBoolean("standardCapture",DeviceProfile.dangbei());}
 static int code(Context c){int value=prefs(c).getInt("voiceCode",0);return value==135||value==219||value==231?value:0;}
 static String codeLabel(Context c){int code=code(c);return code==0?"自动（135 / 219 / 231）":code==135?"F5（135）":code==219?"ASSIST（219）":"VOICE_ASSIST（231）";}
 static void cycle(Context c){int code=code(c);prefs(c).edit().putInt("voiceCode",code==0?135:code==135?219:code==219?231:0).apply();}
}
