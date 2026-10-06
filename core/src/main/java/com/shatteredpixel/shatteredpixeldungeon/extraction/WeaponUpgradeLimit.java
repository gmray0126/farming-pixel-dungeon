/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;

/** Permanent upgrade limits for tiered weapons in this farming fork. */
public final class WeaponUpgradeLimit {
    public static int tier(Item item){
        if(item instanceof MeleeWeapon)return ((MeleeWeapon)item).tier;
        if(item instanceof MissileWeapon)return ((MissileWeapon)item).tier;
        return 0;
    }
    public static int cap(Item item){int tier=tier(item);return tier>0?tier*3:Integer.MAX_VALUE;}
    public static int clamp(Item item,int level){return Math.min(level,cap(item));}
    public static boolean canIncrease(Item item){return item.trueLevel()<cap(item);}
    public static boolean eligible(Item item){return item.isUpgradable()&&canIncrease(item);}
    public static String description(Item item){
        if(tier(item)<=0)return "";
        return "\n\nT"+tier(item)+" · 영구 강화 "+(item.levelKnown?"+"+item.trueLevel()+" / ":"상한 ")+"+"+cap(item)
            +(canIncrease(item)?"":"\n영구 강화 상한에 도달했습니다.");
    }
    private WeaponUpgradeLimit(){}
}
