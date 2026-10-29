param([string]$JdkDirectory=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
if(!$JdkDirectory){throw 'Set JAVA_HOME or pass -JdkDirectory'}
$voiceJdk=$JdkDirectory
$testDir=Join-Path $PSScriptRoot 'test-build'
New-Item -ItemType Directory $testDir -Force | Out-Null
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\VoiceGate.java" "$PSScriptRoot\tests\VoiceGateTest.java" "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\TextPolicy.java" "$PSScriptRoot\tests\TextPolicyTest.java"
if($LASTEXITCODE -ne 0){throw 'Test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.VoiceGateTest
if($LASTEXITCODE -ne 0){throw 'Session/key tests failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.TextPolicyTest
if($LASTEXITCODE -ne 0){throw 'Terminal-period tests failed'}
$testJson=Join-Path $PSScriptRoot 'deps\json-test-20240303.jar'
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -cp $testJson -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\ToolbarNavigation.java" "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\CloudProtocol.java" "$PSScriptRoot\tests\ImprovementTest.java"
if($LASTEXITCODE -ne 0){throw 'Improvement test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp "$testDir;$testJson" app.codex.f5voice.compat.com.android.integrated.ImprovementTest
if($LASTEXITCODE -ne 0){throw 'Improvement tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\AudioBuffer.java" "$PSScriptRoot\tests\AudioBufferTest.java"
if($LASTEXITCODE -ne 0){throw 'Audio buffer compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.AudioBufferTest
if($LASTEXITCODE -ne 0){throw 'Audio buffer tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\PendingAudio.java" "$PSScriptRoot\tests\PendingAudioTest.java"
if($LASTEXITCODE -ne 0){throw 'Pending audio compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.PendingAudioTest
if($LASTEXITCODE -ne 0){throw 'Pending audio tests failed'}

& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\RecoveryPolicy.java" "$PSScriptRoot\tests\RecoveryPolicyTest.java"
if($LASTEXITCODE -ne 0){throw 'Recovery test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.RecoveryPolicyTest
if($LASTEXITCODE -ne 0){throw 'Recovery tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\MigrationPolicy.java" "$PSScriptRoot\tests\MigrationPolicyTest.java"
if($LASTEXITCODE -ne 0){throw 'Migration test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.MigrationPolicyTest
if($LASTEXITCODE -ne 0){throw 'Migration tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\AssetIntegrity.java" "$PSScriptRoot\tests\AssetIntegrityTest.java"
if($LASTEXITCODE -ne 0){throw 'Asset integrity test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.AssetIntegrityTest
if($LASTEXITCODE -ne 0){throw 'Asset integrity tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\DevicePolicy.java" "$PSScriptRoot\tests\DevicePolicyTest.java" "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\SonyRecoveryPolicy.java" "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\SyncFrame.java" "$PSScriptRoot\tests\SonyRecoveryTest.java"
if($LASTEXITCODE -ne 0){throw 'Device/Sony test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.DevicePolicyTest
if($LASTEXITCODE -ne 0){throw 'Device routing tests failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.SonyRecoveryTest
if($LASTEXITCODE -ne 0){throw 'Sony recovery tests failed'}
$transportSources=@(Get-ChildItem "$PSScriptRoot\src\com\cgutman\adblib" -Filter '*.java' | Select-Object -ExpandProperty FullName)
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir @transportSources "$PSScriptRoot\tests\AdbTransportTest.java"
if($LASTEXITCODE -ne 0){throw 'ADB transport compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir com.cgutman.adblib.AdbTransportTest
if($LASTEXITCODE -ne 0){throw 'ADB transport tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\DevicePolicy.java" "$PSScriptRoot\src\app\codex\f5voice\compat\com\android\integrated\SetupPolicy.java" "$PSScriptRoot\tests\SetupPolicyTest.java"
if($LASTEXITCODE -ne 0){throw 'Setup test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.compat.com.android.integrated.SetupPolicyTest
if($LASTEXITCODE -ne 0){throw 'Setup tests failed'}
