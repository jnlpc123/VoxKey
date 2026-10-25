package app.codex.f5voice;
import java.io.*;import java.nio.*;import java.nio.charset.StandardCharsets;
final class SyncFrame {
 static final class Reply {final String id;final byte[] payload;Reply(String id,byte[] p){this.id=id;payload=p;}}
 static byte[] header(String id,int n){if(id.length()!=4)throw new IllegalArgumentException();return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put(id.getBytes(StandardCharsets.US_ASCII)).putInt(n).array();}
 static byte[] packet(String id,byte[] p){byte[] out=new byte[p.length+8];System.arraycopy(header(id,p.length),0,out,0,8);System.arraycopy(p,0,out,8,p.length);return out;}
 static Reply read(InputStream in)throws IOException {byte[] h=new byte[8];new DataInputStream(in).readFully(h);int n=ByteBuffer.wrap(h,4,4).order(ByteOrder.LITTLE_ENDIAN).getInt();if(n<0||n>65536)throw new IOException("SYNC length");byte[] p=new byte[n];new DataInputStream(in).readFully(p);return new Reply(new String(h,0,4,StandardCharsets.US_ASCII),p);}
}
