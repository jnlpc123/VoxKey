import android.os.SystemClock;
import android.view.KeyEvent;
import java.io.IOException;
import java.util.*;
/** Preserves complete synthetic pairs for unexpected nonvoice keys on the mic device. */
final class EvKeyForwarder {
 interface Sink{boolean send(KeyEvent event)throws Exception;}
 private static final class Press {int key,repeats;long time;Press(int key,long time){this.key=key;this.time=time;}}
 private final Map<Integer,Integer> mapping;private final Map<Integer,Press> held=new HashMap<>();private final Sink sink;private final int device,source;private int meta;
 EvKeyForwarder(Map<Integer,Integer> mapping,Sink sink,int device,int source){this.mapping=mapping;this.sink=sink;this.device=device;this.source=source;}
 boolean held(){return !held.isEmpty();}
 void forward(int scan,int value)throws Exception{
  Press press=held.get(scan);long now=SystemClock.uptimeMillis();
  if(value==1&&press==null){Integer key=mapping.get(scan);if(key==null)throw new IOException("unmapped mic key");press=new Press(key,now);held.put(scan,press);}
  if(press==null)return;
  if(value==2)press.repeats++;
  int bit=modifier(press.key);if(value==1)meta|=bit;else if(value==0)meta&=~bit;
  if(value==1&&press.key==KeyEvent.KEYCODE_CAPS_LOCK)meta^=KeyEvent.META_CAPS_LOCK_ON;
  if(value==1&&press.key==KeyEvent.KEYCODE_NUM_LOCK)meta^=KeyEvent.META_NUM_LOCK_ON;
  int flags=KeyEvent.FLAG_FROM_SYSTEM|(value==2&&press.repeats==1?KeyEvent.FLAG_LONG_PRESS:0);
  KeyEvent event=new KeyEvent(press.time,now,value==0?KeyEvent.ACTION_UP:KeyEvent.ACTION_DOWN,press.key,press.repeats,KeyEvent.normalizeMetaState(meta),device,scan,flags,source);
  if(!sink.send(event)){if(value==1)held.remove(scan);throw new IOException("input injection rejected");}
  if(value==0)held.remove(scan);
 }
 void releaseAll(){for(int scan:new ArrayList<>(held.keySet()))try{forward(scan,0);}catch(Exception ignored){}held.clear();meta=0;}
 private static int modifier(int key){switch(key){case KeyEvent.KEYCODE_SHIFT_LEFT:return KeyEvent.META_SHIFT_LEFT_ON;case KeyEvent.KEYCODE_SHIFT_RIGHT:return KeyEvent.META_SHIFT_RIGHT_ON;case KeyEvent.KEYCODE_CTRL_LEFT:return KeyEvent.META_CTRL_LEFT_ON;case KeyEvent.KEYCODE_CTRL_RIGHT:return KeyEvent.META_CTRL_RIGHT_ON;case KeyEvent.KEYCODE_ALT_LEFT:return KeyEvent.META_ALT_LEFT_ON;case KeyEvent.KEYCODE_ALT_RIGHT:return KeyEvent.META_ALT_RIGHT_ON;case KeyEvent.KEYCODE_META_LEFT:return KeyEvent.META_META_LEFT_ON;case KeyEvent.KEYCODE_META_RIGHT:return KeyEvent.META_META_RIGHT_ON;default:return 0;}}
}
