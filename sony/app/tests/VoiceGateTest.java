package app.codex.f5voice;
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
  System.out.println("PASS "+n+" session/key checks");
 }
}
