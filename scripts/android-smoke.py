"""Install and launch the actual APK in an isolated CI Android emulator."""
import gzip
import json
import os
from pathlib import Path
import subprocess
import time

PACKAGE = 'com.kwrousagi.echoextraction.indev'
OUT = Path('android-smoke')
OUT.mkdir(exist_ok=True)

def adb(*args, **kwargs):
    return subprocess.check_output(['adb', *args], **kwargs)

def screenshot(name):
    (OUT / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))

def profile():
    data = adb('exec-out', 'run-as', PACKAGE, 'cat', 'files/extraction-profile.dat')
    if data[:2] == b'\x1f\x8b':
        data = gzip.decompress(data)
    return json.loads(data)

def launch():
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/com.shatteredpixel.shatteredpixeldungeon.android.AndroidLauncher')
    time.sleep(8)
    adb('shell', 'input', 'tap', '360', '642')
    time.sleep(4)

try:
    adb('install', '-r', 'apk/android-debug.apk')
    adb('shell', 'wm', 'size', '720x1280')
    adb('shell', 'wm', 'density', '160')
    adb('shell', 'run-as', PACKAGE, 'mkdir', '-p', 'shared_prefs')
    prefs = b'<map><int name="version" value="921"/><boolean name="intro" value="false"/><boolean name="fullscreen" value="true"/><int name="scale" value="4"/><string name="language">ko</string></map>'
    adb('exec-out', f"run-as {PACKAGE} sh -c 'cat > shared_prefs/ShatteredPixelDungeon.xml'", input=prefs)
    adb('logcat', '-c')
    launch()
    screenshot('01-hub')
    initial = profile()
    assert not initial['active'] and len(initial['stash']) == 5, initial
    # Check the inventory window renders using actual touch input.
    adb('shell', 'input', 'tap', '360', '468')
    time.sleep(2)
    screenshot('02-stash')
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(2)
    # Seed a prepared loadout in the isolated test profile to exercise native
    # equipment initialization, independent of window positioning/font metrics.
    adb('shell', 'am', 'force-stop', PACKAGE)
    initial['prepared'] = initial['stash'][:2]
    initial['stash'] = initial['stash'][2:]
    data = gzip.compress(json.dumps(initial, ensure_ascii=False).encode())
    adb('exec-out', f"run-as {PACKAGE} sh -c 'cat > files/extraction-profile.dat'", input=data)
    launch()
    screenshot('03-prepared-hub')
    adb('shell', 'input', 'tap', '360', '368')
    time.sleep(15)
    screenshot('04-raid')
    state = profile()
    assert state['active'] and len(state['prepared']) == 0 and len(state['escrow']) == 2, state
    assert adb('shell', 'pidof', PACKAGE).strip(), 'Game process exited'
    # Pausing the real scene must persist a resumable native run.
    adb('shell', 'input', 'keyevent', '3')
    time.sleep(3)
    run = adb('exec-out', 'run-as', PACKAGE, 'cat', 'files/game1/game.dat')
    assert len(run) > 1000, 'Native run was not saved'
    raid_id = state['raid']
    adb('shell', 'am', 'force-stop', PACKAGE)
    launch()
    screenshot('05-resume-hub')
    adb('shell', 'input', 'tap', '360', '368')
    time.sleep(12)
    screenshot('06-resumed-raid')
    assert profile()['raid'] == raid_id, 'Resume created a duplicate raid'
    assert adb('shell', 'pidof', PACKAGE).strip(), 'Game process exited on resume'
    logs = adb('logcat', '-d').decode(errors='replace')
    assert 'FATAL EXCEPTION' not in logs, 'Android runtime crashed'
    (OUT / 'result.json').write_text(json.dumps({'installed': True, 'hub': True, 'raid_with_equipment': True, 'saved_run': True, 'resumed_same_raid': True, 'raid_id': raid_id}, indent=2))
finally:
    (OUT / 'logcat.txt').write_bytes(adb('logcat', '-d'))
