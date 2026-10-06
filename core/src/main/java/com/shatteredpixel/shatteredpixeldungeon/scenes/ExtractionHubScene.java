/* Extraction fork © 2026. GPL-3.0-or-later; original game credits retained. */
package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TitleBackground;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Camera;
import java.util.ArrayList;

/** Compact portrait hub; uses original touch UI and graphics. */
public class ExtractionHubScene extends PixelScene {
    @Override public void create() {
        super.create(); uiCamera.visible=false;
        add(new TitleBackground(Camera.main.width, Camera.main.height));
        final ExtractionProfile p=ExtractionProfile.get();
        GamesInProgress.curSlot=1;
        // A settlement is saved before deleting the run; do not reopen a stale settled file.
        if (!p.active && GamesInProgress.check(1)!=null) Dungeon.deleteGame(1,true);
        float left=(Camera.main.width-150)/2f;
        RenderedTextBlock title=renderTextBlock("잔향 원정대",14); title.setPos(left,24); add(title);
        RenderedTextBlock stats=renderTextBlock("거점  |  "+p.gold+" G  |  "+p.points+" P\n보관 "+p.stash.size()+"개 · 준비 "+p.prepared.size()+"개\n영구 성장 Lv."+(1+p.xp/25),7);
        stats.setPos(left,46); stats.maxWidth(150); add(stats);
        float y=stats.bottom()+12;
        hubButton(p.active?"진행 중인 원정 이어하기":"하수도로 출격",left,y,()->{
            p.begin(); GamesInProgress.selectedClass=HeroClass.WARRIOR; GamesInProgress.curSlot=1;
            SPDSettings.intro(false); SPDSettings.challenges(0); ActionIndicator.clearAction();
            if (GamesInProgress.check(1)!=null) InterlevelScene.mode=InterlevelScene.Mode.CONTINUE;
            else { Dungeon.hero=null; Dungeon.daily=Dungeon.dailyReplay=false; Dungeon.customSeedText=""; Dungeon.initSeed(); InterlevelScene.mode=InterlevelScene.Mode.DESCEND; }
            ShatteredPixelDungeon.switchScene(InterlevelScene.class);
        }); y+=25;
        if (!p.active) {
            hubButton("창고에서 챙기기",left,y,()->inventory(false,0)); y+=25;
            hubButton("출격 준비 물품",left,y,()->inventory(true,0)); y+=25;
            hubButton("영구 성장 노드",left,y,()->branches()); y+=25;
            hubButton("회복 물약 구입 · 30 G",left,y,()->{p.buyPotion();ShatteredPixelDungeon.seamlessResetScene();}); y+=25;
        }
        hubButton("원본 크레딧 / 설정",left,y,()->ShatteredPixelDungeon.switchScene(TitleScene.class));
        if (!p.result.isEmpty()) { RenderedTextBlock result=renderTextBlock(p.result,7);result.maxWidth(150);result.setPos(left,y+30);add(result); }
        fadeIn();
    }
    private void hubButton(String text,float x,float y,Runnable action){
        RedButton b=new RedButton(text,7){@Override protected void onClick(){try{action.run();}catch(RuntimeException e){ExtractionHubScene.this.add(new WndMessage(e.getMessage()));}}};b.setRect(x,y,150,21);add(b);
    }
    private void inventory(final boolean prepared,final int page){
        ExtractionProfile p=ExtractionProfile.get();ArrayList<Item> items=prepared?p.prepared:p.stash;
        final ArrayList<Item> shown=new ArrayList<>();for(int i=page*5;i<Math.min(items.size(),page*5+5);i++)shown.add(items.get(i));
        ArrayList<String> options=new ArrayList<>();for(Item i:shown)options.add(i.title());
        final boolean more=(page+1)*5<items.size();if(more)options.add("다음 페이지");options.add("닫기");
        ExtractionHubScene.this.add(new WndOptions(prepared?"출격 준비":"영구 창고","물건을 누르면 챙기기·판매를 선택합니다.\n준비 물품도 사망하면 잃습니다.",options.toArray(new String[0])){
            @Override protected void onSelect(int index){
                if(index<shown.size()){
                    final Item i=shown.get(index);
                    ExtractionHubScene.this.add(new WndOptions(i.name(),prepared?"이 물건을 창고로 되돌릴까요?":"출격에 챙기거나 창고에서 판매할 수 있습니다.",prepared?new String[]{"창고로","취소"}:new String[]{"출격에 챙기기","판매 · "+i.value()+" G","취소"}){
                        @Override protected void onSelect(int choice){try{if(choice==0)p.prepare(i,!prepared);else if(!prepared&&choice==1)p.sell(i);ShatteredPixelDungeon.seamlessResetScene();}catch(RuntimeException e){ExtractionHubScene.this.add(new WndMessage(e.getMessage()));}}
                    });
                }else if(more&&index==shown.size())inventory(prepared,page+1);
            }
        });
    }
    private void branches(){ExtractionHubScene.this.add(new WndOptions("기억의 성장판","노드는 사망해도 유지됩니다.\n25 경험치마다 성장 포인트 +1.","전투","생존","탐사","닫기"){@Override protected void onSelect(int index){if(index<3)nodeBranch(index);}});}
    private void nodeBranch(final int branch){
        ExtractionProfile p=ExtractionProfile.get();String[] opts=new String[4];for(int i=0;i<3;i++){int n=branch*3+i;opts[i]=(p.nodes.contains(ExtractionProfile.IDS[n])?"✓ ":"")+ExtractionProfile.NAMES[n]+" · "+ExtractionProfile.COSTS[n]+" P";}opts[3]="닫기";
        ExtractionHubScene.this.add(new WndOptions("영구 노드 · "+p.points+" P","윗 노드를 배우면 다음 노드가 열립니다.",opts){@Override protected void onSelect(int index){if(index<3){final int n=branch*3+index;ExtractionHubScene.this.add(new WndOptions(ExtractionProfile.NAMES[n],ExtractionProfile.DESCS[n],"배우기","취소"){@Override protected void onSelect(int c){if(c==0)try{p.learn(n);ShatteredPixelDungeon.seamlessResetScene();}catch(RuntimeException e){ExtractionHubScene.this.add(new WndMessage(e.getMessage()));}}});}}});
    }
    @Override protected void onBackPressed(){ShatteredPixelDungeon.switchScene(TitleScene.class);}
}
