/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.levels.LevelTransition;

/** Five original sewer floors; the sixth-floor stair is the extraction point. */
public final class ExtractionRoute {
    public enum Action { BLOCK, TRAVEL, EXTRACT }
    public static Action action(int depth,int branch,LevelTransition.Type type,int destination,boolean bossDefeated,boolean locked){
        if(locked||branch!=0)return Action.BLOCK;
        if(type==LevelTransition.Type.REGULAR_ENTRANCE)
            return depth>1&&depth<=5&&destination==depth-1?Action.TRAVEL:Action.BLOCK;
        if(type!=LevelTransition.Type.REGULAR_EXIT)return Action.BLOCK;
        if(depth>=1&&depth<5&&destination==depth+1)return Action.TRAVEL;
        if(depth==5&&destination==6&&bossDefeated)return Action.EXTRACT;
        return Action.BLOCK;
    }
    private ExtractionRoute(){}
}
