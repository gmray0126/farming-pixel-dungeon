package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.FileUtils;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class HeroSkinTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private byte[] original;
    private com.badlogic.gdx.graphics.GL20 previousGl;
    @Before public void setup() {
        previousGl = Gdx.gl;
        // Texture disposal calls GL even when a texture was never drawn. Headless
        // tests still exercise native PNG decoding and real file persistence.
        Gdx.gl = (com.badlogic.gdx.graphics.GL20)java.lang.reflect.Proxy.newProxyInstance(
                com.badlogic.gdx.graphics.GL20.class.getClassLoader(),new Class[]{com.badlogic.gdx.graphics.GL20.class},
                (proxy,method,args) -> {
                    if (method.getReturnType() == boolean.class) return false;
                    if (method.getReturnType() == int.class) return 0;
                    return null;
                });
        Gdx.files = new HeadlessFiles(); GdxNativesLoader.load(); HeroSkin.clearCache();
        SPDSettings.set(new HeadlessPreferences("skin-test.xml",folder.getRoot().getAbsolutePath()));
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        original = Gdx.files.internal(Assets.Sprites.HUNTRESS).readBytes();
    }
    @After public void cleanup() { try { HeroSkin.clearCache(); } finally { Gdx.gl = previousGl; } }
    @Test public void originalHuntressFitsAllAnimationFramesAndPreviewDoesNotApply() {
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) {
            assertFalse(HeroSkin.enabled());
            TextureFilm rows = new TextureFilm(preview.texture(),HeroSkin.WIDTH,HeroSkin.FRAME_HEIGHT);
            for (int row = 0; row < HeroSkin.ROWS; row++) {
                TextureFilm frames = new TextureFilm(rows,row,HeroSkin.FRAME_WIDTH,HeroSkin.FRAME_HEIGHT);
                for (int frame = 0; frame < 21; frame++) {
                    RectF rect = frames.get(frame); assertNotNull(rect);
                    assertTrue(rect.left >= 0 && rect.top >= 0 && rect.right <= 1 && rect.bottom <= 1);
                }
            }
        }
        assertFalse(HeroSkin.enabled()); assertEquals(0,FileUtils.getFileHandle("").list("png").length);
    }
    @Test public void confirmedSkinIsCopiedReloadedReplacedAndRestoredWithoutChangingSource() {
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) { HeroSkin.apply(preview); }
        assertTrue(HeroSkin.enabled());
        String first = SPDSettings.getString("hero_skin_png","");
        assertArrayEquals(original,FileUtils.getFileHandle(first).readBytes());
        Object loaded = HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR); assertTrue(loaded instanceof Pixmap);
        HeroSkin.clearCache(); SPDSettings.set(new HeadlessPreferences("skin-test.xml",folder.getRoot().getAbsolutePath()));
        assertTrue(HeroSkin.enabled()); assertTrue(HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR) instanceof Pixmap);
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) { HeroSkin.apply(preview); }
        assertFalse(FileUtils.getFileHandle(first).exists()); assertEquals(1,FileUtils.getFileHandle("").list("png").length);
        HeroSkin.restoreDefault(); assertFalse(HeroSkin.enabled());
        assertEquals(Assets.Sprites.WARRIOR,HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR));
        assertEquals(0,FileUtils.getFileHandle("").list("png").length);
        assertArrayEquals(original,Gdx.files.internal(Assets.Sprites.HUNTRESS).readBytes());
    }
    @Test public void invalidSizeDamagedOpaqueAndEmptyImagesKeepTheAppliedSkin() {
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) { HeroSkin.apply(preview); }
        String saved = SPDSettings.getString("hero_skin_png","");
        reject(new byte[64]);
        byte[] wrong = original.clone(); wrong[19] = 1; reject(wrong);
        reject(java.util.Arrays.copyOf(original,33));
        reject(new byte[PlatformSupport.PNG_FILE_LIMIT+1]);
        for (boolean opaque : new boolean[]{false,true}) {
            Pixmap pixels = new Pixmap(256,128,Pixmap.Format.RGBA8888);
            pixels.setColor(opaque ? 0xffffffff : 0); pixels.fill();
            com.badlogic.gdx.files.FileHandle file = FileUtils.getFileHandle("fixture.png");
            PixmapIO.writePNG(file,pixels); pixels.dispose(); reject(file.readBytes()); file.delete();
        }
        assertEquals(saved,SPDSettings.getString("hero_skin_png",""));
        assertArrayEquals(original,FileUtils.getFileHandle(saved).readBytes());
    }
    @Test public void failedImportPreservesPreviousFilePreferenceAndCachedAppearance() throws Exception {
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) { HeroSkin.apply(preview); }
        String saved = SPDSettings.getString("hero_skin_png","");
        Object before = HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR);
        java.io.File blocker = folder.newFile("blocker");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) {
            try { HeroSkin.apply(preview); fail("Failed copy must not replace the current skin"); }
            catch (IllegalStateException expected) {}
        }
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        assertEquals(saved,SPDSettings.getString("hero_skin_png","")); assertArrayEquals(original,FileUtils.getFileHandle(saved).readBytes());
        assertSame(before,HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR));
    }
    @Test public void missingDamagedAndUnsafeSavedPathsFallBackToTheOriginalAppearance() {
        try (HeroSkin.Candidate preview = HeroSkin.inspect(original)) { HeroSkin.apply(preview); }
        String saved = SPDSettings.getString("hero_skin_png",""); HeroSkin.clearCache();
        FileUtils.getFileHandle(saved).writeBytes(new byte[64],false);
        assertEquals(Assets.Sprites.WARRIOR,HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR));
        FileUtils.getFileHandle(saved).delete();
        assertEquals(Assets.Sprites.WARRIOR,HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR));
        SPDSettings.put("hero_skin_png","../sprites/huntress.png"); assertFalse(HeroSkin.enabled());
        assertEquals(Assets.Sprites.WARRIOR,HeroSkin.textureOrDefault(Assets.Sprites.WARRIOR));
    }
    @Test public void platformReadsDocumentsWithABoundedSize() throws Exception {
        assertArrayEquals(original,PlatformSupport.readPngStream(new ByteArrayInputStream(original)));
        try { PlatformSupport.readPngStream(new ByteArrayInputStream(new byte[PlatformSupport.PNG_FILE_LIMIT+1])); fail("Oversized document must be rejected"); }
        catch (IOException expected) {}
    }
    private void reject(byte[] bytes) {
        try (HeroSkin.Candidate ignored = HeroSkin.inspect(bytes)) { fail("Invalid sprite sheet should be rejected"); }
        catch (IllegalArgumentException expected) {}
    }
}
