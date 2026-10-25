package app.codex.f5voice;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
public final class SonyBridgeProvider extends ContentProvider {
 @Override public boolean onCreate(){SonyImeBridge.enabled(SonyRecovery.enabled(getContext()));return true;}
 @Override public Bundle call(String method,String arg,Bundle extras){if(!"connect".equals(method)||Binder.getCallingUid()!=2000)throw new SecurityException("ADB shell only");try{return SonyImeBridge.connect(extras==null?null:extras.getBinder("helper"),extras==null?0:extras.getInt("protocol",0),extras==null?null:extras.getString("build"));}catch(RemoteException e){throw new IllegalStateException("binder",e);}}
 @Override public Cursor query(Uri uri,String[] p,String s,String[] a,String sort){throw new UnsupportedOperationException();}
 @Override public String getType(Uri uri){return null;}
 @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}
 @Override public int delete(Uri uri,String s,String[] a){throw new UnsupportedOperationException();}
 @Override public int update(Uri uri,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
