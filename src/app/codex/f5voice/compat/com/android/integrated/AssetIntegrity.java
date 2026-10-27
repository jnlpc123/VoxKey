package app.codex.f5voice.compat.com.android.integrated;
import java.io.*;
import java.security.*;
final class AssetIntegrity {
 static void verify(InputStream input,long length,String expected)throws IOException {
  try {
   MessageDigest hash=MessageDigest.getInstance("SHA-256");byte[] buffer=new byte[262144];long total=0;int n;
   while((n=input.read(buffer))!=-1){total+=n;if(total>length)throw new IOException("model length");hash.update(buffer,0,n);}
   StringBuilder value=new StringBuilder();for(byte b:hash.digest())value.append(String.format(java.util.Locale.US,"%02x",b&255));
   if(total!=length||!expected.equals(value.toString()))throw new IOException("model checksum");
  } catch(NoSuchAlgorithmException e){throw new IOException("SHA-256",e);}
 }
}
