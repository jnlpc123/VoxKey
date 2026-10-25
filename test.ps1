param([string]$JdkDirectory=$env:JAVA_HOME)
$ErrorActionPreference='Stop'
if(!$JdkDirectory){throw 'Set JAVA_HOME or pass -JdkDirectory'}
$voiceJdk=$JdkDirectory
$testDir=Join-Path $PSScriptRoot 'test-build'
New-Item -ItemType Directory $testDir -Force | Out-Null
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\VoiceGate.java" "$PSScriptRoot\tests\VoiceGateTest.java" "$PSScriptRoot\src\app\codex\f5voice\TextPolicy.java" "$PSScriptRoot\tests\TextPolicyTest.java"
if($LASTEXITCODE -ne 0){throw 'Test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.VoiceGateTest
if($LASTEXITCODE -ne 0){throw 'Session/key tests failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.TextPolicyTest
if($LASTEXITCODE -ne 0){throw 'Terminal-period tests failed'}
$testJson=Join-Path $PSScriptRoot 'deps\json-test-20240303.jar'
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -cp $testJson -d $testDir "$PSScriptRoot\src\app\codex\f5voice\ToolbarNavigation.java" "$PSScriptRoot\src\app\codex\f5voice\CloudProtocol.java" "$PSScriptRoot\tests\ImprovementTest.java"
if($LASTEXITCODE -ne 0){throw 'Improvement test compilation failed'}
& "$voiceJdk\bin\java.exe" -cp "$testDir;$testJson" app.codex.f5voice.ImprovementTest
if($LASTEXITCODE -ne 0){throw 'Improvement tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\AudioBuffer.java" "$PSScriptRoot\tests\AudioBufferTest.java"
if($LASTEXITCODE -ne 0){throw 'Audio buffer compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.AudioBufferTest
if($LASTEXITCODE -ne 0){throw 'Audio buffer tests failed'}
& "$voiceJdk\bin\javac.exe" -encoding UTF-8 -d $testDir "$PSScriptRoot\src\app\codex\f5voice\PendingAudio.java" "$PSScriptRoot\tests\PendingAudioTest.java"
if($LASTEXITCODE -ne 0){throw 'Pending audio compilation failed'}
& "$voiceJdk\bin\java.exe" -cp $testDir app.codex.f5voice.PendingAudioTest
if($LASTEXITCODE -ne 0){throw 'Pending audio tests failed'}
