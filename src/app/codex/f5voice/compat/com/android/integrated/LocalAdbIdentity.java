package app.codex.f5voice.compat.com.android.integrated;
import android.content.*;import android.security.keystore.*;import android.util.Base64;
import java.security.*;import java.security.spec.*;import javax.crypto.*;import javax.crypto.spec.*;import org.json.*;
import com.cgutman.adblib.*;
final class LocalAdbIdentity {
 static final String ALIAS="VoxKeyLocalAdbIdentity";
 static synchronized AdbCrypto load(Context c)throws Exception{
  SharedPreferences p=c.getSharedPreferences("sony-adb-identity",0);KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
  String stored=p.getString("sealed",null);
  if(!ks.containsAlias(ALIAS)){
   if(stored!=null)throw new GeneralSecurityException("local identity key unavailable");
   KeyGenerator g=KeyGenerator.getInstance("AES","AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();
  }
  SecretKey key=(SecretKey)ks.getKey(ALIAS,null);KeyPair pair;
  if(stored==null){
   KeyPairGenerator gen=KeyPairGenerator.getInstance("RSA");gen.initialize(2048);pair=gen.generateKeyPair();
   JSONObject data=new JSONObject().put("private",b64(pair.getPrivate().getEncoded())).put("public",b64(pair.getPublic().getEncoded()));byte[] plain=data.toString().getBytes("UTF-8");
   try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key);String sealed=b64(cipher.getIV())+":"+b64(cipher.doFinal(plain));if(!p.edit().putString("sealed",sealed).commit())throw new java.io.IOException("identity storage");}finally{java.util.Arrays.fill(plain,(byte)0);}
  }else{
   String[] parts=stored.split(":",-1);if(parts.length!=2)throw new GeneralSecurityException("identity format");Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Base64.decode(parts[0],0)));byte[] plain=cipher.doFinal(Base64.decode(parts[1],0));
   try{JSONObject data=new JSONObject(new String(plain,"UTF-8"));KeyFactory f=KeyFactory.getInstance("RSA");byte[] secret=Base64.decode(data.getString("private"),0);try{pair=new KeyPair(f.generatePublic(new X509EncodedKeySpec(Base64.decode(data.getString("public"),0))),f.generatePrivate(new PKCS8EncodedKeySpec(secret)));}finally{java.util.Arrays.fill(secret,(byte)0);}}finally{java.util.Arrays.fill(plain,(byte)0);}
  }
  return AdbCrypto.fromKeyPair(bytes->b64(bytes),pair);
 }
 static String b64(byte[] b){return Base64.encodeToString(b,Base64.NO_WRAP);}
}
