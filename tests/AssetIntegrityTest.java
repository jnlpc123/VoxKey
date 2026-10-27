package app.codex.f5voice.compat.com.android.integrated;
import java.io.*;
public final class AssetIntegrityTest {
 static final String HASH="ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
 static int checks;
 static void run(String text,long length,String hash,boolean valid)throws Exception {
  boolean passed=true;try{AssetIntegrity.verify(new ByteArrayInputStream(text.getBytes("UTF-8")),length,hash);}catch(IOException e){passed=false;}
  if(passed!=valid)throw new AssertionError("integrity check "+checks);checks++;
 }
 public static void main(String[] args)throws Exception {
  run("abc",3,HASH,true);run("abd",3,HASH,false);run("ab",3,HASH,false);run("abcd",3,HASH,false);
  System.out.println("PASS "+checks+" bundled asset integrity checks");
 }
}
