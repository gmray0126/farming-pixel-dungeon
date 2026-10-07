$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Windows.Forms
Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;
public static class DesktopInput {
    [StructLayout(LayoutKind.Sequential)] public struct Rect { public int Left, Top, Right, Bottom; }
    [StructLayout(LayoutKind.Sequential)] public struct Point { public int X, Y; }
    [DllImport("user32.dll")] public static extern bool GetClientRect(IntPtr w, out Rect rect);
    [DllImport("user32.dll")] public static extern bool ClientToScreen(IntPtr w, ref Point point);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr w);
    [DllImport("user32.dll")] public static extern bool SetCursorPos(int x, int y);
    [DllImport("user32.dll")] public static extern void mouse_event(uint flags, uint x, uint y, uint data, UIntPtr extra);
    [DllImport("user32.dll")] public static extern void keybd_event(byte key, byte scan, uint flags, UIntPtr extra);
}
'@
New-Item -ItemType Directory -Force pc-evidence | Out-Null
$env:GALLIUM_DRIVER = 'llvmpipe'
$env:LIBGL_ALWAYS_SOFTWARE = '1'
$exe = (Resolve-Path pc-output/FarmingPixelDungeon/FarmingPixelDungeon.exe).Path
$saveDir = Join-Path $env:USERPROFILE 'AppData/Roaming/.kwrousagi/파밍 픽셀 던전'
function Read-Bundle($path) {
    $bytes = [IO.File]::ReadAllBytes($path)
    if ($bytes[0] -eq 31 -and $bytes[1] -eq 139) {
        $stream = [IO.MemoryStream]::new($bytes, $false)
        $gzip = [IO.Compression.GZipStream]::new($stream, [IO.Compression.CompressionMode]::Decompress)
        $reader = [IO.StreamReader]::new($gzip)
        try { return ($reader.ReadToEnd() | ConvertFrom-Json) } finally { $reader.Dispose() }
    }
    return ([Text.Encoding]::UTF8.GetString($bytes) | ConvertFrom-Json)
}
function Write-Bundle($path, $bundle) {
    $json = $bundle | ConvertTo-Json -Depth 100 -Compress
    $bytes = [Text.Encoding]::UTF8.GetBytes($json)
    $stream = [IO.File]::Create($path)
    $gzip = [IO.Compression.GZipStream]::new($stream, [IO.Compression.CompressionMode]::Compress)
    try { $gzip.Write($bytes,0,$bytes.Length) } finally { $gzip.Dispose() }
}
function Click-Hero($p) {
    $p.Refresh()
    $rect = [DesktopInput+Rect]::new()
    [void][DesktopInput]::GetClientRect($p.MainWindowHandle, [ref]$rect)
    $origin = [DesktopInput+Point]::new()
    [void][DesktopInput]::ClientToScreen($p.MainWindowHandle, [ref]$origin)
    [void][DesktopInput]::SetForegroundWindow($p.MainWindowHandle)
    [void][DesktopInput]::SetCursorPos($origin.X + [int]($rect.Right / 2), $origin.Y + [int]($rect.Bottom / 2) + 12)
    Start-Sleep -Milliseconds 100
    [DesktopInput]::mouse_event(2,0,0,0,[UIntPtr]::Zero)
    Start-Sleep -Milliseconds 80
    [DesktopInput]::mouse_event(4,0,0,0,[UIntPtr]::Zero)
}
function Click-Client($p, [int]$x, [int]$y) {
    $p.Refresh()
    $origin = [DesktopInput+Point]::new()
    [void][DesktopInput]::ClientToScreen($p.MainWindowHandle, [ref]$origin)
    [void][DesktopInput]::SetForegroundWindow($p.MainWindowHandle)
    [void][DesktopInput]::SetCursorPos($origin.X+$x,$origin.Y+$y)
    Start-Sleep -Milliseconds 100
    [DesktopInput]::mouse_event(2,0,0,0,[UIntPtr]::Zero)
    Start-Sleep -Milliseconds 80
    [DesktopInput]::mouse_event(4,0,0,0,[UIntPtr]::Zero)
}
function Start-Game {
    $started = Start-Process $exe -WorkingDirectory (Split-Path $exe) -RedirectStandardOutput (Join-Path (Resolve-Path pc-evidence) 'stdout.txt') -RedirectStandardError (Join-Path (Resolve-Path pc-evidence) 'stderr.txt') -PassThru
    for ($i=0; $i -lt 20; $i++) {
        Start-Sleep -Seconds 1
        $p = Get-Process FarmingPixelDungeon -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowHandle -ne 0 } | Select-Object -First 1
        if ($p) { break }
    }
    if (!$p) {
        $started.Refresh()
        "Launcher exited: $($started.HasExited); exit code: $($started.ExitCode)" | Set-Content pc-evidence/launcher.txt
        Get-Content pc-evidence/stderr.txt -ErrorAction SilentlyContinue | Write-Output
        $bounds = [Windows.Forms.Screen]::PrimaryScreen.Bounds
        $image = [Drawing.Bitmap]::new($bounds.Width, $bounds.Height)
        $graphics = [Drawing.Graphics]::FromImage($image)
        try { $graphics.CopyFromScreen($bounds.X,$bounds.Y,0,0,$image.Size); $image.Save((Join-Path (Resolve-Path pc-evidence) 'failure.png')) } finally { $graphics.Dispose(); $image.Dispose() }
        throw 'Native Windows game window did not open'
    }
    Start-Sleep -Seconds 6
    if ($p.MainWindowTitle -match 'Crashed|Error') { Capture $p 'crash'; throw "Game crashed: $($p.MainWindowTitle)" }
    return $p
}
function Capture($p, $name) {
    $p.Refresh()
    $rect = [DesktopInput+Rect]::new()
    [void][DesktopInput]::GetClientRect($p.MainWindowHandle, [ref]$rect)
    $origin = [DesktopInput+Point]::new()
    [void][DesktopInput]::ClientToScreen($p.MainWindowHandle, [ref]$origin)
    $image = [Drawing.Bitmap]::new($rect.Right, $rect.Bottom)
    $graphics = [Drawing.Graphics]::FromImage($image)
    try {
        $graphics.CopyFromScreen($origin.X, $origin.Y, 0, 0, $image.Size)
        $image.Save((Join-Path (Resolve-Path pc-evidence) "$name.png"))
    } finally { $graphics.Dispose(); $image.Dispose() }
}
function Depart($p, [bool]$fresh = $false) {
    $p.Refresh()
    $rect = [DesktopInput+Rect]::new()
    [void][DesktopInput]::GetClientRect($p.MainWindowHandle, [ref]$rect)
    $origin = [DesktopInput+Point]::new()
    [void][DesktopInput]::ClientToScreen($p.MainWindowHandle, [ref]$origin)
    [void][DesktopInput]::SetForegroundWindow($p.MainWindowHandle)
    [void][DesktopInput]::SetCursorPos($origin.X + [int]($rect.Right / 2) - 35, $origin.Y + $rect.Bottom - 34)
    Start-Sleep -Milliseconds 100
    [DesktopInput]::mouse_event(2,0,0,0,[UIntPtr]::Zero)
    Start-Sleep -Milliseconds 80
    [DesktopInput]::mouse_event(4,0,0,0,[UIntPtr]::Zero)
    Start-Sleep -Seconds 8
    if ($fresh) {
        [DesktopInput]::keybd_event(13,0,0,[UIntPtr]::Zero)
        Start-Sleep -Milliseconds 80
        [DesktopInput]::keybd_event(13,0,2,[UIntPtr]::Zero)
        Start-Sleep -Seconds 4
    }
    $p.Refresh()
    if ($p.HasExited -or $p.MainWindowTitle -match 'Crashed|Error') { throw 'Windows raid failed' }
}
function Close-Game($p) {
    [void]$p.CloseMainWindow()
    if (!$p.WaitForExit(15000)) { $p.Kill(); throw 'Windows game did not close normally' }
    if ($null -ne $p.ExitCode -and $p.ExitCode -ne 0) { throw "Windows game exited with code $($p.ExitCode)" }
}
$p = $null
try {
    $p=Start-Game
    Depart $p $true
    Capture $p '01-new-raid'
    Close-Game $p
    $saved=Read-Bundle "$saveDir/game1/game.dat"
    if($saved.depth -ne 1){throw 'Fresh raid did not start at floor 1'}
    $saved.hero | Add-Member -Force NoteProperty pants @{__className='com.shatteredpixel.shatteredpixeldungeon.extraction.ExpeditionClothing$PlatePants';quantity=1;level=0;levelKnown=$true;cursedKnown=$true}
    $saved.hero | Add-Member -Force NoteProperty boots @{__className='com.shatteredpixel.shatteredpixeldungeon.extraction.ExpeditionClothing$PlateBoots';quantity=1;level=0;levelKnown=$true;cursedKnown=$true}
    Write-Bundle "$saveDir/game1/game.dat" $saved
    $p=Start-Game
    Depart $p
    Capture $p '02-resumed-raid'
    Close-Game $p
    $resumed=Read-Bundle "$saveDir/game1/game.dat"
    if($resumed.depth -ne 1){throw 'Resumed raid changed floors'}
    if($resumed.hero.pants.__className -notmatch 'PlatePants' -or $resumed.hero.boots.__className -notmatch 'PlateBoots'){throw 'Clothing slots did not survive resume'}
    $settingsPath=Join-Path $saveDir 'settings.xml'
    $settings=[IO.File]::ReadAllText($settingsPath)
    if($settings -match 'key="full_ui"'){ $compact=[regex]::Replace($settings,'(<entry key="full_ui"[^>]*>)\d+(</entry>)','${1}0${2}') }
    else { $compact=$settings.Replace('</properties>','<entry key="full_ui" type="Integer">0</entry></properties>') }
    [IO.File]::WriteAllText($settingsPath,$compact)
    $p=Start-Game
    Depart $p
    [DesktopInput]::keybd_event(73,0,0,[UIntPtr]::Zero)
    Start-Sleep -Milliseconds 100
    [DesktopInput]::keybd_event(73,0,2,[UIntPtr]::Zero)
    Start-Sleep -Seconds 1
    Capture $p '03-mobile-equipment'
    Close-Game $p
    [IO.File]::WriteAllText($settingsPath,$settings)
    $profilePath="$saveDir/extraction-profile.dat"
    $profile=Read-Bundle $profilePath
    $profile.active=$false; $profile.prepared=@(); $profile.escrow=@()
    Write-Bundle $profilePath $profile
    $p=Start-Game
    Click-Client $p 648 110
    Start-Sleep -Seconds 1
    Click-Client $p 654 218
    Start-Sleep -Seconds 1
    Capture $p '04-shoe-shop'
    Close-Game $p
    @{clothing_resume=$true;mobile_equipment_render=$true;shoe_shop_render=$true;windows_launch=$true;native_start=$true;native_resume=$true;test_graphics='Mesa llvmpipe'} | ConvertTo-Json | Set-Content pc-evidence/result.json
} finally {
    if ($p) { $p.Refresh(); if (!$p.HasExited) { $p.Kill() } }
}
