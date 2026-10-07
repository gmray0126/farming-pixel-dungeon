"""Verify expedition guide pages, Android departure, and cold-start resume through touch input."""
import gzip
import json
from pathlib import Path
import subprocess
import time

PACKAGE = 'com.kwrousagi.echoextraction.indev'
OUT = Path('android-start')
OUT.mkdir(exist_ok=True)

def adb(*args, **kwargs):
    return subprocess.check_output(['adb', *args], timeout=45, **kwargs)

def launch():
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/com.shatteredpixel.shatteredpixeldungeon.android.AndroidLauncher')
    time.sleep(7)

def read_state(name):
    raw = adb('exec-out', 'run-as', PACKAGE, 'cat', 'files/' + name)
    if raw[:2] == b'\x1f\x8b':
        raw = gzip.decompress(raw)
    return json.loads(raw)

def screenshot(name):
    (OUT / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))

def check_crash():
    assert adb('shell', 'pidof', PACKAGE).strip(), 'Game process exited'
    logs = adb('logcat', '-d').decode(errors='replace')
    assert 'FATAL EXCEPTION' not in logs, 'Android exception occurred'
    assert 'fatal error occurred while moving between floors' not in logs, 'Floor transition failed'
    crash = subprocess.run(['adb', 'shell', 'run-as', PACKAGE, 'test', '-e', 'files/extraction-last-crash.txt'], timeout=45)
    assert crash.returncode != 0, 'Game recorded a handled crash'

try:
    adb('install', '-r', 'apk/android-debug.apk')
    adb('shell', 'wm', 'size', '720x1560')
    adb('shell', 'wm', 'density', '160')
    adb('shell', 'run-as', PACKAGE, 'mkdir', '-p', 'shared_prefs')
    prefs = b'<map><boolean name="fullscreen" value="true"/><int name="scale" value="5"/><string name="language">ko</string></map>'
    adb('exec-in', f"run-as {PACKAGE} sh -c 'cat > shared_prefs/ShatteredPixelDungeon.xml'", input=prefs)
    adb('logcat', '-c')
    launch()
    # Dismiss Android's first-use immersive-mode hint before testing Back.
    adb('shell', 'input', 'tap', '600', '190')
    time.sleep(1)
    screenshot('01-hub')
    assert not read_state('extraction-profile.dat')['active']
    # Each selection closes WndOptions before creating the next window. Exercise
    # all three callbacks on the native scene to catch detached-window access.
    # Native contract board and guide dialogs share the updated five-tab hub.
    adb('shell', 'input', 'tap', '628', '278')
    time.sleep(1)
    adb('shell', 'input', 'tap', '360', '615')
    time.sleep(1)
    check_crash()
    screenshot('05-contract-board')
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(1)
    adb('shell', 'input', 'tap', '360', '278')
    time.sleep(1)
    guide_images = []
    for index, button_y in enumerate((740, 840, 940)):
        adb('shell', 'input', 'tap', '360', '1083')
        time.sleep(1)
        screenshot('guide-menu-' + str(index))
        adb('shell', 'input', 'tap', '360', str(button_y))
        time.sleep(1)
        check_crash()
        screenshot('guide-page-' + str(index))
        page = (OUT / ('guide-page-' + str(index) + '.png')).read_bytes()
        menu = (OUT / ('guide-menu-' + str(index) + '.png')).read_bytes()
        assert page != menu, 'Guide selection did not open a page'
        guide_images.append(page)
        adb('shell', 'input', 'keyevent', '4')
        time.sleep(1)
    assert len(set(guide_images)) == 3, 'Guide checks stayed on the same page'
    adb('shell', 'input', 'tap', '110', '278')
    time.sleep(1)
    adb('shell', 'input', 'tap', '250', '1475')
    time.sleep(12)
    check_crash()
    screenshot('02-started-raid')
    adb('shell', 'input', 'keyevent', '3')
    time.sleep(3)
    run = read_state('game1/game.dat')
    assert run['depth'] == 1 and run['hero']['extraction_raid'] > 0, 'Sewer raid did not start'
    raid_id = run['hero']['extraction_raid']
    adb('shell', 'am', 'force-stop', PACKAGE)
    time.sleep(2)
    launch()
    screenshot('03-resume-hub')
    adb('shell', 'input', 'tap', '250', '1475')
    time.sleep(12)
    check_crash()
    screenshot('04-resumed-raid')
    adb('shell', 'input', 'keyevent', '3')
    time.sleep(3)
    restored = read_state('game1/game.dat')
    assert restored['depth'] == 1 and restored['hero']['extraction_raid'] == raid_id, 'Resume changed the raid'
    (OUT / 'result.json').write_text(json.dumps({'guide_pages_checked': 3, 'native_start': True, 'native_cold_resume': True, 'raid_id': raid_id}, indent=2))
finally:
    (OUT / 'logcat.txt').write_bytes(adb('logcat', '-d'))
