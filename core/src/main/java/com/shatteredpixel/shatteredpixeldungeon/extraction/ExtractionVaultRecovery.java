/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.EscapeCrystal;
import com.shatteredpixel.shatteredpixeldungeon.levels.VaultLevel;
import java.util.ArrayList;
import java.util.Collections;

/** Repairs old saves in which vault entry stored gear but the raid route rejected travel. */
public final class ExtractionVaultRecovery {
    /** Null means no repair. Any overflow is returned to be dropped safely by the scene. */
    public static ArrayList<Item> recover(Hero hero) {
        if (hero == null || hero != Dungeon.hero || hero.extractionRaidID == 0
                || Dungeon.level == null || Dungeon.level instanceof VaultLevel) return null;
        EscapeCrystal crystal = hero.belongings.getItem(EscapeCrystal.class);
        if (crystal == null || crystal.storedItems == null
                || !crystal.storedItems.contains(EscapeCrystal.BELONGINGS)) return null;

        crystal.detachAll(hero.belongings.backpack);
        // Keep everything acquired after the failed entry, including upgraded temporary armor.
        ArrayList<Item> acquired = new ArrayList<>(hero.belongings.backpack.items);
        Collections.addAll(acquired, hero.belongings.weapon, hero.belongings.secondWep,
                hero.belongings.armor, hero.belongings.pants, hero.belongings.boots,
                hero.belongings.artifact, hero.belongings.misc, hero.belongings.ring);
        acquired.removeIf(item -> item == null || item == crystal);
        int gold = Math.addExact(Dungeon.gold, crystal.storedItems.getInt(EscapeCrystal.GOLD));
        int energy = Math.addExact(Dungeon.energy, crystal.storedItems.getInt(EscapeCrystal.ENERGY));
        crystal.restoreHeroBelongings(hero, null);
        Dungeon.gold = gold;
        Dungeon.energy = energy;
        ArrayList<Item> overflow = new ArrayList<>();
        for (Item item : acquired) if (!item.collect(hero.belongings.backpack)) overflow.add(item);
        hero.updateHT(false);
        Item.updateQuickslot();
        return overflow;
    }
    private ExtractionVaultRecovery() {}
}
