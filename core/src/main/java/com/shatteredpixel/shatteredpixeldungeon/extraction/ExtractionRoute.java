/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;

/** Each original chapter is an independent five-floor raid with a boss-gated exit. */
public final class ExtractionRoute {
    public enum Action { BLOCK, TRAVEL, EXTRACT, EMERGENCY_EXTRACT }
    public static Action action(int depth,int branch,LevelTransition.Type type,int destination,boolean bossDefeated,boolean locked){
        return action(1,depth,branch,type,destination,bossDefeated,locked);
    }
    public static Action action(int chapter,int depth,int branch,LevelTransition.Type type,int destination,boolean bossDefeated,boolean locked){
        return action(chapter,depth,branch,type,destination,branch,bossDefeated,locked);
    }
    public static Action action(int chapter,int depth,int branch,LevelTransition.Type type,int destination,int destinationBranch,boolean bossDefeated,boolean locked){
        if(!ExtractionDifficulty.validChapter(chapter))return Action.BLOCK;
        int start=ExtractionDifficulty.startDepth(chapter),end=ExtractionDifficulty.endDepth(chapter);
        if(locked)return Action.BLOCK;
        // Native mines and vaults are side trips on the same floor, never chapter exits.
        if((chapter==3||chapter==4)&&depth>=start&&depth<end&&destination==depth){
            if(branch==0&&destinationBranch==1&&type==LevelTransition.Type.BRANCH_EXIT)return Action.TRAVEL;
            if(branch==1&&destinationBranch==0&&type==LevelTransition.Type.BRANCH_ENTRANCE)return Action.TRAVEL;
        }
        if(branch!=0||destinationBranch!=0)return Action.BLOCK;
        if(depth==start&&destination==start-1&&(type==LevelTransition.Type.REGULAR_ENTRANCE
                ||chapter==1&&type==LevelTransition.Type.SURFACE))return Action.EMERGENCY_EXTRACT;
        if(type==LevelTransition.Type.REGULAR_ENTRANCE)
            return depth>start&&depth<=end&&destination==depth-1?Action.TRAVEL:Action.BLOCK;
        if(type!=LevelTransition.Type.REGULAR_EXIT)return Action.BLOCK;
        if(depth>=start&&depth<end&&destination==depth+1)return Action.TRAVEL;
        if(depth==end&&destination==end+1&&bossDefeated)return Action.EXTRACT;
        return Action.BLOCK;
    }
    /** Falls always relocate within the same floor, including boss floors. */
    public static int fallDepth(int chapter,int depth){
        return depth;
    }
    private ExtractionRoute(){}
}
