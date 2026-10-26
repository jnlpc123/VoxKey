package app.codex.f5voice.compat.com.android.recovery;
final class RecoveryPolicy {
 static final String TARGET="app.codex.f5voice",IME=TARGET+"/.VoiceIme",KEY=TARGET+"/"+TARGET+".KeyService";
 static String canonical(String component){if(component==null)return "";String[] p=component.split("/",-1);if(p.length!=2)return component;return p[0]+"/"+(p[1].startsWith(".")?p[0]+p[1]:p[1]);}
 static boolean same(String a,String b){return canonical(a).equals(canonical(b));}
 static boolean contains(String list,String target,boolean subtype){if(list==null)return false;for(String entry:list.split(":")){String id=subtype?entry.split(";",2)[0]:entry;if(same(id,target))return true;}return false;}
 static String add(String list,String target,boolean subtype){if(list==null||list.isEmpty()||"null".equals(list))return target;if(contains(list,target,subtype))return list;return list+":"+target;}
 static String addService(String list){return add(list,KEY,false);}
 static String addIme(String list){return add(list,IME,true);}
 static String id(String entry,boolean subtype){return subtype?entry.split(";",2)[0]:entry;}
 static String merge(String seen,String latest,String target,boolean subtype){String out=latest==null||"null".equals(latest)?"":latest;if(seen!=null&&!"null".equals(seen))for(String entry:seen.split(":")){if(!entry.isEmpty()&&!contains(out,id(entry,subtype),subtype))out=out.isEmpty()?entry:out+":"+entry;}return add(out,target,subtype);}
 static boolean includesAll(String list,String expected,boolean subtype){if(expected==null||"null".equals(expected))return true;for(String entry:expected.split(":"))if(!entry.isEmpty()&&!contains(list,id(entry,subtype),subtype))return false;return true;}
 static boolean cleanupSignature(String ime,String services){return !same(IME,ime)&&!contains(services,KEY,false);}
 static final class Session {
  String boot;boolean interactive,pending,imeDone;
  Session(String boot,boolean interactive,boolean pending){this.boot=boot;this.interactive=interactive;this.pending=pending;}
  boolean power(String nowBoot,boolean on){boolean newBoot=!nowBoot.equals(boot),wake=on&&!interactive;boot=nowBoot;interactive=on;if(!on||newBoot||wake){pending=true;imeDone=false;}return on&&(newBoot||wake);}
  void complete(){pending=false;}
  void manual(){pending=true;imeDone=false;}
 }
}
