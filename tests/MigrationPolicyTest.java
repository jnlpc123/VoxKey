package app.codex.f5voice.compat.com.android.integrated;
public final class MigrationPolicyTest {
 static int n;static void check(boolean v){n++;if(!v)throw new AssertionError("check "+n);}
 public static void main(String[] a){
  check(MigrationPolicy.authorized(new String[]{"app.codex.f5voice.compat.com.android.integrated"},true));
  check(!MigrationPolicy.authorized(new String[]{"app.codex.f5voice.compat.com.android.integrated"},false));
  check(!MigrationPolicy.authorized(new String[]{"app.codex.f5voice.compat.com.android.integrated.evil"},true));
  check(!MigrationPolicy.authorized(new String[]{"app.codex.f5voice"},true));
  check(!MigrationPolicy.authorized(new String[]{"com.android.shell"},true));
  check(!MigrationPolicy.authorized(null,true));
  check(MigrationPolicy.shouldImport(false,false,true));
  check(!MigrationPolicy.shouldImport(true,false,true));
  check(!MigrationPolicy.shouldImport(false,true,true));
  check(!MigrationPolicy.shouldImport(false,false,false));
  check(MigrationPolicy.valid(1,true,"example"));check(MigrationPolicy.valid(1,false,""));
  check(!MigrationPolicy.valid(2,true,"example"));check(!MigrationPolicy.valid(1,true,""));
  check(!MigrationPolicy.valid(1,false,"example"));check(!MigrationPolicy.valid(1,false,null));
  check(!MigrationPolicy.userConfigured(false,true,true));
  check(MigrationPolicy.userConfigured(true,true,true));
  check(MigrationPolicy.userConfigured(false,true,false));
  check(!MigrationPolicy.userConfigured(false,false,false));
  System.out.println("PASS "+n+" migration ACL/schema/overwrite checks");
 }
}
