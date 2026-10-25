param([string]$ArchivePath)
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
$expectedArchive='73b6b8a625f426132e52592d3247d315723dc0511af49995e7a93d7e90ec396d'
$modelHash='d94222f3a655fe6c0690d31d261a3e1503aa56aa10d1aebe116f7b15ebec9358'
$tokensHash='f449eb28dc567533d7fa59be34e2abca8784f771850c78a47fb731a31429a1dc'
$dest=Join-Path $project 'assets-fast\model'
if((Test-Path "$dest\model.int8.ort") -and (Test-Path "$dest\tokens.txt")){
 if((Get-FileHash "$dest\model.int8.ort" -Algorithm SHA256).Hash -eq $modelHash -and (Get-FileHash "$dest\tokens.txt" -Algorithm SHA256).Hash -eq $tokensHash){Write-Output 'Verified model already available';exit 0}
}
if(!$ArchivePath){
 $cache=Join-Path $project '.download-cache';New-Item -ItemType Directory -Force $cache | Out-Null
 $ArchivePath=Join-Path $cache 'VoxKey-SenseVoice-int8-ort.zip'
 if(!(Test-Path -LiteralPath $ArchivePath)) {Invoke-WebRequest 'https://github.com/jnlpc123/VoxKey/releases/download/v1.0.0/VoxKey-SenseVoice-int8-ort.zip' -OutFile $ArchivePath}
}
if((Get-FileHash -LiteralPath $ArchivePath -Algorithm SHA256).Hash -ne $expectedArchive){throw 'Model archive SHA256 mismatch'}
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $ArchivePath))
try {
 New-Item -ItemType Directory -Force $dest | Out-Null
 foreach($pair in @(@('model.int8.ort',$modelHash),@('tokens.txt',$tokensHash))){
  $entry=$zip.GetEntry($pair[0]);if(!$entry){throw 'Model asset missing from release archive'}
  $target=Join-Path $dest $pair[0];$temp=$target+'.partial'
  try {
   $input=$entry.Open();$output=[IO.File]::Create($temp)
   try{$input.CopyTo($output)}finally{$output.Dispose();$input.Dispose()}
   if((Get-FileHash -LiteralPath $temp -Algorithm SHA256).Hash -ne $pair[1]){throw 'Model asset SHA256 mismatch'}
   Move-Item -LiteralPath $temp -Destination $target -Force
  }finally{if(Test-Path -LiteralPath $temp){Remove-Item -LiteralPath $temp}}
 }
}finally{$zip.Dispose()}
Write-Output 'SenseVoice model and tokens verified'

