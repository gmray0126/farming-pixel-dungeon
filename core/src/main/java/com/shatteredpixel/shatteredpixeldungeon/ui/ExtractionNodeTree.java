/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.extraction.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.badlogic.gdx.graphics.Pixmap;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.PointerEvent;
import com.watabou.input.ScrollEvent;
import com.watabou.noosa.*;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

/** One continuous, clipped native growth atlas with drag and two-finger zoom. */
public class ExtractionNodeTree extends Component {
    public static final int GOLD=0xE4C583,GREEN=0x83C9B5,MUTED=0x88969D;
    public interface Selection { void accept(int index); }
    private static float rememberedScale=-1,rememberedX=GrowthAtlasLayout.CENTER,rememberedY=GrowthAtlasLayout.CENTER;
    private static int rememberedSelection=-1;
    private final GrowthAtlasViewport lens=new GrowthAtlasViewport();
    private final Selection select;
    private final Runnable enlarge;
    private Camera mapCamera;
    private Group world;
    private Image selectionRing;
    private RenderedTextBlock zoomText;
    private float baseZoom;
    private boolean restored;
    public ExtractionNodeTree(Selection select){this(select,null);}
    public ExtractionNodeTree(Selection select,Runnable enlarge){this.select=select;this.enlarge=enlarge;}
    @Override protected void layout(){
        super.layout();
        if(width<=0||height<=15)return;
        if(mapCamera!=null){Camera.remove(mapCamera);mapCamera.destroy();mapCamera=null;}
        for(Gizmo member:members.toArray(new Gizmo[0]))member.destroy();clear();
        lens.bounds((int)width,(int)(height-15));
        if(!restored&&rememberedScale>0){
            lens.scale=rememberedScale;lens.centerX=rememberedX;lens.centerY=rememberedY;
            lens.bounds(lens.width,lens.height);
        }
        restored=true;baseZoom=camera().zoom;
        float bx=x;
        nav("전체",bx,21,()->{lens.fit();apply();});bx+=23;
        nav("-",bx,14,()->{lens.zoomAt(lens.scale/1.5f,lens.width/2,lens.height/2);apply();});bx+=16;
        nav("+",bx,14,()->{lens.zoomAt(lens.scale*1.5f,lens.width/2,lens.height/2);apply();});bx+=16;
        nav("시작",bx,21,()->{lens.origin();apply();});bx+=23;
        if(enlarge!=null){nav("크게",bx,24,enlarge);bx+=26;}
        nav("계통",bx,21,()->chooseGroups(false));bx+=23;
        zoomText=null;
        if(width-(bx-x)>=22){zoomText=PixelScene.renderTextBlock(5);zoomText.hardlight(MUTED);zoomText.setPos(bx+1,y+3);add(zoomText);}
        Point screen=camera().cameraToScreen(x,y+15);
        mapCamera=new Camera(screen.x,screen.y,(int)lens.width,(int)lens.height,baseZoom);
        Camera.add(mapCamera);world=new Group();world.camera=mapCamera;add(world);
        ColorBlock background=new ColorBlock(GrowthAtlasLayout.SIZE,GrowthAtlasLayout.SIZE,0xFF101315);world.add(background);
        buildAtlas();
        add(new Gestures());apply();
    }
    private void buildAtlas(){
        ExtractionProfile p=ExtractionProfile.get();
        for(int b=0;b<ExtractionGrowth.BRANCHES.length;b++)circle(GrowthAtlasLayout.GROUP_X[b],GrowthAtlasLayout.GROUP_Y[b],b<19?42:GrowthAtlasLayout.GROUP_RING[b],GrowthAtlasLayout.COLORS[b],false,0.13f);
        for(int n=0;n<ExtractionGrowth.NODES.length;n++){
            ExtractionGrowth.Node node=ExtractionGrowth.NODES[n];
            for(int parent:node.parents)edge(parent,n,false);
            for(int parent:node.alternatives){
                boolean reverse=false;
                for(int other:ExtractionGrowth.NODES[parent].alternatives)if(other==n)reverse=true;
                if(!reverse||parent<n)edge(parent,n,true);
            }
            if(node.parents.length==0)line(GrowthAtlasLayout.CENTER,GrowthAtlasLayout.CENTER,GrowthAtlasLayout.X[n],GrowthAtlasLayout.Y[n],GOLD,0.7f);
        }
        circle(GrowthAtlasLayout.CENTER,GrowthAtlasLayout.CENTER,18,GOLD,true,1);circle(GrowthAtlasLayout.CENTER,GrowthAtlasLayout.CENTER,22,GOLD,false,0.8f);
        icon(ItemSpriteSheet.ARTIFACT_TALISMAN,GrowthAtlasLayout.CENTER,GrowthAtlasLayout.CENTER,21,1);
        label("시작점",8,GrowthAtlasLayout.CENTER,GrowthAtlasLayout.CENTER+26,70,GOLD);
        for(int b=0;b<ExtractionGrowth.BRANCHES.length;b++){
            int learned=0;for(int n:ExtractionGrowth.BRANCH_NODES[b])if(p.nodes.contains(ExtractionGrowth.IDS[n]))learned++;
            label(ExtractionGrowth.BRANCHES[b],6,GrowthAtlasLayout.GROUP_X[b],GrowthAtlasLayout.GROUP_Y[b]-(b>=19?(GrowthAtlasLayout.GROUP_EXTENT[b]+20):5),b>=19?150:70,GrowthAtlasLayout.COLORS[b]);
            label(learned+" / "+ExtractionGrowth.BRANCH_NODES[b].length,4,GrowthAtlasLayout.GROUP_X[b],GrowthAtlasLayout.GROUP_Y[b]-(b>=19?(GrowthAtlasLayout.GROUP_EXTENT[b]+8):-7),52,MUTED);
        }
        for(int n=0;n<ExtractionGrowth.NODES.length;n++){
            ExtractionGrowth.Node node=ExtractionGrowth.NODES[n];boolean learned=p.nodes.contains(node.id);
            boolean ready=p.unlocked(n)&&p.points>=node.cost&&!p.active;
            int color=learned?GrowthAtlasLayout.COLORS[node.branch]:ready?GOLD:0x555A58;
            float nx=GrowthAtlasLayout.X[n],ny=GrowthAtlasLayout.Y[n],r=GrowthAtlasLayout.RADIUS[n];
            circle(nx,ny,r,color,true,1);
            if(node.row>=4)circle(nx,ny,r+3,color,false,learned||ready?0.8f:0.35f);
            icon(nodeIcon(node),nx,ny,r*1.3f,learned?1:ready?0.9f:0.5f);
        }
        selectionRing=new Image(circleTexture(false));selectionRing.hardlight(0xF6E6B7);world.add(selectionRing);
        selectedRing();
    }
    private void edge(int from,int to,boolean bridge){
        ExtractionProfile p=ExtractionProfile.get();boolean a=p.nodes.contains(ExtractionGrowth.IDS[from]),b=p.nodes.contains(ExtractionGrowth.IDS[to]);
        int color=bridge?0x9A9B80:GrowthAtlasLayout.COLORS[ExtractionGrowth.NODES[to].branch];
        line(GrowthAtlasLayout.X[from],GrowthAtlasLayout.Y[from],GrowthAtlasLayout.X[to],GrowthAtlasLayout.Y[to],color,a&&b?0.95f:a||b?0.6f:0.27f);
    }
    private void line(float ax,float ay,float bx,float by,int color,float alpha){
        float dx=bx-ax,dy=by-ay;ColorBlock line=new ColorBlock((float)Math.sqrt(dx*dx+dy*dy),1.4f,0xFF000000|color);
        line.x=ax;line.y=ay;line.angle=(float)Math.toDegrees(Math.atan2(dy,dx));line.alpha(alpha);world.add(line);
    }
    private static SmartTexture circleTexture(boolean filled){
        String key=filled?"growth-atlas-disk-v1":"growth-atlas-ring-v1";
        if(TextureCache.contains(key))return TextureCache.get(key);
        SmartTexture texture=TextureCache.create(key,64,64);texture.bitmap.setBlending(Pixmap.Blending.None);
        for(int py=0;py<64;py++)for(int px=0;px<64;px++){
            double r=Math.hypot(px-31.5,py-31.5);
            if(r<=29&&(filled||r>=26.5))texture.bitmap.drawPixel(px,py,0xFFFFFFFF);
        }
        return texture;
    }
    private void chooseGroups(boolean hybrid){
        String[] names=hybrid?new String[]{"마검사","그림자술사","연금 사냥꾼","폭풍 유격수","성전사","혈기사","원본 기술"}:new String[]{"수호","비전","그림자","자연","기동","기도","혼합 트리"};
        Game.scene().add(new WndOptions(hybrid?"혼합 트리":"기술 계통","선택한 계통으로 지도를 이동합니다.",names){
            @Override protected void onSelect(int i){if(i==6){chooseGroups(!hybrid);return;}int b=(hybrid?25:19)+i;lens.scale=Math.max(lens.fitScale(),Math.min(lens.width,lens.height)/(2*GrowthAtlasLayout.GROUP_EXTENT[b]+70));lens.centerX=GrowthAtlasLayout.GROUP_X[b];lens.centerY=GrowthAtlasLayout.GROUP_Y[b];lens.bounds(lens.width,lens.height);apply();}
        });
    }
    private void circle(float cx,float cy,float radius,int color,boolean filled,float alpha){
        if(filled){Image disk=new Image(circleTexture(true));disk.scale.set(radius*2/64f);disk.x=cx-radius;disk.y=cy-radius;disk.hardlight(0x182023);world.add(disk);}
        Image ring=new Image(circleTexture(false));ring.scale.set(radius*2/64f);ring.x=cx-radius;ring.y=cy-radius;ring.hardlight(color);ring.alpha(alpha);world.add(ring);
    }
    private void icon(int image,float cx,float cy,float size,float alpha){
        ItemSprite sprite=new ItemSprite(image);sprite.scale.set(size/Math.max(sprite.width(),sprite.height()));
        sprite.x=cx-sprite.width()/2;sprite.y=cy-sprite.height()/2;sprite.alpha(alpha);world.add(sprite);
    }
    private int nodeIcon(ExtractionGrowth.Node node){
        int[] family={ItemSpriteSheet.SWORD,ItemSpriteSheet.GREATSHIELD,ItemSpriteSheet.BACKPACK,ItemSpriteSheet.SWORD,ItemSpriteSheet.GREATSWORD,ItemSpriteSheet.DAGGER,ItemSpriteSheet.SPEAR,ItemSpriteSheet.HAND_AXE,ItemSpriteSheet.MACE,ItemSpriteSheet.GLOVES,ItemSpriteSheet.SPIRIT_BOW,ItemSpriteSheet.WAND_MAGIC_MISSILE,ItemSpriteSheet.ARTIFACT_TALISMAN,ItemSpriteSheet.POTION_CRIMSON,ItemSpriteSheet.RATION,ItemSpriteSheet.ARTIFACT_CLOAK,ItemSpriteSheet.SWORD,ItemSpriteSheet.GREATSHIELD,ItemSpriteSheet.POTION_AZURE,ItemSpriteSheet.ROUND_SHIELD,ItemSpriteSheet.WAND_MAGIC_MISSILE,ItemSpriteSheet.ARTIFACT_CLOAK,ItemSpriteSheet.ARTIFACT_BOOTS,ItemSpriteSheet.SAI,ItemSpriteSheet.ARTIFACT_TOME,ItemSpriteSheet.RUNIC_BLADE,ItemSpriteSheet.ARTIFACT_CLOAK,ItemSpriteSheet.POTION_AZURE,ItemSpriteSheet.SPEAR,ItemSpriteSheet.ARTIFACT_TOME,ItemSpriteSheet.ARTIFACT_CHALICE1};
        if(node.branch>=12||node.row==0||node.row>=4)return family[node.branch];
        ExtractionGrowth.Stat stat=node.effects.keySet().iterator().next();
        switch(stat){
            case HEALTH:return ItemSpriteSheet.ARTIFACT_CHALICE1;
            case DEFENSE:return ItemSpriteSheet.ROUND_SHIELD;
            case CAPACITY:return ItemSpriteSheet.BACKPACK;
            case STRENGTH:return ItemSpriteSheet.GREATAXE;
            case ACCURACY:return ItemSpriteSheet.SPIRIT_BOW;
            case CRIT_CHANCE:case CRIT_POWER:return ItemSpriteSheet.DAGGER;
            case ATTACK_SPEED:return ItemSpriteSheet.SAI;
            case PIERCE:return ItemSpriteSheet.PICKAXE;
            case REACH:return ItemSpriteSheet.SPEAR;
            case MOVE_SPEED:case EVASION:return ItemSpriteSheet.ARTIFACT_BOOTS;
            case GOLD:return ItemSpriteSheet.GOLD;
            case WAND_DAMAGE:case WAND_POWER:case WAND_CHARGE:return ItemSpriteSheet.WAND_MAGIC_MISSILE;
            default:return family[node.branch];
        }
    }
    private void label(String value,int size,float cx,float cy,int width,int color){
        RenderedTextBlock text=PixelScene.renderTextBlock(value,size);text.maxWidth(width);text.align(RenderedTextBlock.CENTER_ALIGN);
        text.hardlight(color);text.setPos(cx-text.width()/2,cy);world.add(text);
    }
    private void nav(String value,float bx,float w,Runnable action){
        Button button=new Button(){@Override protected void onClick(){action.run();}};button.setRect(bx,y,w,12);add(button);
        ColorBlock bg=new ColorBlock(w,12,0xFF283138);bg.x=bx;bg.y=y;button.add(bg);
        RenderedTextBlock text=PixelScene.renderTextBlock(value,5);text.hardlight(GOLD);text.setPos(bx+(w-text.width())/2,y+3);button.add(text);
    }
    private void apply(){
        if(mapCamera==null)return;
        mapCamera.zoom(baseZoom*lens.scale);
        mapCamera.scroll.set(lens.centerX-lens.width/(2*lens.scale),lens.centerY-lens.height/(2*lens.scale));
        rememberedScale=lens.scale;rememberedX=lens.centerX;rememberedY=lens.centerY;
        if(zoomText!=null)zoomText.text(Math.round(lens.scale*100)+"%");
        selectedRing();
    }
    private void selectedRing(){
        if(selectionRing==null)return;selectionRing.visible=rememberedSelection>=0;
        if(rememberedSelection>=0){int n=rememberedSelection;float r=GrowthAtlasLayout.RADIUS[n]+5;selectionRing.scale.set(r*2/64f);selectionRing.x=GrowthAtlasLayout.X[n]-r;selectionRing.y=GrowthAtlasLayout.Y[n]-r;}
    }
    private PointF local(PointF screen){PointF p=camera().screenToCamera((int)screen.x,(int)screen.y);return p.offset(-x,-y-15);}
    private class Gestures extends ScrollArea {
        private PointerEvent another;
        private boolean pinching,dragging;
        private float startSpan,startScale,anchorX,anchorY;
        private final PointF last=new PointF();
        Gestures(){super(ExtractionNodeTree.this.x,ExtractionNodeTree.this.y+15,ExtractionNodeTree.this.width,ExtractionNodeTree.this.height-15);}
        @Override protected void onPointerDown(PointerEvent event){
            if(event==curEvent){dragging=false;last.set(event.current);}
            else if(another==null&&curEvent!=null){
                another=event;pinching=true;dragging=true;startSpan=Math.max(1,PointF.distance(curEvent.current,another.current));startScale=lens.scale;
                PointF midpoint=local(new PointF((curEvent.current.x+another.current.x)/2,(curEvent.current.y+another.current.y)/2));
                anchorX=lens.worldX(midpoint.x);anchorY=lens.worldY(midpoint.y);
            }
        }
        @Override protected void onDrag(PointerEvent event){
            if(pinching&&another!=null&&curEvent!=null){
                float span=PointF.distance(curEvent.current,another.current);
                PointF midpoint=local(new PointF((curEvent.current.x+another.current.x)/2,(curEvent.current.y+another.current.y)/2));
                lens.zoomAt(startScale*span/startSpan,midpoint.x,midpoint.y);lens.anchor(anchorX,anchorY,midpoint.x,midpoint.y);apply();
            }else if(PointF.distance(event.current,event.start)>camera().zoom*3||dragging){
                dragging=true;lens.pan((event.current.x-last.x)/camera().zoom,(event.current.y-last.y)/camera().zoom);last.set(event.current);apply();
            }
        }
        @Override protected void onPointerUp(PointerEvent event){
            if(pinching&&(event==curEvent||event==another)){
                pinching=false;dragging=true;if(event==curEvent)curEvent=another;another=null;
                if(curEvent!=null)last.set(curEvent.current);
            }
        }
        @Override protected void onClick(PointerEvent event){
            if(dragging){dragging=false;return;}
            PointF point=local(event.current);int n=GrowthAtlasLayout.nearest(lens.worldX(point.x),lens.worldY(point.y),8/lens.scale);
            if(n<0)return;rememberedSelection=n;
            if(lens.scale<0.4f){lens.focus(GrowthAtlasLayout.X[n],GrowthAtlasLayout.Y[n]);apply();}
            else{selectedRing();select.accept(n);}
        }
        @Override protected void onScroll(ScrollEvent event){
            PointF point=local(event.pos);lens.zoomAt(lens.scale*(float)Math.pow(1.2,-event.amount),point.x,point.y);apply();
        }
    }
    @Override public void destroy(){if(mapCamera!=null){Camera.remove(mapCamera);mapCamera.destroy();mapCamera=null;}super.destroy();}
}
