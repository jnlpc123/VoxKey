package app.codex.f5voice.compat.com.android.integrated;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
/** Queued PCM can be cleared immediately; running PCM belongs to native code until finish. */
final class PendingAudio {
 final float[] samples;
 private final AtomicInteger state=new AtomicInteger(); // queued, running, finished/canceled
 private volatile boolean canceled;
 PendingAudio(float[] samples){this.samples=samples;}
 boolean begin(){return state.compareAndSet(0,1);}
 void cancel(){canceled=true;if(state.compareAndSet(0,2))Arrays.fill(samples,0);}
 boolean canceled(){return canceled;}
 void finish(){Arrays.fill(samples,0);state.set(2);}
}
