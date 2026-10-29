package app.codex.f5voice.compat.com.android.integrated;
final class DevicePolicy {
 static final int OTHER=0,F5=1,SONY=2,DANGBEI=3;
 static boolean brand(String value){if(value==null)return false;String v=value.toLowerCase(java.util.Locale.ROOT);return v.contains("dangbei")||v.contains("当贝");}
 static boolean dangbei(String manufacturer,String brand,String model){return "DBF5".equals(model)||DevicePolicy.brand(manufacturer)||DevicePolicy.brand(brand);}
 static boolean recoverySupported(String manufacturer,String brand,String model,int sdk){return sdk>=26&&dangbei(manufacturer,brand,model)&&!"Sony".equalsIgnoreCase(manufacturer)&&!"Sony".equalsIgnoreCase(brand);}
 static boolean sonySupported(String manufacturer,String model,int sdk){return sdk>=26&&sdk<=28&&kind(manufacturer,model)==SONY;}
 static int kind(String manufacturer,String model){
  if("DBF5".equals(model))return F5;
  if("Sony".equalsIgnoreCase(manufacturer))return SONY;
  if(brand(manufacturer))return DANGBEI;
  return OTHER;
 }
}
