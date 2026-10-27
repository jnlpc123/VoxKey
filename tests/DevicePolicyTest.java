package app.codex.f5voice.compat.com.android.integrated;
public final class DevicePolicyTest {
 public static void main(String[] args){
  if(DevicePolicy.kind("Dangbei","DBF5")!=DevicePolicy.F5)throw new AssertionError("F5");
  if(DevicePolicy.kind("Sony","BRAVIA 4K GB ATV3")!=DevicePolicy.SONY)throw new AssertionError("Sony");
  if(DevicePolicy.kind("SONY","BRAVIA")!=DevicePolicy.SONY)throw new AssertionError("case");
  if(DevicePolicy.kind("Other","BRAVIA")!=DevicePolicy.OTHER)throw new AssertionError("other");
  if(DevicePolicy.kind(null,null)!=DevicePolicy.OTHER)throw new AssertionError("missing");
  if(DevicePolicy.kind("Sony","DBF5")!=DevicePolicy.F5)throw new AssertionError("F5 priority");
  if(!DevicePolicy.sonySupported("Sony","BRAVIA",26)||!DevicePolicy.sonySupported("Sony","BRAVIA",28))throw new AssertionError("supported SDK");
  if(DevicePolicy.sonySupported("Sony","BRAVIA",25)||DevicePolicy.sonySupported("Sony","BRAVIA",29))throw new AssertionError("unsupported SDK");
  if(DevicePolicy.sonySupported("Dangbei","DBF5",28))throw new AssertionError("backend isolation");
  System.out.println("PASS 11 device capability routing checks");
 }
}
