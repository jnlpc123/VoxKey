param([Parameter(Mandatory=$true)][string]$Device,[string]$Adb='adb')
$ErrorActionPreference='Stop';$package='app.codex.f5voice.compat.com.android.integrated'
function Check {if($LASTEXITCODE -ne 0){throw 'ADB command failed'}}
& $Adb -s $Device install -r --streaming (Join-Path $PSScriptRoot 'VoxKey-1.1.0-preview.2-armv7.apk');Check
& $Adb -s $Device shell pm grant $package android.permission.RECORD_AUDIO;Check
$model=(& $Adb -s $Device shell getprop ro.product.model);Check
$brand=(& $Adb -s $Device shell getprop ro.product.brand);Check
$manufacturer=(& $Adb -s $Device shell getprop ro.product.manufacturer);Check
$sdkText=(& $Adb -s $Device shell getprop ro.build.version.sdk);Check
$sdkLevel=0
[void][int]::TryParse($sdkText.Trim(),[ref]$sdkLevel)
if($sdkLevel -ge 26 -and ($model.Trim() -eq 'DBF5' -or $brand -match 'dangbei|当贝' -or $manufacturer -match 'dangbei|当贝')){
 & $Adb -s $Device shell pm grant $package android.permission.WRITE_SECURE_SETTINGS;Check
}
& $Adb -s $Device shell am start -n "$package/.MainActivity";Check
Write-Output '请在设备上启用并选择新版输入法和语音键服务，再启用对应辅助／恢复功能。'
Write-Output '从旧版迁移请先阅读 README；本脚本不删除旧应用或数据。预览版需实机验收。'
