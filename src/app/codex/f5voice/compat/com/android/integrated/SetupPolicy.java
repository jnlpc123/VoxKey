package app.codex.f5voice.compat.com.android.integrated;
final class SetupPolicy {
 static int next(boolean mic,boolean ime,boolean selected,boolean service,boolean capture){
  if(!mic)return 0;if(!ime)return 1;if(!selected)return 2;if(!service)return 3;if(!capture)return 4;return 5;
 }
}
