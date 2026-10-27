package app.codex.f5voice;
import android.content.*;import android.content.pm.PackageManager;import android.os.*;import android.database.Cursor;import android.net.Uri;
/** Temporary same-signer, exact-package local migration endpoint. Never logs or exports a file. */
public final class MigrationProvider extends ContentProvider {
 @Override public boolean onCreate(){return true;}
 private void authorized(){int uid=Binder.getCallingUid();PackageManager p=getContext().getPackageManager();if(!MigrationPolicy.authorized(p.getPackagesForUid(uid),p.checkSignatures(android.os.Process.myUid(),uid)==PackageManager.SIGNATURE_MATCH))throw new SecurityException("integrated VoxKey only");}
 @Override public Bundle call(String method,String arg,Bundle extras){authorized();if(!"export-v1".equals(method))throw new UnsupportedOperationException();try{return new VoiceSettings(getContext()).exportLegacy();}catch(Exception e){throw new IllegalStateException("legacy configuration unavailable");}}
 @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){throw new UnsupportedOperationException();}
 @Override public String getType(Uri u){return null;}
 @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
 @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
