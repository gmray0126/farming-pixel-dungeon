package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.watabou.utils.FileUtils;
import com.watabou.noosa.Game;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.lang.reflect.Field;
import static org.junit.Assert.*;

public class ExtractionProfileTest {
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private ExtractionProfile profile;
    private void forgetProfile() throws Exception {
        Field field = ExtractionProfile.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }
    @Before public void setUp() throws Exception {
        Gdx.files = new HeadlessFiles();
        GdxNativesLoader.load();
        Game.version = "0.1.0-extraction-INDEV";
        Game.versionCode = 921;
        SPDSettings.set(new HeadlessPreferences("test-preferences.xml", folder.getRoot().getAbsolutePath()));
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute, folder.getRoot().getAbsolutePath()+"/");
        Dungeon.hero = null;
        GamesInProgress.curSlot = 1;
        forgetProfile();
        profile = ExtractionProfile.get();
    }
    @Test public void prerequisitesAndGrowthSurviveReload() throws Exception {
        try { profile.learn(1); fail("A child node needs its parent"); }
        catch (IllegalStateException expected) { }
        assertEquals(3, profile.points);
        profile.learn(0);
        profile.learn(1);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(2, profile.attackBonus());
        assertEquals(1, profile.points);
    }
    @Test public void deathLosesOnlyRaidEquipmentAndKeepsGrowth() throws Exception {
        Item equipment = profile.stash.get(0);
        profile.prepare(equipment, true);
        profile.learn(3);
        profile.begin();
        int id = profile.raidID;
        profile.credit(id, 30);
        profile.settle(id, false);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(4, profile.stash.size());
        assertTrue(profile.prepared.isEmpty());
        assertTrue(profile.nodes.contains("vital"));
        assertEquals(30, profile.xp);
        assertFalse(profile.active);
    }
    @Test public void oldRunSavesCannotDuplicateExperience() throws Exception {
        profile.begin();
        int id = profile.raidID;
        profile.credit(id, 30);
        forgetProfile();
        profile = ExtractionProfile.get();
        profile.credit(id, 20);
        profile.credit(id, 30);
        assertEquals(30, profile.xp);
        profile.credit(id, 50);
        assertEquals(50, profile.xp);
        assertEquals(5, profile.points);
        profile.credit(id+1, 1000);
        assertEquals(50, profile.xp);
    }
    @Test public void repeatedLaunchDoesNotConsumeAnotherLoadout() {
        profile.prepare(profile.stash.get(0), true);
        profile.begin();
        int id = profile.raidID;
        profile.begin();
        assertEquals(id, profile.raidID);
        assertEquals(4, profile.stash.size());
        assertTrue(profile.prepared.isEmpty());
    }
    @Test public void successfulExtractionStoresNestedLootOnce() throws Exception {
        profile.begin();
        int id = profile.raidID;
        Dungeon.hero = new Hero();
        Bag bag = new Bag();
        bag.items.add(new Food());
        Dungeon.hero.belongings.backpack.items.add(bag);
        Dungeon.gold = 17;
        profile.settle(id, true);
        profile.settle(id, true);
        assertEquals(6, profile.stash.size());
        assertEquals(117, profile.gold);
        assertEquals(10, profile.xp);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(6, profile.stash.size());
        assertEquals(1, ((Bag)profile.stash.get(5)).items.size());
    }
    @Test public void failedSaveRollsBackEquipmentMovement() throws Exception {
        Item item = profile.stash.get(0);
        java.io.File blocker = folder.newFile("not-a-directory");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute, blocker.getAbsolutePath()+"/");
        try { profile.prepare(item, true); fail("Saving must fail"); }
        catch (IllegalStateException expected) { }
        assertEquals(5, profile.stash.size());
        assertTrue(profile.prepared.isEmpty());
    }
}
