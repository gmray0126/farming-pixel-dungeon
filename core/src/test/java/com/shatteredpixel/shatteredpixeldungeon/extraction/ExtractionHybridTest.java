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
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
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
    @Test public void arcaneBallisticsBridgesMageAndHuntress(){
        ExtractionGrowth.Node root=ExtractionGrowth.NODES[ExtractionGrowth.index("alchemy_0")];
        assertEquals("마탄학자",ExtractionGrowth.BRANCHES[root.branch]);
        assertArrayEquals(new int[]{ExtractionGrowth.index("skill_beacon"),ExtractionGrowth.index("skill_hawk")},root.parents);
        java.util.HashSet<String> learned=new java.util.HashSet<>();
        learned.add("skill_stealth");learned.add("skill_hawk");
        assertFalse(ExtractionGrowth.unlocked(root,learned));
        learned.add("skill_beacon");assertTrue(ExtractionGrowth.unlocked(root,learned));
        assertTrue(GrowthAtlasLayout.GROUP_X[27]>GrowthAtlasLayout.GROUP_X[20]);
        assertTrue(GrowthAtlasLayout.GROUP_Y[27]>GrowthAtlasLayout.GROUP_Y[20]);
        assertTrue(GrowthAtlasLayout.GROUP_Y[27]<GrowthAtlasLayout.GROUP_Y[22]);
    }

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
    @Test public void spiritBowUsesPermanentGrowthAndStopsAtFifteen(){
        com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow bow=new NodeSpiritBow();
        p.xp=0;assertEquals(0,bow.level());int base=bow.max();
        p.xp=9*25;assertEquals(2,bow.level());assertTrue(bow.max()>base);
        p.xp=74*25;assertEquals(15,bow.level());int min=bow.min(),max=bow.max();
        p.xp=99*25;assertEquals(15,bow.level());assertEquals(min,bow.min());assertEquals(max,bow.max());
        h.lvl=1;assertEquals(1,h.combatLevel());assertEquals(15,bow.level());
        assertFalse(bow.isUpgradable());
    }
    @Test public void blessedAnkhPreventsExpeditionSettlement(){
        h=new Hero(){@Override public void interrupt(){curAction=null;}};h.HP=h.HT=20;Dungeon.hero=h;
        p.begin();h.extractionRaidID=p.raidID;
        com.shatteredpixel.shatteredpixeldungeon.items.Ankh ankh=new com.shatteredpixel.shatteredpixeldungeon.items.Ankh();
        ankh.bless();ankh.collect(h.belongings.backpack);h.HP=0;
        h.die(null);
        assertEquals(5,h.HP);assertTrue(h.isAlive());assertTrue(p.active);
        assertNull(h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Ankh.class));
        assertNotNull(h.buff(Invulnerability.class));assertFalse(h.extractionDeathRecorded);
    }
    @Test public void ordinaryAnkhWaitsForResurrectionWithoutEndingExpedition(){
        h=new Hero(){@Override public void interrupt(){curAction=null;}};h.HP=h.HT=20;Dungeon.hero=h;
        p.begin();h.extractionRaidID=p.raidID;
        com.shatteredpixel.shatteredpixeldungeon.items.Ankh ankh=new com.shatteredpixel.shatteredpixeldungeon.items.Ankh();ankh.collect(h.belongings.backpack);
        Application original=Gdx.app;java.util.ArrayList<Runnable> queued=new java.util.ArrayList<>();
        Gdx.app=(Application)Proxy.newProxyInstance(Application.class.getClassLoader(),new Class[]{Application.class},(o,m,a)->{
            if(m.getName().equals("postRunnable")){queued.add((Runnable)a[0]);return null;}
            return m.invoke(original,a);
        });
        try{
            h.HP=0;h.die(null);
            assertTrue(p.active);assertSame(ankh,h.belongings.getItem(com.shatteredpixel.shatteredpixeldungeon.items.Ankh.class));
            assertNotNull(com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect.instance);assertEquals(1,queued.size());
            Dungeon.fail(null);assertFalse(h.extractionDeathRecorded);
        }finally{Gdx.app=original;com.shatteredpixel.shatteredpixeldungeon.windows.WndResurrect.instance=null;}
    }
    private Mob enemy(){Mob m=new Rat();m.HP=m.HT=100;return m;}
    private void learn(String tree,int... nodes){for(int n:nodes)p.nodes.add(tree+"_"+n);}
    @Test public void sixTreesRequireBothClassNeighboursAndAllThreeConvergingPaths(){
        assertEquals(629,ExtractionGrowth.NODES.length);
        for(int b=25;b<31;b++){
            assertEquals(15,ExtractionGrowth.BRANCH_NODES[b].length);
            ExtractionGrowth.Node root=ExtractionGrowth.NODES[ExtractionGrowth.BRANCH_NODES[b][0]];
            assertEquals(0,root.alternatives.length);assertEquals(2,root.parents.length);
            java.util.Set<String> learned=new java.util.HashSet<>();
            learned.add(ExtractionGrowth.NODES[root.parents[0]].id);assertFalse(ExtractionGrowth.unlocked(root,learned));
            learned.add(ExtractionGrowth.NODES[root.parents[1]].id);assertTrue(ExtractionGrowth.unlocked(root,learned));
            ExtractionGrowth.Node merge=ExtractionGrowth.NODES[ExtractionGrowth.index(ExtractionHybridTrees.PREFIXES[b-25]+"_13")];
            assertEquals(3,merge.parents.length);learned.clear();
            for(int i=0;i<3;i++){assertFalse(ExtractionGrowth.unlocked(merge,learned));learned.add(ExtractionGrowth.NODES[merge.parents[i]].id);}
            assertTrue(ExtractionGrowth.unlocked(merge,learned));
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
    @Test public void gunUpgradesToFifteenAndSharpshootingAppliesToBothShotModes(){
        learn("alchemy",0);AlchemyFirearm gun=new AlchemyFirearm();h.belongings.weapon=gun;
        assertTrue(WeaponUpgradeLimit.eligible(gun));assertEquals(15,WeaponUpgradeLimit.cap(gun));
        int min=gun.min(),max=gun.max();gun.upgrade();assertEquals(min+1,gun.min());assertEquals(max+2,gun.max());
        com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting();ring.level(2);h.belongings.ring=ring;ring.activate(h);
        assertEquals(min+4,gun.min());assertEquals(max+8,gun.max());
        AlchemyFirearm.Shot shot=new AlchemyFirearm.Shot(gun,0);assertEquals(gun.min(),shot.min());assertEquals(gun.max(),shot.max());
        gun.upgrade(100);assertEquals(15,gun.trueLevel());assertFalse(WeaponUpgradeLimit.eligible(gun));
        Bundle saved=new Bundle();saved.put("gun",gun);AlchemyFirearm copy=(AlchemyFirearm)saved.get("gun");assertEquals(15,copy.trueLevel());assertEquals(gun.min(),copy.min());
    }
    @Test public void elementalRoundsAddSeparateResistibleMagicDamageInBothShotModes(){
        learn("alchemy",0);AlchemyFirearm gun=new AlchemyFirearm();h.belongings.weapon=gun;gun.loaded=1;gun.element=AlchemyBullet.FROST;
        Mob target=enemy();int before=target.HP,magic=gun.magicDamage(h);
        assertEquals(10,gun.proc(h,target,10));assertEquals(before-magic,target.HP);
        AlchemyFirearm.Shot shot=new AlchemyFirearm.Shot(gun,AlchemyBullet.FROST);h.belongings.thrownWeapon=shot;
        before=target.HP;assertEquals(10,shot.proc(h,target,10));assertEquals(before-magic,target.HP);
        before=target.HP;gun.bulletEffects(h,target,AlchemyBullet.NORMAL);assertEquals(before,target.HP);
        Buff.affect(target,MagicImmune.class);gun.bulletEffects(h,target,AlchemyBullet.FROST);assertEquals(before,target.HP);
    }
    @Test public void rollCannotPassIntermediateWallsTrapsOrEnemies(){
        learn("alchemy",0,5);
        ExtractionTraining.Room room=ExtractionTraining.prepare();Hero trainee=Dungeon.hero;int start=trainee.pos,mid=start+1,end=start+2;
        room.solid[mid]=true;room.passable[mid]=false;assertFalse(ExtractionHybridCombat.roll(trainee,end));
        room.solid[mid]=false;room.passable[mid]=true;
        room.traps.put(mid,new com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap());assertFalse(ExtractionHybridCombat.roll(trainee,end));room.traps.remove(mid);
        Mob blocker=enemy();blocker.pos=mid;Actor.add(blocker);assertFalse(ExtractionHybridCombat.roll(trainee,end));Actor.remove(blocker);
        assertEquals(start,trainee.pos);assertEquals(0,trainee.extractionHybrid.rollReadyAt,0);assertTrue(ExtractionHybridCombat.roll(trainee,end));
    }
    @Test public void gunReloadConsumesRealAmmunitionAndNeverCreatesRounds(){
        learn("alchemy",0);AlchemyFirearm gun=new AlchemyFirearm();gun.collect(h.belongings.backpack);AlchemyBullet ammo=(AlchemyBullet)new AlchemyBullet().quantity(10);ammo.collect(h.belongings.backpack);
        assertEquals(4,gun.reload(h,0));assertEquals(6,ammo.quantity());assertEquals(0,gun.reload(h,0));gun.loaded=1;assertEquals(3,gun.reload(h,0));assertEquals(3,ammo.quantity());
        assertEquals(0,gun.reload(h,AlchemyBullet.FIRE));Bundle b=new Bundle();b.put("gun",gun);AlchemyFirearm copy=(AlchemyFirearm)b.get("gun");assertEquals(4,copy.loaded);assertEquals(0,copy.element);
    }
    @Test public void ammunitionRecipesConsumeSeedsAndHaveDistinctElementStacks(){
        java.util.ArrayList<Item> seed=picks(new com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom.Seed());AlchemyBullet.BasicRecipe base=new AlchemyBullet.BasicRecipe();assertTrue(base.testIngredients(seed));assertEquals(6,base.brew(seed).quantity());assertEquals(0,seed.get(0).quantity());
        java.util.ArrayList<Item> ingredients=picks(new AlchemyBullet(),new com.shatteredpixel.shatteredpixeldungeon.plants.Icecap.Seed());AlchemyBullet.ElementRecipe recipe=new AlchemyBullet.ElementRecipe();assertTrue(recipe.testIngredients(ingredients));AlchemyBullet result=(AlchemyBullet)recipe.brew(ingredients);assertEquals(AlchemyBullet.FROST,result.element);assertEquals(0,ingredients.get(0).quantity());assertEquals(0,ingredients.get(1).quantity());assertFalse(result.isSimilar(new AlchemyBullet()));
    }
    @Test public void evasionCapAndRingScalingRemainBounded(){
        assertEquals(5,ExtractionEvasion.ringMultiplier(32),.00001f);
        for(float accuracy:new float[]{1,5,20,100})for(float defense:new float[]{0,1,10,100,10000,Float.POSITIVE_INFINITY})assertTrue(ExtractionEvasion.chance(accuracy,defense)<=.80001f);
        assertEquals(.8f,ExtractionEvasion.chance(20,10000),.00001f);assertEquals(0,ExtractionEvasion.chance(20,0),0);assertEquals(.25f,ExtractionEvasion.chance(20,10),.00001f);
    }
    @Test public void swordWaveAddsRangeAndSeparateMagicWithoutEnablingRangedWeapons(){
        learn("spell",0);assertEquals(3,((com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon)h.belongings.weapon).reachFactor(h));Mob e=enemy();ExtractionHybridCombat.waveMagic(h,e,20);assertEquals(97,e.HP);learn("spell",1,9);ExtractionHybridCombat.waveMagic(h,e,20);assertEquals(93,e.HP);assertEquals(4,ExtractionHybridCombat.waveReach(h));h.belongings.weapon=new ThrowingKnife();assertEquals(0,ExtractionHybridCombat.waveReach(h));
    }
    @After public void endTraining(){ExtractionTraining.finish();}
    @Test public void trainingUsesCopiesAndCannotSaveOrAwardExpeditionProgress() throws Exception {
        learn("alchemy",0,5,6);AlchemyFirearm original=new AlchemyFirearm();p.stash.add(original);p.prepare(original,true);AlchemyBullet ammo=(AlchemyBullet)new AlchemyBullet().quantity(8);p.stash.add(ammo);p.prepare(ammo,true);
        byte[] profileBefore=FileUtils.getFileHandle(ExtractionProfile.FILE).readBytes();int xp=p.xp,gold=p.gold;
        ExtractionTraining.Room level=ExtractionTraining.prepare();Hero trainee=Dungeon.hero;assertEquals(-2,trainee.extractionRaidID);assertNotNull(com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode);assertFalse(p.active);assertEquals(3,level.mobs.size());assertNotNull(level.tilesTex());assertNotNull(level.waterTex());
        AlchemyFirearm copy=AlchemyFirearm.find(trainee);assertNotNull(copy);assertNotSame(original,copy);assertSame(copy,trainee.belongings.weapon);assertEquals(4,copy.reload(trainee,0));assertEquals(0,original.loaded);assertEquals(8,ammo.quantity());
        Dungeon.saveAll();Dungeon.saveGame(1);Dungeon.saveLevel(1);assertFalse(FileUtils.fileExists(GamesInProgress.gameFile(1)));assertArrayEquals(profileBefore,FileUtils.getFileHandle(ExtractionProfile.FILE).readBytes());assertEquals(xp,p.xp);assertEquals(gold,p.gold);
        int hp=trainee.HP;trainee.damage(100,new Object());assertEquals(hp,trainee.HP);
    }
    @Test public void rollMovesTwoCellsImmediatelyReloadsFromInventoryAndPersistsCooldown() throws Exception {
        learn("alchemy",0,5,6);AlchemyFirearm gun=new AlchemyFirearm();p.stash.add(gun);p.prepare(gun,true);AlchemyBullet ammo=(AlchemyBullet)new AlchemyBullet().quantity(8);p.stash.add(ammo);p.prepare(ammo,true);
        ExtractionTraining.Room level=ExtractionTraining.prepare();Hero trainee=Dungeon.hero;int pos=trainee.pos;float before=Actor.now();
        assertFalse(ExtractionHybridCombat.roll(trainee,pos+3));assertEquals(pos,trainee.pos);assertTrue(ExtractionHybridCombat.roll(trainee,pos+2));assertEquals(pos+2,trainee.pos);assertEquals(before,Actor.now(),0);assertEquals(4,AlchemyFirearm.find(trainee).loaded);
        assertEquals(4,trainee.belongings.getItem(AlchemyBullet.class).quantity());assertFalse(ExtractionHybridCombat.roll(trainee,pos));
        Bundle b=new Bundle();b.put("state",trainee.extractionHybrid);ExtractionHybridCombat.State restored=(ExtractionHybridCombat.State)b.get("state");assertEquals(before+8,restored.rollReadyAt,0);
    }
    @Test public void trainingDummiesRecordDamageWithoutDyingOrGivingLoot(){
        ExtractionTraining.Dummy dummy=new ExtractionTraining.Dummy();dummy.damage(7,new Object());assertEquals(7,dummy.total);assertEquals(1,dummy.hits);assertEquals(dummy.HT,dummy.HP);dummy.damage(9,new Object());assertEquals(16,dummy.total);assertEquals(9,dummy.highest);assertEquals(0,dummy.EXP);assertTrue(dummy.isAlive());
    }
    @Test public void realHitRollLimitsEvenTemporaryInfiniteDodgeToEightyPercent(){
        h.damageInterrupt=false;Buff.affect(h,MonkEnergy.MonkAbility.Focus.FocusBuff.class);Mob attacker=enemy();int misses=0;Random.pushGenerator(5620L);
        try{for(int i=0;i<10000;i++)if(!Char.hit(attacker,h,1,false))misses++;}finally{Random.popGenerator();}
        assertTrue("Observed dodge: "+misses,misses>7700&&misses<8300);
    }
    @Test public void hubCraftsAmmunitionAtomicallyAndNoSeedOrEnergyProducesFreeRounds() throws Exception {
        Item seed=new com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom.Seed().quantity(2);p.stash.add(seed);p.alchemyEnergy=1;
        Recipe recipe=ExtractionAlchemy.recipes(p,picks(seed)).stream().filter(v->v instanceof AlchemyBullet.BasicRecipe).findFirst().get();ExtractionAlchemy.craft(p,picks(seed),recipe);assertEquals(0,p.alchemyEnergy);assertEquals(1,seed.quantity());assertEquals(6,p.stash.stream().filter(v->v instanceof AlchemyBullet).findFirst().get().quantity());
        int before=p.stash.size();try{ExtractionAlchemy.craft(p,picks(seed),recipe);fail("No energy");}catch(IllegalStateException expected){}assertEquals(before,p.stash.size());assertEquals(1,seed.quantity());
    }
    @Test public void equippedGunCanReachDistantTargetsAndConsumesRoundsEvenOnMiss() throws Exception {
        learn("alchemy",0);AlchemyFirearm gun=new AlchemyFirearm();p.stash.add(gun);p.prepare(gun,true);ExtractionTraining.Room room=ExtractionTraining.prepare();Hero trainee=Dungeon.hero;AlchemyFirearm equipped=AlchemyFirearm.find(trainee);assertSame(equipped,trainee.belongings.weapon);trainee.pos=7*room.width()+7;
        ExtractionTraining.Dummy target=room.mobs.stream().filter(v->v.pos==4*room.width()+7).map(v->(ExtractionTraining.Dummy)v).findFirst().get();equipped.loaded=2;assertTrue(trainee.canAttack(target));assertTrue(equipped.consumeRound(trainee,target));assertEquals(1,equipped.loaded);assertTrue(equipped.consumeRound(trainee,target));assertFalse(equipped.consumeRound(trainee,target));assertEquals(0,equipped.loaded);
        assertTrue(equipped.actions(trainee).contains(com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem.AC_UNEQUIP));
    }
    @Test public void emptyGunUsesFistsAndLastRoundStillUsesGunDamage() throws Exception {
        learn("alchemy",0);AlchemyFirearm gun=new AlchemyFirearm();gun.upgrade(5);p.stash.add(gun);p.prepare(gun,true);
        ExtractionTraining.Room room=ExtractionTraining.prepare();Hero trainee=Dungeon.hero;AlchemyFirearm equipped=AlchemyFirearm.find(trainee);
        ExtractionTraining.Dummy dummy=room.mobs.stream().filter(m->m.pos==4*room.width()+7).map(m->(ExtractionTraining.Dummy)m).findFirst().get();
        trainee.pos=dummy.pos+1;assertSame(equipped,trainee.belongings.weapon);assertNull(trainee.belongings.attackingWeapon());assertEquals(ExtractionGrowth.FIST,ExtractionGrowth.family(trainee.growthWeapon()));assertTrue(trainee.canAttack(dummy));
        trainee.pos=dummy.pos+2;assertFalse(trainee.canAttack(dummy));
        java.util.Arrays.fill(room.heroFOV,false);equipped.loaded=1;int before=dummy.total,min=equipped.min();
        assertTrue(trainee.attack(dummy,1,0,Char.INFINITE_ACCURACY));assertTrue(dummy.total-before>=min);assertEquals(0,equipped.loaded);assertFalse(equipped.firing);assertNull(trainee.belongings.attackingWeapon());
        new AlchemyBullet().quantity(1).collect(trainee.belongings.backpack);assertEquals(1,equipped.reload(trainee,0));assertSame(equipped,trainee.belongings.attackingWeapon());assertEquals(10,ExtractionGrowth.family(trainee.growthWeapon()));
    }
    @Test public void loadedGunDoesNotUseUnarmedStanceAndEmptyGunDoes(){
        AlchemyFirearm gun=new AlchemyFirearm();gun.loaded=1;h.belongings.weapon=gun;com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce ring=new com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce();h.belongings.ring=ring;ring.activate(h);Buff.affect(h,com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce.BrawlersStance.class).active=true;
        assertFalse(com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce.fightingUnarmed(h));assertSame(gun,h.growthWeapon());assertEquals(10,ExtractionGrowth.family(gun));
        gun.loaded=0;assertTrue(com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfForce.fightingUnarmed(h));assertEquals(ExtractionGrowth.FIST,ExtractionGrowth.family(h.growthWeapon()));
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

    @Test public void bloodOathDrainsEveryFiveTurnsWithoutKillingAndKeepsItsRemainder(){
        learn("blood",0);ExtractionHybridCombat.state(h).bloodOath=true;
        Regeneration regen=Buff.affect(h,Regeneration.class);
        for(int i=0;i<4;i++)regen.act();assertEquals(20,h.HP);
        Bundle b=new Bundle();b.put("state",h.extractionHybrid);h.extractionHybrid=(ExtractionHybridCombat.State)b.get("state");
        regen.act();assertEquals(19,h.HP);assertEquals(0,h.extractionHybrid.oathTurns);
        h.extractionHybrid.bloodOath=false;for(int i=0;i<5;i++)ExtractionHybridCombat.bloodTick(h);assertEquals(19,h.HP);
        h.extractionHybrid.bloodOath=true;h.HP=2;for(int i=0;i<5;i++)ExtractionHybridCombat.bloodTick(h);assertEquals(1,h.HP);assertFalse(h.extractionHybrid.bloodOath);
    }
    @Test public void naturalMobFoodReturnsAndPurchasedFoodKeepsItsSaveMarker(){
        assertTrue(new Piranha().createLoot() instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food);
        assertTrue(new Monk().createLoot() instanceof com.shatteredpixel.shatteredpixeldungeon.items.food.Food);
        p.buy(1);Item bought=p.stash.get(p.stash.size()-1);assertTrue(bought.extractionPurchasedFood);
        Bundle b=new Bundle();b.put("food",bought);assertTrue(((Item)b.get("food")).extractionPurchasedFood);
    }
    private java.util.ArrayList<Item> picks(Item...items){return new java.util.ArrayList<>(java.util.Arrays.asList(items));}
    @Test public void hubCraftingNormalizesSupplyPotionsAndPreservesExpeditionHero(){
        Item supply=p.stash.get(1);java.util.ArrayList<Item> selected=picks(supply);
        Recipe recipe=ExtractionAlchemy.recipes(p,selected).stream().filter(r->r instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion.PotionToExotic).findFirst().get();
        int cost=ExtractionAlchemy.cost(p,selected,recipe);assertEquals(4,cost);p.alchemyEnergy=4;
        ExtractionAlchemy.craft(p,selected,recipe);assertEquals(0,p.alchemyEnergy);assertFalse(p.stash.contains(supply));assertSame(h,Dungeon.hero);
        assertTrue(p.stash.get(p.stash.size()-1) instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfShielding);
    }
    @Test public void hubSeedsConsumeExactlyThreeFromOneStackAndKeepOtherMaterials(){
        Item seeds=new com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass.Seed().quantity(5);p.stash.add(seeds);
        java.util.ArrayList<Item> selected=picks(seeds,seeds,seeds);Recipe recipe=ExtractionAlchemy.recipes(p,selected).get(0);
        int before=p.stash.size();ExtractionAlchemy.craft(p,selected,recipe);assertEquals(2,seeds.quantity());assertEquals(before+1,p.stash.size());assertTrue(p.stash.get(p.stash.size()-1) instanceof PotionOfHealing);assertSame(h,Dungeon.hero);
    }
    @Test public void hubCraftingRejectsOverselectedStacksAndMissingEnergy(){
        Item supply=p.stash.get(1);int before=p.stash.size();
        try{ExtractionAlchemy.recipes(p,picks(supply,supply));fail("Overselected stack");}catch(IllegalStateException expected){}
        Recipe recipe=ExtractionAlchemy.recipes(p,picks(supply)).stream().filter(r->r instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion.PotionToExotic).findFirst().get();
        try{ExtractionAlchemy.craft(p,picks(supply),recipe);fail("Missing energy");}catch(IllegalStateException expected){}assertEquals(before,p.stash.size());assertTrue(p.stash.contains(supply));
    }
    @Test public void hubEnergyPurchasesAndDisassemblyPersistWithoutDuplicatingValue() throws Exception{
        ExtractionAlchemy.buyEnergy(p,5);assertEquals(75,p.gold);assertEquals(5,p.alchemyEnergy);
        Item potion=p.stash.get(1);int energy=ExtractionAlchemy.energyValue(potion,1);assertTrue(energy>0);ExtractionAlchemy.energize(p,potion,1);assertEquals(5+energy,p.alchemyEnergy);
        try{ExtractionAlchemy.energize(p,potion,1);fail("Already consumed");}catch(IllegalStateException expected){}
        Field f=ExtractionProfile.class.getDeclaredField("instance");f.setAccessible(true);f.set(null,null);p=ExtractionProfile.get();assertEquals(5+energy,p.alchemyEnergy);assertEquals(75,p.gold);
    }
    @Test public void hubCraftSaveFailureRestoresMaterialsAndEnergy() throws Exception{
        Item supply=p.stash.get(1);p.alchemyEnergy=4;Recipe recipe=ExtractionAlchemy.recipes(p,picks(supply)).stream().filter(r->r instanceof com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion.PotionToExotic).findFirst().get();
        int size=p.stash.size();java.io.File blocker=folder.newFile("alchemy-write-blocker");FileUtils.setDefaultFileProperties(Files.FileType.Absolute,blocker.getAbsolutePath()+"/");
        try{ExtractionAlchemy.craft(p,picks(supply),recipe);fail("Expected save failure");}catch(IllegalStateException expected){}assertEquals(4,p.alchemyEnergy);assertEquals(size,p.stash.size());assertEquals(3,p.stash.stream().filter(i->i instanceof SupplyHealingPotion).count());assertSame(h,Dungeon.hero);
    }
    @Test public void catalystKeepsItsThreeChoicesAcrossReopeningAndOnlyConsumesOnCraft(){
        com.shatteredpixel.shatteredpixeldungeon.items.Generator.fullReset();
        Item catalyst=new com.shatteredpixel.shatteredpixeldungeon.items.trinkets.TrinketCatalyst();p.stash.add(catalyst);p.alchemyEnergy=6;
        java.util.ArrayList<Recipe> first=ExtractionAlchemy.recipes(p,picks(catalyst)),second=ExtractionAlchemy.recipes(p,picks(catalyst));assertEquals(3,first.size());
        for(int i=0;i<3;i++)assertEquals(ExtractionAlchemy.preview(p,picks(catalyst),first.get(i)).getClass(),ExtractionAlchemy.preview(p,picks(catalyst),second.get(i)).getClass());
        ExtractionAlchemy.craft(p,picks(catalyst),first.get(0));assertFalse(p.stash.contains(catalyst));assertEquals(0,p.alchemyEnergy);assertTrue(p.stash.get(p.stash.size()-1) instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket);
    }
    @Test public void runningExpeditionCannotUseHubAlchemy(){
        p.begin();try{ExtractionAlchemy.buyEnergy(p,1);fail("Running raid");}catch(IllegalStateException expected){}
        try{ExtractionAlchemy.recipes(p,picks());fail("Running raid");}catch(IllegalStateException expected){}
    }
    @Test public void shopDescriptionsWorkAtHubWithoutAnExpeditionHero(){
        Dungeon.hero=null;
        com.shatteredpixel.shatteredpixeldungeon.messages.Messages.setup(com.shatteredpixel.shatteredpixeldungeon.messages.Languages.KOREAN);
        for(ExtractionShop.Offer offer:ExtractionShop.OFFERS){
            Item item=offer.item();assertNotNull(item.info());assertFalse(item.info().isEmpty());
        }
        Item staff=new MagesStaff(new com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile()).identify(false);
        assertTrue(staff.info().contains("마탄"));
    }
}
