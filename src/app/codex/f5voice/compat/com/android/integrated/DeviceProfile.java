package app.codex.f5voice.compat.com.android.integrated;
import android.os.Build;
final class DeviceProfile {
 static boolean f5(){return DevicePolicy.kind(Build.MANUFACTURER,Build.MODEL)==DevicePolicy.F5;}
 static boolean dangbei(){return DevicePolicy.dangbei(Build.MANUFACTURER,Build.BRAND,Build.MODEL)&&!sonyBrand();}
 static boolean sonyBrand(){return "Sony".equalsIgnoreCase(Build.MANUFACTURER)||"Sony".equalsIgnoreCase(Build.BRAND);}
 static boolean sony(){return DevicePolicy.sonySupported(Build.MANUFACTURER,Build.MODEL,Build.VERSION.SDK_INT);}
}
