import com.sun.jna.*;
import java.nio.*;
import java.util.*;
import android.view.KeyEvent;
public final class DrainTest {
 static int checks;static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
 static final class Fake implements SonyInputBridge.C {
  int[][] frames;int read;Fake(int[][] frames){this.frames=frames;}
  public int read(int fd,byte[] data,int count){if(read==frames.length){Native.setLastError(19);return -1;}int[] event=frames[read++];ByteBuffer b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);b.putShort(8,(short)1);b.putShort(10,(short)event[0]);b.putInt(12,event[1]);return 16;}
  public int open(String p,int f){return 0;}public int close(int fd){return 0;}public int ioctl(int f,int r,Pointer p){return 0;}public int ioctl(int f,int r,int v){return 0;}public int inotify_init1(int f){return 0;}public int inotify_add_watch(int f,String p,int m){return 0;}public int poll(Pointer p,int n,int t){return 0;}
 }
 public static void main(String[] args)throws Exception{
  System.setProperty("jna.boot.library.path","/data/local/tmp/voxkey-probe");System.setProperty("jna.tmpdir","/data/local/tmp/voxkey-probe");System.setProperty("jna.nounpack","true");
  Map<Integer,Integer> map=new HashMap<>();map.put(105,21);List<KeyEvent> out=new ArrayList<>();EvKeyForwarder f=new EvKeyForwarder(map,e->{out.add(e);return true;},31,769);
  Fake fake=new Fake(new int[][]{{105,1},{105,2},{217,0},{105,0},{217,1}});SonyInputBridge.c=fake;SonyInputBridge.drain(4,true,f);check(fake.read==4,"drains held voice and nonvoice releases");check(out.size()==3,"preserves unexpected down/repeat/up during drain");check(!f.held(),"no forwarded key stuck");check(fake.read<fake.frames.length,"does not swallow future voice press");
  f.forward(105,1);fake=new Fake(new int[][]{{105,0},{217,1}});SonyInputBridge.c=fake;SonyInputBridge.drain(4,false,f);check(fake.read==1&&!f.held(),"editor-close with nonvoice held drains only existing pair");
  System.out.println("PASS "+checks+" Sony disconnect/expiry drain checks");
 }
}
