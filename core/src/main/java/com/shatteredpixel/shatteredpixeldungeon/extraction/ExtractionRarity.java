/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand;
import com.watabou.utils.Random;
/** Quality is independent of tier, enchantment, glyph and permanent upgrade level. */
public final class ExtractionRarity {
    public static boolean supported(Item item){return item instanceof MeleeWeapon||item instanceof MissileWeapon||item instanceof Armor||item instanceof ExpeditionClothing||item instanceof DamageWand;}
    public static <T extends Item> T rollNew(T item){
        if(!supported(item)||item.extractionQualityRolled)return item;
        item.extractionQualityRolled=true;
        return promoteLoot(item,Random.Float(),.08f);
    }
    public static <T extends Item> T promoteLoot(T item,float roll,float chance){
        if(ExtractionDifficulty.hard()&&supported(item)&&roll<chance)item.extractionRare=true;
        return item;
    }
    public static int damage(Item item,int damage){return Math.max(0,Math.round(damage*multiplier(item)));}
    public static float multiplier(Item item){return item!=null&&item.extractionRare?1.2f:1;}
    public static String description(Item item){
        if(!item.extractionRare)return "";
        boolean defensive=item instanceof Armor||item instanceof ExpeditionClothing;
        return "\n\n_레어 등급_ · "+(defensive?"장비의 물리 방어 성능":item instanceof DamageWand?"직접 마법 피해":"물리 공격 피해")+" +20%. 강화 상한·요구 힘·인챈트·문양은 기존 규칙을 따릅니다. 하드 원정에서 획득한 등급은 탈출·강화·보관 후에도 유지됩니다.";
    }
    private ExtractionRarity(){}
}
