package app.codex.f5voice;
final class MigrationPolicy {
 static final String TARGET="app.codex.f5voice.compat.com.android.integrated";
 static boolean authorized(String[] packages,boolean sameSignature){if(!sameSignature||packages==null)return false;for(String p:packages)if(TARGET.equals(p))return true;return false;}
 static boolean shouldImport(boolean done,boolean configured,boolean legacy){return !done&&!configured&&legacy;}
 static boolean userConfigured(boolean explicit,boolean hasKey,boolean importPending){return explicit||(hasKey&&!importPending);}
 static boolean valid(int schema,boolean hasKey,String key){return schema==1&&key!=null&&hasKey==!key.isEmpty();}
}
