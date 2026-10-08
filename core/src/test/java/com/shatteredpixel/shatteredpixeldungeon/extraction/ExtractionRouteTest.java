package com.shatteredpixel.shatteredpixeldungeon.extraction;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionRoute.Action.*;
import static com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition.Type.*;

public class ExtractionRouteTest {
    @Test public void prisonIsAnIndependentFiveFloorRaidWithTenguExtraction(){
        for(int floor=6;floor<10;floor++)assertEquals(TRAVEL,ExtractionRoute.action(2,floor,0,REGULAR_EXIT,floor+1,false,false));
        assertEquals(EMERGENCY_EXTRACT,ExtractionRoute.action(2,6,0,REGULAR_ENTRANCE,5,true,false));
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
    @Test public void backtrackingAndStartFloorRetreatAreAllowedButBranchesAreBlocked(){
        for(int floor=2;floor<=5;floor++)assertEquals(TRAVEL,ExtractionRoute.action(floor,0,REGULAR_ENTRANCE,floor-1,false,false));
        assertEquals(EMERGENCY_EXTRACT,ExtractionRoute.action(1,0,REGULAR_ENTRANCE,0,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(3,0,BRANCH_EXIT,3,false,false));
        assertEquals(BLOCK,ExtractionRoute.action(5,1,REGULAR_EXIT,6,true,false));
        assertEquals(BLOCK,ExtractionRoute.action(6,0,REGULAR_EXIT,7,true,false));
    }
    @Test public void everyChapterHasFourDescentsAndOneBossGatedExtraction(){
        for(int c=1;c<=5;c++){
            int start=1+5*(c-1),end=start+4;
            for(int d=start;d<end;d++)assertEquals(TRAVEL,ExtractionRoute.action(c,d,0,REGULAR_EXIT,d+1,false,false));
            for(int d=start+1;d<=end;d++)assertEquals(TRAVEL,ExtractionRoute.action(c,d,0,REGULAR_ENTRANCE,d-1,false,false));
            assertEquals(EMERGENCY_EXTRACT,ExtractionRoute.action(c,start,0,REGULAR_ENTRANCE,start-1,true,false));
            assertEquals(BLOCK,ExtractionRoute.action(c,end,0,REGULAR_EXIT,end+1,false,false));
            assertEquals(BLOCK,ExtractionRoute.action(c,end,0,REGULAR_EXIT,end+1,true,true));
            assertEquals(EXTRACT,ExtractionRoute.action(c,end,0,REGULAR_EXIT,end+1,true,false));
            assertEquals(BLOCK,ExtractionRoute.action(c,end+1,0,REGULAR_EXIT,end+2,true,false));
        }
    }
    @Test public void unknownChaptersAndOptionalBranchesCannotBypassBosses(){
        for(int c:new int[]{0,6,-1})assertEquals(BLOCK,ExtractionRoute.action(c,25,0,REGULAR_EXIT,26,true,false));
        for(int c=1;c<=5;c++)assertEquals(BLOCK,ExtractionRoute.action(c,ExtractionDifficulty.endDepth(c),1,REGULAR_EXIT,ExtractionDifficulty.endDepth(c)+1,true,false));
    }

    @Test public void emergencyExitUsesOnlyAnUnlockedStartingUpStair(){
        assertEquals(EMERGENCY_EXTRACT,ExtractionRoute.action(1,1,0,SURFACE,0,false,false));
        for(int c=1;c<=5;c++){
            int start=ExtractionDifficulty.startDepth(c);
            assertEquals(EMERGENCY_EXTRACT,ExtractionRoute.action(c,start,0,REGULAR_ENTRANCE,start-1,false,false));
            assertEquals(BLOCK,ExtractionRoute.action(c,start,0,REGULAR_ENTRANCE,start-1,false,true));
            assertEquals(BLOCK,ExtractionRoute.action(c,start,1,REGULAR_ENTRANCE,start-1,false,false));
            assertEquals(BLOCK,ExtractionRoute.action(c,start,0,REGULAR_ENTRANCE,start-2,false,false));
            if(c>1)assertEquals(BLOCK,ExtractionRoute.action(c,start,0,SURFACE,0,false,false));
        }
    }
    @Test public void allFloorFallsStayOnTheirCurrentFloor(){
        for(int c=1;c<=5;c++){
            int start=1+5*(c-1),end=start+4;
            for(int d=start;d<=end;d++) assertEquals(d,ExtractionRoute.fallDepth(c,d));
            assertEquals(end-1,ExtractionRoute.fallDepth(c,end-1));
            assertEquals(end,ExtractionRoute.fallDepth(c,end));
        }
    }

}
