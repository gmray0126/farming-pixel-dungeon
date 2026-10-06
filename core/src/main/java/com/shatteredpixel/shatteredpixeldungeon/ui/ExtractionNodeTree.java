/* Extraction fork © 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import java.util.function.IntConsumer;

/** All permanent nodes, prerequisites and effects remain visible together. */
public class ExtractionNodeTree extends Component {
    public static final int GOLD = 0xE4C583, GREEN = 0x83C9B5, MUTED = 0x88969D;
    public static final String[] EFFECTS = {"피해 +1", "피해 +1", "피해 +2", "최대 HP +6", "방어 +1", "방어 +1", "가방 +2칸", "가방 +2칸", "힘 +2"};
    private final IntConsumer select;
    public ExtractionNodeTree(IntConsumer select) { this.select=select; }
    @Override protected void layout() {
        super.layout();
        for (com.watabou.noosa.Gizmo g : members.toArray(new com.watabou.noosa.Gizmo[0])) g.destroy();
        clear();
        ExtractionProfile p=ExtractionProfile.get();
        float gap=4, cw=(width-2*gap)/3f, header=10, connector=6;
        float ch=(height-header-2*connector)/3f;
        String[] branches={"전투", "생존", "탐사"};
        for (int branch=0;branch<3;branch++) {
            float bx=x+branch*(cw+gap);
            RenderedTextBlock heading=PixelScene.renderTextBlock(branches[branch],7);
            heading.hardlight(GOLD); heading.setPos(bx+(cw-heading.width())/2f,y); add(heading);
            for (int row=0;row<3;row++) {
                final int n=branch*3+row;
                float by=y+header+row*(ch+connector);
                if(row>0){
                    ColorBlock line=new ColorBlock(1,connector,p.nodes.contains(ExtractionProfile.IDS[n-1])?0xFF83C9B5:0xFF3E4C54);
                    line.x=PixelScene.align(bx+cw/2f);line.y=by-connector;add(line);
                }
                boolean learned=p.nodes.contains(ExtractionProfile.IDS[n]);
                boolean unlocked=ExtractionProfile.PARENTS[n]<0 || p.nodes.contains(ExtractionProfile.IDS[ExtractionProfile.PARENTS[n]]);
                Button card=new Button(){@Override protected void onClick(){select.accept(n);}};
                card.setRect(bx,by,cw,ch);add(card);
                ColorBlock border=new ColorBlock(cw,ch,learned?0xFF83C9B5:unlocked?0xFF947F55:0xFF34434C);
                border.x=bx;border.y=by;card.add(border);
                ColorBlock fill=new ColorBlock(cw-2,ch-2,learned?0xFF19342F:0xFF151F28);
                fill.x=bx+1;fill.y=by+1;card.add(fill);
                cardText(card,ExtractionProfile.NAMES[n],6,bx,by+4,cw,learned?GREEN:unlocked?GOLD:MUTED);
                cardText(card,EFFECTS[n],5,bx,by+ch/2f,cw,0xCED8DD);
                cardText(card,learned?"습득":unlocked?ExtractionProfile.COSTS[n]+" P":"선행 필요",5,bx,by+ch-8,cw,learned?GREEN:MUTED);
            }
        }
    }
    private void cardText(Button card,String value,int size,float bx,float by,float cw,int color){
        RenderedTextBlock text=PixelScene.renderTextBlock(value,size);
        text.maxWidth((int)cw-4);text.align(RenderedTextBlock.CENTER_ALIGN);text.hardlight(color);
        text.setPos(bx+(cw-text.width())/2f,by);card.add(text);
    }
}
