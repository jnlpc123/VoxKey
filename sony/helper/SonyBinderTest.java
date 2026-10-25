public final class SonyBinderTest {
 public static void main(String[] args)throws Exception{
  int uid=Integer.parseInt(args[0]);SonyInputBridge.Link first=SonyInputBridge.connect(uid);Thread.sleep(50);boolean active=first.active;if(first.events==null)throw new AssertionError("first connection");SonyInputBridge.disconnect(first);first.events.unlinkToDeath(first.death,0);first.alive=false;
  SonyInputBridge.Link second=SonyInputBridge.connect(uid);Thread.sleep(50);if(second.events==null)throw new AssertionError("reconnection");if(second.active!=active)throw new AssertionError("eligibility lost on reconnect");SonyInputBridge.disconnect(second);second.events.unlinkToDeath(second.death,0);second.alive=false;System.out.println("PASS 3 Binder connect/disconnect/reconnect checks; active="+active);
 }
}
