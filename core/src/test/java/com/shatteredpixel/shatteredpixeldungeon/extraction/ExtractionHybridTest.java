package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.headless.*;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.*;
import com.watabou.utils.*;
import com.watabou.noosa.Game;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.lang.reflect.*;
import static org.junit.Assert.*;

public class ExtractionHybridTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    private ExtractionProfile p;private Hero h;
    @Before public void setup() throws Exception{
        Gdx.files=new HeadlessFiles();new Game(com.watabou.noosa.Scene.class,null);GdxNativesLoader.load();
        Gdx.app=(Application)Proxy.newProxyInstance(Application.class.getClassLoader(),new Class[]{Application.class},(o,m,a)->{
            if(m.getName().equals("getType"))return Application.ApplicationType.Desktop;
            if(m.getName().equals("postRunnable")){((Runnable)a[0]).run();return null;}
            if(m.getReturnType()==boolean.class)return false;if(m.getReturnType()==int.class)return 0;if(m.getReturnType()==long.class)return 0L;return null;
        });
        Game.version="0.26-farming";Game.versionCode=951;
        SPDSettings.set(new HeadlessPreferences("hybrid-prefs.xml",folder.getRoot().getAbsolutePath()));
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        Field f=ExtractionProfile.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,null);
        Actor.clear();Dungeon.hero=null;Dungeon.level=null;Dungeon.depth=1;Dungeon.branch=0;Dungeon.gold=0;GamesInProgress.curSlot=1;Badges.reset();
        p=ExtractionProfile.get();h=new Hero();h.extractionRaidID=1;h.HP=h.HT=20;h.belongings.weapon=new Sword();Dungeon.hero=h;
    }
    private Mob enemy(){Mob m=new Rat();m.HP=m.HT=100;return m;}
    private void learn(String tree,int... nodes){for(int n:nodes)p.nodes.add(tree+"_"+n);}
    @Test public void sixTreesHaveDistinctStableNodesAndEitherNeighbourEntry(){
        assertEquals(622,ExtractionGrowth.NODES.length);
        for(int b=25;b<31;b++){
            assertEquals(15,ExtractionGrowth.BRANCH_NODES[b].length);
            ExtractionGrowth.Node root=ExtractionGrowth.NODES[ExtractionGrowth.BRANCH_NODES[b][0]];
            assertTrue(root.alternatives.length>=2);
            java.util.Set<String> one=new java.util.HashSet<>();one.add(ExtractionGrowth.NODES[root.alternatives[0]].id);
            assertTrue(ExtractionGrowth.unlocked(root,one));
            one.clear();assertFalse(ExtractionGrowth.unlocked(root,one));
        }
    }
    @Test public void spellbladeAlternatesAndConsumesEmpowermentOnlyOncePerCast(){
        learn("spell",0);Mob e=enemy();Wand w=new WandOfMagicMissile();w.curCharges=3;
        assertEquals(10,ExtractionHybridCombat.physical(h,e,10));
        ExtractionHybridCombat.beginCast(h,w,1);assertEquals(12,ExtractionHybridCombat.magic(h,e,10));
        assertEquals(12,ExtractionHybridCombat.magic(h,enemy(),10));ExtractionHybridCombat.endCast(h,w);
        assertEquals(12,ExtractionHybridCombat.physical(h,e,10));
        assertEquals(10,ExtractionHybridCombat.physical(h,e,10));
        ExtractionHybridCombat.beginCast(h,w,1);ExtractionHybridCombat.endCast(h,w);
        assertFalse(ExtractionHybridCombat.state(h).bladeReady);
    }
    @Test public void overloadDoesNotOverdrawMultiChargeWands(){
        learn("spell",0,2,4);Mob e=enemy();Wand w=new WandOfFireblast();w.curCharges=2;
        ExtractionHybridCombat.physical(h,e,10);ExtractionHybridCombat.beginCast(h,w,2);
        assertEquals(14,ExtractionHybridCombat.magic(h,e,10));w.curCharges-=2;ExtractionHybridCombat.endCast(h,w);assertEquals(0,w.curCharges);
        w.curCharges=4;ExtractionHybridCombat.physical(h,e,10);ExtractionHybridCombat.beginCast(h,w,2);
        assertEquals(16,ExtractionHybridCombat.magic(h,e,10));w.curCharges-=2;ExtractionHybridCombat.endCast(h,w);assertEquals(1,w.curCharges);
    }
    @Test public void shadowMarkExplodesOnceAndNeedsDaggerThenMagic(){
        learn("shadow",0,1);h.belongings.weapon=new Dagger();Mob e=enemy();
        ExtractionHybridCombat.physical(h,e,10);assertNotNull(e.buff(ExtractionHybridCombat.ShadowMark.class));
        assertEquals(17,ExtractionHybridCombat.magic(h,e,10));assertNull(e.buff(ExtractionHybridCombat.ShadowMark.class));
        assertEquals(10,ExtractionHybridCombat.magic(h,e,10));
    }
    @Test public void coatingHasChargesAndCooldownAndOnlyRangedHitsConsumeIt(){
        learn("alchemy",0,2,5,14);assertTrue(ExtractionHybridCombat.coat(h,2));assertEquals(7,ExtractionHybridCombat.state(h).coatedShots);
        assertFalse(ExtractionHybridCombat.coat(h,2));assertFalse(ExtractionHybridCombat.coat(h,3));
        Mob e=enemy();ExtractionHybridCombat.physical(h,e,10);assertEquals(7,ExtractionHybridCombat.state(h).coatedShots);
        h.belongings.weapon=new ThrowingKnife();ExtractionHybridCombat.physical(h,e,10);assertEquals(6,ExtractionHybridCombat.state(h).coatedShots);assertNotNull(e.buff(Chill.class));
        assertEquals(10,ExtractionHybridCombat.physical(h,new Hero(),10));assertEquals(6,ExtractionHybridCombat.state(h).coatedShots);
    }
    @Test public void stormRequiresSwitchingDistanceFamilies(){
        learn("storm",0);Mob e=enemy();h.belongings.weapon=new ThrowingKnife();assertEquals(10,ExtractionHybridCombat.physical(h,e,10));
        h.belongings.weapon=new Sword();assertEquals(12,ExtractionHybridCombat.physical(h,e,10));assertEquals(10,ExtractionHybridCombat.physical(h,e,10));
        h.belongings.weapon=new ThrowingKnife();assertEquals(12,ExtractionHybridCombat.physical(h,e,10));
    }
    @Test public void crusaderFaithCapsAndCannotHealAtFullHealth(){
        learn("holy",0,5,9,13);Mob e=enemy();for(int i=0;i<20;i++)ExtractionHybridCombat.physical(h,e,5);
        assertEquals(6,ExtractionHybridCombat.state(h).faith);assertFalse(ExtractionHybridCombat.mend(h));assertEquals(6,ExtractionHybridCombat.state(h).faith);
        h.HP=1;assertTrue(ExtractionHybridCombat.mend(h));assertEquals(8,h.HP);assertEquals(4,ExtractionHybridCombat.state(h).faith);
    }
    @Test public void bloodOathCostsHealthAndCannotKillItsOwner(){
        learn("blood",0);ExtractionHybridCombat.state(h).bloodOath=true;Mob e=enemy();
        assertEquals(13,ExtractionHybridCombat.physical(h,e,10));assertEquals(19,h.HP);assertNotNull(e.buff(Bleeding.class));
        h.HP=1;assertEquals(10,ExtractionHybridCombat.physical(h,enemy(),10));assertEquals(1,h.HP);
    }
    @Test public void runStateSurvivesSerializationWithoutCarryingAnUnfinishedCast(){
        ExtractionHybridCombat.State s=ExtractionHybridCombat.state(h);s.coating=2;s.coatedShots=5;s.faith=4;s.bloodOath=true;s.spellReady=true;s.spellUntil=9;s.castEmpowered=true;
        Bundle b=new Bundle();b.put("state",s);ExtractionHybridCombat.State copy=(ExtractionHybridCombat.State)b.get("state");
        assertEquals(5,copy.coatedShots);assertEquals(4,copy.faith);assertTrue(copy.bloodOath);assertTrue(copy.spellReady);assertEquals(9,copy.spellUntil,0);assertFalse(copy.castEmpowered);
    }
    @Test public void acceptedContractsAndBankedProgressSurviveReload() throws Exception{
        ExtractionContracts.accept(p,"hunt_1");p.contractProgress.put("hunt_1",2);p.setDebugEnabled(true);
        Field f=ExtractionProfile.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,null);p=ExtractionProfile.get();
        assertTrue(p.contracts.contains("hunt_1"));assertEquals(2,ExtractionContracts.progress(p,ExtractionContracts.job("hunt_1")));
    }
    @Test public void deathDiscardsRunProgressAndRetainsAcceptedJobs(){
        ExtractionContracts.accept(p,"hunt_1");p.begin();h.extractionRaidID=p.raidID;p.initialize(h);
        ExtractionContracts.killed(h,new Rat(),0);assertEquals(1,ExtractionContracts.run(h).progress[0]);
        p.settle(p.raidID,false);assertTrue(p.contracts.contains("hunt_1"));assertEquals(0,ExtractionContracts.progress(p,ExtractionContracts.job("hunt_1")));
    }
    @Test public void extractionBanksProgressAndClaimPaysOnlyOnce(){
        ExtractionContracts.accept(p,"hunt_1");p.begin();h.extractionRaidID=p.raidID;p.initialize(h);
        for(int i=0;i<6;i++)ExtractionContracts.killed(h,new Rat(),0);p.settle(p.raidID,true);
        assertEquals(6,ExtractionContracts.progress(p,ExtractionContracts.job("hunt_1")));int before=p.gold;
        ExtractionContracts.claim(p,"hunt_1",null,2);assertEquals(before+80,p.gold);assertTrue(p.completedContracts.contains("hunt_1"));
        assertTrue(p.stash.stream().anyMatch(i->i instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor&&((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)i).tier==2));
        try{ExtractionContracts.claim(p,"hunt_1",null,2);fail("Duplicate claim");}catch(IllegalStateException expected){}assertEquals(before+80,p.gold);
    }
    @Test public void deliveryConsumesOnlyTheSelectedStashWeapon(){
        ExtractionContracts.accept(p,"supply_1");p.begin();h.extractionRaidID=p.raidID;p.initialize(h);p.settle(p.raidID,true);
        Item one=new Shortsword(),two=new Shortsword();p.stash.add(one);p.stash.add(two);
        ExtractionContracts.claim(p,"supply_1",one,1);assertFalse(p.stash.contains(one));assertTrue(p.stash.contains(two));
    }
    @Test public void contractCapAndChapterLocksAreEnforced(){
        try{ExtractionContracts.accept(p,"hunt_2");fail("Prison locked");}catch(IllegalStateException expected){}
        ExtractionContracts.accept(p,"hunt_1");ExtractionContracts.accept(p,"record_1");ExtractionContracts.accept(p,"supply_1");learn("spell",0);
        try{ExtractionContracts.accept(p,"hybrid_0");fail("Three-contract cap");}catch(IllegalStateException expected){}assertEquals(3,p.contracts.size());
    }
    @Test public void rewardEquipmentRespectsEveryChapterAndEverySlot(){
        for(int c=1;c<=5;c++)for(int category=1;category<=4;category++){
            Item item=ExtractionContracts.reward(ExtractionContracts.job("hunt_"+c),category);
            int tier=item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor?((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).tier:WeaponUpgradeLimit.tier(item);
            assertEquals(Math.min(5,c+1),tier);
        }
    }
    @Test public void recoveryRecordIsBoundToAcceptedJobAndRun(){
        ExtractionContracts.accept(p,"record_1");p.begin();h.extractionRaidID=p.raidID;p.initialize(h);
        assertFalse(ExtractionContracts.collect(h,"record_1",p.raidID+1));assertFalse(ExtractionContracts.collect(h,"record_2",p.raidID));
        assertTrue(ExtractionContracts.collect(h,"record_1",p.raidID));assertEquals(1,ExtractionContracts.run(h).progress[0]);assertTrue(h.belongings.backpack.items.stream().noneMatch(i->i instanceof ExtractionContracts.Record));
    }
    @Test public void hybridTrialNeedsTheMatchingCombatEffect(){
        learn("storm",0);ExtractionContracts.accept(p,"hybrid_3");p.begin();h.extractionRaidID=p.raidID;p.initialize(h);
        ExtractionContracts.killed(h,new Rat(),1);assertEquals(0,ExtractionContracts.run(h).progress[0]);
        ExtractionContracts.killed(h,new Rat(),8);assertEquals(1,ExtractionContracts.run(h).progress[0]);
    }
    @Test public void rewardSaveFailureRollsBackGoldAndCompletion() throws Exception{
        ExtractionContracts.accept(p,"hunt_1");p.contractProgress.put("hunt_1",6);p.setDebugEnabled(true);int gold=p.gold;
        java.io.File blocker=folder.newFile("write-blocker");FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{ExtractionContracts.claim(p,"hunt_1",null,1);fail("Expected save failure");}catch(IllegalStateException expected){}
        assertEquals(gold,p.gold);assertTrue(p.contracts.contains("hunt_1"));assertFalse(p.completedContracts.contains("hunt_1"));
    }
}
