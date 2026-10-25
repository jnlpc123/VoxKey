package app.codex.f5voice;
import android.content.Context;
import java.io.*;
import java.security.MessageDigest;
/** Versioned private cache, atomically published only after verifying the official model. */
final class ModelStore {
 static File prepare(Context context)throws Exception{
  File dir=new File(context.getFilesDir(),"sense-d94222f3");if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("mkdir");
  File model=new File(dir,"model.int8.ort"),tokens=new File(dir,"tokens.txt"),ready=new File(dir,"verified");
  if(ready.isFile()&&model.length()==239603368L&&tokens.length()==315894L)return dir;
  if(ready.exists()&&!ready.delete())throw new IOException("marker");
  copy(context,"model/model.int8.ort",model,"d94222f3a655fe6c0690d31d261a3e1503aa56aa10d1aebe116f7b15ebec9358");
  copy(context,"model/tokens.txt",tokens,"f449eb28dc567533d7fa59be34e2abca8784f771850c78a47fb731a31429a1dc");
  try(FileOutputStream out=new FileOutputStream(ready)){out.write(1);out.getFD().sync();}
  return dir;
 }
 static File providerConfig(Context context)throws IOException{
  File file=new File(context.getFilesDir(),"voxkey-cpu.config");
  byte[] config=("GraphOptimizationLevel=0\nSessionConfig.session.disable_prepacking=1\nSessionConfig.session.intra_op.allow_spinning=0\nSessionConfig.session.inter_op.allow_spinning=0\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  android.util.AtomicFile atomic=new android.util.AtomicFile(file);FileOutputStream out=null;
  try{out=atomic.startWrite();out.write(config);atomic.finishWrite(out);}catch(IOException e){if(out!=null)atomic.failWrite(out);throw e;}
  return file;
 }
 private static void copy(Context c,String asset,File destination,String expected)throws Exception{
  File temp=new File(destination.getPath()+".partial");MessageDigest hash=MessageDigest.getInstance("SHA-256");
  try(InputStream in=c.getAssets().open(asset);FileOutputStream out=new FileOutputStream(temp)){
   byte[] buffer=new byte[1024*1024];int n;while((n=in.read(buffer))!=-1){out.write(buffer,0,n);hash.update(buffer,0,n);}out.getFD().sync();
  }
  if(expected!=null){StringBuilder value=new StringBuilder();for(byte b:hash.digest())value.append(String.format(java.util.Locale.US,"%02x",b&255));if(!expected.equals(value.toString()))throw new IOException("checksum");}
  if(destination.exists()&&!destination.delete())throw new IOException("replace");if(!temp.renameTo(destination))throw new IOException("rename");
 }
}
