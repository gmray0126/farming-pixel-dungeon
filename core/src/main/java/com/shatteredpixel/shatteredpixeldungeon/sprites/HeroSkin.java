/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.gltextures.TextureCache;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PlatformSupport;
import java.util.Arrays;
import java.util.UUID;

/** Player-only cosmetics. Uses the upstream 21-frame, eight-row hero layout. */
public final class HeroSkin {
    public static final int WIDTH = 256, HEIGHT = 128, FRAME_WIDTH = 12, FRAME_HEIGHT = 15, ROWS = 8;
    private static final String SETTING = "hero_skin_png";
    private static String cachedName;
    private static Pixmap cachedPixels;

    public static final class Candidate implements AutoCloseable {
        private final byte[] bytes;
        private final Pixmap pixels;
        private boolean registered, closed;
        private Candidate(byte[] bytes, Pixmap pixels) { this.bytes = bytes; this.pixels = pixels; }
        public Object texture() {
            if (closed) throw new IllegalStateException("이미 닫힌 스킨입니다.");
            TextureCache.get(pixels); registered = true; return pixels;
        }
        @Override public void close() {
            if (closed) return;
            closed = true;
            if (registered) TextureCache.remove(pixels); else pixels.dispose();
        }
    }
    public static Candidate inspect(byte[] source) {
        if (source == null || source.length < 33 || source.length > PlatformSupport.PNG_FILE_LIMIT)
            throw new IllegalArgumentException("4 MB 이하의 PNG 파일을 선택해 주세요.");
        byte[] signature = {(byte)137,80,78,71,13,10,26,10};
        for (int i = 0; i < signature.length; i++) if (source[i] != signature[i])
            throw new IllegalArgumentException("PNG 이미지 파일만 등록할 수 있습니다.");
        if (readInt(source,8) != 13 || readInt(source,12) != 0x49484452 || readInt(source,16) != WIDTH || readInt(source,20) != HEIGHT)
            throw new IllegalArgumentException("스킨은 원본과 같은 256×128 PNG여야 합니다. 사냥꾼 시트의 프레임 위치를 유지해 주세요.");
        byte[] bytes = Arrays.copyOf(source, source.length);
        Pixmap pixels;
        try { pixels = new Pixmap(bytes,0,bytes.length); }
        catch (RuntimeException error) { throw new IllegalArgumentException("PNG가 손상되어 읽을 수 없습니다. 기존 스킨은 유지됩니다.",error); }
        boolean transparent = false, visible = false;
        for (int y = 0; y < HEIGHT; y++) for (int x = 0; x < WIDTH; x++) {
            int alpha = pixels.getPixel(x,y) & 255;
            transparent |= alpha < 255; visible |= alpha > 0;
        }
        if (!transparent || !visible) {
            pixels.dispose();
            throw new IllegalArgumentException("투명 배경과 캐릭터 그림이 있는 PNG를 선택해 주세요.");
        }
        return new Candidate(bytes,pixels);
    }
    private static int readInt(byte[] b, int p) { return (b[p]&255)<<24 | (b[p+1]&255)<<16 | (b[p+2]&255)<<8 | (b[p+3]&255); }
    private static String selectedName() {
        String name = SPDSettings.getString(SETTING, "");
        return name.matches("hero-skin-[0-9a-f-]{36}\\.png") ? name : "";
    }
    public static boolean enabled() { return !selectedName().isEmpty(); }
    public static Object textureOrDefault(Object original) {
        String name = selectedName();
        if (name.isEmpty()) return original;
        if (name.equals(cachedName) && cachedPixels != null && TextureCache.contains(cachedPixels)) return cachedPixels;
        clearCache(); cachedName = name;
        try {
            FileHandle file = FileUtils.getFileHandle(name);
            if (!file.exists() || file.length() > PlatformSupport.PNG_FILE_LIMIT) return original;
            Candidate loaded = inspect(file.readBytes());
            cachedPixels = loaded.pixels;
            // Ownership transfers to TextureCache, including context reload and disposal.
            TextureCache.get(cachedPixels); loaded.closed = true;
            return cachedPixels;
        } catch (RuntimeException error) { return original; }
    }
    public static void apply(Candidate candidate) {
        if (candidate.closed) throw new IllegalStateException("이미 닫힌 스킨입니다.");
        String old = selectedName(), next = "hero-skin-" + UUID.randomUUID() + ".png";
        FileHandle file = FileUtils.getFileHandle(next);
        try {
            file.writeBytes(candidate.bytes,false);
            SPDSettings.put(SETTING,next);
        } catch (RuntimeException error) {
            try { SPDSettings.put(SETTING,old); } catch (RuntimeException ignored) {}
            try { file.delete(); } catch (RuntimeException ignored) {}
            throw new IllegalStateException("스킨을 저장하지 못했습니다. 기존 스킨은 유지됩니다.",error);
        }
        clearCache();
        if (!old.isEmpty()) try { FileUtils.getFileHandle(old).delete(); } catch (RuntimeException ignored) {}
    }
    public static void restoreDefault() {
        String old = selectedName();
        try { SPDSettings.put(SETTING, ""); }
        catch (RuntimeException error) {
            try { SPDSettings.put(SETTING,old); } catch (RuntimeException ignored) {}
            throw error;
        }
        clearCache();
        if (!old.isEmpty()) try { FileUtils.getFileHandle(old).delete(); } catch (RuntimeException ignored) {}
    }
    public static void clearCache() {
        if (cachedPixels != null) TextureCache.remove(cachedPixels);
        cachedPixels = null; cachedName = null;
    }
    private HeroSkin() {}
}
