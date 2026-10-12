package com.shatteredpixel.shatteredpixeldungeon.extraction;
import org.junit.Test;
import static org.junit.Assert.*;

public class GrowthAtlasViewportTest {
    @Test public void everyNodeHasClearanceIncludingOuterRingsAcrossAllBranches(){
        for(int i=0;i<ExtractionGrowth.NODES.length;i++){
            float r=GrowthAtlasLayout.visualRadius(i);
            assertTrue(ExtractionGrowth.IDS[i],GrowthAtlasLayout.X[i]>=r&&GrowthAtlasLayout.X[i]<=GrowthAtlasLayout.SIZE-r);
            assertTrue(ExtractionGrowth.IDS[i],GrowthAtlasLayout.Y[i]>=r&&GrowthAtlasLayout.Y[i]<=GrowthAtlasLayout.SIZE-r);
            assertTrue("Start point overlaps "+ExtractionGrowth.IDS[i],Math.hypot(GrowthAtlasLayout.X[i]-GrowthAtlasLayout.CENTER,GrowthAtlasLayout.Y[i]-GrowthAtlasLayout.CENTER)>=22+r+GrowthAtlasLayout.NODE_CLEARANCE-.01f);
            for(int j=0;j<i;j++){
                double gap=Math.hypot(GrowthAtlasLayout.X[i]-GrowthAtlasLayout.X[j],GrowthAtlasLayout.Y[i]-GrowthAtlasLayout.Y[j])-r-GrowthAtlasLayout.visualRadius(j);
                assertTrue(ExtractionGrowth.IDS[i]+" overlaps "+ExtractionGrowth.IDS[j]+"; gap="+gap,gap>=GrowthAtlasLayout.NODE_CLEARANCE-.01f);
            }
        }
    }
    @Test public void crisisResponseAndBagNodesRemainSeparatelySelectable(){
        for(String id:new String[]{"ward_0","ward_7","ward_8","strength_mid","pack","porter",ExtractionBags.NODES[0],ExtractionBags.NODES[1],ExtractionBags.NODES[2],ExtractionBags.NODES[3]}){
            int n=ExtractionGrowth.index(id);
            assertEquals(id,n,GrowthAtlasLayout.nearest(GrowthAtlasLayout.X[n],GrowthAtlasLayout.Y[n],8));
            float x=GrowthAtlasLayout.X[n]+GrowthAtlasLayout.RADIUS[n]*.6f;
            assertEquals(id,n,GrowthAtlasLayout.nearest(x,GrowthAtlasLayout.Y[n],8));
        }
    }
    @Test public void fitShowsTheWholeGraphAndNodeSelectionMatchesPositions(){
        GrowthAtlasViewport view=new GrowthAtlasViewport();view.bounds(128,230);
        assertTrue(view.worldX(0)<=0.001f);assertTrue(view.worldX(128)>=GrowthAtlasLayout.SIZE-0.001f);
        assertTrue(view.worldY(0)<=0);assertTrue(view.worldY(230)>=GrowthAtlasLayout.SIZE);
        for(int n=0;n<ExtractionGrowth.NODES.length;n++){
            assertTrue(GrowthAtlasLayout.X[n]>0&&GrowthAtlasLayout.X[n]<GrowthAtlasLayout.SIZE);
            assertTrue(GrowthAtlasLayout.Y[n]>0&&GrowthAtlasLayout.Y[n]<GrowthAtlasLayout.SIZE);
            assertEquals(n,GrowthAtlasLayout.nearest(GrowthAtlasLayout.X[n],GrowthAtlasLayout.Y[n],0));
        }
    }
    @Test public void roundUtilityClustersKeepEveryNodeDistinctAndInsideTheMap(){
        for(int i=178;i<ExtractionGrowth.NODES.length;i++){
            int branch=ExtractionGrowth.NODES[i].branch;
            if(branch<19)continue;
            assertTrue(GrowthAtlasLayout.GROUP_RING[branch]>0);
            for(int j=0;j<i;j++){
                double gap=Math.hypot(GrowthAtlasLayout.X[i]-GrowthAtlasLayout.X[j],GrowthAtlasLayout.Y[i]-GrowthAtlasLayout.Y[j])-GrowthAtlasLayout.RADIUS[i]-GrowthAtlasLayout.RADIUS[j];
                assertTrue("Node touch targets must not overlap",gap>5);
            }
        }
    }
    @Test public void zoomKeepsTheWorldPointUnderTheFinger(){
        GrowthAtlasViewport view=new GrowthAtlasViewport();view.bounds(128,230);view.origin();
        float x=view.worldX(90),y=view.worldY(95);
        view.zoomAt(1.1f,90,95);
        assertEquals(x,view.worldX(90),0.001f);assertEquals(y,view.worldY(95),0.001f);
    }
    @Test public void dragMovesTheMapWithTheFingerAndBoundsPreventLosingIt(){
        GrowthAtlasViewport view=new GrowthAtlasViewport();view.bounds(128,230);view.origin();
        float before=view.localX(500);view.pan(20,0);assertEquals(before+20,view.localX(500),0.001f);
        view.pan(100000,-100000);
        assertTrue(view.centerX>=0&&view.centerX<=GrowthAtlasLayout.SIZE);assertTrue(view.centerY>=0&&view.centerY<=GrowthAtlasLayout.SIZE);
        view.fit();assertEquals(GrowthAtlasLayout.CENTER,view.centerX,0.001f);assertEquals(GrowthAtlasLayout.CENTER,view.centerY,0.001f);
    }
    @Test public void pinchAnchorAndZoomLimitsRemainStable(){
        GrowthAtlasViewport view=new GrowthAtlasViewport();view.bounds(128,230);view.origin();
        float wx=view.worldX(64),wy=view.worldY(115);
        view.zoomAt(1.3f,80,130);view.anchor(wx,wy,80,130);
        assertEquals(wx,view.worldX(80),0.001f);assertEquals(wy,view.worldY(130),0.001f);
        view.zoomAt(1000,64,115);assertEquals(2f,view.scale,0.001f);
        view.zoomAt(0,64,115);assertEquals(view.fitScale(),view.scale,0.001f);
    }
}
