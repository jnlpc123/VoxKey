param(
 [Parameter(Mandatory=$true)][string]$Device,
 [string]$Adb='adb'
)
$ErrorActionPreference='Stop'
function Invoke-VoiceAdb { & $Adb -s $Device @args; if($LASTEXITCODE -ne 0){throw 'ADB failed'} }
Invoke-VoiceAdb shell ime set com.sohu.inputmethod.sogouoem/.SogouIME
$services=((Invoke-VoiceAdb shell settings get secure enabled_accessibility_services)-join '').Trim()
$kept=@($services.Split(':') | Where-Object { $_ -and $_ -notmatch '^app\.codex\.f5voice/' })
if($kept.Count -gt 0){Invoke-VoiceAdb shell settings put secure enabled_accessibility_services ($kept -join ':')}
else{Invoke-VoiceAdb shell settings delete secure enabled_accessibility_services}
$rows=Invoke-VoiceAdb shell content query --uri content://com.dangbei.dangbeiref.data/process_protect_list_info
foreach($row in $rows){if($row -match 'id=(\d+), appPackageName=app\.codex\.f5voice,'){
 $voiceRowId=$Matches[1]
 Invoke-VoiceAdb shell content delete --uri content://com.dangbei.dangbeiref.data/process_protect_list_info --where "id=$voiceRowId"
}}
Invoke-VoiceAdb shell service call systemutils_manager 24 i32 10005
Invoke-VoiceAdb uninstall app.codex.f5voice
Invoke-VoiceAdb shell settings get secure default_input_method
Invoke-VoiceAdb shell settings get secure enabled_accessibility_services

