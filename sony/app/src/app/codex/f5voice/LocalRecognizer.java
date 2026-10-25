package app.codex.f5voice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.k2fsa.sherpa.onnx.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
/** Process-wide local engine. Its single worker owns all native streams and sessions. */
final class LocalRecognizer {
 private static final class Holder {static final LocalRecognizer INSTANCE=new LocalRecognizer();}
 static LocalRecognizer shared(){return Holder.INSTANCE;}
 interface Result{void complete(String text,long elapsed,String error);}
 private final ThreadPoolExecutor worker=new ThreadPoolExecutor(1,1,0,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),r->new Thread(r,"VoxKey-local"));
 private final Handler main=new Handler(Looper.getMainLooper());
 private OfflineRecognizer recognizer;
 volatile boolean ready;
 private boolean loading;
 private final java.util.LinkedHashSet<Runnable> listeners=new java.util.LinkedHashSet<>();
 synchronized void load(Context context,Runnable done){
  if(ready){if(done!=null)main.post(done);return;}
  if(done!=null)listeners.add(done);if(loading)return;loading=true;
  Context app=context.getApplicationContext();
  worker.execute(()->{
   long start=android.os.SystemClock.elapsedRealtime();
   try{
    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
    long prepareStart=android.os.SystemClock.elapsedRealtime();java.io.File dir=ModelStore.prepare(app);
    Diag.log("model_cache_ready ms="+(android.os.SystemClock.elapsedRealtime()-prepareStart));long nativeStart=android.os.SystemClock.elapsedRealtime();
    OfflineModelConfig model=new OfflineModelConfig();model.getSenseVoice().setModel(new java.io.File(dir,"model.int8.ort").getAbsolutePath());model.getSenseVoice().setLanguage("zh");model.getSenseVoice().setUseInverseTextNormalization(true);
    model.setTokens(new java.io.File(dir,"tokens.txt").getAbsolutePath());model.setNumThreads(2);model.setProvider("cpu:"+ModelStore.providerConfig(app).getAbsolutePath());model.setDebug(false);
    OfflineRecognizerConfig cfg=new OfflineRecognizerConfig();cfg.setModelConfig(model);recognizer=new OfflineRecognizer(null,cfg);
    ready=true;Diag.log("model_native_ready ms="+(android.os.SystemClock.elapsedRealtime()-nativeStart));Diag.log("model_ready load_ms="+(android.os.SystemClock.elapsedRealtime()-start));
   }catch(Throwable e){Diag.log("model_failed "+e.getClass().getSimpleName());}
   java.util.ArrayList<Runnable> callbacks;
   synchronized(LocalRecognizer.this){loading=false;callbacks=new java.util.ArrayList<>(listeners);listeners.clear();}
   for(Runnable callback:callbacks)main.post(callback);
  });
 }
 synchronized void removeListener(Runnable listener){listeners.remove(listener);}
 Request recognize(float[] audio,BooleanSupplier active,Result callback){Request request=new Request(audio,active,callback);worker.execute(request);return request;}
 final class Request implements Runnable {
  private final PendingAudio audio;private final BooleanSupplier active;private final Result callback;
  Request(float[] samples,BooleanSupplier active,Result callback){audio=new PendingAudio(samples);this.active=active;this.callback=callback;}
  void cancel(){audio.cancel();worker.remove(this);}
  @Override public void run(){
   if(!audio.begin())return;
   long start=android.os.SystemClock.elapsedRealtime();String text="",error=null;OfflineStream stream=null;
   try{
    if(audio.canceled()||!active.getAsBoolean())return;
    if(!ready)throw new IllegalStateException("not_ready");
    stream=recognizer.createStream();stream.acceptWaveform(audio.samples,16000);recognizer.decode(stream);text=recognizer.getResult(stream).getText().trim();
   }catch(Throwable e){error=e.getClass().getSimpleName();}
   finally{try{if(stream!=null)stream.release();}finally{audio.finish();}}
   long elapsed=android.os.SystemClock.elapsedRealtime()-start;Diag.log("decode_finished ms="+elapsed+" chars="+text.length()+" error="+error);
   final String result=text,failure=error;main.post(()->{if(!audio.canceled()&&active.getAsBoolean())callback.complete(result,elapsed,failure);});
  }
 }
}
