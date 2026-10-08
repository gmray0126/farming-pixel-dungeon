/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ExtractionHubScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.utils.RectF;

/** A full-height black atlas renders behind transparent overlaid controls. */
public class WndGrowthAtlas extends Window {
    private final Runnable closed;
    private Group body;
    private final int atlasWidth,atlasHeight;
    public WndGrowthAtlas(){this(null);}
    public WndGrowthAtlas(Runnable closed){
        this.closed=closed;
        chrome.visible=false;
        shadow.visible=false;
        RectF insets=Game.platform.getSafeInsets(com.watabou.utils.PlatformSupport.INSET_BLK);
        atlasWidth=Math.max(100,(int)((Game.width-insets.left-insets.right)/PixelScene.defaultZoom)-chrome.marginHor());
        atlasHeight=Math.max(80,(int)((Game.height-insets.top-insets.bottom)/PixelScene.defaultZoom)-chrome.marginVer());
        resize(atlasWidth,atlasHeight);refresh();
        if(Game.scene() instanceof ExtractionHubScene)((ExtractionHubScene)Game.scene()).showHubControls(false);
    }
    private void refresh(){
        if(body!=null){erase(body);body.destroy();}
        body=new Group();add(body);ExtractionProfile p=ExtractionProfile.get();
        ColorBlock black=new ColorBlock(atlasWidth+chrome.marginHor(),atlasHeight+chrome.marginVer(),0xFF090C0F);
        black.x=-chrome.marginLeft();black.y=-chrome.marginTop();body.add(black);
        ExtractionNodeTree tree=new ExtractionNodeTree(this::node,null,true);body.add(tree);tree.setRect(0,0,atlasWidth,atlasHeight);
        text("성장 지도",atlasWidth<170?7:9,0,0,atlasWidth-93,ExtractionNodeTree.GOLD);
        text("남은 "+p.points+" P · 사용한 "+p.spentPoints()+" P",6,0,16,atlasWidth,WHITE);
        text("습득 "+p.nodes.size()+" / "+ExtractionProfile.IDS.length+" · 출격 힘 "+p.startingStrength(),5,0,25,atlasWidth,ExtractionNodeTree.GREEN);
        StyledButton presets=new StyledButton(com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON,"프리셋",6){@Override protected void onClick(){WndGrowthPresets.open(WndGrowthAtlas.this::refresh);}};
        presets.setRect(atlasWidth-93,0,31,13);body.add(presets);
        StyledButton reset=new StyledButton(com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON,"초기화",6){@Override protected void onClick(){WndGrowthReset.open(WndGrowthAtlas.this::refresh);}};
        reset.setRect(atlasWidth-59,0,31,13);body.add(reset);
        StyledButton close=new StyledButton(com.shatteredpixel.shatteredpixeldungeon.Chrome.Type.GREY_BUTTON,"닫기",6){@Override protected void onClick(){hide();}};
        close.setRect(atlasWidth-23,0,23,13);body.add(close);
        text("밀어서 이동 · 두 손가락 확대 · 노드 선택",5,0,atlasHeight-10,atlasWidth,ExtractionNodeTree.GREEN);
    }
    private void node(final int index){
        ExtractionProfile p=ExtractionProfile.get();boolean learned=p.nodes.contains(ExtractionProfile.IDS[index]);
        boolean can=!learned&&!p.active&&p.unlocked(index)&&p.points>=ExtractionProfile.COSTS[index];
        String state=learned?"이미 습득한 노드입니다.":p.active?"거점에서 습득할 수 있습니다.":"비용 "+ExtractionProfile.COSTS[index]+" P · 보유 "+p.points+" P";
        Game.scene().add(new WndOptions(ExtractionProfile.NAMES[index],ExtractionProfile.DESCS[index]+"\n\n연결 조건: "+p.prerequisites(index)+"\n\n"+state,can?new String[]{"습득 · "+ExtractionProfile.COSTS[index]+" P","닫기"}:new String[]{"닫기"}){
            @Override protected void onSelect(int choice){if(can&&choice==0)try{p.learn(index);refresh();}catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}
        });
    }
    private void text(String value,int size,float x,float y,int maxWidth,int color){
        RenderedTextBlock text=PixelScene.renderTextBlock(value,size);text.maxWidth(maxWidth);text.hardlight(color);text.setPos(x,y);body.add(text);
    }
    @Override public void hide(){super.hide();if(Game.scene() instanceof ExtractionHubScene)((ExtractionHubScene)Game.scene()).showHubControls(true);if(closed!=null)closed.run();}
}
