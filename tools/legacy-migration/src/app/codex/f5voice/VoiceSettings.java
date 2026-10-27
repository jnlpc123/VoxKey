package app.codex.f5voice;
import android.content.*;
import android.os.Bundle;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
/** Frozen read-only adapter for the legacy UID. No key generation or settings writes. */
final class VoiceSettings {
 private final SharedPreferences prefs;
 VoiceSettings(Context c){prefs=c.getSharedPreferences("voice-settings",0);}
 Bundle exportLegacy()throws Exception {
  java.util.Map<String,?> values=prefs.getAll();String encrypted=(String)values.get("key"),key="";
  if(encrypted!=null&&!encrypted.isEmpty()){
   KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
   SecretKey secret=(SecretKey)store.getKey("F5VoiceCloudKey",null);if(secret==null)throw new java.security.GeneralSecurityException("legacy key unavailable");
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,secret,new GCMParameterSpec(128,Base64.decode((String)values.get("iv"),Base64.NO_WRAP)));
   byte[] plain=cipher.doFinal(Base64.decode(encrypted,Base64.NO_WRAP));try{key=new String(plain,"UTF-8");}finally{java.util.Arrays.fill(plain,(byte)0);}
  }
  Bundle b=new Bundle();b.putInt("schema",1);b.putBoolean("cloud",Boolean.TRUE.equals(values.get("cloud")));b.putString("workspace",values.get("workspace") instanceof String?(String)values.get("workspace"):"");b.putBoolean("hasKey",!key.isEmpty());b.putString("key",key);return b;
 }
}
