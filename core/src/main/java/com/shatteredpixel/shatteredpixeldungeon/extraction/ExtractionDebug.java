/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.watabou.utils.Reflection;
import java.util.ArrayList;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;

/** Explicit, opt-in developer actions. Normal loot and progression remain independent. */
public final class ExtractionDebug {
    private ExtractionDebug(){}
    public static ArrayList<Item> equipmentCatalog(Generator.Category category){
        ExtractionProfile.get().requireDebug();
        if(category!=Generator.Category.RING&&category!=Generator.Category.ARTIFACT)throw new IllegalArgumentException("반지 또는 유물을 선택하세요.");
        ArrayList<Item> items=new ArrayList<>();
        for(Class<?> type:category.classes){
            Item item=(Item)Reflection.newInstance(type);
            if(item==null)throw new IllegalStateException("물품 생성 실패: "+type.getSimpleName());
            items.add(prepareEquipment(item,0));
        }
        return items;
    }
    public static Item prepareEquipment(Item item,int level){
        ExtractionProfile.get().requireDebug();
        int rank=Math.max(0,Math.min(level,WeaponUpgradeLimit.cap(item)));
        if(item instanceof Artifact)((Artifact)item).transferUpgrade(rank);else item.level(rank);
        return identifyItem(item);
    }
    public static Item identifyItem(Item item){
        item.cursed=false;
        // The lobby can have no live hero or ring appearance handler.
        if(item instanceof Ring)return ((Ring)item).identifyForExtraction();
        return item.identify(false);
    }
    public static Hero requireRaid(){
        ExtractionProfile p=ExtractionProfile.get();p.requireDebug();
        Hero h=Dungeon.hero;
        if(!p.active||h==null||h.extractionRaidID!=p.raidID||!h.isAlive()||Dungeon.level==null)
            throw new IllegalStateException("진행 중인 원정에서 사용하세요.");
        if(!h.ready)throw new IllegalStateException("행동이 끝난 뒤 다시 사용하세요.");
        return h;
    }
    public static void saveRaid(){
        try{Dungeon.saveAll();}catch(java.io.IOException e){throw new IllegalStateException("원정 저장 실패",e);}
    }
    public static void give(Item item,boolean raid){
        if(raid){Hero h=requireRaid();if(!item.collect(h.belongings.backpack))Dungeon.level.drop(item,h.pos);saveRaid();}
        else ExtractionProfile.get().debugStore(item);
    }
    public static void heal(){Hero h=requireRaid();h.HP=h.HT;Hunger hunger=h.buff(Hunger.class);if(hunger!=null)hunger.satisfy(10000);saveRaid();}
    public static void toggleInvulnerable(){Hero h=requireRaid();h.extractionDebugInvulnerable=!h.extractionDebugInvulnerable;saveRaid();}
    public static int checkedDepth(int target){
        ExtractionProfile p=ExtractionProfile.get();p.requireDebug();
        int start=ExtractionDifficulty.startDepth(p.raidChapter);
        if(!p.active||target<start||target>start+4)throw new IllegalArgumentException("현재 챕터 안에서만 이동할 수 있습니다.");
        return target;
    }
    public static void jump(int target){
        Hero h=requireRaid();checkedDepth(target);h.interrupt();
        if(target==Dungeon.depth&&Dungeon.branch==0)return;
        InterlevelScene.returnDepth=target;InterlevelScene.returnBranch=0;InterlevelScene.returnPos=-1;
        InterlevelScene.curTransition=null;InterlevelScene.mode=InterlevelScene.Mode.RETURN;
        ShatteredPixelDungeon.switchScene(InterlevelScene.class);
    }
    public static void reveal(){
        requireRaid();for(int i=0;i<Dungeon.level.length();i++)if(Dungeon.level.discoverable[i])Dungeon.level.mapped[i]=true;
        Dungeon.observe();saveRaid();
    }
    public static void weakenEnemies(){
        requireRaid();for(com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob mob:Dungeon.level.mobs)
            if(mob.alignment==com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY&&mob.HP>0)mob.HP=1;
        saveRaid();
    }
}
