package app.codex.f5voice;
import java.util.Arrays;
/** Bounded local PCM storage; cloud capture uses only its reusable streaming block. */
final class AudioBuffer {
 private float[] data;private int size;private final int maximum;
 AudioBuffer(int initial,int maximum){if(initial<1||maximum<initial)throw new IllegalArgumentException();data=new float[initial];this.maximum=maximum;}
 void append(float[] block,int count){if(count<0||count>block.length||count>maximum-size)throw new IllegalArgumentException();int required=size+count;if(required>data.length){float[] previous=data;data=Arrays.copyOf(previous,Math.min(maximum,Math.max(required,previous.length*2)));Arrays.fill(previous,0);}System.arraycopy(block,0,data,size,count);size=required;}
 int size(){return size;}
 int capacity(){return data.length;}
 float[] take(){float[] result=Arrays.copyOf(data,size);clear();return result;}
 void clear(){Arrays.fill(data,0);size=0;}
 boolean isZeroed(){for(float sample:data)if(sample!=0)return false;return true;}
}
