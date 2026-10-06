/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.WornShortsword;

/** Free fallback equipment cannot be sold for unlimited gold. */
public class BasicExpeditionSword extends WornShortsword {
    @Override public String name(){return "보급 낡은 검";}
    @Override public String desc(){return "준비한 무기가 없으면 출격 때 지급되는 T1 검입니다. 판매할 수 없습니다. 강화하면 탈출 후 창고에 보관되며 최대 +3까지 강화할 수 있습니다.";}
    @Override public int value(){return 0;}
    @Override public int energyVal(){return 0;}
}
