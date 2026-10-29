package app.codex.f5voice.compat.com.android.integrated;
import android.app.*;import android.content.*;import android.content.pm.PackageManager;import android.net.Uri;import android.os.*;import android.provider.Settings;import android.view.inputmethod.InputMethodManager;import android.widget.*;
public final class SetupActivity extends Activity {
 private Button microphone,ime,select,access,capture,key,recovery;private TextView summary;
 private final Handler main=new Handler();private final Runnable refresh=new Runnable(){public void run(){update();main.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle state){super.onCreate(state);CaptureSettings.prefs(this).edit().putBoolean("guideShown",true).apply();
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(TvStyle.dp(this,36),TvStyle.dp(this,18),TvStyle.dp(this,36),TvStyle.dp(this,18));
  root.addView(TvStyle.text(this,"设置 VoxKey",25,TvStyle.TEXT));
  root.addView(TvStyle.text(this,Build.MANUFACTURER+" · "+Build.MODEL,13,TvStyle.MUTED));
  summary=TvStyle.text(this,"",15,TvStyle.MUTED);root.addView(summary);
  microphone=add(root,()->{if(new SetupState(this).mic)appSettings();else if(CaptureSettings.prefs(this).getBoolean("micAsked",false)&&!shouldShowRequestPermissionRationale("android.permission.RECORD_AUDIO"))appSettings();else{CaptureSettings.prefs(this).edit().putBoolean("micAsked",true).apply();requestPermissions(new String[]{"android.permission.RECORD_AUDIO"},16);}});
  ime=add(root,()->open(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)));
  select=add(root,()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker());
  access=add(root,()->open(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
  capture=add(root,()->{if(DeviceProfile.sonyBrand()){if(DeviceProfile.sony())startActivity(new Intent(this,SonyRecoveryActivity.class));else note("此系统的索尼语音键辅助尚未支持");}else{CaptureSettings.prefs(this).edit().putBoolean("standardCapture",!CaptureSettings.enabled(this)).apply();update();}});
  key=add(root,()->{CaptureSettings.cycle(this);update();});
  recovery=add(root,()->{if(DeviceProfile.sonyBrand()){if(DeviceProfile.sony())startActivity(new Intent(this,SonyRecoveryActivity.class));else note("此系统的索尼辅助尚未支持");}else if(Build.VERSION.SDK_INT<26)note("Android 7 可使用语音输入；自动恢复需要 Android 8 及以上。");else if(!DeviceProfile.dangbei())note("未识别到当贝品牌。可手动开启输入框接管；自动恢复需单独适配固件。");else if(checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")!=PackageManager.PERMISSION_GRANTED)note("自动恢复是可选功能。电脑连接设备后执行：\n\nadb shell pm grant "+getPackageName()+" android.permission.WRITE_SECURE_SETTINGS\n\n授权后返回本页启用。普通语音输入不需要此权限。");else startActivity(new Intent(this,RecoverySettingsActivity.class));});
  Button test=add(root,()->{if(!new SetupState(this).ready()){note("请先完成上方权限和输入法设置，再打开测试输入框。");return;}startActivity(new Intent(this,MainActivity.class).putExtra("startInput",true));finish();});test.setText("测试语音输入");
  add(root,()->note("设备："+Build.MANUFACTURER+" / "+Build.BRAND+" / "+Build.MODEL+"\nAndroid："+Build.VERSION.RELEASE+"（API "+Build.VERSION.SDK_INT+"）\n语音键服务："+(KeyService.connected?"已连接":"未连接")+"\n最近收到的标准语音键："+(KeyService.lastVoiceCode==0?"暂无":Integer.toString(KeyService.lastVoiceCode))+"\n当前选择："+CaptureSettings.codeLabel(this)+"\n\n若服务已连接、测试时仍弹当贝助手且没有语音键事件，可能需要按固件另行诊断。" )).setText("设备与按键诊断");
  add(root,this::finish).setText("返回");
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(root);setContentView(TvStyle.scene(this,scroll));update();
  SetupState first=new SetupState(this);int next=SetupPolicy.next(first.mic,first.ime,first.selected,first.service,first.capture);new Button[]{microphone,ime,select,access,capture,test}[next].requestFocus();
 }
 private Button add(LinearLayout root,Runnable action){Button b=TvStyle.button(this,"",action);LinearLayout.LayoutParams p=(LinearLayout.LayoutParams)b.getLayoutParams();p.height=TvStyle.dp(this,43);p.topMargin=p.bottomMargin=TvStyle.dp(this,3);root.addView(b,p);return b;}
 private void update(){SetupState s=new SetupState(this);summary.setText(s.ready()?"权限已就绪；打开输入框后按住语音键测试":"先完成未就绪项目；接管仅在输入面板显示时生效");microphone.setText("1  麦克风 · "+(s.mic?"已授权":"去授权"));ime.setText("2  输入法 · "+(s.ime?"已启用":"去启用"));select.setText("3  当前输入法 · "+(s.selected?"VoxKey":"选择 VoxKey"));access.setText("4  语音键无障碍 · "+(s.service?(KeyService.connected?"已连接":"已开启，等待系统连接"):"去开启"));capture.setText("5  输入框接管 · "+(DeviceProfile.sonyBrand()?(s.capture?"索尼辅助已连接":"设置索尼辅助"):(s.capture?"已开启（点此关闭）":"已关闭（点此开启）")));key.setText("语音键 · "+CaptureSettings.codeLabel(this));key.setEnabled(!DeviceProfile.sonyBrand());recovery.setText(DeviceProfile.sonyBrand()?"辅助启动恢复（可选）":"开机恢复（可选） · "+(RecoveryState.enabled(this)?"已启用":checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==PackageManager.PERMISSION_GRANTED?"设置":"需 ADB 授权"));}
 private void note(String value){new AlertDialog.Builder(this).setTitle("VoxKey").setMessage(value).setPositiveButton("知道了",null).show();}
 private void appSettings(){open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}
 private void open(Intent intent){try{startActivity(intent);}catch(ActivityNotFoundException|SecurityException e){try{startActivity(new Intent(Settings.ACTION_SETTINGS));Toast.makeText(this,"请在系统设置中查找输入法、权限或无障碍",Toast.LENGTH_LONG).show();}catch(RuntimeException ignored){note("无法打开系统设置，请用遥控器手动进入系统设置。");}}}
 @Override protected void onResume(){super.onResume();main.removeCallbacks(refresh);main.post(refresh);}
 @Override protected void onPause(){main.removeCallbacks(refresh);super.onPause();}
 @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);update();}
}
