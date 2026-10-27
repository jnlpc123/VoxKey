package app.codex.f5voice.compat.com.android.integrated;
import android.app.Activity;import android.os.*;import android.widget.*;
public final class SonyRecoveryActivity extends Activity {
 private TextView status;private final Handler main=new Handler();private final Runnable refresh=new Runnable(){public void run(){status.setText(SonyRecovery.status(SonyRecoveryActivity.this));main.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle state){super.onCreate(state);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(TvStyle.dp(this,42),TvStyle.dp(this,28),TvStyle.dp(this,42),TvStyle.dp(this,28));root.addView(TvStyle.text(this,"索尼语音键辅助",28,TvStyle.TEXT));status=TvStyle.text(this,"",20,TvStyle.TEXT);root.addView(status);root.addView(TvStyle.text(this,"首次连接请允许本机调试",16,TvStyle.MUTED));
  Button enable=TvStyle.button(this,"启用辅助",()->SonyRecovery.enable(this));root.addView(enable);root.addView(TvStyle.button(this,"重新连接",()->{SonyRecovery.pause(this);SonyRecovery.enable(this);}));root.addView(TvStyle.button(this,"暂停辅助",()->SonyRecovery.pause(this)));root.addView(TvStyle.button(this,"返回",()->finish()));setContentView(TvStyle.scene(this,root));enable.requestFocus();}
 @Override public void onResume(){super.onResume();main.post(refresh);}
 @Override public void onPause(){main.removeCallbacks(refresh);super.onPause();}
}
