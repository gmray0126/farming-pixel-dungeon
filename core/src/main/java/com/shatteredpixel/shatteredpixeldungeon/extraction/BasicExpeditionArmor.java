/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClothArmor;

/** Free fallback armor follows the existing expedition sword rules. */
public class BasicExpeditionArmor extends ClothArmor {
    @Override public String name(){return "보급 천 갑옷";}
    @Override public String desc(){return "준비한 갑옷이 없으면 출격 때 지급되는 T1 갑옷입니다. 판매할 수 없습니다. 강화하거나 문양을 새기면 탈출 후 창고에 보관됩니다.";}
    @Override public int value(){return 0;}
    @Override public int energyVal(){return 0;}
}
