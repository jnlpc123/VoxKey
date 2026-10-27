param([string]$SdkDirectory=$env:ANDROID_HOME,[string]$JdkDirectory=$env:JAVA_HOME,[string]$SigningKeyPath)
$ErrorActionPreference='Stop'
if(!$SdkDirectory -or !$JdkDirectory){throw 'Set SDK and JDK paths'}
$root=$PSScriptRoot;$build=Join-Path $root 'build';$tools=Join-Path $SdkDirectory 'build-tools/35.0.0';$android=Join-Path $SdkDirectory 'platforms/android-35/android.jar'
New-Item -ItemType Directory -Force "$build/classes","$build/dex" | Out-Null
function Check {if($LASTEXITCODE -ne 0){throw 'Migration bridge build failed'}}
& "$tools/aapt.exe" package -f -M "$root/AndroidManifest.xml" -I $android -F "$build/resources.apk";Check
$sources=@(Get-ChildItem "$root/src" -Filter *.java -Recurse | Select-Object -ExpandProperty FullName)
& "$JdkDirectory/bin/javac.exe" -encoding UTF-8 --release 8 -cp $android -d "$build/classes" @sources;Check
& "$JdkDirectory/bin/jar.exe" cf "$build/classes.jar" -C "$build/classes" .;Check
& "$JdkDirectory/bin/java.exe" -cp "$tools/lib/d8.jar" com.android.tools.r8.D8 --min-api 24 --lib $android --output "$build/dex" "$build/classes.jar";Check
Copy-Item "$build/resources.apk" "$build/unsigned.apk" -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::Open("$build/unsigned.apk",[IO.Compression.ZipArchiveMode]::Update)
try{[IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,"$build/dex/classes.dex",'classes.dex')|Out-Null}finally{$zip.Dispose()}
& "$tools/zipalign.exe" -f 4 "$build/unsigned.apk" "$build/aligned.apk";Check
if(!$SigningKeyPath){$SigningKeyPath=Join-Path $root 'local-signing.jks'}
if(!(Test-Path -LiteralPath $SigningKeyPath)){& "$JdkDirectory/bin/keytool.exe" -genkeypair -keystore $SigningKeyPath -storepass local-build -keypass local-build -alias f5voice -keyalg RSA -keysize 2048 -validity 10000 -dname 'CN=VoxKey Local Build';Check}
& "$JdkDirectory/bin/java.exe" -jar "$tools/lib/apksigner.jar" sign --ks $SigningKeyPath --ks-pass pass:local-build --key-pass pass:local-build --out "$build/VoxKey-Legacy-Migration.apk" "$build/aligned.apk";Check
& "$JdkDirectory/bin/java.exe" -jar "$tools/lib/apksigner.jar" verify --verbose "$build/VoxKey-Legacy-Migration.apk";Check
