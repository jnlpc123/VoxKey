package app.codex.f5voice;
import android.inputmethodservice.InputMethodService;
import android.content.Intent;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.*;
import android.text.InputType;
import java.util.HashSet;
public final class VoiceIme extends InputMethodService {
 static VoiceIme instance;
 private KeyboardPanel panel;
 private Recorder recorder;
 private CloudRecognizer cloud;
 private boolean eligible;
 private LocalRecognizer local;
 private LocalRecognizer.Request decoding;
 private VoiceSettings settings;
 private final Runnable modelLoaded=()->{if(instance==this&&KeyService.gate.phase==0&&!settings.cloud())showReady();};
 private final HashSet<Integer> ownedKeys=new HashSet<>();
 @Override public void onCreate(){super.onCreate();instance=this;settings=new VoiceSettings(this);local=LocalRecognizer.shared();prepareMode();}
 private void prepareMode(){if(!settings.cloud())local.load(this,modelLoaded);}
 @Override public boolean onEvaluateFullscreenMode(){return false;}
 @Override public boolean onEvaluateInputViewShown(){return true;}
 @Override public View onCreateInputView(){panel=new KeyboardPanel(this,this::action);showReady();return panel;}
 private void show(String text){if(panel!=null)panel.status(text,settings.cloud());if(text.contains("失败")||text.contains("尚未就绪")||text.startsWith("请先")||text.startsWith("录音太短")||text.startsWith("未识别")||text.startsWith("麦克风")||text.contains("已失效")||text.startsWith("云端拒绝")||text.startsWith("云端连接"))android.widget.Toast.makeText(this,text,android.widget.Toast.LENGTH_SHORT).show();}
 private void showReady(){show(!eligible?"密码或非编辑输入框不启用语音":settings.cloud()?(settings.hasKey()?"按住语音键说话 · 音频发送到百炼云端":"云端未配置 API Key · 请选择“设置”完成配置"):(local.ready?"按住语音键说话，松开后本地填字":"正在准备本地模型… 可先使用编辑按钮或切换云端"));}
 @Override public boolean onKeyDown(int code,KeyEvent event){
  if(isInputViewShown()&&panel!=null){
   if(code==KeyEvent.KEYCODE_DPAD_UP||code==KeyEvent.KEYCODE_DPAD_DOWN||code==KeyEvent.KEYCODE_DPAD_LEFT||code==KeyEvent.KEYCODE_DPAD_RIGHT||code==KeyEvent.KEYCODE_DPAD_CENTER||code==KeyEvent.KEYCODE_ENTER){
    ownedKeys.add(code);
    if(code==KeyEvent.KEYCODE_DPAD_UP)panel.move(0,-1);else if(code==KeyEvent.KEYCODE_DPAD_DOWN)panel.move(0,1);else if(code==KeyEvent.KEYCODE_DPAD_LEFT)panel.move(-1,0);else if(code==KeyEvent.KEYCODE_DPAD_RIGHT)panel.move(1,0);else if(event.getRepeatCount()==0||panel.navigation.repeatable())panel.click();
    return true;
   }
  }
  return super.onKeyDown(code,event);
 }
 @Override public boolean onKeyUp(int code,KeyEvent e){return ownedKeys.remove(code)||super.onKeyUp(code,e);}
 private void keepEditor(){cancelVoice();KeyService.gate.editor(eligible&&isInputViewShown()&&getCurrentInputBinding()!=null);}
 private void action(int index){
  if(index==8){switchToOrdinary();return;}
  if(index==9){cancelVoice();requestHideSelf(0);return;}
  keepEditor();InputConnection c=getCurrentInputConnection();
  if(index==6){settings.cloud(!settings.cloud());prepareMode();showReady();return;}
  if(index==7){startActivity(new Intent(this,CloudSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));return;}
  if(c==null)return;
  if(index==0){CharSequence selected=c.getSelectedText(0);if(selected!=null&&selected.length()>0)c.commitText("",1);else if(!c.deleteSurroundingTextInCodePoints(1,0))c.deleteSurroundingText(1,0);}
  else if(index==1)c.commitText(" ",1);
  else if(index==2||index==3)send(c,index==2?KeyEvent.KEYCODE_DPAD_LEFT:KeyEvent.KEYCODE_DPAD_RIGHT);
  else if(index==4){if(c.performContextMenuAction(android.R.id.selectAll))c.commitText("",1);else show("当前应用不支持清空，请使用长按删除");}
  else if(index==5){EditorInfo info=getCurrentInputEditorInfo();int a=info==null?0:info.imeOptions&EditorInfo.IME_MASK_ACTION;if(a<=EditorInfo.IME_ACTION_NONE)send(c,KeyEvent.KEYCODE_ENTER);else c.performEditorAction(a);}
 }
 private void switchToOrdinary(){
  cancelVoice();InputMethodManager manager=(InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);InputMethodInfo target=null;
  for(InputMethodInfo info:manager.getEnabledInputMethodList()){
   if(info.getPackageName().equals(getPackageName()))continue;
   if(target==null||info.getPackageName().equals("com.sohu.inputmethod.sogouoem"))target=info;
  }
  if(target!=null){try{switchInputMethod(target.getId());return;}catch(RuntimeException ignored){}}
  KeyService.gate.editor(eligible&&isInputViewShown()&&getCurrentInputBinding()!=null);manager.showInputMethodPicker();
 }
 private void send(InputConnection c,int code){c.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));c.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));}
 @Override public void onUpdateSelection(int oldStart,int oldEnd,int start,int end,int a,int b){super.onUpdateSelection(oldStart,oldEnd,start,end,a,b);if(KeyService.gate.phase!=0&&(start!=oldStart||end!=oldEnd)){keepEditor();show("输入位置已变化，已取消本次语音");}}
 private boolean allowed(EditorInfo e){if(e==null)return false;int t=e.inputType,cls=t&InputType.TYPE_MASK_CLASS,var=t&InputType.TYPE_MASK_VARIATION;return cls!=InputType.TYPE_NULL&&!(cls==InputType.TYPE_CLASS_TEXT&&(var==InputType.TYPE_TEXT_VARIATION_PASSWORD||var==InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD||var==InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))&&!(cls==InputType.TYPE_CLASS_NUMBER&&var==InputType.TYPE_NUMBER_VARIATION_PASSWORD);}
 @Override public void onBindInput(){cancelVoice();eligible=false;super.onBindInput();}
 @Override public void onUnbindInput(){cancelVoice();eligible=false;super.onUnbindInput();}
 @Override public void onStartInput(EditorInfo e,boolean restart){super.onStartInput(e,restart);cancelVoice();eligible=allowed(e);}
 @Override public void onStartInputView(EditorInfo e,boolean restart){super.onStartInputView(e,restart);cancelVoice();eligible=allowed(e);KeyService.gate.editor(eligible);if(panel!=null)panel.navigation.selected=0;prepareMode();showReady();Diag.log("editor_visible eligible="+eligible+" cloud="+settings.cloud());}
 @Override public void onFinishInputView(boolean finishing){cancelVoice();super.onFinishInputView(finishing);}
 @Override public void onFinishInput(){cancelVoice();super.onFinishInput();}
 void startVoice(){
  if(getCurrentInputBinding()==null||!isInputViewShown()){cancelVoice();return;}
  final boolean useCloud=settings.cloud();final long ticket=KeyService.gate.generation;final InputConnection target=getCurrentInputConnection();
  if(!useCloud&&!local.ready){KeyService.gate.done();show("本地模型尚未就绪，请稍等或选择云端");return;}
  if(useCloud){
   try{String key=settings.key();if(key.isEmpty()){KeyService.gate.done();show("请先在设置中保存百炼 API Key；本次未录音、未上传");return;}
    cloud=new CloudRecognizer(CloudProtocol.endpoint(settings.workspace()),key,(text,elapsed,error)->finishResult(ticket,target,text,elapsed,error));
   }catch(Exception e){KeyService.gate.done();show("云端配置不可用，请重新保存 API Key");return;}
  }
  show(useCloud?"正在录音并发送百炼云端 · 松开完成":"正在本地录音 · 松开识别 · 返回取消");
  Recorder current=new Recorder();recorder=current;final CloudRecognizer session=cloud;
  current.start((audio,sampleCount,error)->{
   if(recorder!=current||ticket!=KeyService.gate.generation){java.util.Arrays.fill(audio,0);return;}
   recorder=null;if(KeyService.gate.phase==1)KeyService.gate.phase=2;
   if(error!=null||sampleCount<3200){java.util.Arrays.fill(audio,0);if(session!=null)session.cancel();finishResult(ticket,target,"",0,error==null?"录音太短，请按住后说话":"麦克风不可用，请重试");return;}
   if(useCloud){java.util.Arrays.fill(audio,0);show("云端识别中…");session.end();}
   else{show("本地识别中…");decoding=local.recognize(audio,()->ticket==KeyService.gate.generation&&KeyService.gate.active,(text,elapsed,failure)->finishResult(ticket,target,text,elapsed,failure==null?null:"本地识别失败，请重试"));}
  },session==null?null:session::audio);
 }
 private void finishResult(long ticket,InputConnection target,String text,long elapsed,String error){
  if(ticket!=KeyService.gate.generation||!KeyService.gate.active)return;
  decoding=null;
  if(recorder!=null){recorder.cancel();recorder=null;}
  if(KeyService.gate.phase==1){KeyService.gate.phase=2;if(error==null)error="云端任务提前结束，请重试";}
  if(!KeyService.gate.accept(ticket)||getCurrentInputBinding()==null||!isInputViewShown()||getCurrentInputConnection()!=target){Diag.log("stale_result_discarded");return;}
  cloud=null;String input=TextPolicy.forInput(text);
  if(error!=null)show(error);else if(input.isEmpty())show("未识别到文字，请重试");else if(target!=null&&target.commitText(input,1))show("已填入 "+input.length()+" 字 · "+String.format(java.util.Locale.US,"%.1f",elapsed/1000f)+" 秒");else show("输入框已失效，请重新打开");
  KeyService.gate.done();
 }
 void stopVoice(){if(recorder!=null){show(settings.cloud()?"正在结束云端录音…":"正在结束录音…");recorder.stop();}}
 void cancelVoice(){if(decoding!=null){decoding.cancel();decoding=null;}if(recorder!=null){recorder.cancel();recorder=null;}if(cloud!=null){cloud.cancel();cloud=null;}KeyService.gate.editor(false);}
 @Override public void onDestroy(){cancelVoice();local.removeListener(modelLoaded);instance=null;super.onDestroy();}
}



