package app.codex.f5voice;
import android.app.Activity;
import android.os.*;
import android.graphics.Bitmap;
import android.view.*;
import android.widget.*;
import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.*;
public final class CloudSettingsActivity extends Activity {
 private final Handler handler=new Handler(Looper.getMainLooper());
 private PhonePairing session;private int generation;private boolean visible;
 private ImageView qr;private TextView state,clock,mode;private VoiceSettings settings;
 private final Runnable tick=new Runnable(){public void run(){if(!visible)return;if(session!=null){long seconds=session.remaining();clock.setText(seconds>0?"配对码剩余 "+seconds/60+" 分 "+seconds%60+" 秒":"配对码已过期");}handler.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle saved){super.onCreate(saved);getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);settings=new VoiceSettings(this);
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.HORIZONTAL);int pad=TvStyle.dp(this,36);root.setPadding(pad,pad,pad,pad);root.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout left=new LinearLayout(this);left.setOrientation(1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(0,0,TvStyle.dp(this,40),0);root.addView(left,lp);
  left.addView(TvStyle.text(this,"用手机配置云端识别",28,TvStyle.TEXT));left.addView(TvStyle.text(this,"手机连接同一 Wi-Fi，扫码填写百炼 API Key。\n保存后立即同步到投影仪，无需遥控器打字。",17,TvStyle.MUTED));
  mode=TvStyle.text(this,"",17,TvStyle.TEXT);left.addView(mode);updateMode();
  left.addView(TvStyle.button(this,"使用本地离线",()->{settings.cloud(false);updateMode();}));
  left.addView(TvStyle.button(this,"使用百炼云端",()->{if(settings.hasKey()){settings.cloud(true);updateMode();}else state.setText("请先扫码填写 API Key");}));
  left.addView(TvStyle.button(this,"刷新配对码",this::prepare));
  left.addView(TvStyle.button(this,"清除云端密钥",()->{stopPairing();settings.forget();settings.cloud(false);updateMode();prepare();}));
  left.addView(TvStyle.button(this,"返回",this::finish));for(int i=0;i<left.getChildCount();i++){android.view.View child=left.getChildAt(i);if(child instanceof Button){LinearLayout.LayoutParams buttonParams=(LinearLayout.LayoutParams)child.getLayoutParams();buttonParams.height=TvStyle.dp(this,48);buttonParams.topMargin=TvStyle.dp(this,4);buttonParams.bottomMargin=TvStyle.dp(this,4);child.setLayoutParams(buttonParams);}}
  LinearLayout right=new LinearLayout(this);right.setOrientation(1);right.setGravity(Gravity.CENTER);root.addView(right,new LinearLayout.LayoutParams(TvStyle.dp(this,310),-2));
  qr=new ImageView(this);qr.setScaleType(ImageView.ScaleType.FIT_CENTER);qr.setContentDescription("手机扫码配置云端识别");right.addView(qr,new LinearLayout.LayoutParams(TvStyle.dp(this,260),TvStyle.dp(this,260)));
  clock=TvStyle.text(this,"",16,TvStyle.TEXT);right.addView(clock);state=TvStyle.text(this,"",15,TvStyle.MUTED);state.setGravity(Gravity.CENTER);right.addView(state);
  right.addView(TvStyle.text(this,"仅在可信的同一 Wi-Fi 使用\n离开此页或保存后，配对码立即失效",13,TvStyle.MUTED));setContentView(TvStyle.scene(this,root));
 }
 private void updateMode(){mode.setText("当前："+(settings.cloud()?"百炼云端":"本地离线")+"  ·  "+(settings.hasKey()?"密钥已保存":"尚未配置密钥"));}
 @Override protected void onStart(){super.onStart();visible=true;prepare();handler.post(tick);}
 @Override protected void onStop(){visible=false;stopPairing();handler.removeCallbacks(tick);super.onStop();}
 private void stopPairing(){generation++;PhonePairing old=session;session=null;if(old!=null)old.close();qr.setImageDrawable(null);clock.setText("");}
 private void prepare(){if(!visible)return;stopPairing();final int request=generation;state.setText("正在生成配对码…");new Thread(()->{PhonePairing created=null;try{
   created=new PhonePairing(this,new PhonePairing.Listener(){public void saved(){handler.post(()->{if(visible&&request==generation){stopPairing();state.setText("配置已保存，可返回继续输入");updateMode();}});}public void expired(){handler.post(()->{if(visible&&request==generation){stopPairing();state.setText("配对码已过期，请刷新");}});}});
   Map<EncodeHintType,Object> hints=new EnumMap<>(EncodeHintType.class);hints.put(EncodeHintType.ERROR_CORRECTION,ErrorCorrectionLevel.M);hints.put(EncodeHintType.MARGIN,4);
   BitMatrix matrix=new QRCodeWriter().encode(created.url(),BarcodeFormat.QR_CODE,640,640,hints);int[] pixels=new int[640*640];for(int y=0;y<640;y++)for(int x=0;x<640;x++)pixels[y*640+x]=matrix.get(x,y)?0xFF000000:0xFFFFFFFF;Bitmap bitmap=Bitmap.createBitmap(pixels,640,640,Bitmap.Config.ARGB_8888);PhonePairing ready=created;
   handler.post(()->{if(!visible||request!=generation){ready.close();return;}session=ready;qr.setImageBitmap(bitmap);state.setText("扫码后在手机浏览器中打开\nParaformer-realtime-v2 · 北京");ready.start();});
  }catch(Exception ignored){if(created!=null)created.close();handler.post(()->{if(visible&&request==generation)state.setText("无法生成配对码，请检查 Wi-Fi 后刷新");});}},"F5-prepare-pairing").start();}
}
