/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

/** World coordinates shared by the native canvas and input hit testing. */
public final class GrowthAtlasLayout {
    public static final float SIZE=3800,CENTER=1900;
    public static final float[] X=new float[ExtractionGrowth.NODES.length],Y=new float[ExtractionGrowth.NODES.length],RADIUS=new float[ExtractionGrowth.NODES.length];
    public static final float[] GROUP_X=new float[ExtractionGrowth.BRANCHES.length],GROUP_Y=new float[ExtractionGrowth.BRANCHES.length];
    public static final float[] GROUP_RING=new float[ExtractionGrowth.BRANCHES.length],GROUP_EXTENT=new float[ExtractionGrowth.BRANCHES.length];
    public static final int[] COLORS={0xD6AC67,0x75ABAA,0x92B36D,0xD7B079,0xB88461,0xBD7D91,0x8DAAB8,0xBA7756,0xA2ACAD,0xCAA17A,0x85B47A,0x9B89C4,0xD1C08A,0xD58D9A,0xA5BA76,0x9B91BD,0xD4A16A,0x7CBABD,0x8ECDB5,0xD6AC67,0x9B89C4,0xBD7D91,0x85B47A,0x75ABAA,0xE8C785,0xA992E0,0x927BC8,0xA8C873,0x75BDCF,0xF0D28A,0xCE768A};
    public static final int[] OUTER_ORDER={3,4,7,8,6,10,5,9,11};
    static {
        float[] angles=new float[ExtractionGrowth.BRANCHES.length];angles[0]=-90;angles[1]=30;angles[2]=150;
        for(int k=0;k<OUTER_ORDER.length;k++)angles[OUTER_ORDER[k]]=-90+k*40;
        for(int b=12;b<19;b++)angles[b]=-90+(b-12)*360f/7;
        for(int b=19;b<25;b++)angles[b]=-90+(b-19)*60;
        for(int b=25;b<angles.length;b++)angles[b]=-60+(b-25)*60;
        for(int b=0;b<angles.length;b++){
            double a=Math.toRadians(angles[b]);float radius=b<3?115:b<12?400:b<19?255:b<25?1100:1600;
            GROUP_X[b]=CENTER+(float)Math.cos(a)*radius;GROUP_Y[b]=CENTER+(float)Math.sin(a)*radius;
        }
        for(int i=0;i<ExtractionGrowth.NODES.length;i++){
            ExtractionGrowth.Node n=ExtractionGrowth.NODES[i];float a=angles[n.branch],radius=42;
            if(n.branch>=19)continue;
            if(n.branch==18){radius=82;a=n.row*30;}
            else if(n.row==6){radius=90;a+=n.col==0?-110:110;}
            else if(n.row==0)a+=180;
            else if(n.row==4){}
            else if(n.row==5)radius=72;
            else a+=(n.col==0?1:-1)*(180-45*n.row);
            double radians=Math.toRadians(a);
            X[i]=GROUP_X[n.branch]+(float)Math.cos(radians)*radius;
            Y[i]=GROUP_Y[n.branch]+(float)Math.sin(radians)*radius;
            RADIUS[i]=n.branch==18?7:n.row==6?9:n.row==5?12:n.row==4?9:n.row==0?8:6;
        }
        // Each native ability forms a round hub with its talent chains as radial branches.
        // Coordinates follow actual prerequisites rather than rectangular insertion order.
        for(int b=19;b<25;b++){
            java.util.ArrayList<Integer> hubs=new java.util.ArrayList<>();
            for(int n:ExtractionGrowth.BRANCH_NODES[b])if(ExtractionGrowth.NODES[n].skill!=null||ExtractionGrowth.NODES[n].id.startsWith("subclass_"))hubs.add(n);
            float orbit=Math.max(180,hubs.size()*55);GROUP_RING[b]=orbit;GROUP_EXTENT[b]=orbit+150;
            for(int h=0;h<hubs.size();h++){
                int hub=hubs.get(h);float direction=angles[b]+180+h*360f/hubs.size();
                position(hub,GROUP_X[b],GROUP_Y[b],orbit,direction);RADIUS[hub]=11;
                java.util.ArrayList<Integer> roots=new java.util.ArrayList<>();
                for(int n:ExtractionGrowth.BRANCH_NODES[b])if(!hubs.contains(n)&&ExtractionGrowth.NODES[n].parents[0]==hub)roots.add(n);
                float first=Math.max(58,roots.size()*3.3f);
                for(int k=0;k<roots.size();k++){
                    int root=roots.get(k);float ray=direction+k*360f/roots.size();
                    for(int n:ExtractionGrowth.BRANCH_NODES[b]){
                        if(hubs.contains(n))continue;
                        int ancestor=n,depth=0;
                        while(ancestor!=hub&&ExtractionGrowth.NODES[ancestor].branch==b){
                            if(ancestor==root){depth++;break;}
                            ancestor=ExtractionGrowth.NODES[ancestor].parents[0];depth++;
                        }
                        if(ancestor!=root)continue;
                        float distance=first+(depth-1)*22;
                        position(n,X[hub],Y[hub],distance,ray);RADIUS[n]=7;
                        GROUP_EXTENT[b]=Math.max(GROUP_EXTENT[b],orbit+distance+22);
                    }
                }
            }
        }
        // Hybrid clusters sit between adjacent class themes, with three curved arms.
        for(int b=25;b<ExtractionGrowth.BRANCHES.length;b++){
            GROUP_RING[b]=82;GROUP_EXTENT[b]=155;
            for(int n:ExtractionGrowth.BRANCH_NODES[b]){
                ExtractionGrowth.Node node=ExtractionGrowth.NODES[n];
                if(node.row==0){position(n,GROUP_X[b],GROUP_Y[b],45,angles[b]+180);RADIUS[n]=11;}
                else if(node.row<=4){position(n,GROUP_X[b],GROUP_Y[b],65+(node.row-1)*25,angles[b]+node.col*120+(node.row-1)*12);RADIUS[n]=7;}
                else {position(n,GROUP_X[b],GROUP_Y[b],node.row==5?40:70,angles[b]+60);RADIUS[n]=node.row==5?10:12;}
            }
        }
    }
    private static void position(int n,float cx,float cy,float distance,float degrees){
        double angle=Math.toRadians(degrees);X[n]=cx+(float)Math.cos(angle)*distance;Y[n]=cy+(float)Math.sin(angle)*distance;
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
