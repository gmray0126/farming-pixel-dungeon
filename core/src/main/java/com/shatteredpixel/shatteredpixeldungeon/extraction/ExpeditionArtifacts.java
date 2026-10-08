/* Farming Pixel Dungeon. GPL-3.0-or-later. Original item art reused with distinct glow colors. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import java.util.ArrayList;

/** New relics use the original two artifact slots; bag and stash copies grant no effects. */
public final class ExpeditionArtifacts {
    public static Relic equipped(Hero hero,Class<? extends Relic> type){
        if(hero==null||hero.extractionRaidID==0||hero.buff(MagicImmune.class)!=null)return null;
        Item[] slots={hero.belongings.artifact(),hero.belongings.misc()};
        for(Item i:slots)if(type.isInstance(i)&&!i.cursed)return (Relic)i;
        return null;
    }
    public static boolean has(Hero hero,Class<? extends Relic> type){return equipped(hero,type)!=null;}
    public static boolean canDrop(Class<?> type,int chapter,int stage){
        if(type==BloodLantern.class)return chapter>=2||stage>=3;
        if(type==GreedPouch.class)return chapter>=2||stage>=4;
        if(type==BrokenHourglass.class)return chapter>=2||stage>=5;
        if(type==HuntersMark.class)return chapter>=2||stage>=7;
        return true;
    }
    public static void kill(Hero hero){
        if(hero==null)return;
        Item[] slots={hero.belongings.artifact(),hero.belongings.misc()};
        for(Item i:slots)if(i instanceof Relic&&!i.cursed&&hero.buff(MagicImmune.class)==null)((Relic)i).gainKill();
        Relic seal=equipped(hero,ExtractionContracts.CommissionSeal.class);
        if(seal!=null)Buff.affect(hero,Barrier.class).setShield(3+seal.level()/5);
        Relic lamp=equipped(hero,BloodLantern.class);
        if(lamp!=null)hero.HP=Math.min(hero.HT,hero.HP+1+lamp.level()/2);
    }
    public static float healingMultiplier(Hero hero){return has(hero,BloodLantern.class)?.65f:1;}
    public static int incoming(Hero hero,int damage){
        return has(hero,GreedPouch.class)?Math.round(damage*(1+Math.min(.3f,Dungeon.gold/250f*.1f))):damage;
    }
    public static int attack(Hero hero,Char enemy,int damage){
        Relic mark=equipped(hero,HuntersMark.class);if(mark==null||Dungeon.level==null)return damage;
        if(enemy.buff(Hunted.class)!=null)return Math.round(damage*(1.3f+.04f*mark.level()));
        for(Mob mob:Dungeon.level.mobs)if(mob.isAlive()&&mob.buff(Hunted.class)!=null)return Math.round(damage*.85f);
        return damage;
    }
    public static float tempo(Hero hero){return hero.buff(BurstTempo.class)!=null?2:1;}
    public abstract static class Relic extends Artifact {
        public static final String USE="RELIC_USE";
        protected Relic(){unique=true;levelCap=15;chargeCap=8;charge=0;defaultAction=USE;}
        @Override public Item upgrade(){if(level()<levelCap)return super.upgrade();return this;}
        @Override public Item level(int value){return super.level(Math.min(levelCap,value));}
        public void gainKill(){
            charge=Math.min(chargeCap,charge+1);
            if(level()<levelCap&&++exp>=10+5*level()){exp=0;upgrade();if(Dungeon.hero!=null&&Dungeon.hero.sprite!=null)GLog.p(name()+"의 유물 등급이 올랐습니다.");}
            updateQuickslot();
        }
        @Override public void charge(Hero hero,float amount){partialCharge+=amount;while(partialCharge>=1){charge=Math.min(chargeCap,charge+1);partialCharge--;}}
        @Override protected ArtifactBuff passiveBuff(){return new RelicBuff();}
        public class RelicBuff extends ArtifactBuff {
            @Override public boolean act(){spend(TICK);return true;}
        }
        protected int cost(){return 0;}
        @Override public ArrayList<String> actions(Hero hero){
            ArrayList<String> actions=super.actions(hero);if(cost()>0&&isEquipped(hero)&&!cursed)actions.add(USE);return actions;
        }
        @Override public String actionName(String action,Hero hero){return USE.equals(action)?"유물 사용 · "+cost()+" 충전":super.actionName(action,hero);}
        protected boolean ready(Hero hero){
            if(!isEquipped(hero)||cursed||hero.buff(MagicImmune.class)!=null){GLog.w("착용한 유물만 사용할 수 있습니다.");return false;}
            if(charge<cost()){GLog.w("충전이 부족합니다. 적을 처치해 충전하세요.");return false;}return true;
        }
        protected void consume(Hero hero){charge-=cost();hero.spendAndNext(1f);updateQuickslot();}
        protected String progress(){return "\n\n유물 등급 "+level()+" / 15 · 적 처치로 성장\n충전 "+charge+" / 8 · 적 처치마다 +1";}
        @Override public int value(){return 100+20*level();}
    }
    public static class BloodLantern extends Relic {
        public BloodLantern(){image=ItemSpriteSheet.ARTIFACT_CHALICE1;defaultAction=AC_EQUIP;}
        @Override public String name(){return "피의 등불";}
        @Override public String desc(){return "주변 적을 처치하면 체력 "+(1+level()/2)+" 회복. 대가로 회복 물약의 치유량이 35% 줄어듭니다."+progress();}
        @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0xD64254);}
    }
    public static class GreedPouch extends Relic {
        public GreedPouch(){image=ItemSpriteSheet.POUCH;defaultAction=AC_EQUIP;}
        @Override public String name(){return "탐욕의 주머니";}
        @Override public String desc(){return "현재 챕터에서 최상위 티어 장비의 생성 비중과 정예 장비 드롭 확률이 10%p 증가합니다. 원정 골드 250 G마다 받는 피해 +10%(최대 +30%). 창고 골드는 계산하지 않습니다."+progress();}
        @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0xE4BD58);}
    }
    public static class BrokenHourglass extends Relic {
        public BrokenHourglass(){image=ItemSpriteSheet.ARTIFACT_HOURGLASS;}
        @Override protected int cost(){return 4;}
        @Override public String name(){return "깨진 모래시계";}
        @Override public String desc(){return "충전 4를 사용해 3턴 동안 이동과 공격 속도가 2배가 됩니다. 효과가 끝나면 4턴 동안 둔화됩니다. 시간 가속은 중첩되지 않습니다."+progress();}
        @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0xAA80DF);}
        @Override public void execute(Hero hero,String action){super.execute(hero,action);if(USE.equals(action)&&ready(hero)){
            if(hero.buff(BurstTempo.class)!=null||hero.buff(Slow.class)!=null){GLog.w("시간 왜곡이나 둔화가 끝난 뒤 사용하세요.");return;}
            Buff.affect(hero,BurstTempo.class);consume(hero);
        }}
    }
    public static class HuntersMark extends Relic {
        public HuntersMark(){image=ItemSpriteSheet.ARTIFACT_TALISMAN;}
        @Override protected int cost(){return 2;}
        @Override public String name(){return "사냥꾼의 표식";}
        @Override public String desc(){return "충전 2로 보이는 적 하나를 8턴 동안 표식합니다. 표식한 적에게 물리 피해 +"+(30+4*level())+"%, 표식이 남아 있는 동안 다른 적에게 물리 피해 -15%."+progress();}
        @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x76C28F);}
        @Override public void execute(final Hero hero,String action){super.execute(hero,action);if(USE.equals(action)&&ready(hero))GameScene.selectCell(new CellSelector.Listener(){
            @Override public String prompt(){return "표식할 적을 선택하세요.";}
            @Override public void onSelect(Integer cell){
                if(cell==null)return;Char enemy=Actor.findChar(cell);
                if(!(enemy instanceof Mob)||enemy.alignment!=Char.Alignment.ENEMY||!Dungeon.level.heroFOV[cell]){GLog.w("보이는 적을 선택하세요.");return;}
                for(Mob mob:Dungeon.level.mobs)Buff.detach(mob,Hunted.class);
                Buff.prolong(enemy,Hunted.class,8);GLog.p(enemy.name()+"에게 표식을 남겼습니다.");consume(hero);
            }
        });}
    }
    public static class UnstableCompass extends Relic {
        public UnstableCompass(){image=ItemSpriteSheet.ARTIFACT_BEACON;}
        @Override protected int cost(){return 3;}
        @Override public String name(){return "불안정한 나침반";}
        @Override public String desc(){return "충전 3으로 가까운 숨겨진 문이나 미탐색 보물의 방향을 알아내고 2턴 동안 비밀을 감지합니다. 대가로 주변 8칸의 적을 깨우고 자신의 위치를 알립니다."+progress();}
        @Override public ItemSprite.Glowing glowing(){return new ItemSprite.Glowing(0x58B4D9);}
        @Override public void execute(Hero hero,String action){super.execute(hero,action);if(USE.equals(action)&&ready(hero)){
            int best=-1,distance=Integer.MAX_VALUE;
            for(int cell=0;cell<Dungeon.level.length();cell++)if(Dungeon.level.map[cell]==Terrain.SECRET_DOOR||Dungeon.level.heaps.get(cell)!=null&&!Dungeon.level.visited[cell]){
                int d=Dungeon.level.distance(hero.pos,cell);if(d<distance){distance=d;best=cell;}
            }
            if(best>=0){int dx=best%Dungeon.level.width()-hero.pos%Dungeon.level.width(),dy=best/Dungeon.level.width()-hero.pos/Dungeon.level.width();
                String direction=(dy<0?"북":dy>0?"남":"")+(dx<0?"서":dx>0?"동":"");GLog.p("나침반: "+direction+"쪽, 약 "+distance+"칸에 비밀 또는 보물이 있습니다.");
            }else GLog.i("나침반이 보물의 기척을 찾지 못했습니다.");
            Buff.prolong(hero,Foresight.class,2);
            for(Mob mob:Dungeon.level.mobs)if(mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.distance(hero.pos,mob.pos)<=8)mob.beckon(hero.pos);
            consume(hero);
        }}
    }
    public static class Hunted extends FlavourBuff { {type=buffType.NEGATIVE;} @Override public String name(){return "사냥 표식";} @Override public String desc(){return "사냥꾼의 표식에 지정된 적입니다.";} @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.MARK;} }
    public static class BurstTempo extends Buff {
        private int turns=3; {type=buffType.POSITIVE;}
        @Override public String name(){return "시간 가속";}
        @Override public String desc(){return "이동·공격 속도 2배. 종료 후 4턴 둔화.";}
        @Override public boolean act(){if(--turns<0){Char owner=target;detach();Buff.prolong(owner,Slow.class,4);}else spend(TICK);return true;}
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("turns",turns);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);turns=b.getInt("turns");}
        @Override public int icon(){return com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator.TIME;}
    }
    private ExpeditionArtifacts(){}
}
