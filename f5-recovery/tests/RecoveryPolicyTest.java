package app.codex.f5voice.compat.com.android.recovery;
public final class RecoveryPolicyTest {
 static int n;static void check(boolean ok){n++;if(!ok)throw new AssertionError("check "+n);}
 public static void main(String[] args){
  String aerial="app.orchard.tv/app.orchard.tv.guard.HomeGuardService";
  check(RecoveryPolicy.addService(null).equals(RecoveryPolicy.KEY));
  check(RecoveryPolicy.addService("null").equals(RecoveryPolicy.KEY));
  check(RecoveryPolicy.addService(aerial).equals(aerial+":"+RecoveryPolicy.KEY));
  check(RecoveryPolicy.addService(aerial+":app.codex.f5voice/.KeyService").equals(aerial+":app.codex.f5voice/.KeyService"));
  check(RecoveryPolicy.addService("other/.KeyService").startsWith("other/.KeyService:"));
  check(RecoveryPolicy.addService(RecoveryPolicy.KEY).equals(RecoveryPolicy.KEY));
  check(RecoveryPolicy.addIme("sogou/.Ime;12").equals("sogou/.Ime;12:"+RecoveryPolicy.IME));
  check(RecoveryPolicy.addIme(RecoveryPolicy.IME+";999").equals(RecoveryPolicy.IME+";999"));
  check(RecoveryPolicy.addIme("app.codex.f5voice/app.codex.f5voice.VoiceIme;123").equals("app.codex.f5voice/app.codex.f5voice.VoiceIme;123"));
  check(RecoveryPolicy.same(RecoveryPolicy.IME,"app.codex.f5voice/app.codex.f5voice.VoiceIme"));
  check(!RecoveryPolicy.same(RecoveryPolicy.IME,"other/.VoiceIme"));
  RecoveryPolicy.Session s=new RecoveryPolicy.Session("12",true,false);
  check(!s.power("12",true)); // process restart must preserve intentional ordinary IME
  check(!s.power("12",false));check(s.pending);
  check(s.power("12",true));check(!s.imeDone);
  s.imeDone=true;check(!s.power("12",true));check(s.imeDone);
  s.complete();check(!s.pending);
  check(s.power("13",true));check(s.pending&&!s.imeDone);
  s.imeDone=true;s.complete();check(!s.power("13",true));
  check(!s.power("13",false));check(s.power("13",true));check(!s.imeDone);
  s.manual();check(s.pending&&!s.imeDone);
  check(RecoveryPolicy.merge(aerial,"third/.Service",RecoveryPolicy.KEY,false).equals("third/.Service:"+aerial+":"+RecoveryPolicy.KEY));
  check(RecoveryPolicy.includesAll(aerial+":"+RecoveryPolicy.KEY,aerial,false));
  check(!RecoveryPolicy.includesAll(RecoveryPolicy.KEY,aerial,false));
  check(RecoveryPolicy.merge("sogou/.Ime;1","sogou/.Ime;2",RecoveryPolicy.IME,true).equals("sogou/.Ime;2:"+RecoveryPolicy.IME));
  check(RecoveryPolicy.cleanupSignature("sogou/.Ime",aerial));
  check(!RecoveryPolicy.cleanupSignature("sogou/.Ime",aerial+":"+RecoveryPolicy.KEY));
  check(!RecoveryPolicy.cleanupSignature(RecoveryPolicy.IME,aerial));
  System.out.println("PASS "+n+" recovery merge/wake/manual-switch checks");
 }
}