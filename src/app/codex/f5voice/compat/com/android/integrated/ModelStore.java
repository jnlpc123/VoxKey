package app.codex.f5voice.compat.com.android.integrated;
import android.content.Context;
import java.io.*;
/** Verify bundled assets without storing a second model copy. */
final class ModelStore {
 static void prepare(Context context)throws Exception{
  long updated=context.getPackageManager().getPackageInfo(context.getPackageName(),0).lastUpdateTime;
  android.content.SharedPreferences prefs=context.getSharedPreferences("model-integrity",0);
  if(updated>0&&prefs.getLong("d94222f3-apk",-1)==updated)return;
  verify(context,"model/model.int8.ort",239603368L,"d94222f3a655fe6c0690d31d261a3e1503aa56aa10d1aebe116f7b15ebec9358");
  verify(context,"model/tokens.txt",315894L,"f449eb28dc567533d7fa59be34e2abca8784f771850c78a47fb731a31429a1dc");
  if(!prefs.edit().putLong("d94222f3-apk",updated).commit())throw new IOException("model verification marker");
 }
 static File providerConfig(Context context)throws IOException{
  File file=new File(context.getFilesDir(),"voxkey-cpu.config");
  byte[] config=("GraphOptimizationLevel=0\nSessionConfig.session.disable_prepacking=1\nSessionConfig.session.intra_op.allow_spinning=0\nSessionConfig.session.inter_op.allow_spinning=0\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  android.util.AtomicFile atomic=new android.util.AtomicFile(file);FileOutputStream out=null;
  try{out=atomic.startWrite();out.write(config);atomic.finishWrite(out);}catch(IOException e){if(out!=null)atomic.failWrite(out);throw e;}
  return file;
 }
 private static void verify(Context c,String asset,long length,String expected)throws IOException{
  try(InputStream in=c.getAssets().open(asset)){AssetIntegrity.verify(in,length,expected);}
 }
 static void removeLegacyCopy(Context c){
  File dir=new File(c.getFilesDir(),"sense-d94222f3");
  for(String name:new String[]{"model.int8.ort","tokens.txt","verified","model.int8.ort.partial","tokens.txt.partial"}){
   File file=new File(dir,name);if(file.isFile()&&!file.delete())Diag.log("legacy_model_cleanup_pending");
  }
  if(dir.isDirectory()&&!dir.delete())Diag.log("legacy_model_directory_retained");
 }
 static long legacyBytes(Context c){
  File dir=new File(c.getFilesDir(),"sense-d94222f3");long bytes=0;
  for(String name:new String[]{"model.int8.ort","tokens.txt","verified","model.int8.ort.partial","tokens.txt.partial"})bytes+=new File(dir,name).length();return bytes;
 }
}
