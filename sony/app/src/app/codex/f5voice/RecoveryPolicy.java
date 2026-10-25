package app.codex.f5voice;
final class RecoveryPolicy {
 static long delay(int failures){return failures<=0?5000:failures==1?15000:failures==2?30000:60000;}
 static boolean shouldStart(boolean enabled,boolean connected){return enabled&&!connected;}
}
