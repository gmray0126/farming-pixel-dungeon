/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.levels.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;
/** A disposable native dungeon: real combat on copies; no expedition or profile rewards. */
public final class ExtractionTraining {
    private static boolean running;
    public static boolean active(){return running;}
    public static void finish(){if(running){running=false;Dungeon.hero=null;Dungeon.level=null;Actor.clear();}}
    public static void enter(){
        if(ExtractionProfile.get().active)return;
        running=true;
        try{
            Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();Dungeon.init();Dungeon.depth=1;Dungeon.branch=0;Dungeon.challenges=0;
            Room level=new Room();level.create();Dungeon.switchLevel(level,level.width()*9+7);
            for(Buff b:Dungeon.hero.buffs(Hunger.class))b.detach();
            ShatteredPixelDungeon.switchScene(GameScene.class);
        }catch(RuntimeException e){finish();Game.scene().add(new WndMessage("훈련장을 열지 못했습니다: "+e.getMessage()));}
    }
    public static void report(){
        if(!running||!(Dungeon.level instanceof Room))return;
        Room level=(Room)Dungeon.level;StringBuilder text=new StringBuilder("준비 장비와 성장 노드로 실제 공격·주문을 시험합니다. 훈련에서는 피해를 받지 않고 원정 저장·물품·경험치를 변경하지 않습니다. 종료는 메뉴의 훈련 종료를 누르세요.\n");
        for(Mob m:level.mobs)if(m instanceof Dummy){Dummy d=(Dummy)m;text.append("\n").append(d.name()).append(" · 명중 ").append(d.hits).append("회 · 총 피해 ").append(d.total).append(" · 최근 ").append(d.last).append(" · 최고 ").append(d.highest);}
        GameScene.show(new WndOptions("훈련 기록",text.toString(),"기록·상태 초기화","닫기"){
            @Override protected void onSelect(int i){if(i==0){for(Mob m:level.mobs)if(m instanceof Dummy){Dummy d=(Dummy)m;d.hits=d.total=d.last=d.highest=0;for(Buff b:d.buffs())b.detach();d.HP=d.HT;}Dungeon.hero.HP=Dungeon.hero.HT;}}
        });
    }
    public static class Room extends Level {
        @Override protected boolean build(){setSize(15,13);java.util.Arrays.fill(map,Terrain.WALL);for(int y=1;y<12;y++)for(int x=1;x<14;x++)map[y*15+x]=Terrain.EMPTY;viewDistance=12;transitions.add(new com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition(this,9*width()+7,com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.REGULAR_ENTRANCE));return true;}
        @Override protected void createMobs(){for(int x:new int[]{4,7,10}){Dummy d=new Dummy();d.pos=4*width()+x;d.trainingArmor=x==10?10:0;d.trainingEvasion=x==4?10:0;mobs.add(d);}}
        @Override protected void createItems(){}
        @Override public Actor addRespawner(){return null;}
        @Override public Mob createMob(){return null;}
        @Override public int randomRespawnCell(Char ch){return 9*width()+7;}
        @Override public String tilesTex(){return Assets.Environment.TILES_CITY;}
    }
    public static class Dummy extends Mob {
        public int trainingArmor,trainingEvasion,hits,total,last,highest;
        {HP=HT=100000000;EXP=0;maxLvl=0;spriteClass=DummySprite.class;state=PASSIVE;properties.add(Property.IMMOVABLE);}
        @Override public String name(){return trainingArmor>0?"중갑 허수아비":trainingEvasion>0?"회피 허수아비":"훈련 허수아비";}
        @Override public String description(){return "공격하지 않는 훈련 대상입니다. 물리 방어 "+trainingArmor+", 회피 수치 "+trainingEvasion+". 명중 "+hits+"회 · 누적 피해 "+total+" · 최근 피해 "+last;}
        @Override protected boolean act(){spend(TICK);return true;}
        @Override public int defenseSkill(Char enemy){return trainingEvasion;}
        @Override public int drRoll(){return trainingArmor;}
        @Override public void damage(int damage,Object source){int before=HP;super.damage(damage,source);last=Math.max(0,before-HP);if(last>0){hits++;total=(int)Math.min(Integer.MAX_VALUE,(long)total+last);highest=Math.max(highest,last);}HP=HT;}
        @Override public void die(Object cause){HP=HT;}
    }
    public static class DummySprite extends StatueSprite { public DummySprite(){super();hardlight(.75f,.55f,.3f);} }
    private ExtractionTraining(){}
}
