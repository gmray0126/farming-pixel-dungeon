/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.*;
import java.util.ArrayList;

/** Dedicated containers are permanent node unlocks, reissued each expedition. */
public final class ExtractionBags {
    public static final String[] NODES={"bag_seed","bag_scroll","bag_potion","bag_magic"};
    private ExtractionBags(){}
    public static boolean nodeBag(Item item){return item instanceof VelvetPouch||item instanceof ScrollHolder||item instanceof PotionBandolier||item instanceof MagicalHolster;}
    public static void removeShopStock(com.shatteredpixel.shatteredpixeldungeon.levels.Level level){
        if(level==null||level.heaps==null)return;
        for(com.shatteredpixel.shatteredpixeldungeon.items.Heap heap:level.heaps.valueList())if(heap.type==com.shatteredpixel.shatteredpixeldungeon.items.Heap.Type.FOR_SALE){
            heap.items.removeIf(ExtractionBags::nodeBag);
            if(heap.items.isEmpty())level.heaps.remove(heap.pos);
        }
    }
    public static Bag create(int n){return n==0?new VelvetPouch():n==1?new ScrollHolder():n==2?new PotionBandolier():new MagicalHolster();}
    public static void sync(Hero hero){
        if(hero==null)return;
        ExtractionProfile profile=ExtractionProfile.get();Bag backpack=hero.belongings.backpack;
        for(int n=0;n<NODES.length;n++){
            Bag template=create(n);boolean unlocked=profile.nodes.contains(NODES[n]);Bag kept=null;
            for(Item item:new ArrayList<>(backpack.items))if(template.getClass().isInstance(item)){
                if(unlocked&&kept==null){kept=(Bag)item;continue;}
                Bag obsolete=(Bag)item;backpack.items.remove(obsolete);Dungeon.quickslot.clearItem(obsolete);
                for(Item inside:new ArrayList<>(obsolete.items)){
                    obsolete.items.remove(inside);
                    // Old saves can exceed the new capacity. Preserve contents and let play empty the overflow.
                    if(!inside.collect(backpack)){
                        backpack.items.add(inside);
                        if(inside instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand)((com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand)inside).charge(hero);
                    }
                }
                obsolete.owner=null;
            }
            if(unlocked&&kept==null&&!template.collect(backpack))throw new IllegalStateException("해금한 전용 가방을 지급하지 못했습니다.");
        }
        // All native shops must skip these facilities, including previously chosen bags.
        Dungeon.LimitedDrops.VELVET_POUCH.drop();Dungeon.LimitedDrops.SCROLL_HOLDER.drop();
        Dungeon.LimitedDrops.POTION_BANDOLIER.drop();Dungeon.LimitedDrops.MAGICAL_HOLSTER.drop();
    }
}
