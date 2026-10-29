package app.codex.f5voice.compat.com.android.integrated;
public final class SetupPolicyTest {
 public static void main(String[] args){
  for(int missing=0;missing<5;missing++){boolean[] values={true,true,true,true,true};values[missing]=false;if(SetupPolicy.next(values[0],values[1],values[2],values[3],values[4])!=missing)throw new AssertionError("missing "+missing);}
  if(SetupPolicy.next(true,true,true,true,true)!=5)throw new AssertionError("ready");
  if(SetupPolicy.next(false,false,false,false,false)!=0)throw new AssertionError("order");
  if(!DevicePolicy.dangbei("Dangbei","other","F3"))throw new AssertionError("manufacturer");
  if(!DevicePolicy.dangbei("Amlogic","dangbei","X5"))throw new AssertionError("brand");
  if(!DevicePolicy.dangbei("当贝科技","other","D6"))throw new AssertionError("Chinese brand");
  if(!DevicePolicy.dangbei("unknown","unknown","DBF5"))throw new AssertionError("legacy");
  if(DevicePolicy.dangbei("unknown","unknown","DBOTHER"))throw new AssertionError("do not guess model prefixes");
  if(DevicePolicy.dangbei(null,null,null))throw new AssertionError("missing brand");
  if(DevicePolicy.recoverySupported("Dangbei","Dangbei","X5",24))throw new AssertionError("API24 recovery off");
  if(DevicePolicy.recoverySupported("Dangbei","Dangbei","X5",25))throw new AssertionError("API25 recovery off");
  if(!DevicePolicy.recoverySupported("Dangbei","Dangbei","X5",26))throw new AssertionError("API26 recovery available");
  if(!DevicePolicy.recoverySupported("Amlogic","dangbei","X5",28))throw new AssertionError("brand recovery");
  if(DevicePolicy.recoverySupported("Sony","Dangbei","BRAVIA",28))throw new AssertionError("Sony recovery isolated");
  System.out.println("PASS 18 setup readiness and Dangbei detection checks");
 }
}
