/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
/** The archer starter is reissued by its node and cannot produce gold or energy. */
public class NodeSpiritBow extends SpiritBow {
    @Override public String name(){return "영혼의 활";}
    @Override public String desc(){return super.desc()+"\n\n활·투척의 사격 입문 노드를 배우면 출격 때 기본 지급됩니다. 판매하거나 연금술 에너지로 바꿀 수 없습니다. 손대지 않은 활은 탈출 때 회수되고 다음 출격에 다시 지급됩니다. 인챈트하거나 증강한 활은 보관됩니다.";}
    @Override public int value(){return 0;}
    @Override public int energyVal(){return 0;}
}
