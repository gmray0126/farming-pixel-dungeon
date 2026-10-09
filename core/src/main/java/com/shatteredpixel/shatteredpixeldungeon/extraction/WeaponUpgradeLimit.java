/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

/** Permanent upgrade limits for tiered equipment, wands, rings and artifact ranks. */
public final class WeaponUpgradeLimit {
    public static int tier(Item item){
        if(item instanceof MeleeWeapon)return ((MeleeWeapon)item).tier;
        if(item instanceof MissileWeapon)return ((MissileWeapon)item).tier;
        if(item instanceof ExpeditionClothing)return ((ExpeditionClothing)item).tier;
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)return ((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).tier;
        return 0;
    }
    public static int cap(Item item){
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring
            ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
            ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)return 15;
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket)return 3;
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)return ((com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact)item).nativeLevelCap()>0?15:0;
        int tier=tier(item);
        if(tier>0)return Math.min(15,tier*3);
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem)return 15;
        return Integer.MAX_VALUE;
    }
    public static int clamp(Item item,int level){return Math.min(level,cap(item));}
    public static boolean canIncrease(Item item){return item.trueLevel()<cap(item);}
    public static boolean eligible(Item item){return item.isUpgradable()&&canIncrease(item);}
    public static String description(Item item){
        if(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring
            ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand
            ||item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff)return "\n\n영구 강화 "+(item.levelKnown?"+"+item.trueLevel()+" / ":"상한 ")+"+15"+(canIncrease(item)?"":"\n영구 강화 상한에 도달했습니다.");
        if(tier(item)<=0)return "";
        return "\n\nT"+tier(item)+" · 영구 강화 "+(item.levelKnown?"+"+item.trueLevel()+" / ":"상한 ")+"+"+cap(item)
            +(canIncrease(item)?"":"\n영구 강화 상한에 도달했습니다.");
    }
    private WeaponUpgradeLimit(){}
}
