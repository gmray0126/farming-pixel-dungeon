/* Extraction fork © 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
/** Stable supply item, independent of per-run randomized potion labels. */
public class SupplyHealingPotion extends PotionOfHealing {
    public SupplyHealingPotion() { anonymize(); image=ItemSpriteSheet.POTION_CRIMSON; }
    @Override public String name() { return "보급 회복 물약"; }
    @Override public String desc() { return "체력을 회복하는 거점 보급 물약입니다."; }
}
