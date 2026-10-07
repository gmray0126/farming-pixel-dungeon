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
    [DesktopInput]::mouse_event(2,0,0,0,[UIntPtr]::Zero)
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
    [DesktopInput]::mouse_event(2,0,0,0,[UIntPtr]::Zero)
    [DesktopInput]::mouse_event(4,0,0,0,[UIntPtr]::Zero)
    Start-Sleep -Seconds 8
    if ($fresh) {
        [DesktopInput]::keybd_event(13,0,0,[UIntPtr]::Zero)
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
    $p = Start-Game
    Capture $p '01-hub'
    if (!(Test-Path "$saveDir/extraction-profile.dat")) { throw 'Hub profile was not created' }
    Depart $p $true
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
    # Place the test hero on the native stairs, then descend through the real UI.
    # Enable the exact floor trait from the crash report without test hooks in the game.
    $profilePath = "$saveDir/extraction-profile.dat"
    $profile = Read-Bundle $profilePath
    $profile.nodes = @($profile.nodes) + @('scout_4','scout_5','scout_6')
    Write-Bundle $profilePath $profile
    $floor = Read-Bundle "$saveDir/game1/depth1.dat"
    $exit = $floor.level.transitions | Where-Object { $_.type -eq 'REGULAR_EXIT' } | Select-Object -First 1
    if (!$exit) { throw 'Native sewer stairs missing' }
    $resumed.hero.pos = $exit.center
    $resumed.seed = 2467327059549L
    Write-Bundle "$saveDir/game1/game.dat" $resumed
    $p = Start-Game
    Depart $p
    Capture $p '05-stairs-with-scouting'
    Click-Hero $p
    Start-Sleep -Seconds 10
    $p.Refresh()
    if ($p.HasExited -or $p.MainWindowTitle -match 'Crashed|Error') { Get-Content pc-evidence/stderr.txt -ErrorAction SilentlyContinue | Write-Output; throw 'Scouting floor descent crashed' }
    Capture $p '06-second-floor'
    Close-Game $p
    $second = Read-Bundle "$saveDir/game1/game.dat"
    if ($second.depth -ne 2 -or $second.hero.extraction_raid -ne $raid -or ($second.hero.extraction_visited -band 4) -eq 0) { throw 'Scouting floor descent did not save floor 2' }
    $foresight = @($second.hero.buffs | Where-Object { $_.__className -like '*.Foresight' })
    if ($foresight.Count -eq 0) { throw 'Floor scouting effect was lost' }
    $p = Start-Game
    Depart $p
    Capture $p '07-second-floor-resumed'
    Close-Game $p
    $again = Read-Bundle "$saveDir/game1/game.dat"
    if ($again.depth -ne 2 -or $again.hero.extraction_raid -ne $raid -or $again.hero.extraction_visited -ne $second.hero.extraction_visited) { throw 'Second floor resume lost its saved traits' }
    @{windows_launch=$true; native_start=$true; cold_resume=$true; scouting_descent=$true; second_floor_resume=$true; depth=2; raid_id=$raid; seed=2467327059549L; test_graphics='Mesa llvmpipe'} | ConvertTo-Json | Set-Content pc-evidence/result.json
} finally {
    if ($p) { $p.Refresh(); if (!$p.HasExited) { $p.Kill() } }
}
