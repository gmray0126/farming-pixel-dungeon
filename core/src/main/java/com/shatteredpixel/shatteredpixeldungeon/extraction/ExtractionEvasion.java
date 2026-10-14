/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
/** Uniform attack/defense rolls: defense <= 2.5 * accuracy gives at least 20% hits. */
public final class ExtractionEvasion {
    public static final float MAX_CHANCE=.8f;
    public static final float ENEMY_DEFENSE_FACTOR=.75f;
    /** Apply once to the final enemy rating, including chapter and elite modifiers. */
    public static float enemyDefense(com.shatteredpixel.shatteredpixeldungeon.actors.Char defender,float defense){
        com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero=com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;
        if(hero==null||hero.extractionRaidID<=0
                ||!(defender instanceof com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob)
                ||defender.alignment!=com.shatteredpixel.shatteredpixeldungeon.actors.Char.Alignment.ENEMY
                ||defense>=com.shatteredpixel.shatteredpixeldungeon.actors.Char.INFINITE_EVASION)return defense;
        return Math.max(0,defense)*ENEMY_DEFENSE_FACTOR;
    }
    public static float cappedDefense(float accuracy,float defense){return Math.max(0,Math.min(defense,Math.max(1,accuracy)*2.5f));}
    public static float chance(float accuracy,float defense){
        float a=Math.max(1,accuracy),d=cappedDefense(a,defense);
        return d<=0?0:d>=a?1-a/(2*d):d/(2*a);
    }
    public static float ringMultiplier(int bonus){return Math.max(.1f,1+.125f*bonus);}
    private ExtractionEvasion(){}
}
