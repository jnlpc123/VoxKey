package app.codex.f5voice.compat.com.android.integrated;
import android.content.Context;
import android.os.SystemClock;
import android.util.Base64;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.MGF1ParameterSpec;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.*;
import javax.crypto.spec.*;
/** Short-lived, activity-owned LAN pairing endpoint. Never exposes stored credentials. */
final class PhonePairing implements AutoCloseable {
 interface Listener { void saved(); void expired(); }
 private final Context context; private final VoiceSettings settings; private final Listener listener;
 private final ServerSocket server; private final KeyPair pair; private final String token,host,origin;
 private final long deadline=SystemClock.elapsedRealtime()+600000;
 private final Set<Socket> sockets=Collections.synchronizedSet(new HashSet<Socket>());
 private final ThreadPoolExecutor workers=new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(4));
 private volatile boolean closed; private boolean used;
 PhonePairing(Context c,Listener l)throws Exception {
  context=c.getApplicationContext();settings=new VoiceSettings(context);listener=l;
  KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");generator.initialize(2048);pair=generator.generateKeyPair();
  byte[] secret=new byte[32];new SecureRandom().nextBytes(secret);token=b64url(secret);
  InetAddress address=null;
  for(Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();interfaces.hasMoreElements();){NetworkInterface ni=interfaces.nextElement();if(!ni.isUp()||ni.isLoopback())continue;for(Enumeration<InetAddress> addresses=ni.getInetAddresses();addresses.hasMoreElements();){InetAddress a=addresses.nextElement();if(a instanceof Inet4Address&&a.isSiteLocalAddress()){address=a;break;}}if(address!=null)break;}
  if(address==null)throw new IOException("LAN unavailable");
  server=new ServerSocket(0,4,address);server.setSoTimeout(1000);host=address.getHostAddress()+":"+server.getLocalPort();origin="http://"+host;
 }
 String url(){return origin+"/#t="+token+"&k="+b64url(pair.getPublic().getEncoded());}
 long remaining(){return Math.max(0,(deadline-SystemClock.elapsedRealtime()+999)/1000);}
 void start(){new Thread(()->{try{while(!closed&&remaining()>0){try{Socket socket=server.accept();socket.setSoTimeout(5000);sockets.add(socket);try{workers.execute(()->handle(socket));}catch(RejectedExecutionException e){sockets.remove(socket);socket.close();}}catch(SocketTimeoutException ignored){}}}catch(IOException ignored){}finally{boolean notify=!closed;close();if(notify)listener.expired();}},"F5-phone-pairing").start();}
 private void handle(Socket socket){boolean committed=false;
  try{InputStream input=socket.getInputStream();int[] budget={8192};String first=line(input,budget);String[] parts=first.split(" ");if(parts.length!=3||!parts[2].equals("HTTP/1.1"))throw new IOException();
   Map<String,String> headers=new HashMap<>();String line;while(!(line=line(input,budget)).isEmpty()){int colon=line.indexOf(':');if(colon<=0)throw new IOException();String name=line.substring(0,colon).trim().toLowerCase(Locale.ROOT);if(headers.put(name,line.substring(colon+1).trim())!=null)throw new IOException();}
   if(closed||remaining()==0||!host.equals(headers.get("host"))||headers.containsKey("transfer-encoding")){respond(socket,403,"text/plain","配对已失效".getBytes(StandardCharsets.UTF_8));return;}
   if(parts[0].equals("GET")){
    String file=parts[1].equals("/")?"index.html":parts[1].equals("/app.js")?"app.js":parts[1].equals("/forge.js")?"forge.js":parts[1].equals("/style.css")?"style.css":null;
    if(file==null){respond(socket,404,"text/plain",new byte[0]);return;}
    ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream asset=context.getAssets().open("mobile/"+file)){byte[] buf=new byte[4096];int n;while((n=asset.read(buf))!=-1)out.write(buf,0,n);}
    respond(socket,200,file.endsWith("js")?"application/javascript":file.endsWith("css")?"text/css":"text/html",out.toByteArray());return;
   }
   if(!parts[0].equals("POST")||!parts[1].equals("/save")||!origin.equals(headers.get("origin"))||!"application/json".equals(headers.get("content-type"))||!MessageDigest.isEqual(token.getBytes(StandardCharsets.US_ASCII),headers.getOrDefault("x-pair-token","").getBytes(StandardCharsets.US_ASCII)))throw new IOException();
   int length=Integer.parseInt(headers.getOrDefault("content-length","-1"));if(length<1||length>8192)throw new IOException();byte[] body=new byte[length];int pos=0;while(pos<length){int n=input.read(body,pos,length-pos);if(n<0)throw new IOException();pos+=n;}
   JSONObject envelope=new JSONObject(new String(body,StandardCharsets.UTF_8));Arrays.fill(body,(byte)0);
   Cipher rsa=Cipher.getInstance("RSA/ECB/OAEPPadding");rsa.init(Cipher.DECRYPT_MODE,pair.getPrivate(),new OAEPParameterSpec("SHA-256","MGF1",MGF1ParameterSpec.SHA256,PSource.PSpecified.DEFAULT));
   byte[] aes=rsa.doFinal(decode(envelope.getString("wrappedKey")));byte[] iv=decode(envelope.getString("iv"));byte[] data=decode(envelope.getString("ciphertext"));byte[] tag=decode(envelope.getString("tag"));if(aes.length!=32||iv.length!=12||tag.length!=16)throw new IOException();
   byte[] ciphertext=new byte[data.length+tag.length];System.arraycopy(data,0,ciphertext,0,data.length);System.arraycopy(tag,0,ciphertext,data.length,tag.length);
   byte[] plain;try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(aes,"AES"),new GCMParameterSpec(128,iv));cipher.updateAAD(token.getBytes(StandardCharsets.UTF_8));plain=cipher.doFinal(ciphertext);}finally{Arrays.fill(aes,(byte)0);}
   JSONObject form;try{form=new JSONObject(new String(plain,StandardCharsets.UTF_8));}finally{Arrays.fill(plain,(byte)0);}
   String workspace=form.getString("workspace").trim(),key=form.getString("apiKey").trim();boolean cloud=form.getBoolean("cloud");CloudProtocol.endpoint(workspace);
   if(key.length()>512||(!key.isEmpty()&&!key.matches("[\\x21-\\x7E]+"))||(cloud&&key.isEmpty()&&!settings.hasKey()))throw new IOException();
   synchronized(this){if(closed||used||remaining()==0)throw new IOException();settings.save(workspace,key,cloud);used=true;committed=true;respond(socket,200,"application/json","{\"ok\":true}".getBytes(StandardCharsets.UTF_8));}

  }catch(Exception ignored){try{if(!committed)respond(socket,400,"application/json","{\"ok\":false}".getBytes(StandardCharsets.UTF_8));}catch(Exception ignoredAgain){}}
  finally{if(committed){listener.saved();close();}sockets.remove(socket);try{socket.close();}catch(IOException ignored){}}
 }
 private static String line(InputStream input,int[] budget)throws IOException {ByteArrayOutputStream out=new ByteArrayOutputStream();int value;while((value=input.read())!=-1){if(--budget[0]<0)throw new IOException();if(value==10){byte[] bytes=out.toByteArray();if(bytes.length==0||bytes[bytes.length-1]!=13)throw new IOException();return new String(bytes,0,bytes.length-1,StandardCharsets.US_ASCII);}out.write(value);}throw new EOFException();}
 private static void respond(Socket socket,int status,String type,byte[] body)throws IOException {OutputStream out=socket.getOutputStream();String header="HTTP/1.1 "+status+" Result\r\nContent-Type: "+type+"; charset=utf-8\r\nContent-Length: "+body.length+"\r\nCache-Control: no-store\r\nReferrer-Policy: no-referrer\r\nX-Content-Type-Options: nosniff\r\nContent-Security-Policy: default-src 'none'; script-src 'self'; style-src 'self'; connect-src 'self'; img-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'\r\nConnection: close\r\n\r\n";out.write(header.getBytes(StandardCharsets.US_ASCII));out.write(body);out.flush();}
 private static byte[] decode(String value){return Base64.decode(value,Base64.NO_WRAP);}
 private static String b64url(byte[] value){return Base64.encodeToString(value,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);}
 @Override public synchronized void close(){if(closed)return;closed=true;try{server.close();}catch(IOException ignored){}synchronized(sockets){for(Socket socket:sockets)try{socket.close();}catch(IOException ignored){}sockets.clear();}workers.shutdownNow();}
}
