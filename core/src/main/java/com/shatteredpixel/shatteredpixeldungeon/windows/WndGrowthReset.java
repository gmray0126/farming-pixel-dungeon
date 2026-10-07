/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.watabou.noosa.Game;
/** One confirmation shared by the hub and the fullscreen atlas. */
public final class WndGrowthReset {
    public static void open(Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();
        if(p.active){Game.scene().add(new WndMessage("진행 중인 원정을 끝내거나 포기한 뒤 초기화할 수 있습니다."));return;}
        if(p.nodes.isEmpty()){Game.scene().add(new WndMessage("초기화할 성장 노드가 없습니다."));return;}
        int used=p.spentPoints();
        Game.scene().add(new WndOptions("성장 노드 초기화","배운 노드를 모두 지우고 사용한 "+used+" P를 전액 돌려받습니다. 비용은 없습니다.\n\n남은 "+p.points+" P → "+(p.points+used)+" P\n\n장비·골드·성장 레벨·경험치는 유지됩니다. 가방 한도를 넘는 준비 물품은 창고로 돌려놓습니다. 영혼의 활 지급과 사용은 사격 입문을 다시 배우면 해금됩니다.","초기화 · "+used+" P 반환","취소"){
            @Override protected void onSelect(int choice){if(choice==0)try{p.resetNodes();if(refreshed!=null)refreshed.run();}catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}
        });
    }
    private WndGrowthReset(){}
}
