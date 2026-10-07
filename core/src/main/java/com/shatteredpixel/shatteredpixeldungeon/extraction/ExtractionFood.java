/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
/** Keep bought food droppable while suppressing every natural food source. */
public final class ExtractionFood {
    public static boolean blockNatural(Item item){return ExtractionDifficulty.active()&&item instanceof Food&&!item.extractionPurchasedFood;}
    public static Item purchased(Item item){if(item instanceof Food)item.extractionPurchasedFood=true;return item;}
    private ExtractionFood(){}
}
