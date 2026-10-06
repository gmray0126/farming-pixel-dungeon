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
    @Test public void chapterAndDifficultyUnlocksPersistAndRaidSelectionIsFrozen() throws Exception {
        try{profile.selectRaid(2,1);fail("Prison must be locked");}catch(IllegalStateException expected){}
        try{profile.selectRaid(1,2);fail("Difficulty must be locked");}catch(IllegalStateException expected){}
        profile.begin();Dungeon.hero=new Hero();Dungeon.gold=0;profile.settle(profile.raidID,true);
        assertEquals(2,profile.unlockedDifficulty[0]);assertEquals(1,profile.unlockedDifficulty[1]);
        profile.selectRaid(2,1);profile.begin();int id=profile.raidID;
        try{profile.selectRaid(1,1);fail("Active selection must be frozen");}catch(IllegalStateException expected){}
        forgetProfile();profile=ExtractionProfile.get();assertEquals(2,profile.raidChapter);assertEquals(1,profile.raidDifficulty);
        profile.settle(id,false);assertEquals(1,profile.unlockedDifficulty[1]);
        profile.selectRaid(1,2);profile.begin();profile.settle(profile.raidID,true);assertEquals(3,profile.unlockedDifficulty[0]);
    }
    @Test public void selectedPrisonStartsAtSixAndGeneratesScaledOriginalPrison(){
        profile.unlockedDifficulty[1]=1;profile.selectRaid(2,1);profile.begin();
        Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();
        assertEquals(6,Dungeon.depth);assertEquals(profile.raidID,Dungeon.hero.extractionRaidID);
        com.shatteredpixel.shatteredpixeldungeon.levels.Level level=Dungeon.newLevel();
        assertTrue(level instanceof com.shatteredpixel.shatteredpixeldungeon.levels.PrisonLevel);
        int scaled=0;for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:level.mobs)if(mob.extractionScaled)scaled++;
        assertTrue(scaled>0);assertEquals(20,Dungeon.hero.HT);
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
        for(int i=0;i<200;i++)lantern.gainKill();assertEquals(5,lantern.level());lantern.upgrade(100);assertEquals(5,lantern.level());
        lantern.level(100);assertEquals(5,lantern.level());
        lantern.identify(false);com.watabou.utils.Bundle saved=new com.watabou.utils.Bundle();lantern.storeInBundle(saved);
        ExpeditionArtifacts.BloodLantern copy=new ExpeditionArtifacts.BloodLantern();copy.restoreFromBundle(saved);
        assertEquals(5,copy.level());assertEquals(lantern.status(),copy.status());
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
        assertEquals(6,profile.stash.size());Bag saved=(Bag)((Bag)profile.stash.get(5)).items.get(0);
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
        assertEquals(397,profile.gold);assertEquals(6,profile.stash.size());assertTrue(profile.result.contains("6장"));
        assertEquals(1,((Bag)profile.stash.get(5)).items.size());
        assertEquals(3,Dungeon.hero.belongings.backpack.items.size()); // live run bag remains intact
        assertEquals(2,bag.items.size());
        forgetProfile();profile=ExtractionProfile.get();assertEquals(397,profile.gold);
        assertEquals(6,profile.stash.size());
    }
    @Test public void failedRedemptionKeepsRunScrollsAndDeathPaysNothing() throws Exception {
        profile.begin();int id=profile.raidID;Dungeon.hero=new Hero();Dungeon.gold=0;
        Item scroll=new ExtractionShop.SupplyUpgrade().quantity(2);Dungeon.hero.belongings.backpack.items.add(scroll);
        java.io.File blocker=folder.newFile("settlement-blocker");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.settle(id,true);fail("Saving must fail");}catch(IllegalStateException expected){}
        assertTrue(profile.active);assertEquals(100,profile.gold);assertEquals(5,profile.stash.size());
        assertSame(scroll,Dungeon.hero.belongings.backpack.items.get(0));assertEquals(2,scroll.quantity());
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        profile.settle(id,false);assertEquals(100,profile.gold);assertFalse(profile.active);
    }
    @Test public void shopPurchasesStackSalesAndSupplyIdentitySurviveReload() throws Exception {
        for(ExtractionShop.Offer offer:ExtractionShop.OFFERS){
            Item item=offer.item();assertTrue(item.isIdentified());assertTrue(offer.price>ExtractionShop.salePrice(item));
        }
        profile.gold=1000;profile.buy(4);profile.buy(1);
        assertEquals(730,profile.gold);Item food=profile.stash.get(6);food.quantity(3);
        profile.sell(food,false);assertEquals(740,profile.gold);assertEquals(2,food.quantity());
        profile.sell(food,true);assertEquals(760,profile.gold);assertFalse(profile.stash.contains(food));
        forgetProfile();profile=ExtractionProfile.get();assertEquals(760,profile.gold);
        assertTrue(profile.stash.get(5) instanceof ExtractionShop.SupplyUpgrade);
        assertTrue(((ExtractionShop.SupplyUpgrade)profile.stash.get(5)).isKnown());
        assertEquals("보급 강화 스크롤",profile.stash.get(5).name());
        profile.begin();try{profile.buy(0);fail("Active raids block purchases");}catch(IllegalStateException expected){}
        try{profile.sell(profile.stash.get(0));fail("Active raids block sales");}catch(IllegalStateException expected){}
    }
    @Test public void failedShopSavesRollBackGoldAndStackQuantities() throws Exception {
        Item food=new Food().quantity(3);profile.stash.add(food);profile.buy(1);int gold=profile.gold;
        java.io.File blocker=folder.newFile("shop-blocker");
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{profile.buy(0);fail("Saving must fail");}catch(IllegalStateException expected){}
        assertEquals(gold,profile.gold);assertEquals(7,profile.stash.size());
        Item restored=profile.stash.get(5);
        try{profile.sell(restored,false);fail("Saving must fail");}catch(IllegalStateException expected){}
        assertEquals(gold,profile.gold);assertEquals(3,profile.stash.get(5).quantity());
    }
    @Test public void emptyGoldAndPreparedItemsCannotBeSoldOrBoughtAway() {
        profile.gold=0;try{profile.buy(1);fail("Gold is required");}catch(IllegalStateException expected){}
        assertEquals(5,profile.stash.size());
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
        assertEquals(25,profile.xp);assertEquals(4,profile.points);assertEquals(7,h.HP);assertEquals(20,h.HT);
        assertEquals(Hero.STARTING_STR,h.STR);assertEquals(1,h.combatLevel());
        profile.settle(profile.raidID,false);assertEquals(25,profile.xp);assertEquals(4,profile.points);
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
