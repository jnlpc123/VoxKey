import android.view.KeyEvent;
import java.util.*;
public final class ForwarderTest {
 static int checks;static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
 public static void main(String[] args)throws Exception{
  Map<Integer,Integer> map=new HashMap<>();map.put(105,21);map.put(42,59);List<KeyEvent> events=new ArrayList<>();EvKeyForwarder f=new EvKeyForwarder(map,e->{events.add(e);return true;},31,769);
  f.forward(105,1);f.forward(105,2);check(f.held(),"must retain grab across forwarded down/repeat");f.forward(105,0);check(!f.held(),"release after paired up");check(events.size()==3,"repeat retained");check(events.get(1).getRepeatCount()==1,"repeat count preserved");check(events.get(0).getDeviceId()==31&&events.get(2).getDeviceId()==31,"paired device identity");check(events.get(0).getScanCode()==105&&events.get(2).getScanCode()==105,"scan identity");check(events.get(0).getDownTime()==events.get(2).getDownTime(),"pair downtime");
  f.forward(42,1);check(events.get(3).isShiftPressed(),"modifier preserved");f.releaseAll();check(events.get(4).getAction()==1&&!events.get(4).isShiftPressed()&&!f.held(),"device removal releases modifiers");
  boolean rejected=false;EvKeyForwarder bad=new EvKeyForwarder(map,e->false,31,769);try{bad.forward(105,1);}catch(Exception expected){rejected=true;}check(rejected&&!bad.held(),"rejected injection detected");System.out.println("PASS "+checks+" Sony forwarding ownership checks");
 }
}
