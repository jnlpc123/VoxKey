package app.codex.f5voice.compat.com.android.integrated;
import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
final class VoiceSettings {
 private static final Object LOCK=new Object();
 private static final String ALIAS="F5VoiceCloudKey";
 private final SharedPreferences prefs;
 VoiceSettings(Context c){prefs=c.getSharedPreferences("voice-settings",Context.MODE_PRIVATE);}
 boolean cloud(){return prefs.getBoolean("cloud",false);}
 void watch(SharedPreferences.OnSharedPreferenceChangeListener listener){prefs.registerOnSharedPreferenceChangeListener(listener);}
 void unwatch(SharedPreferences.OnSharedPreferenceChangeListener listener){prefs.unregisterOnSharedPreferenceChangeListener(listener);}
 void cloud(boolean enabled){synchronized(LOCK){prefs.edit().putBoolean("cloud",enabled).putBoolean("configured",true).putBoolean("migrationPending",false).apply();}}
 String workspace(){return prefs.getString("workspace","");}
 void save(String workspace,String key,boolean cloud)throws Exception{synchronized(LOCK){store(workspace,key,cloud,true);}}
 private void store(String workspace,String key,boolean cloud,boolean userChange)throws Exception{
  CloudProtocol.endpoint(workspace);
  SharedPreferences.Editor edit=prefs.edit().putString("workspace",workspace.trim()).putBoolean("cloud",cloud).putBoolean("configured",userChange).putBoolean("migrationPending",!userChange);
  if(key!=null&&!key.trim().isEmpty()){
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,secret());
   edit.putString("key",Base64.encodeToString(cipher.doFinal(key.trim().getBytes("UTF-8")),Base64.NO_WRAP));
   edit.putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP));
  }
  if(!edit.commit())throw new IllegalStateException("save");
 }
 String key()throws Exception{synchronized(LOCK){
  String encrypted=prefs.getString("key","");if(encrypted.isEmpty())return "";
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,secret(),new GCMParameterSpec(128,Base64.decode(prefs.getString("iv",""),Base64.NO_WRAP)));
  return new String(cipher.doFinal(Base64.decode(encrypted,Base64.NO_WRAP)),"UTF-8");
 }}
 boolean hasKey(){return !prefs.getString("key","").isEmpty();}
 void forget(){synchronized(LOCK){prefs.edit().remove("key").remove("iv").putBoolean("configured",true).putBoolean("migrationPending",false).apply();}}
 boolean configured(){return MigrationPolicy.userConfigured(prefs.getBoolean("configured",false),hasKey(),prefs.getBoolean("migrationPending",false));}
 boolean migrationVerified(){return prefs.getBoolean("migrationVerified",false);}
 boolean migrationKeyVerified(){return prefs.getBoolean("migrationKeyVerified",false);}
 int importLegacy(String workspace,String key,boolean cloud)throws Exception{synchronized(LOCK){
  if(migrationVerified())return migrationKeyVerified()?2:1;
  if(configured())return 0;store(workspace,key,cloud,false);
  if(!key.equals(key())||!workspace.trim().equals(workspace())||cloud()!=cloud)throw new IllegalStateException("migration verification");
  if(!prefs.edit().putBoolean("migrationVerified",true).putBoolean("migrationKeyVerified",!key.isEmpty()).putBoolean("migrationPending",false).commit())throw new IllegalStateException("migration verification marker");
  return key.isEmpty()?1:2;
 }}
 private SecretKey secret()throws Exception{
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
  if(ks.containsAlias(ALIAS))return (SecretKey)ks.getKey(ALIAS,null);
  KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
  generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());return generator.generateKey();
 }
}
