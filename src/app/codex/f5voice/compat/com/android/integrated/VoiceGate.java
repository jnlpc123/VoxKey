package app.codex.f5voice.compat.com.android.integrated;
/** Owns paired physical voice events only in an eligible editor. */
public final class VoiceGate {
 public static final int PASS=0, START=1, STOP=2, CONSUMED=3;
 public volatile long generation;
 public volatile boolean active; public boolean held;
 public int configuredCode,heldCode;
 public int phase; // 0 idle, 1 capturing, 2 decoding
 public void editor(boolean allowed) { generation++; active=allowed; phase=0; }
 public int key(int code, boolean down, int repeat, boolean physical) {
  if(!physical)return PASS;
  if(held){if(code!=heldCode)return PASS;}
  else if(configuredCode==0?(code!=135&&code!=219&&code!=231):code!=configuredCode)return PASS;
  if(!down){
   if(!held)return PASS;
   held=false;
   if(active && phase==1){phase=2;return STOP;}
   return CONSUMED;
  }
  if(held)return CONSUMED;
  if(!active || repeat!=0)return PASS;
  held=true;heldCode=code;
  if(phase!=0)return CONSUMED;
  phase=1; return START;
 }
 public boolean accept(long ticket) {return active && phase==2 && generation==ticket;}
 public void done() {phase=0;}
}
