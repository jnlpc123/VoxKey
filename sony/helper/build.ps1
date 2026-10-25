param([string]$SdkDirectory=$env:ANDROID_HOME,[string]$JdkDirectory=$env:JAVA_HOME)
$ErrorActionPreference='Stop';if(!$SdkDirectory -or !$JdkDirectory){throw 'Set ANDROID_HOME and JAVA_HOME or pass tool paths'}
$root=$PSScriptRoot;$android=Join-Path $SdkDirectory 'platforms\android-35\android.jar';$tools=Join-Path $SdkDirectory 'build-tools\35.0.0';$jna=Join-Path $root 'deps\jna.jar'
New-Item -ItemType Directory -Force "$root\build\classes","$root\build\dex" | Out-Null
function Check {if($LASTEXITCODE -ne 0){throw 'Helper build failed'}}
$sources=@(Get-ChildItem $root -Filter *.java | Select-Object -ExpandProperty FullName)
& "$JdkDirectory\bin\javac.exe" -encoding UTF-8 --release 8 -cp "$android;$jna" -d "$root\build\classes" @sources;Check
& "$JdkDirectory\bin\jar.exe" cf "$root\build\classes.jar" -C "$root\build\classes" .;Check
& "$JdkDirectory\bin\java.exe" -cp "$tools\lib\d8.jar" com.android.tools.r8.D8 --min-api 26 --lib $android --output "$root\build\dex" "$root\build\classes.jar" $jna;Check
& "$JdkDirectory\bin\jar.exe" cf "$root\build\sony-helper.jar" -C "$root\build\dex" classes.dex;Check
