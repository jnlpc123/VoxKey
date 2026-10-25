import android.os.*;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.InputDevice;
import com.sun.jna.*;
import java.io.*;
import java.nio.*;
import java.lang.reflect.*;
import java.util.*;
public final class SonyInputBridge {
 public interface C extends Library {int open(String path,int flags);int close(int fd);int read(int fd,byte[] data,int length);int ioctl(int fd,int request,Pointer data);int ioctl(int fd,int request,int value);int inotify_init1(int flags);int inotify_add_watch(int fd,String path,int mask);int poll(Pointer data,int count,int timeout);}
 static final int MAGIC=0x564B5331;static C c;static volatile boolean stopping;static final Map<Integer,Integer> map=new HashMap<>();
 static RandomAccessFile lockFile;static java.nio.channels.FileLock processLock;
 static final String BUILD="20261026.3";static final String DESCRIPTOR="app.codex.f5voice.SonyBridgeV1";
 static final class Link {volatile boolean alive=true,active;IBinder events,state;IBinder.DeathRecipient death;void event(int value)throws Exception{Parcel data=Parcel.obtain();try{data.writeInterfaceToken(DESCRIPTOR);if(!events.transact(value,data,null,IBinder.FLAG_ONEWAY))throw new IOException("event");}finally{data.recycle();}}}
 static Link connect(int expected)throws Exception{
  Link link=new Link();Binder state=new Binder(){@Override protected boolean onTransact(int code,Parcel data,Parcel reply,int flags)throws RemoteException{if(code!=1)return super.onTransact(code,data,reply,flags);if(Binder.getCallingUid()!=expected)throw new SecurityException("VoxKey UID");data.enforceInterface(DESCRIPTOR);int mode=data.readInt();if(mode==2){stopping=true;link.alive=false;return true;}if(mode!=0&&mode!=1)throw new SecurityException("mode");boolean enabled=mode==1;if(enabled!=link.active)System.out.println("MODE_ACTIVE="+enabled);link.active=enabled;return true;}};
  link.state=state;Object manager=Class.forName("android.app.ActivityManager").getDeclaredMethod("getService").invoke(null);Class<?> api=Class.forName("android.app.IActivityManager");IBinder token=new Binder();String authority="app.codex.f5voice.sony.bridge";
  Object holder=api.getMethod("getContentProviderExternal",String.class,int.class,IBinder.class).invoke(manager,authority,0,token);if(holder==null)throw new IOException("provider unavailable");
  try{Object provider=holder.getClass().getField("provider").get(holder);Bundle extras=new Bundle();extras.putBinder("helper",state);extras.putInt("protocol",2);extras.putString("build",BUILD);Bundle result=(Bundle)Class.forName("android.content.IContentProvider").getMethod("call",String.class,String.class,String.class,Bundle.class).invoke(provider,"com.android.shell","connect",null,extras);link.events=result.getBinder("events");if(link.events==null)throw new IOException("events unavailable");link.death=()->link.alive=false;link.events.linkToDeath(link.death,0);return link;}
  catch(Exception failure){disconnect(link);throw failure;}finally{try{api.getMethod("removeContentProviderExternal",String.class,IBinder.class).invoke(manager,authority,token);}catch(Exception ignored){}}
 }
 public static void main(String[] args)throws Exception{
  if(args.length!=1)throw new IllegalArgumentException("expected VoxKey UID");int expected=Integer.parseInt(args[0]);
  lockFile=new RandomAccessFile("/data/local/tmp/voxkey-probe/helper.lock","rw");processLock=lockFile.getChannel().tryLock();if(processLock==null){lockFile.close();return;}
  System.setProperty("jna.boot.library.path","/data/local/tmp/voxkey-probe");System.setProperty("jna.tmpdir","/data/local/tmp/voxkey-probe");System.setProperty("jna.nounpack","true");c=Native.load("c",C.class);stopping=false;
  try(BufferedReader file=new BufferedReader(new FileReader("/system/usr/keylayout/Generic.kl"))){String line;while((line=file.readLine())!=null){String[] p=line.trim().split("\\s+");if(p.length>=3&&p[0].equals("key"))try{int code=KeyEvent.keyCodeFromString("KEYCODE_"+p[2]);if(code!=0)map.put(Integer.decode(p[1]),code);}catch(Exception ignored){}}}
  System.out.println("SONY_HELPER_READY pid="+android.os.Process.myPid());
    boolean waiting=false;while(!stopping){Link link=null;
   try{link=connect(expected);waiting=false;System.out.println("IPC_CONNECTED active="+link.active);run(link);}
   catch(Exception e){if(!waiting){System.out.println("IPC_WAITING "+e);waiting=true;}}
   finally{if(link!=null){disconnect(link);link.alive=false;if(link.events!=null&&link.death!=null)link.events.unlinkToDeath(link.death,0);}}
   Thread.sleep(250);
  }
 }
 static void disconnect(Link link){if(link==null||link.events==null||link.state==null)return;Parcel data=Parcel.obtain(),reply=Parcel.obtain();try{data.writeInterfaceToken(DESCRIPTOR);data.writeStrongBinder(link.state);if(link.events.transact(4,data,reply,0))reply.readException();}catch(Exception ignored){}finally{data.recycle();reply.recycle();}}
 static int find(){File[] files=new File("/dev/input").listFiles();if(files==null)return -1;for(File file:files){if(!file.getName().startsWith("event"))continue;int fd=c.open(file.getAbsolutePath(),2048);if(fd<0)continue;Memory name=new Memory(256);name.clear();if(c.ioctl(fd,0x81004506,name)>=0&&name.getString(0).equals("SONY TV RC MIC 001"))return fd;c.close(fd);}return -1;}
 static void run(Link link)throws Exception{
  int watch=c.inotify_init1(2048|524288);if(watch<0)throw new IOException("inotify");c.inotify_add_watch(watch,"/dev/input",0x100|0x200);int fd=-1;boolean held=false,previous=false,scan=true;EvKeyForwarder forwarder=null;byte[] events=new byte[16],changes=new byte[4096];Memory poll=new Memory(8);poll.clear();poll.setInt(0,watch);poll.setShort(4,(short)1);
  try{while(link.alive&&!stopping){
   if(link.active&&!previous)scan=true;previous=link.active;
   if(fd>=0&&!link.active&&!held&&(forwarder==null||!forwarder.held())){c.ioctl(fd,0x40044590,0);c.close(fd);fd=-1;System.out.println("VOICE_DEVICE_RELEASED");}
   if(fd<0&&link.active&&scan){scan=false;fd=find();if(fd>=0){if(c.ioctl(fd,0x40044590,1)!=0){c.close(fd);fd=-1;throw new IOException("grab");}int device=-1,source=769;for(int id:InputDevice.getDeviceIds()){InputDevice input=InputDevice.getDevice(id);if(input!=null&&input.getName().equals("SONY TV RC MIC 001")){device=id;source=input.getSources();break;}}forwarder=new EvKeyForwarder(map,SonyInputBridge::inject,device,source);System.out.println("VOICE_DEVICE_GRABBED");}}
   if(fd>=0){int n=c.read(fd,events,events.length);if(n>0){ByteBuffer b=ByteBuffer.wrap(events).order(ByteOrder.LITTLE_ENDIAN);for(int p=0;p+16<=n;p+=16){int type=b.getShort(p+8)&65535,code=b.getShort(p+10)&65535,value=b.getInt(p+12);if(type!=1)continue;if(code==217){if(value==1&&!held){held=true;if(link.active){link.event(2);System.out.println("VOICE_DOWN");}}else if(value==0&&held){held=false;link.event(3);System.out.println("VOICE_UP");}}else if(value==0||value==1||value==2){forwarder.forward(code,value);}}}
    else if(n<0&&Native.getLastError()!=11){if(held)link.event(3);held=false;if(forwarder!=null)forwarder.releaseAll();c.ioctl(fd,0x40044590,0);c.close(fd);fd=-1;scan=true;}
   }

   c.poll(poll,1,fd>=0?3:25);if(c.read(watch,changes,changes.length)>0&&fd<0)scan=true;
    }}finally{
   // Never let a physical UP escape after its DOWN was intercepted, even if IPC dies or shutdown is requested.
   if(fd>=0){try{drain(fd,held,forwarder);}finally{if(forwarder!=null)forwarder.releaseAll();c.ioctl(fd,0x40044590,0);c.close(fd);}}
   c.close(watch);
  }
 }
 static void drain(int fd,boolean held,EvKeyForwarder forwarder)throws Exception{
  byte[] one=new byte[16];while(held||(forwarder!=null&&forwarder.held())){int n=c.read(fd,one,16);if(n==16){ByteBuffer b=ByteBuffer.wrap(one).order(ByteOrder.LITTLE_ENDIAN);int type=b.getShort(8)&65535,code=b.getShort(10)&65535,value=b.getInt(12);if(type==1){if(code==217){if(value==0)held=false;else if(value==1)held=true;}else if(code!=217&&forwarder!=null)try{forwarder.forward(code,value);}catch(Exception ignored){}}}else if(n<0&&Native.getLastError()!=11)break;Thread.sleep(3);}
 }
 static boolean inject(KeyEvent event)throws Exception{Class<?> cls=Class.forName("android.hardware.input.InputManager");Object manager=cls.getMethod("getInstance").invoke(null);Method method=cls.getMethod("injectInputEvent",android.view.InputEvent.class,int.class);return Boolean.TRUE.equals(method.invoke(manager,event,0));}
}
