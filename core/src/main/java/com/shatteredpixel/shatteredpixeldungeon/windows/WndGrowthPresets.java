/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.watabou.noosa.Game;
/** Saved allocations share one earned point pool; switching is free at the hub. */
public final class WndGrowthPresets {
    public static void open(Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();
        if(p.active){Game.scene().add(new WndMessage("원정을 끝내거나 포기한 뒤 프리셋을 저장하거나 불러올 수 있습니다."));return;}
        Game.scene().add(new WndOptions("성장 프리셋","저장은 현재 성장 노드 배분을 보관합니다. 불러오기는 사용한 포인트를 돌려받고 저장한 배분을 적용합니다. 총 포인트가 부족하면 바뀌지 않습니다.\n\n"+p.growthDisplay()+"\n남은 "+p.points+" P · 사용한 "+p.spentPoints()+" P","현재 배분 저장","프리셋 불러오기","닫기"){
            @Override protected void onSelect(int choice){if(choice<2)chooseSlot(choice==1,refreshed);}
        });
    }
    private static void chooseSlot(final boolean loadAllocation,Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();
        String[] labels=new String[4];
        for(int i=0;i<3;i++)labels[i]="프리셋 "+(i+1)+(p.hasPreset(i)?" · "+p.presetCost(i)+" P":" · 비어 있음");
        labels[3]="닫기";
        Game.scene().add(new WndOptions(loadAllocation?"프리셋 불러오기":"현재 배분 저장",loadAllocation?"불러올 프리셋을 선택하세요. 비어 있는 칸은 불러올 수 없습니다.":"현재 배분 "+p.spentPoints()+" P를 저장할 칸을 선택하세요.",labels){
            @Override protected boolean enabled(int slot){return slot==3||!loadAllocation||p.hasPreset(slot);}
            @Override protected void onSelect(int slot){
                if(slot>=3)return;
                if(!loadAllocation&&p.hasPreset(slot)){
                    Game.scene().add(new WndOptions("프리셋 "+(slot+1)+" 덮어쓰기","저장된 "+p.presetCost(slot)+" P 배분을 현재 "+p.spentPoints()+" P 배분으로 바꿉니다.","덮어쓰기","취소"){
                        @Override protected void onSelect(int choice){if(choice==0)complete(false,slot,refreshed);}
                    });
                }else complete(loadAllocation,slot,refreshed);
            }
        });
    }
    private static void complete(boolean loadAllocation,int slot,Runnable refreshed){
        try{
            ExtractionProfile p=ExtractionProfile.get();
            if(loadAllocation)p.applyPreset(slot);else p.savePreset(slot);
            if(refreshed!=null)refreshed.run();
            Game.scene().add(new WndMessage("프리셋 "+(slot+1)+(loadAllocation?"을 불러왔습니다.":"에 현재 배분을 저장했습니다.")+"\n\n남은 "+p.points+" P · 사용한 "+p.spentPoints()+" P"));
        }catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}
    }
    private WndGrowthPresets(){}
}
