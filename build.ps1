param([string]$AssetDirectory='assets-fast',[string]$SdkDirectory=$env:ANDROID_HOME,[string]$JdkDirectory=$env:JAVA_HOME)
$ErrorActionPreference = 'Stop'
$projectDir = $PSScriptRoot
if(!$SdkDirectory -or !$JdkDirectory){throw 'Set ANDROID_HOME and JAVA_HOME or pass -SdkDirectory and -JdkDirectory'}
$sdkDir=$SdkDirectory; $jdkDir=$JdkDirectory
$buildToolsDir = Join-Path $sdkDir 'build-tools\35.0.0'
$androidJar = Join-Path $sdkDir 'platforms\android-35\android.jar'
$buildDir = Join-Path $projectDir 'build'
# Clear only compiler output inside this project so deleted Java classes cannot leak into releases.
foreach($name in @('classes','dex')){$target=[IO.Path]::GetFullPath((Join-Path $buildDir $name));if(!$target.StartsWith([IO.Path]::GetFullPath($projectDir)+[IO.Path]::DirectorySeparatorChar)){throw 'Unsafe build path'};if(Test-Path -LiteralPath $target){Remove-Item -LiteralPath $target -Recurse -Force}}
New-Item -ItemType Directory -Path "$buildDir\classes","$buildDir\dex" -Force | Out-Null
function Check-Native { if ($LASTEXITCODE -ne 0) { throw "Build command failed: $LASTEXITCODE" } }
& "$buildToolsDir\aapt.exe" package -f -0 ort -0 onnx -M "$projectDir\AndroidManifest.xml" -S "$projectDir\res" -A "$projectDir\$AssetDirectory" -I $androidJar -F "$buildDir\resources.apk"
Check-Native
$sources = @(Get-ChildItem -LiteralPath "$projectDir\src" -Filter '*.java' -Recurse | Select-Object -ExpandProperty FullName)
$voiceJar = "$projectDir\deps\aar\classes.jar"
$kotlinJar = "$projectDir\deps\kotlin-stdlib-1.9.24.jar"
$okhttpJar = "$projectDir\deps\okhttp-3.14.9.jar"
$zxingJar = "$projectDir\deps\zxing-core-3.5.4.jar"
$okioJar = "$projectDir\deps\okio-1.17.2.jar"
& "$jdkDir\bin\javac.exe" -encoding UTF-8 --release 8 -classpath "$androidJar;$voiceJar;$kotlinJar;$okhttpJar;$okioJar;$zxingJar" -d "$buildDir\classes" @sources
Check-Native
& "$jdkDir\bin\jar.exe" cf "$buildDir\classes.jar" -C "$buildDir\classes" .
Check-Native
& "$jdkDir\bin\java.exe" -cp "$buildToolsDir\lib\d8.jar" com.android.tools.r8.D8 --min-api 24 --lib $androidJar --output "$buildDir\dex" "$buildDir\classes.jar" $voiceJar $kotlinJar $okhttpJar $okioJar $zxingJar
Check-Native
Copy-Item -LiteralPath "$buildDir\resources.apk" -Destination "$buildDir\unsigned.apk" -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$apkZip = [IO.Compression.ZipFile]::Open("$buildDir\unsigned.apk", [IO.Compression.ZipArchiveMode]::Update)
try {
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($apkZip,"$buildDir\dex\classes.dex",'classes.dex') | Out-Null
  foreach($nativeName in @('libonnxruntime.so','libsherpa-onnx-jni.so')) {
    $nativeFile=Get-Item -LiteralPath "$projectDir\deps\aar\jni\armeabi-v7a\$nativeName"
    [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($apkZip,$nativeFile.FullName,"lib/armeabi-v7a/$nativeName") | Out-Null
  }
} finally { $apkZip.Dispose() }
& "$buildToolsDir\zipalign.exe" -f 4 "$buildDir\unsigned.apk" "$buildDir\aligned.apk"
Check-Native
$keystorePath = "$projectDir\local-signing.jks"
if (!(Test-Path -LiteralPath $keystorePath)) {
  & "$jdkDir\bin\keytool.exe" -genkeypair -keystore $keystorePath -storepass local-build -keypass local-build -alias f5voice -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=F5Voice Local Build'
  Check-Native
}
& "$jdkDir\bin\java.exe" -jar "$buildToolsDir\lib\apksigner.jar" sign --ks $keystorePath --ks-pass pass:local-build --key-pass pass:local-build --out "$buildDir\F5Voice.apk" "$buildDir\aligned.apk"
Check-Native
& "$jdkDir\bin\java.exe" -jar "$buildToolsDir\lib\apksigner.jar" verify --verbose "$buildDir\F5Voice.apk"
Check-Native
& "$buildToolsDir\aapt.exe" dump permissions "$buildDir\F5Voice.apk"
Check-Native



