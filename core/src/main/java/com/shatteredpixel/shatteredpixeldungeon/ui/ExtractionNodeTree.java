/* Farming Pixel Dungeon fork. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionGrowth;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

/** Complete graph overview plus readable branch diagrams, without scrolling through menus. */
public class ExtractionNodeTree extends Component {
    public static final int GOLD=0xE4C583, GREEN=0x83C9B5, MUTED=0x88969D;
    public interface Selection { void accept(int index); }
    private static int focusedBranch=-1;
    private final Selection select;
    public ExtractionNodeTree(Selection select) { this.select=select; }
    @Override protected void layout() {
        super.layout();
        for(com.watabou.noosa.Gizmo g:members.toArray(new com.watabou.noosa.Gizmo[0]))g.destroy();
        clear();
        if(focusedBranch<0)overview();else branch();
    }
    private void overview(){
        text(this,"전체 지도 · 108 노드",6,x,y,width,GOLD);
        float gap=3,cw=(width-gap*2)/3f,ch=(height-14-gap*3)/4f;
        ExtractionProfile p=ExtractionProfile.get();
        for(int b=0;b<ExtractionGrowth.BRANCHES.length;b++){
            final int branch=b;
            float bx=x+(b%3)*(cw+gap),by=y+14+(b/3)*(ch+gap);
            int learned=0;boolean available=false;
            for(int n:ExtractionGrowth.BRANCH_NODES[b]){
                if(p.nodes.contains(ExtractionGrowth.IDS[n]))learned++;
                else if(p.unlocked(n)&&p.points>=ExtractionGrowth.COSTS[n]&&!p.active)available=true;
            }
            Button card=card(bx,by,cw,ch,learned==9?GREEN:available?GOLD:MUTED,()->{focusedBranch=branch;layout();});
            text(card,ExtractionGrowth.BRANCHES[b],5,bx+1,by+2,cw-2,learned>0?GREEN:GOLD);
            float graphW=Math.min(20,cw-16),graphH=Math.max(7,ch-10);
            float gx=bx+3,gy=by+9;
            miniGraph(card,b,gx,gy,graphW,graphH);
            text(card,learned+"/9",4,bx+cw-14,by+ch-7,13,MUTED);
        }
    }
    private void miniGraph(Button card,int branch,float bx,float by,float w,float h){
        ExtractionProfile p=ExtractionProfile.get();
        float dot=h<14?1:2;
        float stepX=(w-dot)/2f,stepY=(h-dot)/5f;
        for(int index:ExtractionGrowth.BRANCH_NODES[branch]){
            ExtractionGrowth.Node n=ExtractionGrowth.NODES[index];
            for(int parent:n.parents){
                ExtractionGrowth.Node from=ExtractionGrowth.NODES[parent];
                if(from.branch==branch)link(card,bx+from.col*stepX+1,by+from.row*stepY+dot,bx+n.col*stepX+1,by+n.row*stepY,p.nodes.contains(from.id)?GREEN:0x34434C);
            }
        }
        for(int index:ExtractionGrowth.BRANCH_NODES[branch]){
            ExtractionGrowth.Node n=ExtractionGrowth.NODES[index];
            block(card,bx+n.col*stepX,by+n.row*stepY,dot,dot,p.nodes.contains(n.id)?GREEN:p.unlocked(index)?GOLD:MUTED);
        }
    }
    private void branch(){
        ExtractionProfile p=ExtractionProfile.get();final int b=focusedBranch;
        nav("전체",x,y,23,11,()->{focusedBranch=-1;layout();});
        nav("<",x+25,y,12,11,()->{focusedBranch=(b+11)%12;layout();});
        nav(">",x+width-12,y,12,11,()->{focusedBranch=(b+1)%12;layout();});
        int learned=0;for(int n:ExtractionGrowth.BRANCH_NODES[b])if(p.nodes.contains(ExtractionGrowth.IDS[n]))learned++;
        text(this,ExtractionGrowth.BRANCHES[b]+" "+learned+"/9",6,x+39,y+2,width-53,GOLD);
        float gap=2,cw=(width-2*gap)/3f,ch=(height-15-5*gap)/6f;
        for(int index:ExtractionGrowth.BRANCH_NODES[b]){
            ExtractionGrowth.Node n=ExtractionGrowth.NODES[index];
            for(int parent:n.parents){
                ExtractionGrowth.Node from=ExtractionGrowth.NODES[parent];
                if(from.branch==b)link(this,x+from.col*(cw+gap)+cw/2f,y+15+from.row*(ch+gap)+ch,
                    x+n.col*(cw+gap)+cw/2f,y+15+n.row*(ch+gap),p.nodes.contains(from.id)?GREEN:0x34434C);
            }
        }
        for(int index:ExtractionGrowth.BRANCH_NODES[b]){
            final int selected=index;ExtractionGrowth.Node n=ExtractionGrowth.NODES[index];
            float bx=x+n.col*(cw+gap),by=y+15+n.row*(ch+gap);
            boolean learnedNode=p.nodes.contains(n.id),ready=p.unlocked(index)&&p.points>=n.cost&&!p.active;
            int color=learnedNode?GREEN:ready?GOLD:MUTED;
            Button card=card(bx,by,cw,ch,color,()->select.accept(selected));
            text(card,n.name,5,bx+1,by+1,cw-2,color);
            text(card,n.summary(),4,bx+1,by+ch-5,cw-2,0xCED8DD);
            if(ch>=21)text(card,learnedNode?"습득":n.cost+" P",4,bx+1,by+ch/2f-1,cw-2,color);
        }
        // The empty side space makes convergence requirements visible without a second menu.
        if(ch>=19){
            text(this,"양쪽 갈래\n모두 습득\n↓ 합류",4,x,y+15+4*(ch+gap)+2,cw,MUTED);
            text(this,"노드를 눌러\n효과·비용\n선행 확인",4,x+2*(cw+gap),y+15+4*(ch+gap)+2,cw,MUTED);
        }
    }
    private Button card(float bx,float by,float w,float h,int color,Runnable action){
        Button button=new Button(){@Override protected void onClick(){action.run();}};
        button.setRect(bx,by,w,h);add(button);
        block(button,bx,by,w,h,color);block(button,bx+1,by+1,w-2,h-2,color==GREEN?0x19342F:0x151F28);return button;
    }
    private void nav(String label,float bx,float by,float w,float h,Runnable action){
        Button button=card(bx,by,w,h,MUTED,action);text(button,label,5,bx,by+2,w,GOLD);
    }
    private void link(com.watabou.noosa.Group group,float ax,float ay,float bx,float by,int color){
        float mid=(ay+by)/2f;
        block(group,ax,Math.min(ay,mid),1,Math.max(1,Math.abs(mid-ay)),color);
        block(group,Math.min(ax,bx),mid,Math.max(1,Math.abs(bx-ax)),1,color);
        block(group,bx,Math.min(mid,by),1,Math.max(1,Math.abs(by-mid)),color);
    }
    private void block(com.watabou.noosa.Group group,float bx,float by,float w,float h,int color){
        ColorBlock line=new ColorBlock(w,h,0xFF000000|color);line.x=PixelScene.align(bx);line.y=PixelScene.align(by);group.add(line);
    }
    private void text(com.watabou.noosa.Group group,String value,int size,float bx,float by,float w,int color){
        RenderedTextBlock text=PixelScene.renderTextBlock(value,size);text.maxWidth(Math.max(1,(int)w));
        text.align(RenderedTextBlock.CENTER_ALIGN);text.hardlight(color);text.setPos(bx+(w-text.width())/2f,by);group.add(text);
    }
}
