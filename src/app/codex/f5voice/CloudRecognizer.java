package app.codex.f5voice;
import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.*;
import okio.ByteString;
/** PCM goes to the chosen cloud only after explicit cloud-mode recording. */
final class CloudRecognizer extends WebSocketListener {
 interface Result{void complete(String text,long elapsed,String error);}
 private static final OkHttpClient CLIENT=new OkHttpClient.Builder().connectTimeout(10,TimeUnit.SECONDS).readTimeout(0,TimeUnit.SECONDS).retryOnConnectionFailure(false).build();
 private final Handler main=new Handler(Looper.getMainLooper());
 private final CloudProtocol protocol=new CloudProtocol(UUID.randomUUID().toString());
 private final ArrayList<ByteString> queue=new ArrayList<>();
 private final Result callback;
 private WebSocket socket;
 private boolean started,ended,finished,sentFinish;
 private long begin=android.os.SystemClock.elapsedRealtime();
 private int queuedBytes;
 private final Runnable timeout=()->fail("云端连接或识别超时，请稍后重试");
 CloudRecognizer(String endpoint,String key,Result callback){this.callback=callback;socket=CLIENT.newWebSocket(new Request.Builder().url(endpoint).header("Authorization","Bearer "+key).build(),this);main.postDelayed(timeout,65000);}
 @Override public synchronized void onOpen(WebSocket ws,Response response){if(finished){ws.cancel();return;}socket=ws;try{if(!ws.send(protocol.runTask()))fail("云端任务发送失败");}catch(Exception e){fail("云端任务参数异常");}}
 synchronized void audio(float[] block,int count){
  if(finished)return;byte[] pcm=CloudProtocol.pcm(block,count);ByteString bytes=ByteString.of(pcm);java.util.Arrays.fill(pcm,(byte)0);
  if(started){if(!socket.send(bytes))fail("云端音频发送失败");}
  else{queuedBytes+=bytes.size();if(queuedBytes>960000){fail("云端连接超时，已取消发送");return;}queue.add(bytes);}
 }
 synchronized void end(){if(finished)return;ended=true;main.removeCallbacks(timeout);main.postDelayed(timeout,25000);finishIfReady();}
 private void finishIfReady(){if(started&&ended&&!sentFinish&&!finished){sentFinish=true;try{if(!socket.send(protocol.finishTask()))fail("云端结束任务失败");}catch(Exception e){fail("云端任务参数异常");}}}
 @Override public synchronized void onMessage(WebSocket ws,String message){
  if(finished)return;
  try{
   String event=protocol.event(message);
   if(event.equals("task-started")){
    if(started)return;started=true;for(ByteString bytes:queue){if(!ws.send(bytes)){fail("云端音频发送失败");return;}}queue.clear();queuedBytes=0;finishIfReady();
   }else if(event.equals("task-finished")){complete(protocol.text(),null);}
   else if(event.equals("task-failed")){fail("云端拒绝任务，请检查模型权限、密钥和账号额度");}
  }catch(Exception e){fail("云端返回格式异常");}
 }
 @Override public void onFailure(WebSocket ws,Throwable error,Response response){int code=response==null?0:response.code();fail(code==401||code==403?"云端鉴权失败，请检查北京地域 API Key":"云端连接失败，请检查网络和服务配置");}
 @Override public synchronized void onClosed(WebSocket ws,int code,String reason){if(!finished)fail("云端连接已关闭，请重试");}
 private synchronized void fail(String message){complete("",message);}
 private void complete(String text,String error){
  if(finished)return;finished=true;queue.clear();queuedBytes=0;main.removeCallbacks(timeout);if(socket!=null)socket.cancel();
  long elapsed=android.os.SystemClock.elapsedRealtime()-begin;Diag.log("cloud_finished ms="+elapsed+" chars="+text.length()+" success="+(error==null));main.post(()->callback.complete(text,elapsed,error));
 }
 synchronized void cancel(){if(finished)return;finished=true;queue.clear();queuedBytes=0;main.removeCallbacks(timeout);if(socket!=null)socket.cancel();}
}
