package app.codex.f5voice.compat.com.android.integrated;
import java.util.Arrays;
import org.json.*;
public class ImprovementTest {
 static int n;
 static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);n++;}
 public static void main(String[] args)throws Exception{
  ToolbarNavigation nav=new ToolbarNavigation();check(nav.selected==0,"delete initially focused");nav.move(-1,0);check(nav.selected==0,"left boundary");nav.move(1,0);check(nav.selected==1,"right");nav.move(0,1);check(nav.selected==6,"row below preserves column");nav.move(0,1);check(nav.selected==6,"bottom boundary");nav.move(0,-1);check(nav.selected==1,"row above");nav.move(-1,0);check(nav.repeatable(),"hold delete repeats");nav.move(1,0);check(!nav.repeatable(),"hold space must not repeat");
  check(CloudProtocol.endpoint("").equals("wss://dashscope.aliyuncs.com/api-ws/v1/inference"),"default official endpoint");check(CloudProtocol.endpoint("ws-test").equals("wss://ws-test.cn-beijing.maas.aliyuncs.com/api-ws/v1/inference"),"workspace endpoint");boolean rejected=false;try{CloudProtocol.endpoint("bad.com/path");}catch(IllegalArgumentException e){rejected=true;}check(rejected,"reject workspace URL injection");
  check(Arrays.equals(CloudProtocol.pcm(new float[]{-1,0,.5f,1},4),new byte[]{0,-128,0,0,0,64,-1,127}),"signed little endian PCM16");
  float[] reusable={-1,0,.5f,1};byte[] copied=CloudProtocol.pcm(reusable,4);Arrays.fill(reusable,0);check(Arrays.equals(copied,new byte[]{0,-128,0,0,0,64,-1,127}),"cloud PCM survives recorder block clearing");
  CloudProtocol p=new CloudProtocol("test-task");JSONObject run=new JSONObject(p.runTask());check(run.getJSONObject("header").getString("task_id").equals("test-task"),"run task id");JSONObject params=run.getJSONObject("payload").getJSONObject("parameters");check(!params.getBoolean("punctuation_prediction_enabled"),"cloud punctuation disabled");check(params.getInt("sample_rate")==16000,"matching sample rate");check(run.getJSONObject("payload").getString("model").equals("paraformer-realtime-v2"),"monthly-quota model");
  check(p.event("{\"header\":{\"task_id\":\"other\",\"event\":\"task-started\"}}").equals("ignore"),"foreign task ignored");
  check(p.event("{\"header\":{\"task_id\":\"test-task\",\"event\":\"task-started\"}}").equals("task-started"),"ready event");
  p.event(result(0,"流浪",false));p.event(result(0,"流浪地球",true));p.event(result(0,"流浪地球",true));p.event(result(1000,"第二部",false));check(p.text().equals("流浪地球第二部"),"dedup and replace interim");p.event(result(1000,"第二部",true));check(p.text().equals("流浪地球第二部"),"final does not duplicate interim");
  check(new JSONObject(p.finishTask()).getJSONObject("header").getString("task_id").equals("test-task"),"finish same id");
  System.out.println("PASS "+n+" UI navigation/cloud protocol checks");
 }
 static String result(int begin,String text,boolean end)throws Exception{return new JSONObject().put("header",new JSONObject().put("task_id","test-task").put("event","result-generated")).put("payload",new JSONObject().put("output",new JSONObject().put("sentence",new JSONObject().put("begin_time",begin).put("text",text).put("sentence_end",end)))).toString();}
}

