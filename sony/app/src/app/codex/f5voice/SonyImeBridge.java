package app.codex.f5voice;
import android.os.*;
/** Binder IPC authenticated in both directions; no network or global assistant mutation. */
final class SonyImeBridge implements AutoCloseable {
 interface Listener{void key(boolean down);void disconnected();}
 static final String DESCRIPTOR="app.codex.f5voice.SonyBridgeV1";
 private static final Object LOCK=new Object();private static final Handler MAIN=new Handler(Looper.getMainLooper());
 private static SonyImeBridge current;private static IBinder helper;private static IBinder.DeathRecipient death;private static boolean active;private static int protocol;private static String build;private static boolean enabled;
 static void enabled(boolean value){synchronized(LOCK){enabled=value;if(!value)stopHelper();else sendState();}}
 private final Listener listener;
 private static final Binder EVENTS=new Binder(){@Override protected boolean onTransact(int code,Parcel data,Parcel reply,int flags)throws RemoteException{
  if(code!=2&&code!=3&&code!=4)return super.onTransact(code,data,reply,flags);if(Binder.getCallingUid()!=2000)throw new SecurityException("ADB shell only");data.enforceInterface(DESCRIPTOR);
  if(code==4){detach(data.readStrongBinder());if(reply!=null)reply.writeNoException();return true;}
  final IBinder origin;synchronized(LOCK){origin=helper;}
  MAIN.post(()->{synchronized(LOCK){if(helper==origin&&origin!=null&&current!=null){if(enabled||code==3)current.listener.key(code==2);}}});return true;
 }};
 SonyImeBridge(Listener listener){this.listener=listener;synchronized(LOCK){current=this;active=false;sendState();}}
 static Bundle connect(IBinder binder,int version,String helperBuild)throws RemoteException{
  if(Binder.getCallingUid()!=2000||binder==null)throw new SecurityException("ADB shell only");
  synchronized(LOCK){if(helper!=null&&helper.isBinderAlive()&&helper!=binder)throw new IllegalStateException("already connected");helper=binder;protocol=version;build=helperBuild;
   death=()->{synchronized(LOCK){if(helper!=binder)return;helper=null;}MAIN.post(()->{synchronized(LOCK){if(current!=null)current.listener.disconnected();}});};binder.linkToDeath(death,0);if(!enabled)stopHelper();else sendState();}
  Bundle result=new Bundle();result.putBinder("events",EVENTS);Diag.log("sony_helper_connected");return result;
 }
 private static void detach(IBinder binder){synchronized(LOCK){if(helper!=binder)return;if(death!=null)helper.unlinkToDeath(death,0);helper=null;}MAIN.post(()->{synchronized(LOCK){if(current!=null)current.listener.disconnected();}});}
 private static void sendState(){if(helper==null)return;Parcel data=Parcel.obtain();try{data.writeInterfaceToken(DESCRIPTOR);data.writeInt(active&&enabled?1:0);if(!helper.transact(1,data,null,IBinder.FLAG_ONEWAY))throw new RemoteException("state");}catch(RemoteException ignored){helper=null;}finally{data.recycle();}}
 static boolean connected(){synchronized(LOCK){return helper!=null&&helper.isBinderAlive();}}
 static boolean ready(){synchronized(LOCK){return helper!=null&&helper.isBinderAlive()&&protocol>=2&&"20261026.3".equals(build)&&enabled;}}
 static void stopHelper(){synchronized(LOCK){if(helper==null)return;Parcel p=Parcel.obtain();try{p.writeInterfaceToken(DESCRIPTOR);p.writeInt(2);helper.transact(1,p,null,IBinder.FLAG_ONEWAY);}catch(RemoteException ignored){}finally{p.recycle();}}}
 void update(boolean allowed){synchronized(LOCK){if(current==this){active=allowed;sendState();}}}
 @Override public void close(){synchronized(LOCK){if(current==this){active=false;sendState();current=null;}}}
}
