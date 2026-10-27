package com.cgutman.adblib;
import java.net.*;import java.io.*;import java.security.*;import java.util.concurrent.*;import java.util.*;
public class AdbTransportTest {
 public static void main(String[] args)throws Exception{
  ServerSocket server=new ServerSocket(0,1,InetAddress.getLoopbackAddress());ExecutorService pool=Executors.newSingleThreadExecutor();
  Future<?> peer=pool.submit(()->{try(Socket s=server.accept()){s.setSoTimeout(3000);InputStream in=s.getInputStream();OutputStream out=s.getOutputStream();AdbProtocol.AdbMessage.parseAdbMessage(in);out.write(AdbProtocol.generateMessage(AdbProtocol.CMD_CNXN,0x01000000,4096,new byte[]{0}));out.flush();for(int i=0;i<100;i++){AdbProtocol.AdbMessage open;do{open=AdbProtocol.AdbMessage.parseAdbMessage(in);}while(open.command!=AdbProtocol.CMD_OPEN);int id=open.arg0;out.write(AdbProtocol.generateReady(7,id));out.write(AdbProtocol.generateWrite(7,id,new byte[]{42,(byte)i}));out.write(AdbProtocol.generateClose(7,id));out.flush();}}catch(Exception e){throw new RuntimeException(e);}});
  AdbCrypto crypto=AdbCrypto.generateAdbKeyPair(b->Base64.getEncoder().encodeToString(b));
  try(Socket socket=new Socket(InetAddress.getLoopbackAddress(),server.getLocalPort());AdbConnection c=AdbConnection.create(socket,crypto)){socket.setSoTimeout(3000);c.connect();for(int i=0;i<100;i++){try(AdbStream stream=c.open("shell:test")){byte[] b=stream.read();if(b.length!=2||b[0]!=42||b[1]!=(byte)i)throw new AssertionError("lost stream tail "+i);}}}
  peer.get(10,TimeUnit.SECONDS);server.close();pool.shutdownNow();
  byte[] token=new byte[20];new SecureRandom().nextBytes(token);byte[] signature=crypto.signAdbTokenPayload(token);if(signature.length!=256)throw new AssertionError("signature");
  System.out.println("PASS 100 immediate OPEN/data/CLOSE roundtrips and RSA authentication signature");
 }
}
