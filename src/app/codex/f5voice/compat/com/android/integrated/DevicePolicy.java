package app.codex.f5voice.compat.com.android.integrated;
final class DevicePolicy {
 static final int OTHER=0,F5=1,SONY=2;
 static boolean sonySupported(String manufacturer,String model,int sdk){return sdk>=26&&sdk<=28&&kind(manufacturer,model)==SONY;}
 static int kind(String manufacturer,String model){
  if("DBF5".equals(model))return F5;
  if("Sony".equalsIgnoreCase(manufacturer))return SONY;
  return OTHER;
 }
}
