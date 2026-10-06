"""Exercise the actual libGDX touch UI, persistent loadout and permanent growth."""
import gzip
import json
from pathlib import Path
import subprocess
import sys
import time

MODE = sys.argv[1] if len(sys.argv) > 1 else 'phone'
PACKAGE = 'com.kwrousagi.echoextraction.indev'
OUT = Path('android-smoke')
OUT.mkdir(exist_ok=True)

def adb(*args, **kwargs):
    return subprocess.check_output(['adb', *args], timeout=60, **kwargs)

def screenshot(name):
    (OUT / (name + '.png')).write_bytes(adb('exec-out', 'screencap', '-p'))

def profile():
    data = adb('exec-out', 'run-as', PACKAGE, 'cat', 'files/extraction-profile.dat')
    if data[:2] == b'\x1f\x8b': data = gzip.decompress(data)
    return json.loads(data)

def tap(x, y, delay=1):
    adb('shell', 'input', 'tap', str(x), str(y))
    time.sleep(delay)

def launch():
    adb('shell', 'am', 'start', '-W', '-n', PACKAGE + '/com.shatteredpixel.shatteredpixeldungeon.android.AndroidLauncher')
    time.sleep(8)

def screen(width, height, scale):
    adb('shell', 'am', 'force-stop', PACKAGE)
    time.sleep(3)
    adb('shell', 'wm', 'size', f'{width}x{height}')
    adb('shell', 'wm', 'density', '160')
    adb('shell', 'run-as', PACKAGE, 'mkdir', '-p', 'shared_prefs')
    # Do not suppress an intro or seed a version: first-run class selection must really be gone.
    prefs = f'<map><boolean name="fullscreen" value="true"/><int name="scale" value="{scale}"/><string name="language">ko</string></map>'.encode()
    adb('exec-in', f"run-as {PACKAGE} sh -c 'cat > shared_prefs/ShatteredPixelDungeon.xml'", input=prefs)
    launch()

try:
    adb('install', '-r', 'apk/android-debug.apk')
    adb('logcat', '-c')
    if MODE == 'minimum':
        screen(540, 900, 4)
        tap(470, 202)
        screenshot('01-minimum-inventory')
        initial = profile()
        assert not initial['active'] and len(initial['stash']) == 5, initial
        tap(270, 194)
        tap(270, 194)
        screenshot('02-minimum-all-nodes')
        assert adb('shell', 'pidof', PACKAGE).strip(), 'Game exited on minimum layout'
        assert 'FATAL EXCEPTION' not in adb('logcat', '-d').decode(errors='replace')
        (OUT / 'result.json').write_text(json.dumps({'minimum_layout': '135x225', 'first_run_hub': True}, indent=2))
        sys.exit(0)
    # Each geometry is tested on a separate device: never resize a live graphics context.
    screen(720, 1560, 5)
    tap(470, 202)
    screenshot('03-phone-inventory')
    # Tap stock sword, then armor. No option dialog or injected equipment.
    tap(109, 430)
    assert len(profile()['prepared']) == 1, 'Sword not moved into loadout'
    tap(109, 430)
    assert len(profile()['prepared']) == 2, 'Armor not moved into loadout'
    screenshot('04-equipped-loadout')
    # Put sword back into the stash, then take it again from fourth stash slot.
    tap(109, 805)
    assert len(profile()['prepared']) == 1 and len(profile()['stash']) == 4, 'Return to stash failed'
    tap(611, 430)
    assert len(profile()['prepared']) == 2 and len(profile()['stash']) == 3, 'Take sword back failed'
    screenshot('05-reversible-inventory')
    # Buy supply through the native button.
    tap(550, 325)
    assert profile()['gold'] == 70 and len(profile()['stash']) == 4, 'Supply purchase failed'
    # All nine nodes are visible in one tab, with prerequisite connectors.
    tap(360, 243)
    screenshot('06-growth-tree')
    tap(133, 590)
    screenshot('07-node-details')
    tap(360, 835)
    assert 'power' in profile()['nodes'] and profile()['points'] == 2, 'Root node learning failed'
    screenshot('08-learned-node')
    # The serialized profile must contain the selected equipment and learned node.
    tap(133, 243)
    assert len(profile()['prepared']) == 2 and 'power' in profile()['nodes'], 'Hub persistence failed'
    screenshot('09-saved-loadout')
    tap(360, 1475, 15)
    screenshot('10-raid')
    state = profile()
    assert state['active'] and not state['prepared'] and len(state['escrow']) == 2, state
    assert adb('shell', 'pidof', PACKAGE).strip(), 'Game exited'
    # Hero portrait now opens expedition statistics and the same permanent tree.
    tap(80, 80)
    screenshot('11-expedition-info')
    adb('shell', 'input', 'keyevent', '4')
    time.sleep(1)
    adb('shell', 'input', 'keyevent', '3')
    time.sleep(3)
    (OUT / 'logcat-before-restart.txt').write_bytes(adb('logcat', '-d'))
    run = adb('exec-out', 'run-as', PACKAGE, 'cat', 'files/game1/game.dat')
    assert len(run) > 1000, 'Native run was not saved'
    raid_id = state['raid']
    adb('shell', 'am', 'force-stop', PACKAGE)
    time.sleep(3)  # Let the test device retire its old EGL surface.
    launch()
    screenshot('12-resume-hub')
    tap(360, 1475, 12)
    screenshot('13-resumed-raid')
    assert profile()['raid'] == raid_id, 'Resume duplicated raid'
    assert adb('shell', 'pidof', PACKAGE).strip(), 'Game exited on resume'
    logs = adb('logcat', '-d').decode(errors='replace')
    assert 'FATAL EXCEPTION' not in logs, 'Android runtime crashed'
    (OUT / 'result.json').write_text(json.dumps({'installed': True, 'phone_layout': '144x312', 'inventory_round_trip': True, 'supply_purchase': True, 'node_learned': True, 'hub_saved': True, 'raid_with_equipment': True, 'saved_run': True, 'resumed_same_raid': True, 'raid_id': raid_id}, indent=2))
except Exception as error:
    (OUT / 'error.txt').write_text(str(error))
    try: screenshot('failure')
    except Exception: pass
    raise
finally:
    try: (OUT / 'logcat.txt').write_bytes(adb('logcat', '-d'))
    except Exception as error: (OUT / 'logcat-error.txt').write_text(str(error))
