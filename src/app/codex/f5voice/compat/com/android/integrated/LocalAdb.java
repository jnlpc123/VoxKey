package app.codex.f5voice.compat.com.android.integrated;
import com.cgutman.adblib.*;import java.net.*;import java.io.*;import java.util.*;import java.security.*;import android.content.Context;
/** Only the TV's own loopback ADB endpoint; no endpoint or shell command is user configurable. */
final class LocalAdb implements AutoCloseable {
 private final Socket socket=new Socket();private AdbConnection connection;
 void connect(Context c)throws Exception{AdbCrypto crypto=LocalAdbIdentity.load(c);socket.connect(new InetSocketAddress("127.0.0.1",5555),3000);socket.setSoTimeout(90000);connection=AdbConnection.create(socket,crypto);connection.connect();socket.setSoTimeout(15000);Diag.log("sony_local_adb_connected");}
 String shell(String command)throws Exception{
  Diag.log("sony_local_adb_shell "+command.split(" ")[0]);ByteArrayOutputStream out=new ByteArrayOutputStream();try(AdbStream s=connection.open("shell:"+command)){
   while(true){byte[] b;try{b=s.read();}catch(IOException e){if(s.isClosed())break;throw e;}if(out.size()+b.length>65536)throw new IOException("ADB output limit");out.write(b);}
  }return out.toString("UTF-8");
 }
 void push(String path,byte[] bytes)throws Exception{
  try(AdbStream s=connection.open("sync:")){
   s.write(SyncFrame.packet("SEND",(path+",33188").getBytes("UTF-8")));
   int chunk=Math.min(3072,connection.getMaxData()-8);if(chunk<=0)throw new IOException("ADB max data");
   for(int p=0;p<bytes.length;p+=chunk)s.write(SyncFrame.packet("DATA",Arrays.copyOfRange(bytes,p,Math.min(p+chunk,bytes.length))));
   s.write(SyncFrame.header("DONE",(int)(System.currentTimeMillis()/1000)));
   InputStream in=new InputStream(){byte[] pending=new byte[0];int at;public int read()throws IOException{if(at==pending.length)try{pending=s.read();at=0;}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException(e);}return pending[at++]&255;}};
   SyncFrame.Reply reply=SyncFrame.read(in);if(!reply.id.equals("OKAY"))throw new IOException("ADB sync rejected");
  }
 }
 void deploy(Context context)throws Exception{
  if(SonyImeBridge.connected()&&!SonyImeBridge.ready()){
   SonyImeBridge.stopHelper();for(int n=0;n<100&&SonyImeBridge.connected();n++)Thread.sleep(50);
   if(SonyImeBridge.connected())throw new IOException("legacy helper requires safe stop");
  }
  String dir="/data/local/tmp/voxkey-unified/";shell("mkdir -p "+dir);
  for(String name:new String[]{"sony-helper.jar","libjnidispatch.so","start-helper.sh"}){
   byte[] bytes;try(InputStream in=context.getAssets().open("sony/"+name);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);bytes=out.toByteArray();}
   String expected=hex(MessageDigest.getInstance("SHA-256").digest(bytes));String actual=shell("sha256sum "+dir+name).trim();
   if(!actual.startsWith(expected+" ")){push(dir+name+".new",bytes);String check=shell("sha256sum "+dir+name+".new").trim();if(!check.startsWith(expected+" "))throw new IOException("helper asset verification");shell("mv "+dir+name+".new "+dir+name);}
  }
  if(!SonyRecovery.enabled(context))return;
  String result=shell("sh "+dir+"start-helper.sh "+android.os.Process.myUid()).trim();
  if(!result.matches("[0-9]+"))throw new IOException("helper launch failed");
 }
 private static String hex(byte[] b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format(java.util.Locale.ROOT,"%02x",x&255));return s.toString();}
 public void close(){try{socket.close();}catch(IOException ignored){}if(connection!=null)try{connection.close();}catch(IOException ignored){}}
}
