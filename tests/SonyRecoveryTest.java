package app.codex.f5voice.compat.com.android.integrated;
import java.io.*;import java.nio.*;import java.util.*;
public class SonyRecoveryTest {
 static int checks;static void check(boolean ok){checks++;if(!ok)throw new AssertionError("check "+checks);}
 public static void main(String[] args)throws Exception{
  check(SonyRecoveryPolicy.delay(0)==5000);check(SonyRecoveryPolicy.delay(1)==15000);check(SonyRecoveryPolicy.delay(20)==60000);
  byte[] frame=SyncFrame.packet("DATA",new byte[]{1,2,3});check(frame.length==11);check(ByteBuffer.wrap(frame,4,4).order(ByteOrder.LITTLE_ENDIAN).getInt()==3);
  ByteArrayInputStream tiny=new ByteArrayInputStream(frame){public int read(byte[] b,int o,int n){return super.read(b,o,Math.min(1,n));}};
  SyncFrame.Reply a=SyncFrame.read(tiny);check(a.id.equals("DATA"));check(Arrays.equals(a.payload,new byte[]{1,2,3}));
  try{SyncFrame.read(new ByteArrayInputStream(new byte[4]));throw new AssertionError();}catch(EOFException expected){checks++;}
  byte[] bad=SyncFrame.header("FAIL",1000000);try{SyncFrame.read(new ByteArrayInputStream(bad));throw new AssertionError();}catch(IOException expected){checks++;}
  check(SonyRecoveryPolicy.shouldStart(true,false));check(!SonyRecoveryPolicy.shouldStart(false,false));check(!SonyRecoveryPolicy.shouldStart(true,true));
  System.out.println("PASS "+checks+" recovery/backoff/SYNC checks");
 }
}
