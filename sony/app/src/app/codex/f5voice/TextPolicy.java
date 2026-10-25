package app.codex.f5voice;
final class TextPolicy {
 static String forInput(String text){
  String result=text==null?"":text.trim();
  while(result.endsWith("。")||result.endsWith("."))result=result.substring(0,result.length()-1).trim();
  return result;
 }
}
