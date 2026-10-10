/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
import java.util.Comparator;

/** Stash operations preserve distinct gear and all consumable state. */
public final class ExtractionStash {
    public static boolean equipment(Item item){
        return item instanceof EquipableItem||item instanceof Wand||item instanceof Trinket;
    }
    public static void identifyEquipment(Item item){
        if(item instanceof Ring)((Ring)item).identifyForExtraction();
        else if(equipment(item))item.identify(false);
    }
    private static int category(Item item){
        if(item instanceof KindOfWeapon)return 0;
        if(item instanceof Armor)return 1;
        if(item instanceof ExpeditionClothing)return 2;
        if(item instanceof Ring)return 3;
        if(item instanceof Wand)return 4;
        if(item instanceof Artifact)return 5;
        if(item instanceof Trinket)return 6;
        if(item instanceof Potion)return 7;
        if(item instanceof Scroll)return 8;
        if(item instanceof Plant.Seed)return 9;
        if(item instanceof Food)return 10;
        return 11;
    }
    private static String stackState(Item item){
        Bundle state=new Bundle();item.storeInBundle(state);
        state.remove("quantity");state.remove("levelKnown");state.remove("cursedKnown");state.remove("quickslotpos");
        return state.toString();
    }
    /** Work on copies so both caller-held items and failed saves remain unchanged. */
    public static ArrayList<Item> organized(ArrayList<Item> source){
        ArrayList<Item> result=new ArrayList<>();
        for(Item original:source){
            Item item=original.duplicate();
            if(item==null)throw new IllegalStateException("창고 물품을 복사하지 못했습니다.");
            boolean combined=false;
            if(item.stackable&&!equipment(item)&&!(item instanceof Bag)){
                String state=stackState(item);
                for(Item existing:result){
                    if(existing.stackable&&!equipment(existing)&&existing.isSimilar(item)&&item.isSimilar(existing)
                            &&stackState(existing).equals(state)&&(long)existing.quantity()+item.quantity()<=Integer.MAX_VALUE){
                        existing.quantity(Math.addExact(existing.quantity(),item.quantity()));
                        existing.levelKnown|=item.levelKnown;existing.cursedKnown|=item.cursedKnown;
                        combined=true;break;
                    }
                }
            }
            if(!combined)result.add(item);
        }
        result.sort(Comparator.comparingInt(ExtractionStash::category).thenComparing(Item::name)
                .thenComparing(i->i.getClass().getName()).thenComparing(Comparator.comparingInt(Item::visiblyUpgraded).reversed()));
        return result;
    }
    private ExtractionStash(){}
}
