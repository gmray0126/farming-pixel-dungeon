/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/** Selected difficulty is fixed for a raid; it never follows the player's equipment. */
public final class ExtractionDifficulty {
    public static boolean active(){return Dungeon.hero!=null&&Dungeon.hero.extractionRaidID!=0;}
    public static int stage(){return active()?ExtractionProfile.get().raidDifficulty:1;}
    public static int chapter(){return active()?ExtractionProfile.get().raidChapter:1;}
    public static String chapterName(int chapter){return chapter==2?"감옥":"하수도";}
    public static int startDepth(int chapter){return chapter==2?6:1;}
    public static int maxTier(int chapter,int stage){return Math.min(5,2+(stage-1)/3+(chapter-1));}
    public static float healthMultiplier(int chapter,int stage){int d=stage-1;return 1.25f+.23f*d+.055f*d*d+.2f*(chapter-1);}
    public static float damageMultiplier(int stage){int d=stage-1;return 1.1f+.09f*d+.018f*d*d;}
    public static float rewardMultiplier(int chapter,int stage){return 1+.18f*(stage-1)+.25f*(chapter-1);}
    public static float[] tierWeights(int chapter,int stage,boolean greed){
        int max=maxTier(chapter,stage);float[] weights=new float[5];
        if(max==2){weights[0]=greed?60:70;weights[1]=greed?40:30;}
        else{weights[max-3]=20;weights[max-2]=greed?45:55;weights[max-1]=greed?35:25;}
        return weights;
    }
    public static float[] tierWeights(){return tierWeights(chapter(),stage(),ExpeditionArtifacts.has(Dungeon.hero,ExpeditionArtifacts.GreedPouch.class));}
    public static int extraMobs(){return active()?2+(stage()-1)/2:0;}
    public static void prepare(Mob mob){
        if(!active()||mob.extractionScaled||mob.alignment!=Char.Alignment.ENEMY)return;
        mob.extractionScaled=true;
        boolean boss=mob.properties().contains(Char.Property.BOSS);
        if(!boss&&mob.EXP>0&&Random.Float()<.08f+.035f*(stage()-1))mob.extractionElite=1+Random.Int(3);
        float fraction=mob.HP/(float)Math.max(1,mob.HT);
        mob.HT=Math.max(1,Math.round(mob.HT*healthMultiplier(chapter(),stage())*(mob.extractionElite>0?1.25f:1)));
        mob.HP=Math.max(1,Math.round(mob.HT*fraction));
        if(mob.extractionElite>0)mob.EXP=Math.round(mob.EXP*1.5f);
    }
    public static int incoming(int damage,Object source){
        if(!active()||damage<=0)return damage;
        boolean enemy=source instanceof Mob&&((Mob)source).alignment==Char.Alignment.ENEMY;
        boolean tengu=source!=null&&(source.getClass().getName().contains("Tengu$")||source instanceof Class&&((Class<?>)source).getName().contains("Tengu$"));
        if(enemy||tengu){float modifier=damageMultiplier(stage());if(source instanceof Mob&&((Mob)source).extractionElite==1)modifier*=1.2f;return Math.round(damage*modifier);}
        return damage;
    }
    public static int extraArmor(Char enemy){return enemy instanceof Mob&&((Mob)enemy).extractionElite==2?2+(stage()-1)/3:0;}
    public static String eliteName(int type){return type==1?"광폭":type==2?"철갑":type==3?"추적":"";}
    public static void reinforce(Mob boss){
        if(!active()||stage()<3||Dungeon.level==null||!Dungeon.level.locked||boss.HP<=0||!(boss instanceof Goo||boss instanceof Tengu))return;
        int wanted=boss.HP*2<=boss.HT?1:0;if(stage()>=6&&boss.HP*4<=boss.HT)wanted=2;
        if(wanted<=boss.extractionWaves)return;
        boss.extractionWaves=wanted;
        for(int n=0;n<2;n++){
            Mob add=boss instanceof Tengu?(n==0?new Guard():new DM100()):(n==0?new Rat():new Gnoll());
            int cell=-1;
            for(int tries=0;tries<20;tries++){
                int candidate=boss.pos+com.watabou.utils.PathFinder.NEIGHBOURS8[Random.Int(8)];
                if(candidate>=0&&candidate<Dungeon.level.length()&&Dungeon.level.passable[candidate]&&com.shatteredpixel.shatteredpixeldungeon.actors.Actor.findChar(candidate)==null){cell=candidate;break;}
            }
            if(cell>=0){add.pos=cell;add.state=add.HUNTING;prepare(add);GameScene.add(add);add.sprite.showAlert();}
        }
        GLog.w("보스가 지원 병력을 불렀습니다!");
    }
    public static void bossLoot(Mob boss){
        if(!active()||Dungeon.level==null||!(boss instanceof Goo||boss instanceof Tengu))return;
        Item gear=Generator.randomUsingDefaults(Generator.wepTiers[maxTier(chapter(),stage())-1]);
        Dungeon.level.drop(gear,boss.pos).sprite.drop();
        if(Random.Float()<.12f+.035f*(stage()-1)+.1f*(chapter()-1)){
            Artifact relic=Generator.randomArtifact();if(relic!=null)Dungeon.level.drop(relic,boss.pos).sprite.drop();
        }
    }
    public static void eliteLoot(Mob mob){
        if(!active()||mob.extractionElite==0||Dungeon.level==null)return;
        if(Random.Float()<.3f+(ExpeditionArtifacts.has(Dungeon.hero,ExpeditionArtifacts.GreedPouch.class)?.1f:0))Dungeon.level.drop(Generator.randomWeapon(),mob.pos).sprite.drop();
    }
    private ExtractionDifficulty(){}
}
