param([string]$JdkDirectory=$env:JAVA_HOME)
$ErrorActionPreference='Stop';if(!$JdkDirectory){throw 'Set JAVA_HOME or pass JDK path'}
$root=$PSScriptRoot;$testOut=Join-Path $root 'test-build';New-Item -ItemType Directory -Force $testOut | Out-Null
& "$JdkDirectory/bin/javac.exe" -encoding UTF-8 -d $testOut "$root/src/app/codex/f5voice/compat/com/android/recovery/RecoveryPolicy.java" "$root/tests/RecoveryPolicyTest.java"
if($LASTEXITCODE -ne 0){throw 'Test compilation failed'}
& "$JdkDirectory/bin/java.exe" -cp $testOut app.codex.f5voice.compat.com.android.recovery.RecoveryPolicyTest
if($LASTEXITCODE -ne 0){throw 'Tests failed'}