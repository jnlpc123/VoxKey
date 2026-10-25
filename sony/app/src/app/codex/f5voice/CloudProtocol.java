package app.codex.f5voice;
import org.json.*;
import java.util.TreeMap;
final class CloudProtocol {
 final String id;
 private final TreeMap<Long,String> finals=new TreeMap<>();
 private long pendingTime=-1;private String pending="";
 CloudProtocol(String id){this.id=id;}
 static String endpoint(String workspace){String w=workspace==null?"":workspace.trim();if(w.isEmpty())return "wss://dashscope.aliyuncs.com/api-ws/v1/inference";if(!w.matches("[A-Za-z0-9_-]{1,128}"))throw new IllegalArgumentException("workspace");return "wss://"+w+".cn-beijing.maas.aliyuncs.com/api-ws/v1/inference";}
 static byte[] pcm(float[] samples,int size){byte[] b=new byte[size*2];for(int i=0;i<size;i++){int v=Math.max(-32768,Math.min(32767,Math.round(samples[i]*32768)));b[i*2]=(byte)v;b[i*2+1]=(byte)(v>>8);}return b;}
 private JSONObject header(String action)throws JSONException{return new JSONObject().put("action",action).put("task_id",id).put("streaming","duplex");}
 String runTask() throws JSONException{
  JSONObject parameters=new JSONObject().put("format","pcm").put("sample_rate",16000).put("punctuation_prediction_enabled",false).put("language_hints",new JSONArray().put("zh").put("en"));
  JSONObject payload=new JSONObject().put("task_group","audio").put("task","asr").put("function","recognition").put("model","paraformer-realtime-v2").put("parameters",parameters).put("input",new JSONObject());
  return new JSONObject().put("header",header("run-task")).put("payload",payload).toString();
 }
 String finishTask() throws JSONException{return new JSONObject().put("header",header("finish-task")).put("payload",new JSONObject().put("input",new JSONObject())).toString();}
 String event(String message) throws JSONException{
  JSONObject root=new JSONObject(message),h=root.getJSONObject("header");if(!id.equals(h.optString("task_id")))return "ignore";
  String event=h.optString("event");
  if(event.equals("result-generated")){
   JSONObject sentence=root.getJSONObject("payload").getJSONObject("output").getJSONObject("sentence");
   if(!sentence.optBoolean("heartbeat",false)){
    long time=sentence.optLong("begin_time",0);String value=sentence.optString("text","");
    if(sentence.optBoolean("sentence_end",false)){finals.put(time,value);if(pendingTime==time){pendingTime=-1;pending="";}}
    else if(!finals.containsKey(time)){pendingTime=time;pending=value;}
   }
  }
  return event;
 }
 String text(){TreeMap<Long,String> all=new TreeMap<>(finals);if(pendingTime>=0&&!all.containsKey(pendingTime))all.put(pendingTime,pending);StringBuilder b=new StringBuilder();for(String s:all.values())b.append(s);return b.toString();}
}
