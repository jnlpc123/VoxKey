package app.codex.f5voice;
/** Owns only paired physical F5 events; generation protects the editor from late results. */
public final class VoiceGate {
 public static final int PASS=0, START=1, STOP=2, CONSUMED=3;
 public volatile long generation;
 public volatile boolean active; public boolean held;
 public int phase; // 0 idle, 1 capturing, 2 decoding
 public void editor(boolean allowed) { generation++; active=allowed; phase=0; }
 public int key(int code, boolean down, int repeat, boolean physical) {
  if(code!=135 || !physical)return PASS;
  if(!down){
   if(!held)return PASS;
   held=false;
   if(active && phase==1){phase=2;return STOP;}
   return CONSUMED;
  }
  if(held)return CONSUMED;
  if(!active || repeat!=0)return PASS;
  held=true;
  if(phase!=0)return CONSUMED;
  phase=1; return START;
 }
 public boolean accept(long ticket) {return active && phase==2 && generation==ticket;}
 public void done() {phase=0;}
}
