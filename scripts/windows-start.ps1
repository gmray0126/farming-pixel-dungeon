$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
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
function Start-Game {
    $p = Start-Process $exe -WorkingDirectory (Split-Path $exe) -PassThru
    Start-Sleep -Seconds 10
    $p.Refresh()
    if ($p.HasExited -or $p.MainWindowHandle -eq 0) { throw 'Native Windows game window did not open' }
    if ($p.MainWindowTitle -match 'Crashed|Error') { throw "Game crashed: $($p.MainWindowTitle)" }
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
function Depart($p) {
    $p.Refresh()
    $rect = [DesktopInput+Rect]::new()
    [void][DesktopInput]::GetClientRect($p.MainWindowHandle, [ref]$rect)
    $origin = [DesktopInput+Point]::new()
    [void][DesktopInput]::ClientToScreen($p.MainWindowHandle, [ref]$origin)
    [void][DesktopInput]::SetForegroundWindow($p.MainWindowHandle)
    [void][DesktopInput]::SetCursorPos($origin.X + [int]($rect.Right / 2) - 35, $origin.Y + $rect.Bottom - 34)
    [DesktopInput]::mouse_event(2,0,0,0,[UIntPtr]::Zero)
    [DesktopInput]::mouse_event(4,0,0,0,[UIntPtr]::Zero)
    Start-Sleep -Seconds 8
    $p.Refresh()
    if ($p.HasExited -or $p.MainWindowTitle -match 'Crashed|Error') { throw 'Windows raid failed' }
}
function Close-Game($p) {
    [void]$p.CloseMainWindow()
    if (!$p.WaitForExit(15000)) { $p.Kill(); throw 'Windows game did not close normally' }
    if ($p.ExitCode -ne 0) { throw "Windows game exited with code $($p.ExitCode)" }
}
$p = $null
try {
    $p = Start-Game
    Capture $p '01-hub'
    if (!(Test-Path "$saveDir/extraction-profile.dat")) { throw 'Hub profile was not created' }
    Depart $p
    Capture $p '02-raid'
    Close-Game $p
    $run = Read-Bundle "$saveDir/game1/game.dat"
    if ($run.depth -ne 1 -or $run.hero.extraction_raid -le 0) { throw 'Sewer raid did not save' }
    $raid = $run.hero.extraction_raid
    $p = Start-Game
    Capture $p '03-resume-hub'
    Depart $p
    Capture $p '04-resumed-raid'
    Close-Game $p
    $resumed = Read-Bundle "$saveDir/game1/game.dat"
    if ($resumed.depth -ne 1 -or $resumed.hero.extraction_raid -ne $raid) { throw 'Cold resume changed the raid' }
    @{windows_launch=$true; native_start=$true; cold_resume=$true; raid_id=$raid; test_graphics='Mesa llvmpipe'} | ConvertTo-Json | Set-Content pc-evidence/result.json
} finally {
    if ($p) { $p.Refresh(); if (!$p.HasExited) { $p.Kill() } }
}
