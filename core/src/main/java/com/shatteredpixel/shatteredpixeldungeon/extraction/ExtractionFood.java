/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
/** Preserve the purchased-food marker for existing saves and alchemy. */
public final class ExtractionFood {
    public static Item purchased(Item item){if(item instanceof Food)item.extractionPurchasedFood=true;return item;}
    private ExtractionFood(){}
}
