package app.codex.f5voice.compat.com.android.integrated;
public class VoiceGateTest {
 static int n;
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);n++;}
 public static void main(String[] args){
  VoiceGate g=new VoiceGate();
  check(g.key(135,true,0,true)==0,"inactive must pass");
  g.editor(true);
  check(g.key(23,true,0,true)==0,"confirm must pass");
  check(g.key(135,true,0,false)==0,"injected key must pass");
  check(g.key(135,true,0,true)==1,"editor physical F5 must start");
  long ticket=g.generation;
  check(g.key(135,true,1,true)==3,"repeat must not start again");
  check(g.key(135,false,0,true)==2,"paired release must stop");
  check(g.accept(ticket),"same session recognition may commit");
  check(g.key(135,true,0,true)==3,"busy new press consumed without recording");
  check(g.key(135,false,0,true)==3,"busy release consumed");
  g.editor(false);
  check(!g.accept(ticket),"late recognition must not reach another editor");
  check(g.key(135,true,0,true)==0,"outside editor vendor voice must pass");
  g.editor(true);check(g.key(135,true,0,true)==1,"new session starts");
  g.editor(false);
  check(g.key(135,false,0,true)==3,"loss of focus still consumes owned release");
  check(g.key(135,true,0,true)==0,"next outside press passes");
  g.editor(true);check(g.key(135,true,1,true)==0,"orphan repeat cannot start");
  g=new VoiceGate();g.editor(true);check(g.key(231,true,0,true)==1,"voice assist starts");
  check(g.key(219,false,0,true)==0,"different release passes");
  check(g.key(231,false,0,true)==2,"voice assist paired release");
  g=new VoiceGate();g.editor(true);check(g.key(219,true,0,true)==1,"assist starts");check(g.key(219,false,0,true)==2,"assist stops");
  g=new VoiceGate();g.editor(true);g.configuredCode=231;check(g.key(135,true,0,true)==0,"manual code excludes others");
  check(g.key(231,true,0,true)==1,"manual code starts");g.configuredCode=135;check(g.key(231,false,0,true)==2,"owned release survives config change");
  check(g.key(3,true,0,true)==0,"Home passes");check(g.key(4,true,0,true)==0,"back passes");check(g.key(19,true,0,true)==0,"direction passes");
  g=new VoiceGate();g.editor(false);check(g.key(219,true,0,true)==0,"assistant outside editor passes");
  g.editor(true);check(g.key(231,true,0,false)==0,"injected assist passes");
  System.out.println("PASS "+n+" session/key checks");
 }
}
