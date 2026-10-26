param([string]$SdkDirectory=$env:ANDROID_HOME,[string]$JdkDirectory=$env:JAVA_HOME,[string]$SigningKeyPath)
$ErrorActionPreference='Stop'
if(!$SdkDirectory -or !$JdkDirectory){throw 'Set ANDROID_HOME/JAVA_HOME or pass SDK/JDK paths'}
$root=$PSScriptRoot;$out=Join-Path $root 'build';$tools=Join-Path $SdkDirectory 'build-tools/35.0.0';$android=Join-Path $SdkDirectory 'platforms/android-35/android.jar'
New-Item -ItemType Directory -Force "$out/classes","$out/dex" | Out-Null
function Check {if($LASTEXITCODE -ne 0){throw "Build failed: $LASTEXITCODE"}}
$sources=@(Get-ChildItem "$root/src" -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
& "$tools/aapt.exe" package -f -M "$root/AndroidManifest.xml" -S "$root/res" -I $android -F "$out/resources.apk";Check
& "$JdkDirectory/bin/javac.exe" -encoding UTF-8 --release 8 -cp $android -d "$out/classes" @sources;Check
& "$JdkDirectory/bin/jar.exe" cf "$out/classes.jar" -C "$out/classes" .;Check
& "$JdkDirectory/bin/java.exe" -cp "$tools/lib/d8.jar" com.android.tools.r8.D8 --min-api 26 --lib $android --output "$out/dex" "$out/classes.jar";Check
Copy-Item "$out/resources.apk" "$out/unsigned.apk" -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::Open("$out/unsigned.apk",[IO.Compression.ZipArchiveMode]::Update)
try{[IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,"$out/dex/classes.dex",'classes.dex') | Out-Null}finally{$zip.Dispose()}
& "$tools/zipalign.exe" -f 4 "$out/unsigned.apk" "$out/aligned.apk";Check
if(!$SigningKeyPath){$SigningKeyPath=Join-Path $root 'local-signing.jks'}
if(!(Test-Path -LiteralPath $SigningKeyPath)){& "$JdkDirectory/bin/keytool.exe" -genkeypair -keystore $SigningKeyPath -storepass local-build -keypass local-build -alias f5voice -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=VoxKey Recovery Local';Check}
& "$JdkDirectory/bin/java.exe" -jar "$tools/lib/apksigner.jar" sign --ks $SigningKeyPath --ks-pass pass:local-build --key-pass pass:local-build --out "$out/VoxKey-F5-Recovery.apk" "$out/aligned.apk";Check
& "$JdkDirectory/bin/java.exe" -jar "$tools/lib/apksigner.jar" verify --verbose "$out/VoxKey-F5-Recovery.apk";Check
& "$tools/aapt.exe" dump permissions "$out/VoxKey-F5-Recovery.apk";Check