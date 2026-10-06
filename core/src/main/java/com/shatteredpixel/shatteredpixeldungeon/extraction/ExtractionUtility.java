/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Goo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.Chasm;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import static com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionGrowth.Stat.*;

/** Shared utility traits, independent of original classes and held weapons. */
public final class ExtractionUtility {
    private ExtractionUtility(){}
    public static float value(Hero hero,ExtractionGrowth.Stat stat){
        return hero!=null&&hero.extractionRaidID!=0?ExtractionProfile.get().bonus(stat):0;
    }
    public static float timeMultiplier(Hero hero,ExtractionGrowth.Stat stat){return 1-Math.min(75,value(hero,stat))/100f;}
    public static float lowHealthMultiplier(Hero hero,ExtractionGrowth.Stat stat){
        return hero.HP<=hero.HT*0.35f?1+value(hero,stat)/100f:1;
    }
    private static void heal(Hero h,float amount){h.HP=Math.min(h.HT,h.HP+Math.round(amount));}
    // Refresh to a maximum rather than accumulating shields or durations indefinitely.
    private static void shield(Hero h,float amount){if(amount>0)Buff.affect(h,Barrier.class).setShield(Math.round(amount));}
    private static void haste(Hero h,float turns){if(turns>0)Buff.prolong(h,Haste.class,turns);}
    private static void recharge(Hero h,float turns){if(turns>0)Buff.prolong(h,Recharging.class,turns);}
    public static void potion(Hero h){
        shield(h,value(h,POTION_SHIELD));haste(h,value(h,POTION_HASTE));recharge(h,value(h,POTION_RECHARGE));
    }
    public static void food(Hero h){
        heal(h,value(h,FOOD_HEAL));shield(h,value(h,FOOD_SHIELD));haste(h,value(h,FOOD_HASTE));recharge(h,value(h,FOOD_RECHARGE));
        float turns=value(h,FOOD_INVIS);if(turns>0)Buff.prolong(h,Invisibility.class,turns);
    }
    public static boolean floor(Hero h,int depth){
        if(h==null||h.extractionRaidID==0||depth<1||depth>10||(h.extractionVisited&(1<<depth))!=0)return false;
        h.extractionVisited|=1<<depth;
        heal(h,value(h,FLOOR_HEAL));shield(h,value(h,FLOOR_SHIELD));
        float turns=value(h,FLOOR_AWARENESS);if(turns>0)Buff.prolong(h,Awareness.class,turns);
        turns=value(h,FLOOR_MINDVISION);if(turns>0)Buff.prolong(h,MindVision.class,turns);
        turns=value(h,FLOOR_FORESIGHT);if(turns>0)Buff.prolong(h,Foresight.class,turns);
        turns=value(h,FLOOR_INVIS);if(turns>0)Buff.prolong(h,Invisibility.class,turns);
        return true;
    }
    public static void defeated(Hero h,Mob mob){
        if(h==null||h.extractionRaidID==0||!h.isAlive()||mob.alignment!=Char.Alignment.ENEMY)return;
        if(!h.extractionBossDefeated&&Dungeon.branch==0&&((mob instanceof Goo&&Dungeon.depth==5&&ExtractionDifficulty.chapter()==1)||(mob instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Tengu&&Dungeon.depth==10&&ExtractionDifficulty.chapter()==2))){
            h.extractionBossDefeated=true;
            if(h.sprite!=null)ExtractionDifficulty.bossLoot(mob);
            if(h.sprite!=null)GLog.p("챕터 보스 처치! 아래 계단으로 탈출할 수 있습니다.");
        }
        if(mob.EXP>0&&Dungeon.level!=null&&Dungeon.level.heroFOV[mob.pos]){kill(h);ExpeditionArtifacts.kill(h);}
        if(h.sprite!=null)ExtractionDifficulty.eliteLoot(mob);
    }
    public static void kill(Hero h){
        heal(h,value(h,KILL_HEAL));shield(h,value(h,KILL_SHIELD));haste(h,value(h,KILL_HASTE));recharge(h,value(h,KILL_RECHARGE));
    }
    public static int attack(Hero h,Char enemy,int damage){
        float multiplier=lowHealthMultiplier(h,LOW_HP_DAMAGE);
        if(h.extractionRaidID!=0&&enemy instanceof Mob&&((Mob)enemy).surprisedBy(h)){
            multiplier*=1+value(h,SURPRISE_DAMAGE)/100f;recharge(h,value(h,SURPRISE_RECHARGE));
        }
        return ExpeditionArtifacts.attack(h,enemy,Math.round(damage*multiplier));
    }
    private static boolean source(Object src,Class<?> type){return src==type||type.isInstance(src);}
    public static int incoming(Hero h,int damage,Object src){
        if(h.extractionRaidID==0||damage<=0)return damage;
        damage=ExpeditionArtifacts.incoming(h,ExtractionDifficulty.incoming(damage,src));
        float reduction=0;
        boolean poison=source(src,Poison.class),burn=source(src,Burning.class),bleed=source(src,Bleeding.class),ooze=source(src,Ooze.class);
        if(poison||burn||bleed||ooze)reduction+=value(h,DOT_REDUCTION);
        if(poison)reduction+=value(h,POISON_REDUCTION);
        if(burn)reduction+=value(h,BURN_REDUCTION);
        if(bleed)reduction+=value(h,BLEED_REDUCTION);
        if(src instanceof Trap)reduction+=value(h,TRAP_REDUCTION);
        if(source(src,Chasm.class))reduction+=value(h,FALL_REDUCTION);
        if(src instanceof Char){
            Char enemy=(Char)src;
            if(Char.hasProp(enemy,Char.Property.BOSS))reduction+=value(h,BOSS_REDUCTION);
            if(Dungeon.level!=null&&Dungeon.level.distance(h.pos,enemy.pos)>1)reduction+=value(h,RANGED_REDUCTION);
        }
        return (int)Math.ceil(damage*(1-Math.min(75,reduction)/100f));
    }
    /** One shared rescue charge for both final traits; persistent in the hero save. */
    public static boolean rescue(Hero h,int damage,Object src){
        float percent=value(h,SECOND_WIND);
        int protection=src instanceof Hunger?0:h.shielding();
        if(percent<=0||h.extractionSecondWindUsed||h.HP<=0||damage<h.HP+protection)return false;
        h.extractionSecondWindUsed=true;h.HP=Math.max(1,Math.round(h.HT*percent/100f));
        Buff.detach(h,Poison.class);Buff.detach(h,Burning.class);Buff.detach(h,Bleeding.class);Buff.detach(h,Ooze.class);
        Buff.prolong(h,Invisibility.class,3);
        if(h.sprite!=null)GLog.p("생존 특성 발동! 치명상을 한 번 견뎠습니다.");
        return true;
    }
}
