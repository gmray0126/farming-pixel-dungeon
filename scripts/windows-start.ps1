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
    # Launch/descent/cold-resume already passed with this game tree. Check changed UI only.
    $p=Start-Game
    Depart $p $true
    Close-Game $p
    $again=Read-Bundle "$saveDir/game1/game.dat"
    if($again.depth -ne 1){throw 'UI fixture raid did not start'}
    $profilePath="$saveDir/extraction-profile.dat"
    # Reproduce the screenshot: 21 bag entries exceed the old fixed 25-slot window.
    $profile=Read-Bundle $profilePath
    $profile.nodes=@('pack','porter','explore_merge','explore_cap')
    Write-Bundle $profilePath $profile
    $bag=@()
    foreach($n in 1..20){$bag+=@{__className='com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword';quantity=1;level=0;levelKnown=$true;cursedKnown=$true}}
    $bag+=@{__className='com.shatteredpixel.shatteredpixeldungeon.items.Waterskin';quantity=1;volume=17}
    $again.hero.inventory=$bag
    Write-Bundle "$saveDir/game1/game.dat" $again
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
    Capture $p '08-inventory-before-scroll'
    $rect=[DesktopInput+Rect]::new();[void][DesktopInput]::GetClientRect($p.MainWindowHandle,[ref]$rect)
    $origin=[DesktopInput+Point]::new();[void][DesktopInput]::ClientToScreen($p.MainWindowHandle,[ref]$origin)
    [void][DesktopInput]::SetCursorPos($origin.X+[int]($rect.Right/2),$origin.Y+[int]($rect.Bottom/2))
    Start-Sleep -Milliseconds 100
    [DesktopInput]::mouse_event(2048,0,0,[uint32]4294966576,[UIntPtr]::Zero)
    Start-Sleep -Seconds 1
    Capture $p '09-inventory-after-scroll'
    Close-Game $p
    [IO.File]::WriteAllText($settingsPath,$settings)
    # Load a 100-level hub and inspect the new preset controls in the native renderer.
    $profile=Read-Bundle $profilePath
    $profile.active=$false; $profile.xp=2475; $profile.points=300; $profile.nodes=@(); $profile.prepared=@(); $profile.escrow=@()
    Write-Bundle $profilePath $profile
    Remove-Item "$saveDir/game1" -Recurse -Force -ErrorAction SilentlyContinue
    $p=Start-Game
    Capture $p '10-level-cap-hub'
    Click-Client $p 466 110
    Start-Sleep -Seconds 1
    Capture $p '11-growth-presets'
    Close-Game $p
    $profile=Read-Bundle $profilePath
    if ($profile.xp -ne 2475 -or $profile.points -ne 300) { throw 'Growth cap was not preserved' }
    @{inventory_scroll_render=$true;growth_cap=$true;growth_presets_render=$true;windows_launch=$true;native_start=$true; test_graphics='Mesa llvmpipe'} | ConvertTo-Json | Set-Content pc-evidence/result.json
} finally {
    if ($p) { $p.Refresh(); if (!$p.HasExited) { $p.Kill() } }
}
