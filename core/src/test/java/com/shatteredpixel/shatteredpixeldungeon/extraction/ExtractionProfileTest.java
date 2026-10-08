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
    @Test public void sewerArtifactDrawsRemainPossibleAndDoNotEraseLaterChapterRelics() {
        profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.fullReset();
        java.util.Set<Class<?>> drawn=new java.util.HashSet<>();
        com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact artifact;
        int expected=0;
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category cat=com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARTIFACT;
        for(int i=0;i<cat.classes.length;i++)if(cat.probs[i]>0&&ExpeditionArtifacts.canDrop(cat.classes[i],1,1))expected++;
        assertTrue(expected>0);
        for(int i=0;i<=cat.classes.length;i++){
            artifact=com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomArtifact();if(artifact==null)break;
            assertTrue(ExpeditionArtifacts.canDrop(artifact.getClass(),1,1));assertTrue(drawn.add(artifact.getClass()));
        }
        assertEquals(expected,drawn.size());assertTrue(drawn.contains(ExpeditionArtifacts.UnstableCompass.class));
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.items.Generator.storeInBundle(saved);
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.restoreFromBundle(saved);
        assertNull(com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomArtifact());
        profile.raidChapter=2;profile.raidDifficulty=6;
        java.util.Set<Class<?>> later=new java.util.HashSet<>();
        while((artifact=com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomArtifact())!=null){assertFalse(drawn.contains(artifact.getClass()));assertTrue(later.add(artifact.getClass()));}
        assertEquals(4,later.size());assertTrue(later.contains(ExpeditionArtifacts.HuntersMark.class));
        for(int c=2;c<=5;c++)for(Class<?> type:later)assertTrue(ExpeditionArtifacts.canDrop(type,c,ExtractionDifficulty.fixedStage(c)));
    }
    @Test public void oldBlockedSewerArtifactDeckRepairsOnceAndExcludesCarriedArtifacts() {
        profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;
        Dungeon.hero.belongings.artifact=new com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains();
        com.watabou.utils.Bundle old=new com.watabou.utils.Bundle();old.put("artifact_probs",new float[18]);
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.restoreFromBundle(old);
        com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact dropped=com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomArtifact();
        assertNotNull(dropped);assertFalse(dropped instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();com.shatteredpixel.shatteredpixeldungeon.items.Generator.storeInBundle(saved);
        float[] before=com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARTIFACT.probs.clone();
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.restoreFromBundle(saved);
        assertArrayEquals(before,com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARTIFACT.probs,0);
    }
    @Test public void allArmorTiersClampUpgradesAndOldSavesAndBlockScrollSelection() {
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor[] armor={
            new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor(),
            new com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.ScaleArmor(),new com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor()};
        for(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor item:armor){
            int cap=item.tier*3;assertEquals(cap,WeaponUpgradeLimit.cap(item));item.upgrade(cap-1);assertTrue(WeaponUpgradeLimit.eligible(item));item.upgrade();assertEquals(cap,item.trueLevel());
            assertFalse(new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade().getSelector(false).itemSelectable(item));
            item.cursed=true;item.glyphHardened=true;item.curseInfusionBonus=true;item.upgrade(true);item.upgrade(100);assertTrue(item.cursed);assertTrue(item.glyphHardened);assertEquals(cap,item.trueLevel());assertEquals(cap,item.level());
            assertSame(item,new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade().upgradeItem(item));assertSame(item,new com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion().upgradeItem(item));
            item.level(100);assertEquals(cap,item.trueLevel());
            com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();item.storeInBundle(saved);saved.put("level",100);
            com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor copy=(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)com.watabou.utils.Reflection.newInstance(item.getClass());copy.restoreFromBundle(saved);
            assertEquals(cap,copy.trueLevel());assertEquals(cap,copy.level());assertTrue(copy.cursed);assertTrue(copy.glyphHardened);
        }
    }
    @Test public void classArmorRestoresAndTransfersUsingItsOriginalTierCap() {
        com.shatteredpixel.shatteredpixeldungeon.items.armor.WarriorArmor item=new com.shatteredpixel.shatteredpixeldungeon.items.armor.WarriorArmor();item.tier=2;item.level(100);assertEquals(6,item.trueLevel());
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();item.storeInBundle(saved);saved.put("level",100);
        com.shatteredpixel.shatteredpixeldungeon.items.armor.WarriorArmor copy=new com.shatteredpixel.shatteredpixeldungeon.items.armor.WarriorArmor();copy.restoreFromBundle(saved);assertEquals(2,copy.tier);assertEquals(6,copy.trueLevel());
        Hero hero=new Hero();hero.heroClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.WARRIOR;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor source=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();source.upgrade(3);
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor transferred=com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor.upgrade(hero,source);
        assertEquals(1,transferred.tier);assertEquals(3,transferred.trueLevel());
    }
    @Test public void emergencyExtractionKeepsLootAndEarnedXpButRejectsAllCurrentContractCredit() throws Exception {
        ExtractionContracts.accept(profile,"hunt_1");ExtractionContracts.accept(profile,"record_1");ExtractionContracts.accept(profile,"supply_1");
        profile.contractProgress.put("hunt_1",2);
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);Dungeon.gold=17;
        ExtractionContracts.Run run=ExtractionContracts.run(h);for(int i=0;i<run.progress.length;i++)run.progress[i]=6;
        Item weapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword().identify(false);assertTrue(weapon.collect(h.belongings.backpack));
        assertTrue(new SupplyHealingPotion().quantity(2).collect(h.belongings.backpack));
        assertTrue(new ExtractionShop.SupplyUpgrade().quantity(3).collect(h.belongings.backpack));
        int initialGold=profile.gold,initialStash=profile.stash.size(),id=profile.raidID;
        profile.credit(id,25);int xp=profile.xp,points=profile.points;
        profile.emergencyExtract(id);int gold=profile.gold,stash=profile.stash.size();profile.emergencyExtract(id);profile.settle(id,true);
        assertFalse(profile.active);assertEquals(initialGold+17+30+150,gold);assertEquals(gold,profile.gold);assertEquals(stash,profile.stash.size());assertTrue(stash>initialStash);
        assertTrue(profile.stash.stream().anyMatch(i->i instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword&&!(i instanceof BasicExpeditionSword)));
        assertEquals(xp,profile.xp);assertEquals(points,profile.points);assertEquals(0,profile.unlockedDifficulty[1]);
        assertEquals(2,ExtractionContracts.progress(profile,ExtractionContracts.job("hunt_1")));assertEquals(0,ExtractionContracts.progress(profile,ExtractionContracts.job("record_1")));assertEquals(0,ExtractionContracts.progress(profile,ExtractionContracts.job("supply_1")));
        assertEquals(3,profile.contracts.size());assertTrue(profile.result.startsWith("비상탈출"));
        forgetProfile();profile=ExtractionProfile.get();assertFalse(profile.active);assertEquals(gold,profile.gold);assertEquals(xp,profile.xp);assertEquals(2,ExtractionContracts.progress(profile,ExtractionContracts.job("hunt_1")));
    }
    @Test public void starterBowShowsEnchantmentInItsNameAfterSaving() {
        NodeSpiritBow bow=new NodeSpiritBow();
        String plain=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow().name();
        assertEquals(plain,bow.name());
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic enchant=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments.Kinetic();
        bow.enchant(enchant);assertEquals(enchant.name(plain),bow.name());
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("bow",bow);
        NodeSpiritBow copy=(NodeSpiritBow)saved.get("bow");
        assertEquals(enchant.getClass(),copy.enchantment.getClass());assertEquals(bow.name(),copy.name());
    }
    @Test public void stylusSelectionInscribesBothClothingSlotsAndConsumesOnlySuccessfulUses() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Hero h=Dungeon.hero;
        com.shatteredpixel.shatteredpixeldungeon.items.Stylus stylus=new com.shatteredpixel.shatteredpixeldungeon.items.Stylus();stylus.quantity(2);assertTrue(stylus.collect(h.belongings.backpack));
        Field selection=com.shatteredpixel.shatteredpixeldungeon.items.Stylus.class.getDeclaredField("itemSelector");selection.setAccessible(true);
        com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector selector=(com.shatteredpixel.shatteredpixeldungeon.windows.WndBag.ItemSelector)selection.get(stylus);
        Field user=Item.class.getDeclaredField("curUser");user.setAccessible(true);Object previous=user.get(null);user.set(null,h);
        try {
            ExpeditionClothing unknown=ExpeditionClothing.create(false,1);selector.onSelect(unknown);assertNull(unknown.glyph);assertEquals(2,stylus.quantity());
            ExpeditionClothing cursed=(ExpeditionClothing)ExpeditionClothing.create(true,1).identify(false);cursed.cursed=true;selector.onSelect(cursed);assertNull(cursed.glyph);assertEquals(2,stylus.quantity());
            assertFalse(selector.itemSelectable(new Food()));
            for(boolean boots:new boolean[]{false,true}){
                ExpeditionClothing item=(ExpeditionClothing)ExpeditionClothing.create(boots,3).identify(false);
                if(boots)h.belongings.boots=item;else h.belongings.pants=item;
                assertTrue(selector.itemSelectable(item));selector.onSelect(item);assertNotNull(item.glyph);assertFalse(item.glyph.curse());
                com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("clothing",item);
                ExpeditionClothing restored=(ExpeditionClothing)saved.get("clothing");assertEquals(item.glyph.getClass(),restored.glyph.getClass());assertEquals(item.name(),restored.name());
            }
            assertNull(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Stylus.class));
        } finally {user.set(null,previous);}
    }
    @Test public void allBagsRoutePreparedSuppliesAndRemainUniqueAfterSave() throws Exception {
        Item potion=new SupplyHealingPotion().quantity(2);
        Item scroll=new ExtractionShop.SupplyUpgrade().quantity(3);
        Item seed=new com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom.Seed();
        for(Item item:new Item[]{potion,scroll,seed}){profile.stash.add(item);profile.prepare(item,true);}
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);
        com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier potions=h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier.class);
        assertEquals(2,h.belongings.getItem(SupplyHealingPotion.class).quantity());assertTrue(potions.items.contains(h.belongings.getItem(SupplyHealingPotion.class)));
        assertEquals(3,h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder.class).items.get(0).quantity());
        assertTrue(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch.class).items.get(0) instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom.Seed);
        assertNotNull(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster.class));
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("hero",h);
        h=(Hero)saved.get("hero");Dungeon.hero=h;ExtractionProfile.ensureBags(h);ExtractionProfile.ensureBags(h);
        assertEquals(5,h.belongings.getBags().size());
        assertEquals(2,h.belongings.getItem(SupplyHealingPotion.class).quantity());
        assertTrue(Dungeon.LimitedDrops.MAGICAL_HOLSTER.dropped());
    }
    @Test public void freeBagsDepositContentsOnceAndAreReissuedAfterDeath() throws Exception {
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);Dungeon.gold=0;
        new ExtractionShop.SupplyUpgrade().quantity(2).collect(h.belongings.backpack);
        new com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom.Seed().quantity(3).collect(h.belongings.backpack);
        int before=profile.gold,id=profile.raidID;profile.settle(id,true);profile.settle(id,true);
        assertEquals(before+100,profile.gold);
        int seeds=0;for(Item item:profile.stash){assertFalse(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bags.VelvetPouch || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bags.PotionBandolier || item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bags.MagicalHolster);if(item instanceof com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom.Seed)seeds+=item.quantity();}
        assertEquals(3,seeds);assertEquals(2,h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.bags.ScrollHolder.class).items.get(0).quantity());
        profile.begin();profile.settle(profile.raidID,false);profile.begin();h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);
        assertEquals(5,h.belongings.getBags().size());
    }
    private static class EffectSprite extends com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite {
        @Override public void showStatusWithIcon(int color,String text,int icon,Object... args){}
        @Override public void add(State state){}
        @Override public void remove(State state){}
        @Override public void die(){}
    }
    @Test public void sewerMonsterDeathsDropTheirRealLootWithoutRequiringAnImmediateSprite() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Dungeon.level=Dungeon.newLevel();Hero h=Dungeon.hero;h.pos=Dungeon.level.entrance();
        h.sprite=new EffectSprite();h.sprite.visible=false;
        java.util.Arrays.fill(Dungeon.level.heroFOV,true);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[] mobs={
            new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Gnoll(),
            new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab(),
            new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Snake(),
            new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Swarm(),
            new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Slime()};
        Field chance=com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob.class.getDeclaredField("lootChance");chance.setAccessible(true);
        int heapCount=Dungeon.level.heaps.size;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:mobs){
            chance.setFloat(mob,1);mob.pos=h.pos;mob.sprite=new EffectSprite();Dungeon.level.mobs.add(mob);
            mob.die(h);assertFalse(Dungeon.level.mobs.contains(mob));
        }
        assertTrue(Dungeon.level.heaps.size>heapCount);assertTrue(profile.xp>0);
        h.sprite=null;
    }
    @Test public void drinkingNonHazardousPotionsExecutesTheirRealEffects() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Dungeon.level=Dungeon.newLevel();Hero h=Dungeon.hero;h.pos=Dungeon.level.entrance();
        h.sprite=new EffectSprite();h.sprite.visible=false;new com.watabou.noosa.Group().add(h.sprite);
        com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion[] potions={
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMindVision(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfLevitation(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHaste(),
            new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfPurity()};
        Field user=Item.class.getDeclaredField("curUser");user.setAccessible(true);user.set(null,h);
        for(com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion potion:potions){
            potion.apply(h);assertTrue(potion.isKnown());
        }
        assertEquals(11,h.STR);assertEquals(20,h.HT);assertEquals(10,profile.xp);
        h.sprite=null;
    }
    @Test public void resumeRestoresMightRingWhileGlobalHeroIsStillNull(){
        Hero h=new Hero();h.extractionRaidID=1;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        h.belongings.ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight();
        h.belongings.ring.activate(h);Dungeon.hero=h;h.updateHT(false);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("hero",h);
        Dungeon.hero=null;
        Hero restored=(Hero)saved.get("hero");
        assertNotNull(restored);assertEquals(21,restored.HT);assertEquals(11,restored.STR());
    }
    @Test public void abandonmentNeedsNoLiveHeroAndKeepsCreditedGrowth() throws Exception {
        Item sword=profile.stash.get(0);profile.prepare(sword,true);profile.begin();int id=profile.raidID;
        profile.credit(id,30);Dungeon.hero=null;Dungeon.level=null;
        profile.abandon();profile.abandon();
        assertFalse(profile.active);assertEquals(30,profile.xp);assertEquals(6,profile.points);
        assertEquals(3,profile.stash.size());assertTrue(profile.prepared.isEmpty());assertEquals(100,profile.gold);
        assertTrue(profile.result.contains("포기"));
        forgetProfile();profile=ExtractionProfile.get();assertFalse(profile.active);assertEquals(30,profile.xp);
        profile.begin();assertTrue(profile.raidID>id);Hero fresh=new Hero();profile.initialize(fresh);
        assertTrue(fresh.belongings.weapon instanceof BasicExpeditionSword);
    }
    @Test public void failedAbandonmentPreservesRaidAndEscrow() throws Exception {
        Item sword=profile.stash.get(0);profile.prepare(sword,true);profile.begin();int id=profile.raidID;
        java.io.File blocker=folder.newFile("abandon-blocker");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.abandon();fail("Saving must fail");}catch(IllegalStateException expected){}
        assertTrue(profile.active);assertEquals(id,profile.raidID);
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        Hero fresh=new Hero();profile.initialize(fresh);assertNotNull(fresh.belongings.weapon);
    }
    @Test public void generatedLootAndPotionStatesSurviveNativeRunResume() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";
        Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();
        Hero h=Dungeon.hero;h.pos=Dungeon.level.entrance();
        for(com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category category:new com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category[]{
                com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.POTION,
                com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.SEED,
                com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.WEAPON,
                com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARMOR,
                com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.RING}){
            for(int i=0;i<20;i++){
                Item loot=com.shatteredpixel.shatteredpixeldungeon.items.Generator.random(category);
                assertNotNull(loot);com.watabou.utils.Bundle copy=new com.watabou.utils.Bundle();copy.put("loot",loot);
                assertNotNull(copy.get("loot"));
            }
        }
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(h,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision.class,10);
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.prolong(h,com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Levitation.class,10);
        com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing.heal(h);
        h.belongings.ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight();h.belongings.ring.activate(h);
        Dungeon.saveGame(1);Dungeon.saveLevel(1);
        int id=h.extractionRaidID;Dungeon.hero=null;Dungeon.level=null;
        Dungeon.loadGame(1);Dungeon.level=Dungeon.loadLevel(1);
        assertEquals(id,Dungeon.hero.extractionRaidID);assertEquals(1,Dungeon.depth);
        assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing.class));
        assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision.class));
        assertFalse(Dungeon.level.mobs.isEmpty());
    }
    @Test public void floorSwitchDefersScoutingUntilSceneCreationAndResumeKeepsItsReward() throws Exception {
        profile.points=1000;learnPath("scout_6");profile.begin();
        Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";
        Dungeon.seed=2467327059549L;Dungeon.init();
        Dungeon.switchLevel(Dungeon.newLevel(),-1);
        assertEquals(1,Dungeon.depth);
        assertEquals(0,Dungeon.hero.extractionVisited);
        assertNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight.class));
        Dungeon.depth=2;Dungeon.switchLevel(Dungeon.newLevel(),-1);
        Hero h=Dungeon.hero;assertEquals(2,Dungeon.depth);
        // Ensure this regression includes a nearby discovery, even if generator changes.
        int hidden=h.pos+1;
        com.shatteredpixel.shatteredpixeldungeon.levels.Level.set(hidden,
                com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.SECRET_DOOR);
        Dungeon.switchLevel(Dungeon.level,h.pos);
        assertTrue(Dungeon.level.secret[hidden]);assertEquals(0,h.extractionVisited);
        assertNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight.class));
        // Rendering/discovery is covered by the native Windows floor transition test.
        java.util.Arrays.fill(Dungeon.level.secret,false);
        assertTrue(ExtractionUtility.floor(h,2));
        assertNotNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight.class));
        assertNotNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness.class));
        assertNotNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MindVision.class));
        assertTrue(Dungeon.level.mapped[hidden]);
        Dungeon.saveAll();int id=h.extractionRaidID;
        Dungeon.hero=null;Dungeon.level=null;Dungeon.loadGame(1);Dungeon.switchLevel(Dungeon.loadLevel(1),-1);
        assertEquals(2,Dungeon.depth);assertEquals(id,Dungeon.hero.extractionRaidID);
        assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Foresight.class));
        assertFalse(ExtractionUtility.floor(Dungeon.hero,2));
    }
    @Test public void debugIsOptInAndResourcesPersistWithoutChangingStats() throws Exception {
        assertFalse(profile.debugEnabled);int gold=profile.gold,points=profile.points;
        try{profile.debugResources(10000,250,100);fail("Debug must be enabled");}catch(IllegalStateException expected){}
        assertEquals(gold,profile.gold);assertEquals(points,profile.points);
        Hero hero=new Hero();int hp=hero.HT,str=hero.STR;
        profile.setDebugEnabled(true);profile.debugResources(10000,250,100);profile.debugUnlockPrison();
        forgetProfile();profile=ExtractionProfile.get();assertTrue(profile.debugEnabled);
        assertEquals(gold+10000,profile.gold);assertEquals(250,profile.xp);assertEquals(133,profile.points);
        assertEquals(hp,hero.HT);assertEquals(str,hero.STR);assertEquals(1,profile.unlockedDifficulty[1]);
        profile.setDebugEnabled(false);forgetProfile();profile=ExtractionProfile.get();assertFalse(profile.debugEnabled);
    }
    @Test public void debugItemsUseStashAndRunInventoryWithoutChangingNormalLoot() throws Exception {
        profile.setDebugEnabled(true);int stash=profile.stash.size();
        Item sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword().identify(false).level(15);
        ExtractionDebug.give(sword,false);forgetProfile();profile=ExtractionProfile.get();assertEquals(stash+1,profile.stash.size());
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Dungeon.level=Dungeon.newLevel();Dungeon.hero.pos=Dungeon.level.entrance();Dungeon.hero.ready=true;
        Item scroll=new ExtractionShop.SupplyUpgrade().quantity(20);ExtractionDebug.give(scroll,true);
        assertSame(scroll,Dungeon.hero.belongings.getItem(ExtractionShop.SupplyUpgrade.class));assertEquals(2,ExtractionDifficulty.raidMaxTier());
        assertEquals(0,ExtractionDifficulty.tierWeights()[4],0);
        try{ExtractionDebug.give(new Food(),false);fail("Escrow must not be edited");}catch(IllegalStateException expected){}
    }
    @Test public void debugRaidToolsPreserveSnapshotAndDisabledModeStopsInvulnerability() throws Exception {
        profile.setDebugEnabled(true);profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Dungeon.level=Dungeon.newLevel();Hero hero=Dungeon.hero;hero.pos=Dungeon.level.entrance();hero.ready=true;hero.sprite=new EffectSprite();hero.sprite.visible=false;
        hero.HP=5;ExtractionDebug.heal();assertEquals(hero.HT,hero.HP);
        ExtractionDebug.toggleInvulnerable();hero.damage(100,new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger());assertEquals(hero.HT,hero.HP);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();hero.storeInBundle(saved);Hero copy=new Hero();copy.restoreFromBundle(saved);assertTrue(copy.extractionDebugInvulnerable);
        assertEquals(5,ExtractionDebug.checkedDepth(5));try{ExtractionDebug.checkedDepth(6);fail("Cross chapter warp must be rejected");}catch(IllegalArgumentException expected){}
        ExtractionDebug.weakenEnemies();for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs)if(mob.alignment==com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY)assertEquals(1,mob.HP);
        int id=profile.raidID;profile.setDebugEnabled(false);hero.damage(1,new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger());assertEquals(hero.HT-1,hero.HP);assertEquals(id,profile.raidID);
        hero.sprite=null;
    }
    @Test public void chapterUnlocksPersistAndFixedRaidSelectionIsFrozen() throws Exception {
        try{profile.selectRaid(2);fail("Prison must be locked");}catch(IllegalStateException expected){}
        profile.begin();Dungeon.hero=new Hero();Dungeon.gold=0;profile.settle(profile.raidID,true);
        assertEquals(1,profile.unlockedDifficulty[0]);assertEquals(1,profile.unlockedDifficulty[1]);
        profile.selectRaid(2);profile.begin();int id=profile.raidID;
        try{profile.selectRaid(1);fail("Active selection must be frozen");}catch(IllegalStateException expected){}
        forgetProfile();profile=ExtractionProfile.get();assertEquals(2,profile.raidChapter);assertEquals(6,profile.raidDifficulty);assertEquals(4,profile.raidRules);
        profile.settle(id,false);assertEquals(1,profile.unlockedDifficulty[1]);
        profile.selectRaid(1);profile.selectedDifficulty=10;profile.begin();assertEquals(1,profile.raidDifficulty);profile.settle(profile.raidID,true);assertEquals(1,profile.unlockedDifficulty[0]);
        assertFalse(profile.result.contains("난이도"));
    }
    @Test public void selectedPrisonStartsAtSixAndGeneratesScaledOriginalPrison(){
        profile.unlockedDifficulty[1]=1;profile.selectRaid(2);profile.begin();
        Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        assertEquals(6,Dungeon.depth);assertEquals(profile.raidID,Dungeon.hero.extractionRaidID);
        com.shatteredpixel.shatteredpixeldungeon.levels.Level level=Dungeon.newLevel();
        assertTrue(level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel);
        int scaled=0;for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs)if(mob.extractionScaled)scaled++;
        assertTrue(scaled>0);assertEquals(20,Dungeon.hero.HT);
    }
    @Test public void fixedChapterLootAndEnemyPowerRequirePreparation(){
        profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;
        assertEquals(2,ExtractionDifficulty.raidMaxTier());
        assertEquals(30,ExtractionDifficulty.tierWeights()[1],0);assertEquals(0,ExtractionDifficulty.tierWeights()[2],0);
        profile.settle(profile.raidID,false);profile.unlockedDifficulty[1]=1;profile.selectRaid(2);profile.begin();
        Dungeon.hero.extractionRaidID=profile.raidID;
        assertEquals(3,ExtractionDifficulty.raidMaxTier());assertEquals(25,ExtractionDifficulty.tierWeights()[2],0);assertEquals(0,ExtractionDifficulty.tierWeights()[3],0);
        assertEquals(4,ExtractionDifficulty.extraMobs());
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard guard=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Guard();
        int hp=guard.HT;ExtractionDifficulty.prepare(guard);assertTrue(guard.HT>=Math.round(hp*3.975f));
        assertEquals(20,ExtractionDifficulty.incoming(10,guard)/(guard.extractionElite==1?1.2f:1),1);
        Dungeon.gold=100;int beforeGold=profile.gold,beforeXP=profile.xp;
        profile.settle(profile.raidID,true);assertEquals(215,profile.gold-beforeGold);assertEquals(45,profile.xp-beforeXP);
    }
    @Test public void oldActiveRaidKeepsSnapshotUntilNextDeparture() throws Exception {
        profile.begin();profile.raidRules=1;profile.raidDifficulty=10;
        java.lang.reflect.Method snapshot=ExtractionProfile.class.getDeclaredMethod("bundle");snapshot.setAccessible(true);
        com.watabou.utils.Bundle saved=(com.watabou.utils.Bundle)snapshot.invoke(profile);
        saved.remove("raid_rules");
        java.lang.reflect.Method restore=ExtractionProfile.class.getDeclaredMethod("restore",com.watabou.utils.Bundle.class);restore.setAccessible(true);restore.invoke(profile,saved);
        profile.begin();assertEquals(10,profile.raidDifficulty);assertEquals(1,profile.raidRules);
        Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;assertEquals(5,ExtractionDifficulty.raidMaxTier());
        profile.abandon();profile.begin();assertEquals(1,profile.raidDifficulty);assertEquals(4,profile.raidRules);
    }
    @Test public void lootCeilingsScalingAndShopRestrictionsDoNotFollowPlayerPower(){
        assertEquals(2,ExtractionDifficulty.maxTier(1,1));assertEquals(3,ExtractionDifficulty.maxTier(1,4));
        assertEquals(4,ExtractionDifficulty.maxTier(1,7));assertEquals(5,ExtractionDifficulty.maxTier(1,10));
        assertEquals(3,ExtractionDifficulty.maxTier(2,1));assertEquals(5,ExtractionDifficulty.maxTier(2,7));
        for(int chapter=1;chapter<=2;chapter++)for(int stage=1;stage<=10;stage++){
            float[] weights=ExtractionDifficulty.tierWeights(chapter,stage,false);float sum=0;
            for(int i=0;i<5;i++){sum+=weights[i];if(i>=ExtractionDifficulty.maxTier(chapter,stage))assertEquals(0,weights[i],0);}
            assertEquals(100,sum,0);
        }
        assertTrue(ExtractionDifficulty.healthMultiplier(1,10)>ExtractionDifficulty.healthMultiplier(1,1));
        assertTrue(ExtractionDifficulty.damageMultiplier(10)>ExtractionDifficulty.damageMultiplier(1));
        profile.gold=99999;
        for(int n=0;n<ExtractionShop.OFFERS.size();n++)if(!ExtractionShop.OFFERS.get(n).available()){
            try{profile.buy(n);fail("High tier shop purchase must be rejected");}catch(IllegalStateException expected){}
        }
    }
    @Test public void scaledEnemiesAndEliteFlagsRoundTripWithoutDoubleScaling(){
        profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        int original=rat.HT;ExtractionDifficulty.prepare(rat);int scaled=rat.HT;
        assertTrue(scaled>original);ExtractionDifficulty.prepare(rat);assertEquals(scaled,rat.HT);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();rat.storeInBundle(saved);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat restored=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();restored.restoreFromBundle(saved);
        ExtractionDifficulty.prepare(restored);assertEquals(scaled,restored.HT);assertEquals(rat.extractionElite,restored.extractionElite);
    }
    @Test public void relicsOnlyWorkEquippedAndCapsAndChargesPersist() throws Exception {
        Hero hero=new Hero();hero.extractionRaidID=1;Dungeon.hero=hero;
        ExpeditionArtifacts.BloodLantern lantern=new ExpeditionArtifacts.BloodLantern();
        hero.belongings.backpack.items.add(lantern);hero.HP=5;
        ExpeditionArtifacts.kill(hero);assertEquals(5,hero.HP);assertEquals(1,ExpeditionArtifacts.healingMultiplier(hero),0);
        hero.belongings.artifact=lantern;lantern.activate(hero);ExpeditionArtifacts.kill(hero);
        assertEquals(6,hero.HP);assertEquals(.65f,ExpeditionArtifacts.healingMultiplier(hero),.001f);
        for(int i=0;i<1000;i++)lantern.gainKill();assertEquals(15,lantern.level());lantern.upgrade(100);assertEquals(15,lantern.level());
        lantern.level(100);assertEquals(15,lantern.level());
        lantern.identify(false);com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();lantern.storeInBundle(saved);
        ExpeditionArtifacts.BloodLantern copy=new ExpeditionArtifacts.BloodLantern();copy.restoreFromBundle(saved);
        assertEquals(15,copy.level());assertEquals(lantern.status(),copy.status());
        assertEquals("8/8",copy.status());
        profile.stash.add(copy);profile.prepare(copy,true);profile.begin();Hero next=new Hero();next.extractionRaidID=profile.raidID;
        profile.initialize(next);assertTrue(next.belongings.artifact instanceof ExpeditionArtifacts.BloodLantern);
    }
    @Test public void olderArtifactDeckKeepsUsedOriginalRelicsAndAddsNewRelics(){
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("artifact_probs",new float[13]);
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.restoreFromBundle(saved);
        float[] probs=com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARTIFACT.probs;
        assertEquals(18,probs.length);for(int i=0;i<13;i++)assertEquals(0,probs[i],0);
        for(int i=13;i<18;i++)assertEquals(1,probs[i],0);
    }
    @Test public void relicTradeoffsAndTempoApplyAndTenguUnlocksExit(){
        Hero hero=new Hero();hero.extractionRaidID=1;Dungeon.hero=hero;
        hero.belongings.artifact=new ExpeditionArtifacts.GreedPouch();Dungeon.gold=500;
        assertEquals(12,ExpeditionArtifacts.incoming(hero,10));
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff.affect(hero,ExpeditionArtifacts.BurstTempo.class);
        assertEquals(2,ExpeditionArtifacts.tempo(hero),0);
        profile.raidChapter=2;Dungeon.depth=10;Dungeon.branch=0;
        ExtractionUtility.defeated(hero,new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu());assertTrue(hero.extractionBossDefeated);
        assertFalse(ExpeditionArtifacts.canDrop(ExpeditionArtifacts.HuntersMark.class,1,1));
        assertTrue(ExpeditionArtifacts.canDrop(ExpeditionArtifacts.HuntersMark.class,2,1));
    }
    @Test public void allPotionAndScrollVariantsRedeemAcrossNestedBags() throws Exception {
        profile.begin();Dungeon.hero=new Hero();Dungeon.gold=0;
        Bag outer=new Bag(),inner=new Bag();outer.items.add(inner);inner.items.add(new Food());
        Item[] consumables={new SupplyHealingPotion().quantity(2),
                new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength(),
                new com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion().quantity(2),
                new com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfCleansing(),
                new ExtractionShop.SupplyRemoveCurse().quantity(2),
                new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment()};
        int expected=100;for(Item i:consumables){expected+=ExtractionShop.salePrice(i);inner.items.add(i);}
        Dungeon.hero.belongings.backpack.items.add(outer);profile.settle(profile.raidID,true);
        assertEquals(expected,profile.gold);assertTrue(profile.result.contains("포션 6개 · 스크롤 3장"));
        assertEquals(5,profile.stash.size());Bag saved=(Bag)((Bag)profile.stash.get(4)).items.get(0);
        assertEquals(1,saved.items.size());assertTrue(saved.items.get(0) instanceof Food);
        assertEquals(7,inner.items.size());
        forgetProfile();profile=ExtractionProfile.get();assertEquals(expected,profile.gold);
    }
    @Test public void restoredOldRaidRemovesAutomaticLevelBonusesButPreservesNodeHealth(){
        Hero h=new Hero();h.extractionRaidID=1;h.lvl=8;h.HTBoost=6;h.HT=61;h.HP=55;
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();h.storeInBundle(saved);
        saved.put("attackSkill",17);saved.put("defenseSkill",12);
        Hero restored=new Hero();restored.restoreFromBundle(saved);Dungeon.hero=restored;
        assertEquals(8,restored.lvl);assertEquals(26,restored.HT);assertEquals(26,restored.HP);
        assertEquals(10,restored.attackSkill(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat()));
        assertEquals(5,restored.defenseSkill(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat()));
        restored.lvl=20;restored.updateHT(true);assertEquals(26,restored.HT);assertEquals(26,restored.HP);
    }
    @Test public void spiritBowAndTemporaryArmorDoNotScaleWithExtractionLevel(){
        Hero h=new Hero();h.extractionRaidID=1;Dungeon.hero=h;
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow();
        int min=bow.min(),max=bow.max(),level=bow.level();
        com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight.HTBoost boost=
                com.watabou.utils.Reflection.newInstance(com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight.HTBoost.class);
        boost.attachTo(h);boost.reset();int healthBonus=boost.boost();
        h.lvl=30;assertEquals(min,bow.min());assertEquals(max,bow.max());assertEquals(level,bow.level());
        assertEquals(healthBonus,boost.boost());
        h.extractionRaidID=0;assertEquals(30,h.combatLevel());assertTrue(bow.max()>max);
    }
    @Test public void leftoverScrollsRedeemNestedStacksOnceAndPersist() throws Exception {
        profile.begin();int id=profile.raidID;Dungeon.hero=new Hero();Dungeon.gold=17;
        Bag bag=new Bag();bag.items.add(new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade().quantity(3));
        bag.items.add(new Food());Dungeon.hero.belongings.backpack.items.add(bag);
        Dungeon.hero.belongings.backpack.items.add(new ExtractionShop.SupplyUpgrade().quantity(2));
        Dungeon.hero.belongings.backpack.items.add(new ExtractionShop.SupplyIdentify());
        profile.settle(id,true);profile.settle(id,true);
        assertEquals(397,profile.gold);assertEquals(5,profile.stash.size());assertTrue(profile.result.contains("6장"));
        assertEquals(1,((Bag)profile.stash.get(4)).items.size());
        assertEquals(3,Dungeon.hero.belongings.backpack.items.size()); // live run bag remains intact
        assertEquals(2,bag.items.size());
        forgetProfile();profile=ExtractionProfile.get();assertEquals(397,profile.gold);
        assertEquals(5,profile.stash.size());
    }
    @Test public void failedRedemptionKeepsRunScrollsAndDeathPaysNothing() throws Exception {
        profile.begin();int id=profile.raidID;Dungeon.hero=new Hero();Dungeon.gold=0;
        Item scroll=new ExtractionShop.SupplyUpgrade().quantity(2);Dungeon.hero.belongings.backpack.items.add(scroll);
        java.io.File blocker=folder.newFile("settlement-blocker");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.settle(id,true);fail("Saving must fail");}catch(IllegalStateException expected){}
        assertTrue(profile.active);assertEquals(100,profile.gold);assertEquals(4,profile.stash.size());
        assertSame(scroll,Dungeon.hero.belongings.backpack.items.get(0));assertEquals(2,scroll.quantity());
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        profile.settle(id,false);assertEquals(100,profile.gold);assertFalse(profile.active);
    }
    @Test public void shopPurchasesStackSalesAndSupplyIdentitySurviveReload() throws Exception {
        for(ExtractionShop.Offer offer:ExtractionShop.OFFERS){
            Item item=offer.item();assertTrue(item.isIdentified());assertTrue(offer.price>ExtractionShop.salePrice(item));
        }
        profile.gold=1000;profile.buy(4);profile.buy(1);
        assertEquals(730,profile.gold);Item food=profile.stash.get(5);food.quantity(3);
        profile.sell(food,false);assertEquals(740,profile.gold);assertEquals(2,food.quantity());
        profile.sell(food,true);assertEquals(760,profile.gold);assertFalse(profile.stash.contains(food));
        forgetProfile();profile=ExtractionProfile.get();assertEquals(760,profile.gold);
        assertTrue(profile.stash.get(4) instanceof ExtractionShop.SupplyUpgrade);
        assertTrue(((ExtractionShop.SupplyUpgrade)profile.stash.get(4)).isKnown());
        assertEquals("보급 강화 스크롤",profile.stash.get(4).name());
        profile.begin();try{profile.buy(0);fail("Active raids block purchases");}catch(IllegalStateException expected){}
        try{profile.sell(profile.stash.get(0));fail("Active raids block sales");}catch(IllegalStateException expected){}
    }
    @Test public void failedShopSavesRollBackGoldAndStackQuantities() throws Exception {
        Item food=new Food().quantity(3);profile.stash.add(food);profile.buy(1);int gold=profile.gold;
        java.io.File blocker=folder.newFile("shop-blocker");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.buy(0);fail("Saving must fail");}catch(IllegalStateException expected){}
        assertEquals(gold,profile.gold);assertEquals(6,profile.stash.size());
        Item restored=profile.stash.get(4);
        try{profile.sell(restored,false);fail("Saving must fail");}catch(IllegalStateException expected){}
        assertEquals(gold,profile.gold);assertEquals(3,profile.stash.get(4).quantity());
    }
    @Test public void emptyGoldAndPreparedItemsCannotBeSoldOrBoughtAway() {
        profile.gold=0;try{profile.buy(1);fail("Gold is required");}catch(IllegalStateException expected){}
        assertEquals(4,profile.stash.size());
        Item gear=profile.stash.get(0);profile.prepare(gear,true);profile.sell(gear);
        assertEquals(0,profile.gold);assertTrue(profile.prepared.contains(gear));
        Bag bag=new Bag();bag.items.add(new Food().quantity(3));profile.stash.add(bag);
        profile.sell(bag);assertEquals(30,profile.gold);assertFalse(profile.stash.contains(bag));
    }
    @Test public void permanentWeaponCapsAreThreeTimesEveryTier(){
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon[] weapons={
            new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword(),
            new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword(),
            new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword(),
            new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword(),
            new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword()};
        for(int i=0;i<weapons.length;i++){
            Item weapon=weapons[i];int cap=(i+1)*3;
            assertEquals(cap,WeaponUpgradeLimit.cap(weapon));weapon.upgrade(cap-1);
            assertTrue(WeaponUpgradeLimit.eligible(weapon));weapon.upgrade();
            assertFalse(WeaponUpgradeLimit.eligible(weapon));weapon.upgrade(10);
            assertEquals(cap,weapon.trueLevel());weapon.level(100);assertEquals(cap,weapon.trueLevel());
        }
    }
    @Test public void blockedWeaponUpgradeKeepsItsCurseAndHardeningAndInfusionCannotExceedCap(){
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword weapon=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword();
        weapon.level(15);weapon.cursed=true;weapon.enchantHardened=true;weapon.curseInfusionBonus=true;
        weapon.upgrade(true);assertTrue(weapon.cursed);assertTrue(weapon.enchantHardened);
        assertEquals(15,weapon.trueLevel());assertEquals(15,weapon.level());
        assertSame(weapon,new com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade().upgradeItem(weapon));
        assertSame(weapon,new com.shatteredpixel.shatteredpixeldungeon.items.spells.MagicalInfusion().upgradeItem(weapon));
    }
    @Test public void legacyOverCapWeaponSaveIsRestoredAtTierCap(){
        Item sword=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword().level(8);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();sword.storeInBundle(saved);saved.put("level",100);
        Item restored=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();restored.restoreFromBundle(saved);
        assertEquals(9,restored.trueLevel());assertFalse(WeaponUpgradeLimit.eligible(restored));
        com.watabou.utils.Bundle roundTrip=new com.watabou.utils.Bundle();restored.storeInBundle(roundTrip);
        Item again=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword();again.restoreFromBundle(roundTrip);assertEquals(9,again.trueLevel());
    }
    @Test public void boundWandAndThrownWeaponFollowTheirWeaponTierLimits(){
        Dungeon.hero=new Hero();
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff staff=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff();
        com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand wand=new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile();wand.level(20);
        staff.imbueWand(wand,null);assertEquals(3,staff.trueLevel());assertEquals(3,wand.trueLevel());
        staff.upgrade(10);assertEquals(3,staff.trueLevel());assertEquals(3,wand.trueLevel());
        Item trident=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.Trident();trident.level(100);
        assertEquals(15,trident.trueLevel());trident.upgrade();assertEquals(15,trident.trueLevel());
    }
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
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        Dungeon.depth=5;Dungeon.branch=0;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo boss=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo();boss.EXP=0;
        ExtractionUtility.defeated(h,boss);assertTrue(h.extractionBossDefeated);
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
    @Test public void raidExperienceGivesPointsWithoutChangingStats(){
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        h.earnExp(10,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class);
        assertEquals(2,h.lvl);assertEquals(10,profile.xp);assertEquals(10,h.extractionXP);
        assertEquals(0,h.talents.get(0).size());
        assertEquals(20,h.HT);assertEquals(10,h.attackSkill(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat()));
        assertEquals(5,h.defenseSkill(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat()));
        h.HP=7;h.earnExp(15,com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class);
        assertEquals(25,profile.xp);assertEquals(6,profile.points);assertEquals(7,h.HP);assertEquals(20,h.HT);
        assertEquals(Hero.STARTING_STR,h.STR);assertEquals(1,h.combatLevel());
        profile.settle(profile.raidID,false);assertEquals(25,profile.xp);assertEquals(6,profile.points);
        profile.learn(ExtractionGrowth.index("vital"));profile.learn(ExtractionGrowth.index("power"));
        profile.learn(ExtractionGrowth.index("combat_right_1"));profile.begin();
        Hero next=new Hero();next.extractionRaidID=profile.raidID;Dungeon.hero=next;profile.initialize(next);
        assertEquals(26,next.HT);assertEquals(11,next.attackSkill(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat()));
    }
    @Rule public TemporaryFolder folder = new TemporaryFolder();
    private ExtractionProfile profile;
    private void forgetProfile() throws Exception {
        Field field = ExtractionProfile.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }
    @Test public void emptyLoadoutGetsFreeSwordAgainAfterDeathAndPreparedWeaponWins() throws Exception {
        for(Item item:profile.stash)assertFalse(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor);
        profile.begin();Dungeon.hero=new Hero();profile.initialize(Dungeon.hero);
        assertTrue(Dungeon.hero.belongings.weapon instanceof BasicExpeditionSword);
        assertTrue(Dungeon.hero.belongings.armor instanceof BasicExpeditionArmor);
        assertEquals(3,Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone.class).quantity());
        assertEquals(0,ExtractionShop.salePrice(Dungeon.hero.belongings.armor));
        assertEquals(0,ExtractionShop.salePrice(Dungeon.hero.belongings.weapon));
        com.watabou.utils.Bundle copy=new com.watabou.utils.Bundle();copy.put("sword",Dungeon.hero.belongings.weapon);
        assertTrue(copy.get("sword") instanceof BasicExpeditionSword);
        profile.settle(profile.raidID,false);forgetProfile();profile=ExtractionProfile.get();
        profile.begin();Dungeon.hero=new Hero();profile.initialize(Dungeon.hero);
        assertTrue(Dungeon.hero.belongings.weapon instanceof BasicExpeditionSword);
        profile.settle(profile.raidID,true);assertEquals(5,profile.stash.size());
        Item chosen=profile.stash.get(0);profile.prepare(chosen,true);profile.begin();
        Hero next=new Hero();profile.initialize(next);assertEquals(chosen.getClass(),next.belongings.weapon.getClass());
        assertEquals(4,next.belongings.backpack.items.size());
        assertNotNull(next.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class));
    }
    @Test public void preparedSwordAndArmorDoNotGetDuplicateFallbackGearAndStonesSurviveSave(){
        Item sword=profile.stash.get(0);
        Item armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.LeatherArmor().identify(false).upgrade();
        profile.stash.add(armor);profile.prepare(sword,true);profile.prepare(armor,true);profile.begin();
        Hero h=new Hero();Dungeon.hero=h;h.extractionRaidID=profile.raidID;profile.initialize(h);
        assertEquals(sword.getClass(),h.belongings.weapon.getClass());assertEquals(armor.getClass(),h.belongings.armor.getClass());
        int armors=0;for(Item item:h.belongings){assertFalse(item instanceof BasicExpeditionSword);assertFalse(item instanceof BasicExpeditionArmor);if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)armors++;}
        assertEquals(1,armors);assertEquals(3,h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone.class).quantity());
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("hero",h);Hero restored=(Hero)saved.get("hero");
        assertEquals(armor.getClass(),restored.belongings.armor.getClass());assertEquals(3,restored.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone.class).quantity());
    }
    private void killForRecord(Hero h,Object cause){
        com.badlogic.gdx.Application previous=com.badlogic.gdx.Gdx.app;
        h.sprite=new EffectSprite();h.HP=0;
        com.badlogic.gdx.Gdx.app=(com.badlogic.gdx.Application)java.lang.reflect.Proxy.newProxyInstance(
            getClass().getClassLoader(),new Class[]{com.badlogic.gdx.Application.class},(proxy,method,args)->{
                if(method.getName().equals("postRunnable"))return null; // The headless test has no scene to switch to.
                return method.invoke(previous,args);
            });
        try{h.die(cause);}finally{com.badlogic.gdx.Gdx.app=previous;h.sprite=null;}
    }
    @Test public void expeditionDeathStoresNativeCauseAndEquipmentExactlyOnce() throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.Rankings ranks=com.shatteredpixel.shatteredpixeldungeon.Rankings.INSTANCE;
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();
        Hero h=Dungeon.hero;h.pos=Dungeon.level.entrance();profile.credit(profile.raidID,25);
        killForRecord(h,new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat());
        assertFalse(profile.active);assertEquals(25,profile.xp);assertEquals(1,ranks.records.size());
        Dungeon.fail(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class);assertEquals(1,ranks.records.size());
        ranks.records=null;ranks.load();assertEquals(1,ranks.records.size());
        com.shatteredpixel.shatteredpixeldungeon.Rankings.Record rec=ranks.records.get(0);
        assertEquals(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat.class,rec.cause);assertFalse(rec.win);assertEquals(1,rec.depth);
        Hero recorded=(Hero)rec.gameData.get("hero");assertTrue(recorded.belongings.weapon instanceof BasicExpeditionSword);assertTrue(recorded.belongings.armor instanceof BasicExpeditionArmor);
    }
    @Test public void viewingADeathRecordDoesNotReplaceTheSavedActiveExpedition() throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.Rankings ranks=com.shatteredpixel.shatteredpixeldungeon.Rankings.INSTANCE;
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();
        Dungeon.hero.pos=Dungeon.level.entrance();killForRecord(Dungeon.hero,new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger());
        assertEquals(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.class,ranks.records.get(0).cause);
        profile.begin();Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();Dungeon.hero.pos=Dungeon.level.entrance();Dungeon.hero.HP=13;
        int id=profile.raidID;Dungeon.saveGame(1);Dungeon.saveLevel(1);
        ranks.loadGameData(ranks.records.get(0));assertTrue(profile.active);assertEquals(id,profile.raidID);
        Dungeon.loadGame(1);Dungeon.level=Dungeon.loadLevel(1);
        assertEquals(id,Dungeon.hero.extractionRaidID);assertEquals(13,Dungeon.hero.HP);assertNotNull(Dungeon.hero.belongings.armor);
    }
    @Test public void foodSpawnsOnExpeditionFloorsAgain(){
        profile.begin();Dungeon.challenges=0;Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Dungeon.level=Dungeon.newLevel();
        int food=0;for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:Dungeon.level.heaps.values())for(Item item:heap.items)if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food)food++;
        assertTrue("Natural food should be present on floor one",food>0);
    }
    @Test public void potionKnowledgeIsSelectiveAndSurvivesNewColorsAndNativeSave() throws Exception {
        profile.learn(ExtractionGrowth.index("pack"));profile.learn(ExtractionGrowth.index("know_healing"));
        forgetProfile();profile=ExtractionProfile.get();
        for(int i=0;i<2;i++){
            profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
            assertTrue(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing().isKnown());
            assertTrue(new com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding().isKnown());
            assertFalse(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength().isKnown());
            com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();
            com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.save(saved);
            com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.clearColors();
            com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion.restore(saved);
            assertTrue(new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing().isKnown());
            profile.settle(profile.raidID,false);
        }
        profile.points=20;
        for(String id:ExtractionPotionKnowledge.IDS)profile.learn(ExtractionGrowth.index(id));
        profile.begin();Dungeon.initSeed();Dungeon.init();
        for(Class type:com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.POTION.classes)
            assertTrue(((com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion)com.watabou.utils.Reflection.newInstance(type)).isKnown());
    }
    @Test public void earnedExperienceLevelsPermanentProfileAfterDeathWithoutAutomaticStats() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Hero h=Dungeon.hero;int health=h.HT,strength=h.STR;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat target=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();
        int attack=h.attackSkill(target),defense=h.defenseSkill(target);
        h.earnExp(26,target.getClass());
        assertEquals(health,h.HT);assertEquals(strength,h.STR);
        assertEquals(attack,h.attackSkill(target));assertEquals(defense,h.defenseSkill(target));
        profile.settle(profile.raidID,false);forgetProfile();profile=ExtractionProfile.get();
        assertEquals(2,profile.growthLevel());assertEquals(1,profile.growthExperience());
        assertEquals(25,profile.growthExperienceRequired());assertEquals(6,profile.points);
        profile.begin();Hero next=new Hero();profile.initialize(next);assertEquals(20,next.HT);assertEquals(10,next.STR);
    }
    @Test public void waterskinIsReissuedAfterDeathAndPreparedDewSurvivesWithoutDuplicates() throws Exception {
        profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;profile.initialize(Dungeon.hero);
        com.shatteredpixel.shatteredpixeldungeon.items.Waterskin skin=Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class);
        assertNotNull(skin);assertTrue(skin.isEmpty());
        for(int i=0;i<5;i++)skin.collectDew(new com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop());
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("water",skin);
        assertEquals("5/20",((com.shatteredpixel.shatteredpixeldungeon.items.Waterskin)saved.get("water")).status());
        profile.settle(profile.raidID,true);forgetProfile();profile=ExtractionProfile.get();
        Item prepared=null;for(Item item:profile.stash)if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.Waterskin)prepared=item;assertNotNull(prepared);
        profile.prepare(prepared,true);profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;profile.initialize(Dungeon.hero);
        int count=0;for(Item i:Dungeon.hero.belongings)if(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.Waterskin)count++;
        assertEquals(1,count);assertEquals("5/20",Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class).status());
        profile.settle(profile.raidID,false);
        for(int i=0;i<profile.capacity();i++){Item item=new Item();profile.stash.add(item);profile.prepare(item,true);}
        profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;profile.initialize(Dungeon.hero);
        assertEquals(16,Dungeon.hero.belongings.backpack.items.size());
        assertTrue(Dungeon.hero.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Waterskin.class).isEmpty());
    }
    @Before public void setUp() throws Exception {
        Gdx.files = new HeadlessFiles();
        new Game(com.watabou.noosa.Scene.class,null);
        Gdx.app=(com.badlogic.gdx.Application)java.lang.reflect.Proxy.newProxyInstance(
                com.badlogic.gdx.Application.class.getClassLoader(),new Class[]{com.badlogic.gdx.Application.class},(proxy,method,args)->{
                    if(method.getName().equals("getType"))return com.badlogic.gdx.Application.ApplicationType.Desktop;
                    if(method.getName().equals("postRunnable")){((Runnable)args[0]).run();return null;}
                    if(method.getReturnType()==boolean.class)return false;
                    if(method.getReturnType()==int.class)return 0;
                    if(method.getReturnType()==long.class)return 0L;
                    return null;
                });
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
        com.shatteredpixel.shatteredpixeldungeon.Rankings ranks=com.shatteredpixel.shatteredpixeldungeon.Rankings.INSTANCE;
        ranks.records=null;ranks.totalNumber=ranks.wonNumber=ranks.localTotal=ranks.localWon=0;ranks.latestDaily=null;ranks.dailyScoreHistory.clear();
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
        assertEquals(3,profile.stash.size());
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
        assertEquals(9, profile.points);
        profile.credit(id+1, 1000);
        assertEquals(50, profile.xp);
    }
    @Test public void repeatedLaunchDoesNotConsumeAnotherLoadout() {
        profile.prepare(profile.stash.get(0), true);
        profile.begin();
        int id = profile.raidID;
        profile.begin();
        assertEquals(id, profile.raidID);
        assertEquals(3,profile.stash.size());
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
        assertEquals(5,profile.stash.size());
        assertEquals(117, profile.gold);
        assertEquals(10, profile.xp);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(5,profile.stash.size());
        assertEquals(1, ((Bag)profile.stash.get(4)).items.size());
    }
    @Test public void failedSaveRollsBackEquipmentMovement() throws Exception {
        Item item = profile.stash.get(0);
        java.io.File blocker = folder.newFile("not-a-directory");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute, blocker.getAbsolutePath()+"/");
        try { profile.prepare(item, true); fail("Saving must fail"); }
        catch (IllegalStateException expected) { }
        assertEquals(4,profile.stash.size());
        assertTrue(profile.prepared.isEmpty());
    }
    @Test public void inventoryRoundTripSurvivesReload() throws Exception {
        Item sword = profile.stash.get(0);
        profile.prepare(sword, true);
        profile.prepare(sword, false);
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(4,profile.stash.size());
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
        assertEquals(5, Dungeon.hero.belongings.backpack.items.size());
    }
    @Test public void returnAllPreservesEveryPreparedItemAfterReload() throws Exception {
        for (Item item : new java.util.ArrayList<>(profile.stash)) profile.prepare(item, true);
        profile.returnPrepared();
        forgetProfile();
        profile = ExtractionProfile.get();
        assertEquals(4,profile.stash.size());
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
    @Test public void distributedStrengthNodesReachT5RequirementsWithoutRingAndPotionsStayRaidOnly() throws Exception {
        profile.points=1000;learnPath("explore_cap");assertEquals(14,profile.startingStrength());
        for(String id:new String[]{"strength_early","strength_mid","strength_advanced","strength_master"}){
            int index=ExtractionGrowth.index(id);assertTrue(profile.unlocked(index));profile.learn(index);
        }
        assertEquals(20,profile.startingStrength());profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Hero hero=Dungeon.hero;assertEquals(20,hero.STR);assertTrue(hero.STR>=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greataxe().STRReq(0));assertNull(hero.belongings.ring);
        hero.sprite=new EffectSprite();hero.sprite.visible=false;new com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength().apply(hero);assertEquals(21,hero.STR);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();hero.storeInBundle(saved);Hero restored=new Hero();restored.restoreFromBundle(saved);assertEquals(21,restored.STR);
        profile.settle(profile.raidID,false);forgetProfile();profile=ExtractionProfile.get();assertEquals(20,profile.startingStrength());
        profile.begin();Hero next=new Hero();profile.initialize(next);assertEquals(20,next.STR);assertNull(next.belongings.ring);hero.sprite=null;
    }

    @Test public void allTwelveSubclassCoresAndTheirOriginalTalentsRequireNodes(){
        Hero h=new Hero();h.extractionRaidID=1;
        for(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass cls:com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.values()){
            if(cls==com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.NONE)continue;
            assertFalse(h.hasSubclass(cls));
            String id="subclass_"+cls.name().toLowerCase(java.util.Locale.ROOT);
            assertTrue(ExtractionGrowth.index(id)>=178);profile.nodes.add(id);assertTrue(h.hasSubclass(cls));
            java.util.ArrayList<java.util.LinkedHashMap<com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent,Integer>> original=new java.util.ArrayList<>();
            com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initSubclassTalents(cls,original);
            for(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent talent:original.get(2).keySet()){
                assertEquals(0,h.pointsInTalent(talent));
                String node="utility_"+talent.name().toLowerCase(java.util.Locale.ROOT)+"_3";
                assertTrue(ExtractionGrowth.index(node)>=178);profile.nodes.add(node);assertEquals(3,h.pointsInTalent(talent));
            }
        }
        assertEquals(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.NONE,h.subClass);
        assertTrue(h.hasWeaponAbilities());
    }
    @Test public void learnedCrossClassTalentsSurviveDeathAndProfileReload() throws Exception{
        profile.points=1000;learnPath("utility_thiefs_intuition_2");learnPath("utility_soul_eater_3");
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;profile.initialize(h);
        assertEquals(2,h.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.THIEFS_INTUITION));
        assertEquals(3,h.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SOUL_EATER));
        assertTrue(h.hasSubclass(com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass.WARLOCK));
        profile.settle(profile.raidID,false);forgetProfile();profile=ExtractionProfile.get();profile.begin();
        Hero next=new Hero();next.extractionRaidID=profile.raidID;profile.initialize(next);assertEquals(3,next.pointsInTalent(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.SOUL_EATER));assertEquals(20,next.HT);
    }
    @Test public void stealthUsesSavedEnergyAndActivatesNativeAssassinPreparation() throws Exception{
        profile.points=1000;learnPath("subclass_assassin");learnPath("utility_protective_shadows_2");profile.begin();
        Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();
        Hero h=Dungeon.hero;h.pos=Dungeon.level.entrance();h.sprite=new EffectSprite();h.ready=true;
        assertTrue(ExtractionClassSkills.stealth(h));assertEquals(80,h.extractionSkills.armor.charge,0.001f);
        assertNotNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility.class));
        assertNotNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Preparation.class));
        assertNotNull(h.buff(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.ProtectiveShadowsTracker.class));
        h.ready=true;h.extractionSkills.armor.charge=19;assertFalse(ExtractionClassSkills.stealth(h));assertEquals(19,h.extractionSkills.armor.charge,0.001f);h.sprite=null;
    }
    @Test public void activeSkillsSaveSeparateResourcesAndNeverRequireArmorOrArtifacts(){
        profile.nodes.add("skill_stealth");profile.nodes.add("skill_prayer");profile.nodes.add("utility_sunray_2");
        Hero h=new Hero();h.extractionRaidID=1;Dungeon.hero=h;ExtractionClassSkills.ensure(h);
        assertNull(h.belongings.armor);assertNull(h.belongings.artifact);assertEquals(3,h.extractionSkills.prayer.capacity());
        assertTrue(ExtractionClassSkills.spells(h).contains(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Sunray.INSTANCE));
        assertFalse(ExtractionClassSkills.spells(h).contains(com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.Smite.INSTANCE));
        h.extractionSkills.armor.charge=37;h.extractionSkills.prayer.spendCharge(1.5f);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("state",h.extractionSkills);
        ExtractionClassSkills.State restored=(ExtractionClassSkills.State)saved.get("state");
        assertEquals(37,restored.armor.charge,0.001f);assertEquals(1.5f,restored.prayer.remaining(),0.001f);
        h.extractionSkills=restored;h.lvl=30;ExtractionClassSkills.ensure(h);assertEquals(3,restored.prayer.capacity());assertEquals(1.5f,restored.prayer.remaining(),0.001f);
        for(int i=1;i<=4;i++)profile.nodes.add("prayer_capacity_"+i);ExtractionClassSkills.ensure(h);assertEquals(11,restored.prayer.capacity());assertEquals(1.5f,restored.prayer.remaining(),0.001f);
    }
    @Test public void subclassResourcesDoNotGrowAutomaticallyWithLevel(){
        profile.nodes.add("subclass_monk");profile.nodes.add("subclass_champion");
        Hero h=new Hero();h.extractionRaidID=1;Dungeon.hero=h;
        com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy monk=new com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy();
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.Charger weapons=new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon.Charger();
        h.lvl=1;assertEquals(10,monk.energyCap());assertEquals(4,weapons.chargeCap());
        h.lvl=30;assertEquals(10,monk.energyCap());assertEquals(4,weapons.chargeCap());
    }

    @Test public void resetRefundsAllNodeCostsAndPreservesProgressAndGearAfterReload() throws Exception {
        profile.points=1000;learnPath("utility_soul_eater_3");learnPath("strength");
        Item item=profile.stash.get(0);profile.prepare(item,true);profile.xp=77;
        int before=profile.points,used=profile.spentPoints(),gold=profile.gold,stash=profile.stash.size();
        assertEquals(1000,before+used);assertTrue(used>0);
        assertEquals(used,profile.resetNodes());assertEquals(300,profile.points);assertEquals(0,profile.spentPoints());assertTrue(profile.nodes.isEmpty());
        assertEquals(gold,profile.gold);assertEquals(77,profile.xp);assertEquals(stash,profile.stash.size());assertEquals(1,profile.prepared.size());assertEquals(10,profile.startingStrength());
        assertEquals(0,profile.resetNodes());assertEquals(300,profile.points);
        forgetProfile();profile=ExtractionProfile.get();assertEquals(300,profile.points);assertEquals(0,profile.spentPoints());assertEquals(77,profile.xp);assertEquals(1,profile.prepared.size());
    }
    @Test public void resetCannotChangeAnActiveRaidAndFailedSaveRollsBackTheRefund() throws Exception {
        profile.points=100;learnPath("utility_protective_shadows_2");int before=profile.points,used=profile.spentPoints();
        java.util.HashSet<String> learned=new java.util.HashSet<>(profile.nodes);profile.begin();
        try{profile.resetNodes();fail("Reset must be blocked during a raid");}catch(IllegalStateException expected){}
        assertEquals(before,profile.points);assertEquals(learned,profile.nodes);assertEquals(used,profile.spentPoints());
        profile.settle(profile.raidID,false);
        java.io.File blocker=folder.newFile("reset-blocker");FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.resetNodes();fail("Reset save must fail");}catch(IllegalStateException expected){}
        assertEquals(before,profile.points);assertEquals(learned,profile.nodes);assertEquals(used,profile.spentPoints());
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");forgetProfile();profile=ExtractionProfile.get();assertEquals(before,profile.points);assertEquals(learned,profile.nodes);
    }
    @Test public void resetReturnsOnlyOverflowPreparedItemsToTheStash(){
        profile.points=100;learnPath("porter");int cap=profile.capacity(),stored=profile.stash.size();assertTrue(cap>12);
        for(int i=0;i<cap;i++){Item item=new Item();profile.stash.add(item);profile.prepare(item,true);}
        profile.resetNodes();assertEquals(12,profile.prepared.size());assertEquals(stored+cap-12,profile.stash.size());
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);assertEquals(16,h.belongings.backpack.items.size());
    }
    @Test public void firstArcherNodeGivesTheNativeBowEveryRaidAndResetLocksItAgain(){
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);
        assertNull(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.class));profile.settle(profile.raidID,false);
        learnPath("ranged_0");profile.begin();h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow=h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.class);
        assertTrue(bow instanceof NodeSpiritBow);assertTrue(h.belongings.weapon instanceof BasicExpeditionSword);assertTrue(bow.actions(h).contains(com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.AC_SHOOT));assertEquals(0,bow.value());assertEquals(0,bow.energyVal());
        profile.settle(profile.raidID,false);profile.begin();Hero next=new Hero();next.extractionRaidID=profile.raidID;Dungeon.hero=next;profile.initialize(next);assertNotNull(next.belongings.getItem(NodeSpiritBow.class));
        profile.settle(profile.raidID,false);profile.resetNodes();assertFalse(bow.actions(h).contains(com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow.AC_SHOOT));
        for(ExtractionShop.Offer offer:ExtractionShop.OFFERS)assertFalse(offer.item() instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow);
    }
    @Test public void freeBowFitsAFullLoadoutAndPreparedBowNeverReplacesTheSword(){
        learnPath("ranged_0");for(int i=0;i<profile.capacity();i++){Item item=new Item();profile.stash.add(item);profile.prepare(item,true);}
        profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);assertEquals(17,h.belongings.backpack.items.size());
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent.initClassTalents(h);
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("hero",h);Hero restored=(Hero)saved.get("hero");assertNotNull(restored.belongings.getItem(NodeSpiritBow.class));
        Dungeon.gold=0;profile.settle(profile.raidID,true);for(Item item:profile.stash)assertFalse(item instanceof NodeSpiritBow);
        Item bow=new NodeSpiritBow().identify(false);profile.stash.add(bow);profile.prepare(bow,true);assertNull(profile.preparedWeapon());
        profile.begin();h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;profile.initialize(h);int bows=0;for(Item item:h.belongings)if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow)bows++;
        assertEquals(1,bows);assertTrue(h.belongings.weapon instanceof BasicExpeditionSword);
    }
    @Test public void graphPreserves178ExistingNodesAndAddsClassSkillsAndSubclasses() {
        assertEquals(622, ExtractionGrowth.NODES.length);
        java.util.HashSet<String> ids=new java.util.HashSet<>();
        int totalCost=0,convergences=0;
        for(int i=0;i<ExtractionGrowth.NODES.length;i++){
            ExtractionGrowth.Node node=ExtractionGrowth.NODES[i];
            assertTrue(ids.add(node.id));
            assertTrue(!node.effects.isEmpty()||node.utilityDescription!=null);
            for(int parent:node.parents)assertTrue("Primary paths must be acyclic", parent<i);
            if(node.parents.length==2)convergences++;
            if(i<178)totalCost+=node.cost;
        }
        assertEquals(18,convergences);
        assertEquals(422,totalCost);
        for(int b=0;b<19;b++)assertEquals(b==18?12:b==0?11:b==1||b==2?10:9,ExtractionGrowth.BRANCH_NODES[b].length);
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
    @Test public void allFiveChapterUnlocksRequireSuccessfulExtractionAndPersist() throws Exception {
        for(int c=2;c<=5;c++){
            try{profile.selectRaid(c);fail("Later chapters must start locked");}catch(IllegalStateException expected){}
        }
        for(int c=1;c<=5;c++){
            profile.selectRaid(c);profile.begin();int id=profile.raidID;
            Dungeon.hero=new Hero();Dungeon.gold=0;
            assertEquals(c,profile.raidChapter);assertEquals(ExtractionDifficulty.fixedStage(c),profile.raidDifficulty);
            if(c<5){profile.settle(id,false);assertEquals(0,profile.unlockedDifficulty[c]);profile.begin();id=profile.raidID;}
            profile.settle(id,true);int gold=profile.gold,xp=profile.xp;
            profile.settle(id,true);assertEquals(gold,profile.gold);assertEquals(xp,profile.xp);
            forgetProfile();profile=ExtractionProfile.get();
            for(int n=0;n<Math.min(5,c+1);n++)assertTrue(profile.unlockedDifficulty[n]>0);
        }
        assertTrue(profile.result.contains("5챕터 완주"));
        profile.selectRaid(5);profile.begin();assertEquals(5,profile.raidChapter);
    }
    @Test public void oldTwoChapterProfileMigratesWithoutUnlockingOrChangingAnActiveRaid() throws Exception {
        profile.debugEnabled=true;profile.debugUnlockPrison();profile.selectRaid(2);profile.begin();
        profile.raidRules=2;profile.raidDifficulty=6;profile.xp=78;profile.points=12;profile.nodes.add("ranged_0");
        java.lang.reflect.Method snapshot=ExtractionProfile.class.getDeclaredMethod("bundle");snapshot.setAccessible(true);
        com.watabou.utils.Bundle saved=(com.watabou.utils.Bundle)snapshot.invoke(profile);saved.put("difficulty_unlocks",new int[]{1,1});
        FileUtils.bundleToFile(ExtractionProfile.FILE,saved);forgetProfile();profile=ExtractionProfile.get();
        assertArrayEquals(new int[]{1,1,0,0,0},profile.unlockedDifficulty);
        assertTrue(profile.active);assertEquals(2,profile.raidRules);assertEquals(2,profile.raidChapter);assertEquals(6,profile.raidDifficulty);
        assertEquals(78,profile.xp);assertEquals(12,profile.points);assertTrue(profile.nodes.contains("ranged_0"));
        Dungeon.hero=new Hero();Dungeon.gold=0;profile.settle(profile.raidID,true);
        assertEquals(1,profile.unlockedDifficulty[2]);profile.selectRaid(3);profile.begin();assertEquals(4,profile.raidRules);
    }
    @Test public void laterChaptersGenerateAllOriginalFloorsAndResumeWithCappedLoot() throws Exception {
        profile.debugEnabled=true;profile.debugUnlockChapters();
        Class<?>[] normal={com.shatteredpixel.shatteredpixeldungeon.levels.CavesLevel.class,com.shatteredpixel.shatteredpixeldungeon.levels.CityLevel.class,com.shatteredpixel.shatteredpixeldungeon.levels.HallsLevel.class};
        Class<?>[] boss={com.shatteredpixel.shatteredpixeldungeon.levels.CavesBossLevel.class,com.shatteredpixel.shatteredpixeldungeon.levels.CityBossLevel.class,com.shatteredpixel.shatteredpixeldungeon.levels.HallsBossLevel.class};
        for(int c=3;c<=5;c++){
            profile.selectRaid(c);profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
            int start=ExtractionDifficulty.startDepth(c);assertEquals(start,Dungeon.depth);
            assertEquals(2*(c-1),Dungeon.LimitedDrops.STRENGTH_POTIONS.count);assertEquals(3*(c-1),Dungeon.LimitedDrops.UPGRADE_SCROLLS.count);
            if(c>=4)assertTrue(Dungeon.LimitedDrops.ENCH_STONE.dropped());
            for(int d=start;d<=start+4;d++){
                Dungeon.depth=d;Dungeon.level=Dungeon.newLevel();
                assertEquals(d==start+4?boss[c-3]:normal[c-3],Dungeon.level.getClass());
                assertNotNull(Dungeon.level.getTransition(com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_EXIT));
                for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob m:Dungeon.level.mobs)assertEquals(0,m.extractionElite);
                for(int n=0;n<10;n++){
                    com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon weapon=com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomWeapon();
                    assertTrue(WeaponUpgradeLimit.tier(weapon)<=ExtractionDifficulty.chapterMaxTier(c));
                    assertTrue(com.shatteredpixel.shatteredpixeldungeon.items.Generator.randomArmor().tier<=ExtractionDifficulty.chapterMaxTier(c));
                }
            }
            Dungeon.hero.pos=Dungeon.level.entrance();Dungeon.saveGame(1);Dungeon.saveLevel(1);Dungeon.hero=null;Dungeon.level=null;
            Dungeon.loadGame(1);Dungeon.level=Dungeon.loadLevel(1);
            assertEquals(start+4,Dungeon.depth);assertEquals(boss[c-3],Dungeon.level.getClass());assertEquals(profile.raidID,Dungeon.hero.extractionRaidID);
            profile.settle(profile.raidID,false);
        }
    }
    @Test public void onlyTheCurrentChapterMainBossOpensExtractionAndFloorTraitsReachTwentyFive(){
        profile.debugEnabled=true;profile.debugUnlockChapters();
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob[] bosses={new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DM300(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing(),new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa()};
        Dungeon.level=null;
        for(int c=1;c<=5;c++){
            profile.selectRaid(c);profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;Dungeon.branch=0;Dungeon.depth=ExtractionDifficulty.endDepth(c);
            for(int n=0;n<5;n++)if(n!=c-1){ExtractionUtility.defeated(h,bosses[n]);assertFalse(h.extractionBossDefeated);}
            Dungeon.depth--;ExtractionUtility.defeated(h,bosses[c-1]);assertFalse(h.extractionBossDefeated);Dungeon.depth++;
            ExtractionUtility.defeated(h,bosses[c-1]);assertTrue(h.extractionBossDefeated);
            for(int d=ExtractionDifficulty.startDepth(c);d<=Dungeon.depth;d++){assertTrue(ExtractionUtility.floor(h,d));assertFalse(ExtractionUtility.floor(h,d));}
            assertFalse(ExtractionUtility.floor(h,26));
            profile.settle(profile.raidID,false);
        }
    }
    @Test public void scaledLateBossPhasesKeepTheirNativeHealthAndShieldRatios(){
        Dungeon.challenges=com.shatteredpixel.shatteredpixeldungeon.Challenges.STRONGER_BOSSES;
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing king=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DwarfKing();
        king.HT=2936;assertEquals(1957,king.phaseHealth(300));assertEquals(979,king.phaseHealth(150));assertEquals(652,king.phaseHealth(100));
        int damage=(int)Math.ceil(king.HT/18f);assertTrue(king.HT-6*damage<=king.phaseHealth(300));assertTrue(king.HT-12*damage<=king.phaseHealth(150));assertTrue(18*damage>=king.HT);
        com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa yog=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.YogDzewa();yog.HT=7850;
        assertEquals(2355,yog.phaseHealth(300));assertEquals(785,yog.phaseHealth(100));assertEquals(3140,yog.phaseHealth(400));
        assertEquals(yog.phaseHealth(100),yog.HT-3*yog.phaseHealth(300));
    }
    @Test public void debugUnlocksAllChaptersButNewNormalRaidsNeverGenerateAffixElites() throws Exception {
        try{profile.debugUnlockChapters();fail("Debug must be enabled");}catch(IllegalStateException expected){}
        profile.setDebugEnabled(true);profile.debugUnlockChapters();forgetProfile();profile=ExtractionProfile.get();assertArrayEquals(new int[]{1,1,1,1,1},profile.unlockedDifficulty);
        profile.selectRaid(5);profile.begin();Hero h=new Hero();h.extractionRaidID=profile.raidID;Dungeon.hero=h;
        for(int n=0;n<100;n++){
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();ExtractionDifficulty.prepare(rat);
            assertTrue(rat.extractionScaled);assertEquals(0,rat.extractionElite);
        }
    }

    @Test public void growthStopsAt100AndCreditsStayIdempotentWithoutChangingStats() throws Exception {
        profile.begin();Dungeon.hero=new Hero();profile.initialize(Dungeon.hero);
        int ht=Dungeon.hero.HT,str=Dungeon.hero.STR;
        profile.credit(profile.raidID,2474);assertEquals(99,profile.growthLevel());assertEquals(24,profile.growthExperience());assertEquals(297,profile.points);
        profile.credit(profile.raidID,Integer.MAX_VALUE);assertEquals(100,profile.growthLevel());assertEquals(2475,profile.xp);assertEquals(300,profile.points);assertEquals(0,profile.growthExperienceRequired());
        profile.credit(profile.raidID,Integer.MAX_VALUE);Dungeon.gold=0;profile.settle(profile.raidID,true);
        assertEquals(2475,profile.xp);assertEquals(300,profile.points);assertEquals(ht,Dungeon.hero.HT);assertEquals(str,Dungeon.hero.STR);
        forgetProfile();profile=ExtractionProfile.get();profile.debugEnabled=true;profile.debugResources(0,Integer.MAX_VALUE,Integer.MAX_VALUE);
        assertEquals(100,profile.growthLevel());assertEquals(300,profile.points);
    }
    @Test public void presetsSaveSwitchAndReloadUsingOneBudget() throws Exception {
        profile.points=30;learnPath("strength");profile.savePreset(0);int cost=profile.spentPoints();
        java.util.HashSet<String> original=new java.util.HashSet<>(profile.nodes);
        profile.resetNodes();learnPath("ranged_0");profile.savePreset(1);profile.resetNodes();profile.savePreset(2);
        profile.applyPreset(0);assertEquals(original,profile.nodes);assertEquals(30-cost,profile.points);
        for(int n=0;n<10;n++){profile.applyPreset(1);profile.applyPreset(0);}assertEquals(30-cost,profile.points);
        forgetProfile();profile=ExtractionProfile.get();assertEquals(cost,profile.presetCost(0));assertTrue(profile.hasPreset(2));
        profile.applyPreset(2);assertTrue(profile.nodes.isEmpty());assertEquals(30,profile.points);
        profile.applyPreset(1);assertTrue(profile.nodes.contains("ranged_0"));
    }
    @Test public void presetsRejectInsufficientPointsAndActiveRaidsAndRollBackFailedWrites() throws Exception {
        profile.points=30;learnPath("strength");profile.savePreset(0);profile.resetNodes();profile.savePreset(1);profile.points=0;
        try{profile.applyPreset(0);fail("Insufficient points");}catch(IllegalStateException expected){}assertTrue(profile.nodes.isEmpty());assertEquals(0,profile.points);
        profile.points=30;profile.begin();
        try{profile.applyPreset(0);fail("Active raid");}catch(IllegalStateException expected){}
        try{profile.savePreset(1);fail("Active raid");}catch(IllegalStateException expected){}
        Dungeon.hero=new Hero();profile.settle(profile.raidID,false);profile.applyPreset(0);
        java.util.HashSet<String> before=new java.util.HashSet<>(profile.nodes);int points=profile.points;
        java.io.File blocker=folder.newFile("preset-blocker");FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.applyPreset(1);fail("Save must fail");}catch(IllegalStateException expected){}assertEquals(before,profile.nodes);assertEquals(points,profile.points);
        try{profile.savePreset(1);fail("Save must fail");}catch(IllegalStateException expected){}assertEquals(0,profile.presetCost(1));
    }
    @Test public void presetsValidateConnectionsAndReturnCapacityOverflowToStash() throws Exception {
        profile.points=100;learnPath("porter");int capacity=profile.capacity();
        profile.resetNodes();profile.savePreset(0);learnPath("porter");
        for(int i=0;i<capacity;i++)profile.prepared.add(new Food());int stash=profile.stash.size();
        profile.applyPreset(0);assertEquals(12,profile.prepared.size());assertEquals(stash+capacity-12,profile.stash.size());
        profile.nodes.add("sword_8");profile.savePreset(1);profile.nodes.clear();
        try{profile.applyPreset(1);fail("Invalid prerequisites");}catch(IllegalStateException expected){}assertTrue(profile.nodes.isEmpty());
    }
    @Test public void legacyOverBudgetGrowthRefundsAtHubAndPreservesRunningHeroUntilSettlement() throws Exception {
        java.lang.reflect.Method snapshot=ExtractionProfile.class.getDeclaredMethod("bundle");snapshot.setAccessible(true);
        for(ExtractionGrowth.Node node:ExtractionGrowth.NODES)profile.nodes.add(node.id);profile.xp=100000;profile.points=1000;
        com.watabou.utils.Bundle saved=(com.watabou.utils.Bundle)snapshot.invoke(profile);saved.put("active",true);saved.put("raid",7);
        FileUtils.bundleToFile(ExtractionProfile.FILE,saved);forgetProfile();profile=ExtractionProfile.get();
        assertTrue(profile.active);assertEquals(622,profile.nodes.size());assertEquals(100,profile.growthLevel());assertEquals(0,profile.points);
        profile.abandon();assertTrue(profile.nodes.isEmpty());assertEquals(300,profile.points);assertEquals(2475,profile.xp);
        forgetProfile();profile=ExtractionProfile.get();assertEquals(300,profile.points);assertTrue(profile.result.contains("300 P"));
    }
    @Test public void oldOnePointLevelsReceiveBackpayOnceAndFutureLevelsGrantThree() throws Exception {
        learnPath("porter");profile.xp=250;profile.points=11;
        Field rate=ExtractionProfile.class.getDeclaredField("growthPointRate");rate.setAccessible(true);rate.setInt(profile,1);
        java.lang.reflect.Method snapshot=ExtractionProfile.class.getDeclaredMethod("bundle");snapshot.setAccessible(true);
        FileUtils.bundleToFile(ExtractionProfile.FILE,(com.watabou.utils.Bundle)snapshot.invoke(profile));
        forgetProfile();profile=ExtractionProfile.get();assertEquals(11,profile.growthLevel());assertEquals(31,profile.points);assertEquals(2,profile.spentPoints());
        forgetProfile();profile=ExtractionProfile.get();assertEquals(31,profile.points);
        profile.begin();profile.credit(profile.raidID,25);assertEquals(12,profile.growthLevel());assertEquals(34,profile.points);
        profile.credit(profile.raidID,25);assertEquals(34,profile.points);
    }
    @Test public void ringsStopAt15AndLegacyOverCapSavesClampWithoutChangingWeapons() throws Exception {
        com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight();
        ring.level(100);assertEquals(15,ring.trueLevel());ring.upgrade(100);assertEquals(15,ring.trueLevel());assertFalse(WeaponUpgradeLimit.eligible(ring));
        ring.level(14);assertTrue(WeaponUpgradeLimit.eligible(ring));ring.upgrade();assertEquals(15,ring.trueLevel());
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();ring.storeInBundle(saved);saved.put("level",50);
        com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring restored=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfMight();restored.restoreFromBundle(saved);assertEquals(15,restored.trueLevel());
        assertEquals(3,WeaponUpgradeLimit.cap(new BasicExpeditionSword()));assertEquals(15,WeaponUpgradeLimit.cap(new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword()));
    }
    @Test public void everyOriginalArtifactKeepsNativeEffectsAndRestoresIts15RankAndLegacyStages() throws Exception {
        Class<?>[] types={com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TimekeepersHourglass.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.EtherealChains.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CloakOfShadows.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.ChaliceOfBlood.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.UnstableSpellbook.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.MasterThievesArmband.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.TalismanOfForesight.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.AlchemistsToolkit.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.LloydsBeacon.class,
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey.class,com.shatteredpixel.shatteredpixeldungeon.items.artifacts.CapeOfThorns.class};
        for(Class<?> type:types){
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact a=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)com.watabou.utils.Reflection.newInstance(type);a.identify(false);a.upgrade(100);
            assertEquals(type.getName(),15,a.trueLevel());assertEquals(15,a.visiblyUpgraded());assertEquals(a.nativeLevelCap(),a.level());assertFalse(WeaponUpgradeLimit.canIncrease(a));
            String status=a.status();a.upgrade();assertEquals(status,a.status());assertEquals(15,a.trueLevel());
            com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();a.storeInBundle(saved);
            com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact copy=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)com.watabou.utils.Reflection.newInstance(type);copy.restoreFromBundle(saved);
            assertEquals(15,copy.trueLevel());assertEquals(a.level(),copy.level());assertEquals(status,copy.status());assertEquals(15,saved.getInt("level"));
            saved.put("artifact_rank_15",false);saved.put("level",a.nativeLevelCap());copy=(com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)com.watabou.utils.Reflection.newInstance(type);copy.restoreFromBundle(saved);
            assertEquals(15,copy.trueLevel());assertEquals(a.nativeLevelCap(),copy.level());
        }
    }
    @Test public void actualFallTransitionKeepsTheFloorRaidAndSavedState() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        java.lang.reflect.Method fall=com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.class.getDeclaredMethod("fall");fall.setAccessible(true);
        for(int depth:new int[]{1,2,5}) {
            Dungeon.depth=depth;Dungeon.level=Dungeon.newLevel();Dungeon.hero.pos=Dungeon.level.entrance();
            int origin=Dungeon.hero.pos,id=Dungeon.hero.extractionRaidID,hp=Dungeon.hero.HP;
            com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.fallIntoPit=true;
            fall.invoke(new com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene());
            assertEquals(depth,Dungeon.depth);assertEquals(id,Dungeon.hero.extractionRaidID);
            assertNotEquals(origin,Dungeon.hero.pos);assertTrue(Dungeon.level.passable[Dungeon.hero.pos]);assertFalse(Dungeon.level.pit[Dungeon.hero.pos]);
            assertEquals(hp,Dungeon.hero.HP);assertNotNull(Dungeon.hero.buff(com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm.Falling.class));
            assertFalse(Dungeon.levelHasBeenGenerated(depth+1,0));
            Dungeon.hero=null;Dungeon.level=null;Dungeon.loadGame(1);Dungeon.switchLevel(Dungeon.loadLevel(1),-1);
            assertEquals(depth,Dungeon.depth);assertEquals(id,Dungeon.hero.extractionRaidID);
        }
    }
    @Test public void landingCannotCrossLockedDoorsOrUseTrapsOrOccupiedCells() {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel level=(com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)Dungeon.newLevel();
        level.rooms().clear();level.mobs.clear();level.traps.clear();level.setSize(9,7);
        java.util.Arrays.fill(level.map,com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.WALL);
        for(int x=1;x<=7;x++)level.map[3*9+x]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.EMPTY;
        int origin=29;level.map[origin]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.CHASM;
        level.map[31]=com.shatteredpixel.shatteredpixeldungeon.levels.Terrain.LOCKED_DOOR;level.buildFlagMaps();
        level.traps.put(28,new com.shatteredpixel.shatteredpixeldungeon.levels.traps.PitfallTrap());
        for(int n=0;n<20;n++)assertEquals(30,ExtractionFalls.landingCell(level,origin));
        level.locked=true;assertEquals(30,ExtractionFalls.landingCell(level,origin));
        level.traps.clear();com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();rat.pos=28;level.mobs.add(rat);
        assertEquals(30,ExtractionFalls.landingCell(level,origin));
    }
    @Test public void pitRoomKeySpawnsOutsideAndOldKeyMovesOnlyOnce() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        Field pit=com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.SpecialRoom.class.getDeclaredField("pitNeededDepth");pit.setAccessible(true);pit.setInt(null,2);
        Dungeon.depth=2;Dungeon.level=Dungeon.newLevel();
        com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel level=(com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)Dungeon.level;
        com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom room=null;
        for(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.Room r:level.rooms())if(r instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom)room=(com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom)r;
        assertNotNull(room);Item key=null;com.shatteredpixel.shatteredpixeldungeon.items.Heap source=null;int count=0;
        for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:level.heaps.valueList())for(Item item:heap.items)if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey){
            assertFalse(level.room(heap.pos) instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom);count+=item.quantity();key=item;source=heap;
        }
        assertEquals(1,count);source.items.remove(key);if(source.items.isEmpty())level.heaps.remove(source.pos);
        int inside=level.pointToCell(room.center());level.drop(key,inside); // Old map fixture.
        com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("level",level);
        level=(com.shatteredpixel.shatteredpixeldungeon.levels.RegularLevel)saved.get("level");
        ExtractionFalls.movePitKeys(level);ExtractionFalls.movePitKeys(level);count=0;
        for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:level.heaps.valueList())for(Item item:heap.items)if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey){
            assertFalse(level.room(heap.pos) instanceof com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special.PitRoom);count+=item.quantity();
        }
        assertEquals(1,count);
    }
    @Test public void newChapterBalanceRisesSharplyAndOldRaidsKeepTheirMultipliers() {
        profile.debugEnabled=true;profile.debugUnlockChapters();
        float previousHP=0,previousDamage=0;
        for(int chapter=1;chapter<=5;chapter++) {
            profile.selectRaid(chapter);profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;profile.initialize(Dungeon.hero);
            float health=ExtractionDifficulty.raidHealthMultiplier(),damage=ExtractionDifficulty.raidDamageMultiplier();
            assertTrue(health>previousHP);assertTrue(damage>previousDamage);
            if(chapter>=3)assertTrue(health>=previousHP*1.4f);
            com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat rat=new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat();int hp=rat.HT;
            ExtractionDifficulty.prepare(rat);assertEquals(Math.round(hp*health),rat.HT);assertEquals(Math.round(10*damage),ExtractionDifficulty.incoming(10,rat));
            profile.raidRules=3;
            assertEquals(ExtractionDifficulty.healthMultiplier(chapter,profile.raidDifficulty),ExtractionDifficulty.raidHealthMultiplier(),0);
            assertEquals(ExtractionDifficulty.damageMultiplier(profile.raidDifficulty),ExtractionDifficulty.raidDamageMultiplier(),0);
            profile.settle(profile.raidID,false);previousHP=health;previousDamage=damage;
        }
    }

    @Test public void tenClothingItemsKeepTheirTierImageUpgradeAndIdentityOnSave() {
        java.util.HashSet<Integer> images=new java.util.HashSet<>();
        for(int t=1;t<=5;t++)for(boolean boots:new boolean[]{false,true}){
            ExpeditionClothing item=ExpeditionClothing.create(boots,t);assertTrue(images.add(item.image));
            assertEquals(8+2*t,item.STRReq());assertEquals(t*3,WeaponUpgradeLimit.cap(item));
            item.upgrade(100);assertEquals(t*3,item.trueLevel());item.level(100);assertEquals(t*3,item.trueLevel());
            item.identify(false);com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("item",item);
            ExpeditionClothing copy=(ExpeditionClothing)saved.get("item");
            assertEquals(item.getClass(),copy.getClass());assertEquals(item.name(),copy.name());assertEquals(t,copy.tier);assertEquals(item.image,copy.image);assertEquals(item.trueLevel(),copy.trueLevel());
        }
        assertEquals(10,images.size());
    }
    @Test public void allFiveBodyTiersCanBeBoughtWhileHighWeaponsRemainRestricted() throws Exception {
        profile.gold=10000;int armors=0,pants=0,boots=0;
        for(int n=0;n<ExtractionShop.OFFERS.size();n++){
            ExtractionShop.Offer offer=ExtractionShop.OFFERS.get(n);Item item=offer.item();
            if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor||item instanceof ExpeditionClothing){
                assertTrue(offer.available());int size=profile.stash.size();profile.buy(n);assertEquals(size+1,profile.stash.size());
                if(item instanceof ExpeditionClothing){if(((ExpeditionClothing)item).boots())boots++;else pants++;}else armors++;
            }else if(WeaponUpgradeLimit.tier(item)>2)assertFalse(offer.available());
        }
        assertEquals(5,armors);assertEquals(5,pants);assertEquals(5,boots);
        forgetProfile();profile=ExtractionProfile.get();assertEquals(19,profile.stash.size());
    }
    @Test public void weakHeroCanEquipBothSlotsSwapAndUncurseThem() throws Exception {
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();
        Hero hero=Dungeon.hero;hero.pos=Dungeon.level.entrance();
        ExpeditionClothing pants=new ExpeditionClothing.PlatePants(),boots=new ExpeditionClothing.PlateBoots();
        hero.belongings.backpack.items.add(pants);hero.belongings.backpack.items.add(boots);
        assertTrue(pants.doEquip(hero));assertTrue(boots.doEquip(hero));assertTrue(pants.isEquipped(hero));assertTrue(boots.isEquipped(hero));
        assertEquals(8,pants.missingStrength(hero));assertEquals(8,boots.missingStrength(hero));assertTrue(boots.movementFactor(hero)<1);assertTrue(pants.evasionFactor(hero)<1);
        pants.cursed=true;ExpeditionClothing replacement=new ExpeditionClothing.ClothPants();hero.belongings.backpack.items.add(replacement);
        assertFalse(replacement.doEquip(hero));assertSame(pants,hero.belongings.pants);assertTrue(hero.belongings.backpack.contains(replacement));
        hero.belongings.uncurseEquipped();assertFalse(pants.cursed);assertTrue(replacement.doEquip(hero));assertSame(replacement,hero.belongings.pants);assertTrue(hero.belongings.backpack.contains(pants));
        assertTrue(boots.doUnequip(hero,true));assertNull(hero.belongings.boots);assertTrue(hero.belongings.backpack.contains(boots));
    }
    @Test public void preparedClothesEquipIndependentlyResumeAndReturnToStash() throws Exception {
        ExpeditionClothing first=new ExpeditionClothing.ClothPants(),chosen=new ExpeditionClothing.PlatePants(),boots=new ExpeditionClothing.LeatherBoots();
        for(Item item:new Item[]{first,chosen,boots}){profile.stash.add(item);profile.prepare(item,true);}
        profile.selectEquipment(chosen);assertSame(chosen,profile.preparedPants());assertSame(boots,profile.preparedBoots());
        profile.begin();Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();Dungeon.level=Dungeon.newLevel();Dungeon.hero.pos=Dungeon.level.entrance();
        assertTrue(Dungeon.hero.belongings.pants instanceof ExpeditionClothing.PlatePants);assertTrue(Dungeon.hero.belongings.boots instanceof ExpeditionClothing.LeatherBoots);
        assertNotNull(Dungeon.hero.belongings.getItem(ExpeditionClothing.ClothPants.class));
        Dungeon.hero.belongings.pants.upgrade();Dungeon.saveAll();Dungeon.hero=null;Dungeon.level=null;Dungeon.loadGame(1);Dungeon.switchLevel(Dungeon.loadLevel(1),-1);
        assertEquals(1,Dungeon.hero.belongings.pants.trueLevel());assertEquals(2,Dungeon.hero.belongings.boots.tier);
        Dungeon.gold=0;profile.settle(profile.raidID,true);forgetProfile();profile=ExtractionProfile.get();
        int count=0;for(Item item:profile.stash)if(item instanceof ExpeditionClothing)count++;assertEquals(3,count);
    }
    @Test public void bodyArmorIsRedistributedAndStrengthBurdenIsShared() {
        Hero hero=new Hero();hero.extractionRaidID=1;hero.STR=30;Dungeon.hero=hero;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.MailArmor();armor.level(6);
        ExpeditionClothing pants=new ExpeditionClothing.MailPants(),boots=new ExpeditionClothing.MailBoots();pants.level(6);boots.level(6);
        hero.belongings.armor=armor;hero.belongings.pants=pants;hero.belongings.boots=boots;
        float average=0;for(int i=0;i<4000;i++){int defense=ExpeditionClothing.bodyDefense(hero);assertTrue(defense>=armor.DRMin());assertTrue(defense<=armor.DRMax());average+=defense;}
        assertEquals((armor.DRMin()+armor.DRMax())/2f,average/4000,.4f);
        hero.belongings.pants=null;hero.belongings.boots=null;average=0;for(int i=0;i<4000;i++)average+=ExpeditionClothing.bodyDefense(hero);
        assertEquals((armor.DRMin()+armor.DRMax())/4f,average/4000,.4f);
        hero.belongings.pants=pants;hero.belongings.boots=boots;hero.STR=10;int deficit=armor.STRReq()-hero.STR();
        assertEquals((float)Math.pow(1.2,-deficit),armor.speedFactor(hero,1)*pants.movementFactor(hero)*boots.movementFactor(hero)/boots.speedBonus(),.00001f);
        assertEquals((float)Math.pow(1.5,-deficit),armor.evasionFactor(hero,1)*pants.evasionFactor(hero)*boots.evasionFactor(hero),.00001f);
        hero.STR=30;assertEquals(1,pants.movementFactor(hero),0);assertEquals(1.06f,boots.movementFactor(hero),.00001f);assertEquals(1,boots.evasionFactor(hero),0);
    }
    @Test public void clothingLootRespectsEveryChapterTierAndIterationCanRemoveSlots() {
        profile.debugEnabled=true;profile.debugUnlockChapters();
        for(int chapter=1;chapter<=5;chapter++){
            profile.selectRaid(chapter);profile.begin();Dungeon.hero=new Hero();Dungeon.hero.extractionRaidID=profile.raidID;
            int pants=0,boots=0;
            for(int i=0;i<100;i++){
                Item item=com.shatteredpixel.shatteredpixeldungeon.items.Generator.random(com.shatteredpixel.shatteredpixeldungeon.items.Generator.Category.ARMOR);
                int tier=item instanceof ExpeditionClothing?((ExpeditionClothing)item).tier:((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).tier;
                assertTrue(tier<=ExtractionDifficulty.chapterMaxTier(chapter));
                if(item instanceof ExpeditionClothing){if(((ExpeditionClothing)item).boots())boots++;else pants++;}
            }
            assertTrue(pants>0);assertTrue(boots>0);profile.settle(profile.raidID,false);
        }
        Hero hero=new Hero();hero.belongings.pants=new ExpeditionClothing.ClothPants();hero.belongings.boots=new ExpeditionClothing.ClothShoes();hero.belongings.backpack.items.add(new Food());
        java.util.Iterator<Item> items=hero.belongings.iterator();while(items.hasNext()){items.next();items.remove();}
        assertNull(hero.belongings.pants);assertNull(hero.belongings.boots);assertTrue(hero.belongings.backpack.items.isEmpty());
    }

    public static class CountingGlyph extends com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph {
        static int calls,lastLevel;
        @Override public int proc(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor armor,com.shatteredpixel.shatteredpixeldungeon.actors.Char attacker,com.shatteredpixel.shatteredpixeldungeon.actors.Char defender,int damage){calls++;lastLevel=armor.buffedLvl();return damage;}
        @Override public com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing glowing(){return null;}
    }
    @Test public void everyNativeGlyphAndCurseCanBeInscribedSavedAndRemovedOnClothing() {
        Hero hero=new Hero();Dungeon.hero=hero;hero.extractionRaidID=1;
        Class<?>[][] groups={com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.common,com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.uncommon,com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.rare,com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph.curses};
        for(Class<?>[] group:groups)for(Class<?> type:group)for(boolean boots:new boolean[]{false,true}){
            ExpeditionClothing item=ExpeditionClothing.create(boots,5);item.level(3);item.identify(false);
            com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph glyph=(com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph)com.watabou.utils.Reflection.newInstance(type);
            assertTrue(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment.enchantable(item));
            com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfEnchantment.inscribe(item,glyph);
            hero.belongings.pants=boots?null:item;hero.belongings.boots=boots?item:null;
            assertEquals(3,hero.glyphLevel(glyph.getClass()));
            com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();saved.put("item",item);ExpeditionClothing copy=(ExpeditionClothing)saved.get("item");
            assertEquals(type,copy.glyph.getClass());assertEquals(item.name(),copy.name());
            if(glyph.curse()){assertTrue(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse.uncursable(item));assertTrue(com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse.uncurse(null,item));assertNull(item.glyph);}
        }
    }
    @Test public void strongestDuplicateGlyphProcsOnceAndDistinctPassiveGlyphsWorkWithoutChest() {
        Hero hero=new Hero();Dungeon.hero=hero;hero.extractionRaidID=1;hero.STR=30;
        hero.belongings.armor=new com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor();hero.belongings.armor.glyph=new CountingGlyph();hero.belongings.armor.level(1);
        ExpeditionClothing pants=new ExpeditionClothing.PlatePants(),boots=new ExpeditionClothing.PlateBoots();pants.level(2);boots.level(4);pants.inscribe(new CountingGlyph());boots.inscribe(new CountingGlyph());hero.belongings.pants=pants;hero.belongings.boots=boots;
        CountingGlyph.calls=0;CountingGlyph.lastLevel=-1;hero.defenseProc(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat(),10);assertEquals(1,CountingGlyph.calls);assertEquals(4,CountingGlyph.lastLevel);
        hero.belongings.armor=null;pants.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation());boots.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone());
        assertEquals(2,hero.glyphLevel(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation.class));assertEquals(4,hero.glyphLevel(com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone.class));assertTrue(hero.isImmune(com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning.class));
        boots.inscribe(new com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone());assertEquals(0,hero.defenseSkill(new com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Rat()));
        for(int tier=1;tier<=5;tier++)assertEquals(1+tier*.02f,ExpeditionClothing.create(true,tier).movementFactor(hero),.00001f);
    }

}
