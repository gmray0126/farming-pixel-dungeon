"""One native growth-atlas check: render, zoom controls, drag, node details."""
import json
from pathlib import Path
import subprocess
import time

PACKAGE='com.kwrousagi.echoextraction.indev'
OUT=Path('android-atlas');OUT.mkdir(exist_ok=True)
def adb(*args,**kwargs):return subprocess.check_output(['adb',*args],timeout=45,**kwargs)
def tap(x,y):adb('shell','input','tap',str(x),str(y));time.sleep(1)
def shot(name):
    data=adb('exec-out','screencap','-p');(OUT/(name+'.png')).write_bytes(data);return data
try:
    adb('install','-r','apk/android-debug.apk')
    adb('shell','wm','size','720x1560');adb('shell','wm','density','160')
    adb('shell','run-as',PACKAGE,'mkdir','-p','shared_prefs')
    prefs=b'<map><boolean name="fullscreen" value="true"/><int name="scale" value="5"/><string name="language">ko</string></map>'
    adb('exec-in',f"run-as {PACKAGE} sh -c 'cat > shared_prefs/ShatteredPixelDungeon.xml'",input=prefs)
    adb('logcat','-c')
    adb('shell','am','start','-W','-n',PACKAGE+'/com.shatteredpixel.shatteredpixeldungeon.android.AndroidLauncher')
    time.sleep(8);tap(470,202);tap(360,243)
    overview=shot('01-growth-atlas-overview')
    tap(368,230);zoomed=shot('02-growth-atlas-zoom')
    assert overview!=zoomed,'Zoom control did not change rendered screen'
    adb('shell','input','swipe','360','900','460','1000','800');time.sleep(1)
    dragged=shot('03-growth-atlas-drag')
    assert zoomed!=dragged,'Drag did not change rendered screen'
    tap(368,230);tap(360,613);shot('04-growth-atlas-node-details')
    assert adb('shell','pidof',PACKAGE).strip(),'Game exited'
    log=adb('logcat','-d').decode(errors='replace')
    assert 'FATAL EXCEPTION' not in log,log
    (OUT/'result.json').write_text(json.dumps({'native_atlas':True,'zoom_control':True,'drag':True,'node_details_tap':True,'physical_pinch_tested':False},indent=2))
finally:
    try:(OUT/'logcat.txt').write_bytes(adb('logcat','-d'))
    except Exception:pass
