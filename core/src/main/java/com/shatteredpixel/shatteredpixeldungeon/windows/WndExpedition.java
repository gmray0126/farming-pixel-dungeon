/* Extraction fork © 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;

/** Expedition statistics and permanent growth, without original class/subclass tabs. */
public class WndExpedition extends Window {
    public WndExpedition(){
        super();
        final int w=118;resize(w,188);Hero hero=Dungeon.hero;ExtractionProfile p=ExtractionProfile.get();
        text("원정자 · 성장 "+(1+p.xp/25),10,0,0,w,ExtractionNodeTree.GOLD);
        text("HP "+hero.HP+" / "+hero.HT+"  ·  힘 "+hero.STR(),7,0,16,w,WHITE);
        text("영구 성장 "+(1+p.xp/25)+"  ·  "+p.points+" P\n다음 성장 "+(p.xp%25)+" / 25 XP",6,0,28,w,WHITE);
        text("통합 성장 지도",8,0,51,w,ExtractionNodeTree.GOLD);
        ExtractionNodeTree tree=new ExtractionNodeTree(n->GameScene.show(new WndMessage(
            ExtractionProfile.NAMES[n]+"\n\n"+ExtractionProfile.DESCS[n]+"\n\n"+
            (p.nodes.contains(ExtractionProfile.IDS[n])?"습득한 영구 노드입니다.":"선행: "+p.prerequisites(n)+"\n\n거점에서 분배할 수 있습니다."))),
            ()->GameScene.show(new WndGrowthAtlas()));
        add(tree);tree.setRect(0,65,w,109);
        text(com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.chapterName(p.raidChapter)+" "+Dungeon.depth+"층 · "+(hero.extractionBossDefeated?"탈출 가능":"보스 목표"),6,0,176,w,ExtractionNodeTree.GREEN);
        resize(w,188);
    }
    private void text(String value,int size,float x,float y,int width,int color){
        RenderedTextBlock t=PixelScene.renderTextBlock(value,size);t.maxWidth(width);t.hardlight(color);t.setPos(x,y);add(t);
    }
}
