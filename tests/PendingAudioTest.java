package app.codex.f5voice.compat.com.android.integrated;
public final class PendingAudioTest {
 static int checks;static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
 public static void main(String[] args){
  float[] queued={1,2};PendingAudio a=new PendingAudio(queued);a.cancel();check(queued[0]==0&&queued[1]==0,"queued cancellation wipes immediately");check(!a.begin(),"canceled task cannot start");
  float[] running={1,2};PendingAudio b=new PendingAudio(running);check(b.begin(),"one worker claims task");check(!b.begin(),"cannot claim twice");b.cancel();check(running[0]==1,"in-flight native PCM cannot be changed concurrently");check(b.canceled(),"in-flight callback suppressed");b.finish();check(running[0]==0&&running[1]==0,"running PCM wiped after native completion");
  float[] normal={3};PendingAudio c=new PendingAudio(normal);check(c.begin(),"normal task starts");c.finish();check(normal[0]==0&&!c.begin(),"completed audio cleared and cannot restart");
  System.out.println("PASS "+checks+" queued audio cancellation checks");
 }
}
