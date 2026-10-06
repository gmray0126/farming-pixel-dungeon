/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

/** World coordinates shared by the native canvas and input hit testing. */
public final class GrowthAtlasLayout {
    public static final float SIZE=1000,CENTER=500;
    public static final float[] X=new float[ExtractionGrowth.NODES.length],Y=new float[ExtractionGrowth.NODES.length],RADIUS=new float[ExtractionGrowth.NODES.length];
    public static final float[] GROUP_X=new float[ExtractionGrowth.BRANCHES.length],GROUP_Y=new float[ExtractionGrowth.BRANCHES.length];
    public static final int[] COLORS={0xD6AC67,0x75ABAA,0x92B36D,0xD7B079,0xB88461,0xBD7D91,0x8DAAB8,0xBA7756,0xA2ACAD,0xCAA17A,0x85B47A,0x9B89C4,0xD1C08A,0xD58D9A,0xA5BA76,0x9B91BD,0xD4A16A,0x7CBABD};
    public static final int[] OUTER_ORDER={3,4,7,8,6,10,5,9,11};
    static {
        float[] angles=new float[ExtractionGrowth.BRANCHES.length];angles[0]=-90;angles[1]=30;angles[2]=150;
        for(int k=0;k<OUTER_ORDER.length;k++)angles[OUTER_ORDER[k]]=-90+k*40;
        for(int b=12;b<angles.length;b++)angles[b]=-90+(b-12)*60;
        for(int b=0;b<angles.length;b++){
            double a=Math.toRadians(angles[b]);float radius=b<3?115:b<12?400:245;
            GROUP_X[b]=CENTER+(float)Math.cos(a)*radius;GROUP_Y[b]=CENTER+(float)Math.sin(a)*radius;
        }
        for(int i=0;i<ExtractionGrowth.NODES.length;i++){
            ExtractionGrowth.Node n=ExtractionGrowth.NODES[i];float a=angles[n.branch],radius=42;
            if(n.row==0)a+=180;
            else if(n.row==4){}
            else if(n.row==5)radius=72;
            else a+=(n.col==0?1:-1)*(180-45*n.row);
            double radians=Math.toRadians(a);
            X[i]=GROUP_X[n.branch]+(float)Math.cos(radians)*radius;
            Y[i]=GROUP_Y[n.branch]+(float)Math.sin(radians)*radius;
            RADIUS[i]=n.row==5?12:n.row==4?9:n.row==0?8:6;
        }
    }
    public static int nearest(float x,float y,float minimumRadius){
        int selected=-1;float nearest=Float.MAX_VALUE;
        for(int i=0;i<X.length;i++){
            float dx=x-X[i],dy=y-Y[i],distance=dx*dx+dy*dy;
            float radius=Math.max(RADIUS[i]+2,minimumRadius);
            if(distance<=radius*radius&&distance<nearest){selected=i;nearest=distance;}
        }
        return selected;
    }
    private GrowthAtlasLayout(){}
}
