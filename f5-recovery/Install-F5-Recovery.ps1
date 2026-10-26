param([Parameter(Mandatory=$true)][string]$Device,[string]$Adb='adb')
$ErrorActionPreference='Stop';$package='app.codex.f5voice.compat.com.android.recovery'
function Check {if($LASTEXITCODE -ne 0){throw 'ADB command failed'}}
& $Adb -s $Device install -r --streaming (Join-Path $PSScriptRoot 'VoxKey-F5-Recovery.apk');Check
& $Adb -s $Device shell pm grant $package android.permission.WRITE_SECURE_SETTINGS;Check
& $Adb -s $Device shell am start -n "$package/.MainActivity";Check
Write-Output '在投影仪界面选择“启用自动恢复”。'