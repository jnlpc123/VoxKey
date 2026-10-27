package app.codex.f5voice.compat.com.android.integrated;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioDeviceInfo;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import java.util.Arrays;
final class Recorder {
 interface Callback{void finished(float[] audio,int sampleCount,String error);}
 interface Chunk{void audio(float[] samples,int count);}
 private volatile boolean stop,canceled;
 private static final Object MIC_LOCK=new Object();
 private final Handler main=new Handler(Looper.getMainLooper());
 void stop(){stop=true;}
 void cancel(){canceled=true;stop=true;}
 void start(Callback callback,Chunk chunk){new Thread(()->{synchronized(MIC_LOCK){capture(callback,chunk);}},"VoxKey-capture").start();}
 private void capture(Callback cb,Chunk chunk){
  AudioRecord record=null;AudioBuffer local=chunk==null?new AudioBuffer(16000,480000):null;
  short[] block=new short[1600];float[] part=new float[1600];int count=0,peak=0;double energy=0;String stats="",error=null;float[] result=new float[0];
  try{
   if(canceled)return;
   if(stop)return;
   int min=AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
   if(min<=0)throw new IllegalStateException("format");
   record=new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(6400,min*2));
   if(record.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("init");
   if(canceled||stop)return;
   record.startRecording();Diag.log("capture_started source=6 rate=16000");
   long deadline=android.os.SystemClock.elapsedRealtime()+30000;
   while(!stop&&count<480000&&android.os.SystemClock.elapsedRealtime()<deadline){
    int n=record.read(block,0,Math.min(block.length,480000-count),AudioRecord.READ_NON_BLOCKING);
    if(n<0)throw new IllegalStateException("read_"+n);
    for(int i=0;i<n;i++){int x=block[i];part[i]=x/32768f;peak=Math.max(peak,Math.abs(x));energy+=(double)x*x;}
    count+=n;
    if(n>0&&!canceled){if(local!=null)local.append(part,n);else chunk.audio(part,n);}
    Arrays.fill(part,0);Arrays.fill(block,(short)0);
    if(n==0)Thread.sleep(10);
   }
   AudioDeviceInfo device=record.getRoutedDevice();stats="samples="+count+" peak="+peak+" rms="+(count==0?0:(int)Math.sqrt(energy/count))+" route="+(device==null?"null":device.getId()+"/type"+device.getType()+"/"+device.getProductName());
   if(!canceled&&local!=null)result=local.take();
  }catch(Exception e){error=e.getClass().getSimpleName();Diag.log("capture_failed "+error);}
  finally{
   if(record!=null){try{record.stop();}catch(Exception ignored){}record.release();}
   if(local!=null)local.clear();Arrays.fill(part,0);Arrays.fill(block,(short)0);
   final float[] audio=result;final int samples=count;final String failure=error;
   Diag.log("capture_finished canceled="+canceled+" "+stats);
   if(!canceled)main.post(()->{if(canceled)Arrays.fill(audio,0);else cb.finished(audio,samples,failure);});else Arrays.fill(audio,0);
  }
 }
}
