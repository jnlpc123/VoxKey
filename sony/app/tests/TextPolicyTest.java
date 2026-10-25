package app.codex.f5voice;
public class TextPolicyTest {
 static int n;
 static void same(String expected,String actual){if(!expected.equals(actual))throw new AssertionError("expected ["+expected+"] but got ["+actual+"]");n++;}
 public static void main(String[] args){
  same("流浪地球",TextPolicy.forInput("流浪地球。"));
  same("流浪地球",TextPolicy.forInput(" 流浪地球. \n"));
  same("3.14",TextPolicy.forInput("3.14。"));
  same("bilibili.com",TextPolicy.forInput("bilibili.com。"));
  same("第一句。第二句",TextPolicy.forInput("第一句。第二句。"));
  same("你好！",TextPolicy.forInput("你好！"));
  same("",TextPolicy.forInput(null));
  same("",TextPolicy.forInput("。"));
  same("原文",TextPolicy.forInput("原文"));
  System.out.println("PASS "+n+" terminal-period checks");
 }
}
