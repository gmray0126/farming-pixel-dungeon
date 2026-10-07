/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.watabou.noosa.Game;
/** Saved allocations share one earned point pool; switching is free at the hub. */
public final class WndGrowthPresets {
    public static void open(Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();
        String[] labels=new String[4];
        for(int i=0;i<3;i++)labels[i]="프리셋 "+(i+1)+(p.hasPreset(i)?" · "+p.presetCost(i)+" P":" · 비어 있음");
        labels[3]="닫기";
        Game.scene().add(new WndOptions("성장 프리셋","현재 배분을 3칸에 저장합니다. 불러오면 사용한 포인트를 돌려받고 저장한 배분을 적용합니다. 총 포인트가 부족하면 바뀌지 않습니다.\n\n"+p.growthDisplay()+"\n남은 "+p.points+" P · 사용한 "+p.spentPoints()+" P · 한도 300 P · 레벨업당 3 P",labels){
            @Override protected void onSelect(int slot){if(slot<3)choose(slot,refreshed);}
        });
    }
    private static void choose(int slot,Runnable refreshed){
        ExtractionProfile p=ExtractionProfile.get();boolean exists=p.hasPreset(slot);
        Game.scene().add(new WndOptions("프리셋 "+(slot+1),exists?"저장된 배분: "+p.presetCost(slot)+" P\n현재 배분: "+p.spentPoints()+" P":"현재 배분 "+p.spentPoints()+" P를 저장할 수 있습니다.",exists?new String[]{"불러오기","현재 배분으로 덮어쓰기","취소"}:new String[]{"현재 배분 저장","취소"}){
            @Override protected void onSelect(int choice){if(choice==(exists?2:1))return;try{if(exists&&choice==0)p.applyPreset(slot);else p.savePreset(slot);if(refreshed!=null)refreshed.run();}catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}
        });
    }
    private WndGrowthPresets(){}
}
