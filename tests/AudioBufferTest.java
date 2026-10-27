package app.codex.f5voice.compat.com.android.integrated;
public final class AudioBufferTest {
 static int checks;
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
 public static void main(String[] args){
  AudioBuffer buffer=new AudioBuffer(4,10);check(buffer.capacity()==4,"small initial allocation");
  buffer.append(new float[]{1,2,3},3);buffer.append(new float[]{4,5,6},3);check(buffer.capacity()==8,"grows on demand");check(buffer.size()==6,"count preserved");
  float[] samples=buffer.take();check(samples.length==6&&samples[0]==1&&samples[5]==6,"all samples preserved");check(buffer.size()==0&&buffer.isZeroed(),"internal PCM wiped after transfer");
  buffer.append(new float[]{1,2,3,4,5,6,7,8,9,10},10);check(buffer.capacity()==10,"growth capped");boolean rejected=false;try{buffer.append(new float[]{11},1);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"30 second limit cannot overflow");
  buffer.clear();check(buffer.size()==0&&buffer.isZeroed(),"cancellation clears PCM");
  rejected=false;try{buffer.append(new float[1],2);}catch(IllegalArgumentException e){rejected=true;}check(rejected,"invalid block length rejected");
  System.out.println("PASS "+checks+" audio buffer checks");
 }
}
