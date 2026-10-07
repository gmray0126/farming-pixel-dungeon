/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

/** Each original chapter is an independent five-floor raid with a boss-gated exit. */
public final class ExtractionRoute {
    public enum Action { BLOCK, TRAVEL, EXTRACT }
    public static Action action(int depth,int branch,LevelTransition.Type type,int destination,boolean bossDefeated,boolean locked){
        return action(1,depth,branch,type,destination,bossDefeated,locked);
    }
    public static Action action(int chapter,int depth,int branch,LevelTransition.Type type,int destination,boolean bossDefeated,boolean locked){
        if(!ExtractionDifficulty.validChapter(chapter))return Action.BLOCK;
        int start=ExtractionDifficulty.startDepth(chapter),end=ExtractionDifficulty.endDepth(chapter);
        if(locked||branch!=0)return Action.BLOCK;
        if(type==LevelTransition.Type.REGULAR_ENTRANCE)
            return depth>start&&depth<=end&&destination==depth-1?Action.TRAVEL:Action.BLOCK;
        if(type!=LevelTransition.Type.REGULAR_EXIT)return Action.BLOCK;
        if(depth>=start&&depth<end&&destination==depth+1)return Action.TRAVEL;
        if(depth==end&&destination==end+1&&bossDefeated)return Action.EXTRACT;
        return Action.BLOCK;
    }
    private ExtractionRoute(){}
}
