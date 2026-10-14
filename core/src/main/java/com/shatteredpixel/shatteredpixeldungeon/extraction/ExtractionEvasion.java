/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
/** Uniform attack/defense rolls: defense <= 2.5 * accuracy gives at least 20% hits. */
public final class ExtractionEvasion {
    public static final float MAX_CHANCE=.8f;
    public static float cappedDefense(float accuracy,float defense){return Math.max(0,Math.min(defense,Math.max(1,accuracy)*2.5f));}
    public static float chance(float accuracy,float defense){
        float a=Math.max(1,accuracy),d=cappedDefense(a,defense);
        return d<=0?0:d>=a?1-a/(2*d):d/(2*a);
    }
    public static float ringMultiplier(int bonus){return Math.max(.1f,1+.125f*bonus);}
    private ExtractionEvasion(){}
}
