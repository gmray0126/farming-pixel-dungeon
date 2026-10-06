package com.shatteredpixel.shatteredpixeldungeon.extraction;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionRoute.Action.*;
import static com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.*;

public class ExtractionRouteTest {
    @Test public void prisonIsAnIndependentFiveFloorRaidWithTenguExtraction(){
        for(int floor=6;floor<10;floor++)assertEquals(TRAVEL,ExtractionRoute.action(2,floor,0,REGULAR_EXIT,floor+1,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(2,6,0,REGULAR_ENTRANCE,5,true,false));
        assertEquals(BLOCK,ExtractionRoute.action(2,10,0,REGULAR_EXIT,11,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(2,10,0,REGULAR_EXIT,11,true,true));
        assertEquals(EXTRACT,ExtractionRoute.action(2,10,0,REGULAR_EXIT,11,true,false));
        assertEquals(BLOCK,ExtractionRoute.action(1,6,0,REGULAR_EXIT,7,true,false));
    }
    @Test public void firstFourSewerExitsContinueTheRaid(){
        for(int floor=1;floor<5;floor++)assertEquals(TRAVEL,ExtractionRoute.action(floor,0,REGULAR_EXIT,floor+1,false,false));
    }
    @Test public void fifthFloorExitRequiresDefeatedAndUnlockedBoss(){
        assertEquals(BLOCK,ExtractionRoute.action(5,0,REGULAR_EXIT,6,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(5,0,REGULAR_EXIT,6,true,true));
        assertEquals(EXTRACT,ExtractionRoute.action(5,0,REGULAR_EXIT,6,true,false));
    }
    @Test public void backtrackingIsAllowedButNoEarlyOrBranchEscape(){
        for(int floor=2;floor<=5;floor++)assertEquals(TRAVEL,ExtractionRoute.action(floor,0,REGULAR_ENTRANCE,floor-1,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(1,0,REGULAR_ENTRANCE,0,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(3,0,BRANCH_EXIT,3,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(5,1,REGULAR_EXIT,6,true,false));
        assertEquals(BLOCK,ExtractionRoute.action(6,0,REGULAR_EXIT,7,true,false));
    }
}
