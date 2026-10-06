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
    @Test public void utilitiesApplyAcrossWeaponsAndRemainRaidOnly(){
        profile.nodes.add("scout_0");profile.nodes.add("food_0");
        assertEquals(1,profile.bonus(ExtractionGrowth.Stat.VISION,3),0.001f);
        assertEquals(1,profile.bonus(ExtractionGrowth.Stat.VISION,11),0.001f);
        Hero h=new Hero();assertEquals(0,ExtractionUtility.value(h,ExtractionGrowth.Stat.VISION),0.001f);
        h.extractionRaidID=1;assertEquals(1,ExtractionUtility.value(h,ExtractionGrowth.Stat.VISION),0.001f);
        assertFalse(ExtractionGrowth.DESCS[ExtractionGrowth.index("scout_0")].contains("계통의 무기를 사용할 때"));
    }
    @Test public void foodAndKillEffectsHealAndRefreshShieldInsteadOfStacking(){
        Hero h=new Hero();h.extractionRaidID=1;h.HT=40;h.HP=10;
        profile.nodes.add("food_1");profile.nodes.add("food_2");profile.nodes.add("momentum_0");profile.nodes.add("momentum_1");
        ExtractionUtility.food(h);assertEquals(13,h.HP);assertEquals(5,h.shielding());
        ExtractionUtility.food(h);assertEquals(16,h.HP);assertEquals(5,h.shielding());
        ExtractionUtility.kill(h);assertEquals(17,h.HP);assertEquals(5,h.shielding());
    }
    @Test public void newFloorRewardsAndEmergencyRescueSurviveHeroReload(){
        Hero h=new Hero();h.extractionRaidID=1;h.HT=40;h.HP=10;
        profile.nodes.add("medic_4");profile.nodes.add("medic_8");
        assertTrue(ExtractionUtility.floor(h,2));assertEquals(13,h.HP);
        assertFalse(ExtractionUtility.floor(h,2));assertEquals(13,h.HP);
        assertFalse(ExtractionUtility.rescue(h,12,new Object()));
        assertTrue(ExtractionUtility.rescue(h,13,new Object()));assertEquals(10,h.HP);
        h.extractionBossDefeated=true;
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();h.storeInBundle(saved);
        Hero loaded=new Hero();loaded.restoreFromBundle(saved);
        assertTrue(loaded.extractionBossDefeated);assertTrue(loaded.extractionSecondWindUsed);
        loaded.HP=1;assertFalse(ExtractionUtility.floor(loaded,2));assertEquals(1,loaded.HP);
        assertFalse(ExtractionUtility.rescue(loaded,100,new Object()));
        assertTrue(ExtractionUtility.floor(loaded,3));assertEquals(4,loaded.HP);
    }
    @Test public void utilityDamageReductionAndLowHealthEffectsChangeCombat(){
        Hero h=new Hero();h.extractionRaidID=1;h.HT=40;h.HP=14;
        profile.nodes.add("ward_0");profile.nodes.add("ward_1");profile.nodes.add("stealth_4");profile.nodes.add("momentum_5");
        assertEquals(7,ExtractionUtility.incoming(h,10,new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison()));
        assertEquals(10,ExtractionUtility.incoming(h,10,new Object()));
        assertEquals(1.15f,ExtractionUtility.lowHealthMultiplier(h,ExtractionGrowth.Stat.LOW_HP_DAMAGE),0.001f);
        h.HP=15;assertEquals(1f,ExtractionUtility.lowHealthMultiplier(h,ExtractionGrowth.Stat.LOW_HP_DAMAGE),0.001f);
    }
    @Test public void raidExperienceAdvancesOriginalCombatLevelAndPermanentProgress(){
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        h.earnExp(10,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class);
        assertEquals(2,h.lvl);assertEquals(10,profile.xp);assertEquals(10,h.extractionXP);
        assertEquals(0,h.talents.get(0).size());
    }
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
        Game.version = "0.2.0-extraction-INDEV";
        Game.versionCode = 922;
        SPDSettings.set(new HeadlessPreferences("test-preferences.xml", folder.getRoot().getAbsolutePath()));
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute, folder.getRoot().getAbsolutePath()+"/");
        com.shatteredpixel.shatteredpixeldungeon.Badges.reset();
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
    @Test public void inventoryRoundTripSurvivesReload() throws Exception {
        Item sword = profile.stash.get(0);
        profile.prepare(sword, true);
        profile.prepare(sword, false);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(5, profile.stash.size());
        assertTrue(profile.prepared.isEmpty());
    }
    @Test public void preparationCapacityUsesLearnedPackNodes() throws Exception {
        profile.learn(6);
        assertEquals(14, profile.capacity());
        for (int n = 0; n < 13; n++) {
            Food item = new Food();
            profile.stash.add(item);
            profile.prepare(item, true);
        }
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(13, profile.prepared.size());
        assertEquals(14, profile.capacity());
    }
    @Test public void selectedEquipmentSurvivesReloadAndIsWornInRaid() throws Exception {
        Item original = profile.stash.get(0);
        Item chosen = new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword().identify().upgrade();
        profile.stash.add(chosen);
        profile.prepare(original, true);
        profile.prepare(chosen, true);
        profile.selectEquipment(chosen);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(1, profile.preparedWeapon().level());
        profile.begin();
        Dungeon.hero = new Hero();
        Dungeon.hero.heroClass = com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.WARRIOR;
        profile.initialize(Dungeon.hero);
        assertEquals(1, Dungeon.hero.belongings.weapon.level());
        assertEquals(1, Dungeon.hero.belongings.backpack.items.size());
    }
    @Test public void returnAllPreservesEveryPreparedItemAfterReload() throws Exception {
        for (Item item : new java.util.ArrayList<>(profile.stash)) profile.prepare(item, true);
        profile.returnPrepared();
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(5, profile.stash.size());
        assertTrue(profile.prepared.isEmpty());
    }
    @Test public void expeditionDoesNotRestoreOriginalClassTalents() {
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent = com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.HEARTY_MEAL;
        Hero hero = new Hero();
        hero.heroClass = com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.WARRIOR;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(hero);
        hero.talents.get(0).put(talent, 1);
        com.watabou.utils.Bundle saved = new com.watabou.utils.Bundle();
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.storeTalentsInBundle(saved, hero);
        hero.extractionRaidID = 1;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.restoreTalentsFromBundle(saved, hero);
        assertEquals(4, hero.talents.size());
        for (java.util.Map<?, ?> tier : hero.talents) assertTrue(tier.isEmpty());
        assertFalse(hero.hasTalent(talent));
    }
    private void learnPath(String id) {
        int index=ExtractionGrowth.index(id);
        for(int parent:ExtractionGrowth.NODES[index].parents)learnPath(ExtractionGrowth.IDS[parent]);
        if(!profile.nodes.contains(id))profile.learn(index);
    }
    @Test public void graphHas162UniqueNodesWithBranchingAndConvergence() {
        assertEquals(162, ExtractionGrowth.NODES.length);
        java.util.HashSet<String> ids=new java.util.HashSet<>();
        int totalCost=0,convergences=0;
        for(int i=0;i<ExtractionGrowth.NODES.length;i++){
            ExtractionGrowth.Node node=ExtractionGrowth.NODES[i];
            assertTrue(ids.add(node.id));
            assertFalse(node.effects.isEmpty());
            for(int parent:node.parents)assertTrue("Primary paths must be acyclic", parent<i);
            if(node.parents.length==2)convergences++;
            totalCost+=node.cost;
        }
        assertEquals(18,convergences);
        assertEquals(396,totalCost);
        for(int[] branch:ExtractionGrowth.BRANCH_NODES)assertEquals(9,branch.length);
    }
    @Test public void convergenceRequiresBothPathsAndPersists() throws Exception {
        profile.points=1000;
        learnPath("sword_3");
        int merge=ExtractionGrowth.index("sword_7"),before=profile.points;
        try { profile.learn(merge); fail("Both paths are required"); }
        catch(IllegalStateException expected) { }
        assertEquals(before,profile.points);
        learnPath("sword_6");
        profile.learn(merge);
        forgetProfile();profile=ExtractionProfile.get();
        assertTrue(profile.nodes.contains("sword_7"));
        assertTrue(profile.unlocked(ExtractionGrowth.index("sword_8")));
    }
    @Test public void weaponSpecializationChangesWhenWeaponChanges() {
        profile.points=1000;learnPath("sword_2");
        assertEquals(14,profile.physicalDamage(10,new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword(),1f));
        assertEquals(11,profile.physicalDamage(10,new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger(),1f));
        assertEquals(11,profile.physicalDamage(10,null,1f));
        assertEquals(10,profile.magicDamage(10));
    }
    @Test public void daggerCriticalPathUsesItsCriticalMultiplier() {
        profile.points=1000;learnPath("dagger_3");
        Item dagger=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger();
        assertEquals(12,profile.physicalDamage(10,(com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon)dagger,1f));
        assertEquals(21,profile.physicalDamage(10,(com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon)dagger,0f));
        assertEquals(11,profile.physicalDamage(10,new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword(),0f));
    }
    @Test public void spearReachIsAppliedByOriginalWeaponFormula() {
        profile.points=1000;learnPath("spear_3");
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear spear=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear();
        Hero hero=new Hero();hero.belongings.weapon=spear;
        assertEquals(2,spear.reachFactor(hero));
        hero.extractionRaidID=1;
        assertEquals(3,spear.reachFactor(hero));
        assertEquals(1,profile.armorPierce(spear));
        assertEquals(1,new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword().reachFactor(hero));
    }
    @Test public void magicAndExplorationPathsAffectTheirOwnSystems() throws Exception {
        profile.points=1000;learnPath("magic_3");learnPath("magic_6");
        assertEquals(16,profile.magicDamage(10));
        assertEquals(1.45f,profile.wandChargeMultiplier(),0.001f);
        learnPath("explore_cap");
        assertEquals(20,profile.capacity());
        assertEquals(1.1f,profile.moveSpeedMultiplier(),0.001f);
        profile.begin();Dungeon.hero=new Hero();Dungeon.gold=50;
        profile.settle(profile.raidID,true);
        assertEquals(170,profile.gold);
        forgetProfile();profile=ExtractionProfile.get();
        assertEquals(170,profile.gold);
        assertTrue(profile.nodes.contains("magic_3"));
    }
    @Test public void legacyNineNodeSaveKeepsItsOriginalBonuses() throws Exception {
        com.watabou.utils.Bundle old=new com.watabou.utils.Bundle();
        old.put("schema",1);old.put("stash",profile.stash);old.put("prepared",profile.prepared);
        old.put("escrow",new java.util.ArrayList<Item>());
        old.put("nodes",new String[]{"power","edge","master","vital","guard","iron","pack","porter","strength"});
        old.put("gold",123);old.put("xp",200);old.put("points",4);old.put("active",false);
        old.put("raid",0);old.put("next",1);old.put("raid_xp",0);old.put("result","");
        FileUtils.bundleToFile(ExtractionProfile.FILE,old);
        forgetProfile();profile=ExtractionProfile.get();
        assertEquals(4,profile.attackBonus());assertEquals(2,profile.defenseBonus());
        assertEquals(16,profile.capacity());assertEquals(6f,profile.bonus(ExtractionGrowth.Stat.HEALTH),0.001f);
        assertEquals(2f,profile.bonus(ExtractionGrowth.Stat.STRENGTH),0.001f);
        assertEquals(123,profile.gold);assertEquals(9,profile.nodes.size());
    }
    @Test public void connectedWeaponRouteUnlocksAcrossSpecializations() throws Exception {
        profile.points=1000;
        learnPath("sword_2");
        int connected=ExtractionGrowth.index("greatsword_5");
        assertFalse(profile.nodes.contains("greatsword_4"));
        assertTrue(profile.unlocked(connected));
        profile.learn(connected);
        forgetProfile();profile=ExtractionProfile.get();
        assertTrue(profile.nodes.contains("greatsword_5"));
        assertEquals(1,profile.defenseBonus(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword()));
    }
}
