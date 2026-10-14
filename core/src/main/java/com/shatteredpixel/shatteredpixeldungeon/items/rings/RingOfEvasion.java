/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RingOfEvasion extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_EVASION;
		buffClass = Evasion.class;
	}

    public String statsInfo() {
        String info=isIdentified()?"이 반지의 회피 수치 배율: ×"+Messages.decimalFormat("#.##",com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionEvasion.ringMultiplier(soloBuffedBonus())):"일반적인 회피 수치 배율: ×1.13";
        if(isIdentified()&&Dungeon.hero!=null&&isEquipped(Dungeon.hero)&&soloBuffedBonus()!=combinedBuffedBonus(Dungeon.hero))
            info+="\n\n장착한 회피 반지 합산 배율: ×"+Messages.decimalFormat("#.##",com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionEvasion.ringMultiplier(combinedBuffedBonus(Dungeon.hero)));
        return info+"\n\n회피 확률은 적의 명중과 버프에 따라 달라지며 원정에서는 최대 80%입니다. 반지 배율은 확률이 아닙니다. 일시적인 회피 기술에도 같은 상한을 적용합니다.";
    }
    public String upgradeStat1(int level){
        if(cursed&&cursedKnown)level=Math.min(-1,level-3);
        return "×"+Messages.decimalFormat("#.##",com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionEvasion.ringMultiplier(level+1));
    }

	@Override
	protected RingBuff buff( ) {
		return new Evasion();
	}
	
	public static float evasionMultiplier( Char target ){
		int bonus=getBuffedBonus(target,Evasion.class);
        if(target instanceof com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero&&((com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero)target).extractionRaidID!=0)
            return com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionEvasion.ringMultiplier(bonus);
        return (float)com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionScaling.linear(1.125,bonus);
	}

	public class Evasion extends RingBuff {
	}
}
