/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/** Short, paged native menus shared by Android and desktop. */
public final class ExtractionDebugMenu {
    private final Consumer<Window> show;
    private final Runnable refresh;
    private final boolean raid;
    public ExtractionDebugMenu(Consumer<Window> show,Runnable refresh,boolean raid){this.show=show;this.refresh=refresh;this.raid=raid;}
    private void choose(String title,String message,IntConsumer action,String... labels){
        show.accept(new WndOptions(title,message,labels){@Override protected void onSelect(int index){
            try{ExtractionProfile.get().requireDebug();action.accept(index);}
            catch(RuntimeException e){show.accept(new WndMessage(e.getMessage()==null?"디버그 작업 실패":e.getMessage()));}
        }});
    }
    private void done(String message){refresh.run();show.accept(new WndMessage(message));}
    public void open(){
        choose("디버그","지급한 재화와 물품은 저장됩니다.",i->{
            if(i==0)resources();else if(i==1)items();else if(i==2){if(raid)tools();else done("원정을 시작한 뒤 게임 메뉴의 디버그를 여세요.");}
            else{ExtractionProfile.get().setDebugEnabled(false);done("디버그를 껐습니다. 다시 열기: 로비 제목 7번 누르기");}
        },"골드 / 성장 / 해금","물품 생성",raid?"원정 테스트 도구":"원정 도구 안내","디버그 끄기");
    }
    private void resources(){
        choose("재화 / 성장","레벨만으로 스탯은 오르지 않습니다.",i->{
            ExtractionProfile p=ExtractionProfile.get();
            if(i==0){p.debugResources(10000,0,0);done("10,000 G 지급");}
            else if(i==1){p.debugResources(0,250,0);done("250 XP · 10 P 지급");}
            else if(i==2){p.debugResources(0,0,100);done("100 P 지급");}
            else{p.debugUnlockChapters();done("5개 챕터를 모두 해금했습니다.");}
        },"골드 +10,000","경험치 +250","성장 포인트 +100","전체 챕터 해금");
    }
    private void items(){
        if(!raid&&ExtractionProfile.get().active)throw new IllegalStateException("원정을 이어간 뒤 원정 가방으로 지급하세요.");
        choose("물품 생성",raid?"가방이 가득 차면 발밑에 놓습니다.":"창고에 지급합니다.",i->{
            if(i<2){ArrayList<Item> list=new ArrayList<>();for(ExtractionShop.Offer offer:ExtractionShop.OFFERS)if(offer.category==i+1||i==1&&offer.category>=3)list.add(offer.item());catalog(list,0,true);}
            else if(i==2){ArrayList<Item> list=new ArrayList<>();list.add(new ExpeditionArtifacts.BloodLantern());list.add(new ExpeditionArtifacts.GreedPouch());list.add(new ExpeditionArtifacts.BrokenHourglass());list.add(new ExpeditionArtifacts.HuntersMark());list.add(new ExpeditionArtifacts.UnstableCompass());catalog(list,0,false);}
            else if(i==3){ArrayList<Item> list=new ArrayList<>();for(int n=0;n<ExtractionPotionKnowledge.NAMES.length;n++)list.add(ExtractionPotionKnowledge.createPotion(n).quantity(5));catalog(list,0,false,ExtractionPotionKnowledge.NAMES);}
            else{ArrayList<Item> list=new ArrayList<>();list.add(new SupplyHealingPotion().quantity(10));list.add(new ExtractionShop.SupplyUpgrade().quantity(20));list.add(new ExtractionShop.SupplyIdentify().quantity(10));list.add(new ExtractionShop.SupplyRemoveCurse().quantity(10));list.add(new ScrollOfMagicMapping().quantity(5));list.add(new Food().quantity(10));catalog(list,0,false);}
        },"무기 / 투척","갑옷 / 바지 / 신발","신규 유물 5종","물약 12종 · 각 5개","스크롤 / 보급품");
    }
    private void catalog(ArrayList<Item> list,int page,boolean upgrades){catalog(list,page,upgrades,null);}
    private void catalog(ArrayList<Item> list,int page,boolean upgrades,String[] names){
        int from=page*4,count=Math.min(4,list.size()-from);ArrayList<String> labels=new ArrayList<>();
        for(int n=0;n<count;n++)labels.add(names==null?list.get(from+n).title():names[from+n]+" ×5");
        if(page>0)labels.add("이전 페이지");if(from+count<list.size())labels.add("다음 페이지");
        choose("물품 선택 · "+(page+1),"원하는 물품을 선택하세요.",i->{
            if(i<count){Item item=list.get(from+i);if(upgrades)upgrade(item);else give(item);}
            else if(page>0&&i==count)catalog(list,page-1,upgrades,names);else catalog(list,page+1,upgrades,names);
        },labels.toArray(new String[0]));
    }
    private void upgrade(Item item){
        int cap=item instanceof Armor?9:WeaponUpgradeLimit.tier(item)*3;
        choose(item.title(),"강화 수치를 선택하세요.",i->{int level=i==0?0:i==1?Math.min(3,cap):cap;item.level(level);give(item);},"+0","+3","+"+cap);
    }
    private void give(Item item){
        item.identify(false);if(item instanceof Weapon)((Weapon)item).cursed=false;if(item instanceof Armor)((Armor)item).cursed=false;
        ExtractionDebug.give(item,raid);done(item.title()+" 지급");
    }
    private void tools(){
        ExtractionDebug.requireRaid();
        choose("원정 테스트","보스의 단계 진행과 일반 전투 규칙은 유지됩니다.",i->{
            if(i==0){ExtractionDebug.heal();done("체력과 허기를 회복했습니다.");}
            else if(i==1){ExtractionDebug.toggleInvulnerable();done("피해 무적 "+(Dungeon.hero.extractionDebugInvulnerable?"켜짐":"꺼짐"));}
            else if(i==2){int start=ExtractionDifficulty.startDepth(ExtractionProfile.get().raidChapter);String[] floors=new String[5];for(int n=0;n<5;n++)floors[n]=(start+n)+"층";choose("층 이동","현재 챕터의 층으로 이동합니다.",n->ExtractionDebug.jump(start+n),floors);}
            else if(i==3){ExtractionDebug.reveal();done("현재 층 지도를 밝혔습니다.");}
            else{ExtractionDebug.weakenEnemies();done("현재 등장한 적의 체력을 1로 낮췄습니다. 보스의 다음 단계에서는 다시 사용하세요.");}
        },"체력 / 허기 회복","피해 무적 "+(Dungeon.hero.extractionDebugInvulnerable?"끄기":"켜기"),"층 이동 · 보스층 포함","현재 층 지도 밝히기","현재 적 체력 1");
    }
}
