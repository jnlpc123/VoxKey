package app.codex.f5voice;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.widget.*;
import android.view.inputmethod.InputMethodManager;
public final class MainActivity extends Activity {
 private VoiceSettings settings;
 private TextView engine;
 private final Runnable modelLoaded=()->{if(!isFinishing()&&!isDestroyed()&&!settings.cloud())engine.setText(LocalRecognizer.shared().ready?"本地离线  ·  已就绪":"本地模型暂未就绪，可在设置中切换云端");};
 private final android.content.SharedPreferences.OnSharedPreferenceChangeListener changes=(prefs,key)->{if("cloud".equals(key))refreshMode();};
 private void refreshMode(){
  if(settings==null||engine==null)return;
  if(settings.cloud()){engine.setText("百炼云端  ·  Paraformer");return;}
  engine.setText(LocalRecognizer.shared().ready?"本地离线  ·  已就绪":"本地离线  ·  准备模型中…");
  if(!LocalRecognizer.shared().ready)LocalRecognizer.shared().load(this,modelLoaded);
 }
 @Override public void onCreate(Bundle state){
  super.onCreate(state);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(android.graphics.Color.TRANSPARENT);LinearLayout root=new LinearLayout(this);root.setOrientation(1);int p=TvStyle.dp(this,42);root.setPadding(p,TvStyle.dp(this,28),p,TvStyle.dp(this,28));scroll.addView(root);setContentView(TvStyle.scene(this,scroll));
  LinearLayout header=new LinearLayout(this);header.setGravity(android.view.Gravity.CENTER_VERTICAL);ImageView icon=new ImageView(this);icon.setImageResource(getResources().getIdentifier("voice_icon","drawable",getPackageName()));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(TvStyle.dp(this,44),TvStyle.dp(this,44));ip.setMargins(0,0,TvStyle.dp(this,12),0);header.addView(icon,ip);header.addView(TvStyle.text(this,"VoxKey",23,TvStyle.TEXT),new LinearLayout.LayoutParams(0,-2,1));
  TextClock clock=new TextClock(this);clock.setFormat12Hour("HH:mm");clock.setFormat24Hour("HH:mm");clock.setTextSize(18);clock.setTextColor(TvStyle.TEXT);header.addView(clock);root.addView(header);
  TextView title=TvStyle.text(this,"说出你想看的",31,TvStyle.TEXT);root.addView(title);TextView help=TvStyle.text(this,"按住遥控器语音键，说完松开。",16,TvStyle.MUTED);root.addView(help);
  settings=new VoiceSettings(this);engine=TvStyle.text(this,"",14,TvStyle.MUTED);root.addView(engine);settings.watch(changes);refreshMode();
  EditText editor=TvStyle.edit(this,"在这里试试语音输入",false);root.addView(editor,new LinearLayout.LayoutParams(-1,TvStyle.dp(this,58)));
  android.view.View space=new android.view.View(this);root.addView(space,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout dock=new LinearLayout(this);dock.setPadding(TvStyle.dp(this,10),TvStyle.dp(this,8),TvStyle.dp(this,10),TvStyle.dp(this,8));dock.setBackground(TvStyle.shape(this,0xFF24344C,false));LinearLayout.LayoutParams dockParams=new LinearLayout.LayoutParams(-1,-2);dockParams.setMargins(0,TvStyle.dp(this,16),0,0);root.addView(dock,dockParams);
  TextView actionLabel=TvStyle.text(this,"输入操作",18,TvStyle.TEXT);actionLabel.setGravity(android.view.Gravity.CENTER_VERTICAL);dock.addView(actionLabel,new LinearLayout.LayoutParams(0,-1,1));
  Button begin=TvStyle.button(this,"开始输入",()->{editor.requestFocus();((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(editor,InputMethodManager.SHOW_IMPLICIT);});
  Button config=TvStyle.button(this,"识别设置",()->startActivity(new Intent(this,CloudSettingsActivity.class)));Button keyboard=TvStyle.button(this,"切换键盘",()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
  for(Button button:new Button[]{begin,config,keyboard}){LinearLayout.LayoutParams b=new LinearLayout.LayoutParams(TvStyle.dp(this,146),TvStyle.dp(this,52));b.setMargins(TvStyle.dp(this,4),TvStyle.dp(this,4),TvStyle.dp(this,4),TvStyle.dp(this,4));dock.addView(button,b);}
  final android.view.View[] landing={header,title,help,engine,dock};
  scroll.getViewTreeObserver().addOnGlobalLayoutListener(()->{
   boolean typing=getResources().getDisplayMetrics().heightPixels-scroll.getHeight()>TvStyle.dp(this,100);
   int visibility=typing?android.view.View.GONE:android.view.View.VISIBLE;
   for(android.view.View view:landing)if(view.getVisibility()!=visibility)view.setVisibility(visibility);
  });
  begin.requestFocus();
 }
 @Override public void onResume(){super.onResume();refreshMode();}
 @Override public void onDestroy(){if(settings!=null)settings.unwatch(changes);LocalRecognizer.shared().removeListener(modelLoaded);super.onDestroy();}
}




