/* Extraction fork © 2026. GPL-3.0-or-later; original credits retained. */
package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionShop;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.noosa.*;
import com.watabou.utils.RectF;
import java.util.ArrayList;

/** Responsive native hub: simultaneous stash/loadout grids and a complete node tree. */
public class ExtractionHubScene extends PixelScene {
    private static int tab=0, stashPage=0, bagPage=0, shopCategory=0, shopPage=0;
    private static boolean shopSelling=false;
    private Group body;
    private float left, top, width, bottom;
    private int stashRows;
    private float cellStep;
    private int debugTaps;
    private long lastDebugTap;
    private static final int TEXT=0xD5DFE4, MUTED=0x8D9FA9, GOLD=0xE4C583, GREEN=0x83C9B5;

    @Override public void create() {
        super.create();uiCamera.visible=false;
        // The hub replaces TitleScene, including its global badge initialization.
        Badges.loadGlobal();
        SPDSettings.intro(false);SPDSettings.version(ShatteredPixelDungeon.versionCode);
        add(new TitleBackground(Camera.main.width,Camera.main.height));
        add(new ColorBlock(Camera.main.width,Camera.main.height,0xEA080F18));
        RectF insets=getCommonInsets();
        width=Math.min(180,Camera.main.width-insets.left-insets.right-12);
        left=insets.left+(Camera.main.width-insets.left-insets.right-width)/2f;
        top=insets.top+6;bottom=Camera.main.height-insets.bottom-6;
        stashRows=bottom-top>=260?2:1;cellStep=bottom-top<240?21:24;
        GamesInProgress.curSlot=1;
        ExtractionProfile p=ExtractionProfile.get();
        if(!p.active&&GamesInProgress.check(1)!=null)Dungeon.deleteGame(1,true);
        refresh();fadeIn();
    }
    private void refresh(){
        if(body!=null){erase(body);body.destroy();}
        body=new Group();add(body);
        ExtractionProfile p=ExtractionProfile.get();
        label("파밍 픽셀 던전",9,left,top,width-34,GOLD);
        Button secret=new Button(){@Override protected void onClick(){
            long now=System.currentTimeMillis();if(now-lastDebugTap>4000)debugTaps=0;lastDebugTap=now;
            if(++debugTaps>=7){debugTaps=0;try{p.setDebugEnabled(true);refresh();debugMenu();}catch(RuntimeException e){error(e);}}
        }};secret.setRect(left,top,width-34,16);body.add(secret);
        button("설정",left+width-30,top,30,15,()->add(new WndSettings()),false);
        label(p.gold+" G · 남음 "+p.points+" P · 사용 "+p.spentPoints()+" P",6,left,top+20,p.debugEnabled?width-43:width,TEXT);
        if(p.debugEnabled)button("디버그",left+width-40,top+18,40,13,this::debugMenu,false);
        label(p.growthDisplay()+" · 출격 힘 "+p.startingStrength(),6,left,top+29,width,GREEN);
        float tw=(width-6)/4f;
        String[] titles={"준비","성장","원정","상점"};
        for(int i=0;i<4;i++){
            final int n=i;
            HubButton b=button(titles[i],left+i*(tw+2),top+40,tw,19,()->{tab=n;refresh();if(n==1)add(new WndGrowthAtlas(this::refresh));},i==tab);
            if(i==tab)b.textColor(GOLD);
        }
        float y=top+65;
        if(tab==0)equipment(y);else if(tab==1)growth(y);else if(tab==2)expedition(y);else shop(y);
        button(p.active?"원정 이어하기":com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.chapterName(p.selectedChapter)+" 출격",left,bottom-22,p.active?width-51:width,22,this::depart,true);
        if(p.active)button("포기하기",left+width-48,bottom-22,48,22,this::abandon,false);
    }
    private void equipment(float y){
        ExtractionProfile p=ExtractionProfile.get();
        if(p.active){
            label("진행 중인 원정",9,left,y,width,GOLD);
            label("원정 중에는 장비를 바꿀 수 없습니다.\n아래 버튼으로 원정을 이어가거나 포기하세요.",7,left,y+20,width,TEXT);
            if(com.watabou.utils.FileUtils.fileExists("extraction-last-crash.txt"))button("최근 오류 기록",left,y+51,width,17,()->{
                String[] lines=com.watabou.utils.FileUtils.getFileHandle("extraction-last-crash.txt").readString("UTF-8").split("\n");
                StringBuilder details=new StringBuilder();int frames=0;
                for(String line:lines){
                    if(!line.startsWith("\tat ")||line.contains("shatteredpixel")){
                        details.append(line.replace("com.shatteredpixel.shatteredpixeldungeon.","")).append('\n');
                        if(line.startsWith("\tat ")&&++frames==3)break;
                    }
                }
                add(new WndMessage(details.toString()));
            },false);
            return;
        }
        label("보관 창고  "+p.stash.size(),7,left,y,width-57,GOLD);
        button("물약 + · 30 G",left+width-56,y-3,56,14,()->{p.buyPotion();refresh();},false);
        int count=4*stashRows, pages=Math.max(1,(p.stash.size()+count-1)/count);
        stashPage=Math.min(stashPage,pages-1);
        grid(p.stash,stashPage*count,stashRows,y+12,false);
        float pager=y+12+stashRows*cellStep;
        button("<",left,pager,22,13,()->{stashPage=Math.max(0,stashPage-1);refresh();},false);
        centered("창고 "+(stashPage+1)+" / "+pages,6,left+24,pager+3,width-48,MUTED);
        button(">",left+width-22,pager,22,13,()->{stashPage=Math.min(pages-1,stashPage+1);refresh();},false);
        float bagY=pager+17;
        label("출격  "+p.prepared.size()+" / "+p.capacity()+(p.preparedWeapon()==null?" · 기본 검 지급":""),7,left,bagY,width-(p.capacity()>12?56:30),GREEN);
        button("비우기",left+width-29,bagY-2,29,13,()->{p.returnPrepared();bagPage=0;refresh();},false);
        if(p.capacity()>12)button((bagPage+1)+"/2",left+width-54,bagY-2,23,13,()->{bagPage=1-bagPage;refresh();},false);
        bagPage=Math.min(bagPage,Math.max(0,(p.capacity()-1)/12));
        grid(p.prepared,bagPage*12,3,bagY+12,true);
        float after=bagY+12+3*cellStep;
        if(after+10<bottom-26)label(p.nodes.contains("ranged_0")?"물통·영혼의 활 자동 지급 · 탭: 넣기 / 빼기":"탭: 넣기 / 빼기 · 물통 기본 지급",6,left,after+3,width,MUTED);
        if(after+77<bottom-26){
            panel(left,after+16,width,61);
            Item weapon=p.preparedWeapon(), armor=p.preparedArmor();
            label("착용 무기  "+(weapon==null?"보급 낡은 검 (자동 지급)":weapon.title()),6,left+5,after+20,width-10,GOLD);
            label("착용 갑옷  "+(armor==null?"없음":armor.title()),6,left+5,after+34,width-10,GREEN);
            label("착용 바지  "+(p.preparedPants()==null?"없음":p.preparedPants().title()),6,left+5,after+48,width-10,GREEN);
            label("착용 신발  "+(p.preparedBoots()==null?"없음":p.preparedBoots().title()),6,left+5,after+62,width-10,GREEN);
        }
    }
    private void debugMenu(){new com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDebugMenu(this::add,this::refresh,false).open();}
    private void grid(ArrayList<Item> items,int offset,int rows,float y,boolean bag){
        ExtractionProfile p=ExtractionProfile.get();float cw=(width-6)/4f;
        Item weapon=p.preparedWeapon(),armor=p.preparedArmor();
        for(int j=0;j<rows*4;j++){
            int index=offset+j;float x=left+(j%4)*(cw+2),sy=y+(j/4)*cellStep;
            boolean usable=!bag||index<p.capacity();
            ColorBlock border=new ColorBlock(cw,cellStep-2,bag?0xFF3F6C60:0xFF394957);border.x=x;border.y=sy;body.add(border);
            ColorBlock fill=new ColorBlock(cw-2,cellStep-4,usable?0xFF131E29:0xFF090E15);fill.x=x+1;fill.y=sy+1;body.add(fill);
            final Item i=index<items.size()?items.get(index):null;
            if(i!=null){
                ItemSlot slot=new ItemSlot(i){
                    @Override protected void onClick(){try{p.prepare(i,!bag);refresh();}catch(RuntimeException e){error(e);}}
                    @Override protected boolean onLongClick(){itemDetails(i,bag);return true;}
                };
                slot.setRect(x+1,sy+1,cw-2,cellStep-4);body.add(slot);
                if(bag&&(p.preparedEquipment(i)))label("착용",5,x+2,sy+cellStep-10,cw-4,GREEN);
            }
        }
    }
    private void itemDetails(final Item i,boolean bag){
        ExtractionProfile p=ExtractionProfile.get();
        boolean gear=bag&&(!(i instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow)&&i instanceof KindOfWeapon||i instanceof Armor||i instanceof com.shatteredpixel.shatteredpixeldungeon.extraction.ExpeditionClothing||i instanceof com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc);
        boolean worn=p.preparedEquipment(i);
        ArrayList<String> options=new ArrayList<>();
        options.add(bag?"창고로 빼기":"출격 가방에 넣기");
        options.add("성능 / 상세 보기");
        if(gear&&!worn)options.add("출격 시 착용하기");
        if(!bag)options.add("판매 메뉴 · "+ExtractionShop.salePrice(i)+" G");
        options.add("닫기");
        add(new WndOptions(i.title(),bag?(worn?"출격 시 착용하는 장비입니다.":"출격 가방에 준비한 물품입니다."):"창고 보관 물품입니다. 출격에 가져간 물품은 사망하면 잃습니다.",options.toArray(new String[0])){
            @Override protected void onSelect(int c){try{
                if(c==0){p.prepare(i,!bag);refresh();}
                else if(c==1)ExtractionHubScene.this.add(new WndInfoItem(i));
                else if(c==2&&gear&&!worn){p.selectEquipment(i);refresh();}
                else if(c==2&&!bag){saleDetails(i);}
            }catch(RuntimeException e){error(e);}}
        });
    }
    private void shop(float y){
        ExtractionProfile p=ExtractionProfile.get();
        if(p.active){label("원정 중에는 상점을 이용할 수 없습니다.",7,left,y,width,TEXT);return;}
        float half=(width-2)/2f;
        button("구매",left,y,half,18,()->{shopSelling=false;shopPage=0;refresh();},!shopSelling);
        button("판매",left+half+2,y,half,18,()->{shopSelling=true;shopPage=0;refresh();},shopSelling);
        ArrayList<Item> items=new ArrayList<>();ArrayList<Integer> offers=new ArrayList<>();
        if(shopSelling){
            label("창고 물품 · 준비한 물품은 먼저 빼 주세요",6,left,y+24,width,MUTED);
            items.addAll(p.stash);
        }else{
            float cw=(width-2*(ExtractionShop.CATEGORIES.length-1))/ExtractionShop.CATEGORIES.length;
            for(int c=0;c<ExtractionShop.CATEGORIES.length;c++){final int category=c;
                button(ExtractionShop.CATEGORIES[c],left+c*(cw+2),y+22,cw,16,()->{shopCategory=category;shopPage=0;refresh();},c==shopCategory);
            }
            for(int n=0;n<ExtractionShop.OFFERS.size();n++)if(ExtractionShop.OFFERS.get(n).category==shopCategory&&ExtractionShop.OFFERS.get(n).available()){
                offers.add(n);items.add(ExtractionShop.OFFERS.get(n).item());
            }
        }
        float gy=y+43, step=33, cw=(width-6)/4f;
        int rows=Math.max(1,Math.min(5,(int)((bottom-43-gy)/step))), count=rows*4;
        int pages=Math.max(1,(items.size()+count-1)/count);shopPage=Math.min(shopPage,pages-1);
        for(int j=0;j<count;j++){
            final int index=shopPage*count+j;float x=left+(j%4)*(cw+2),sy=gy+(j/4)*step;
            panel(x,sy,cw,step-2);
            if(index>=items.size())continue;
            final Item item=items.get(index);final int offerIndex=shopSelling?-1:offers.get(index);
            final boolean selling=shopSelling;
            ItemSlot slot=new ItemSlot(item){
                @Override protected void onClick(){if(selling)saleDetails(item);else purchaseDetails(offerIndex,item);}
                @Override protected boolean onLongClick(){ExtractionHubScene.this.add(new WndInfoItem(item));return true;}
            };
            slot.setRect(x+1,sy+1,cw-2,22);body.add(slot);
            int price=selling?ExtractionShop.salePrice(item):ExtractionShop.OFFERS.get(offerIndex).price;
            centered(price+" G",5,x,sy+24,cw,selling||p.gold>=price?GOLD:MUTED);
        }
        float pager=gy+rows*step+2;
        button("<",left,pager,22,13,()->{shopPage=Math.max(0,shopPage-1);refresh();},false);
        centered((shopPage+1)+" / "+pages,6,left+24,pager+3,width-48,MUTED);
        button(">",left+width-22,pager,22,13,()->{shopPage=Math.min(pages-1,shopPage+1);refresh();},false);
        if(pager+25<bottom-25)label("탭: 거래 · 길게: 성능 / 상세 보기",6,left,pager+19,width,MUTED);
    }
    private void purchaseDetails(final int offerIndex,final Item item){
        ExtractionProfile p=ExtractionProfile.get();int price=ExtractionShop.OFFERS.get(offerIndex).price;
        boolean can=p.gold>=price;
        String description="구매가 "+price+" G · 보유 "+p.gold+" G\n구매한 물품은 창고에 보관됩니다."
                +(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon?"\n착용에 필요한 힘과 성능은 상세에서 확인하세요.":"");
        add(new WndOptions(item.title(),description,can?new String[]{"구매 · "+price+" G","성능 / 상세 보기","닫기"}:new String[]{"성능 / 상세 보기","닫기"}){
            @Override protected void onSelect(int c){try{
                if(can&&c==0){p.buy(offerIndex);refresh();}
                else if(c==(can?1:0))ExtractionHubScene.this.add(new WndInfoItem(item));
            }catch(RuntimeException e){error(e);}}
        });
    }
    private void saleDetails(final Item item){
        ExtractionProfile p=ExtractionProfile.get();int price=ExtractionShop.salePrice(item);
        boolean one=ExtractionShop.canSellOne(item), can=price>0;
        ArrayList<String> choices=new ArrayList<>();
        if(can&&one)choices.add("1개 판매 · "+ExtractionShop.salePrice(item.duplicate().quantity(1))+" G");
        if(can)choices.add((one?"전부 판매":"판매")+" · "+price+" G");
        int detail=choices.size();choices.add("성능 / 상세 보기");choices.add("닫기");
        String description=(can?"판매하면 창고에서 제거되고 골드로 정산됩니다.":"판매할 수 없는 물품입니다.")
                +(item instanceof com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag?"\n가방 안의 물품도 함께 판매합니다.":"");
        add(new WndOptions(item.title(),description,choices.toArray(new String[0])){
            @Override protected void onSelect(int c){try{
                if(c==detail)ExtractionHubScene.this.add(new WndInfoItem(item));
                else if(can&&c<detail){p.sell(item,!(one&&c==0));refresh();}
            }catch(RuntimeException e){error(e);}}
        });
    }
    private void growth(float y){
        ExtractionProfile p=ExtractionProfile.get();
        label("성장 노드 "+p.nodes.size()+" / "+ExtractionProfile.IDS.length,8,left,y,width-40,GOLD);
        button("초기화",left+width-38,y-2,38,14,()->com.shatteredpixel.shatteredpixeldungeon.windows.WndGrowthReset.open(this::refresh),false);
        label("남은 "+p.points+" P · 사용한 "+p.spentPoints()+" P",6,left,y+12,width,GREEN);
        ExtractionNodeTree tree=new ExtractionNodeTree(this::node,()->add(new WndGrowthAtlas(this::refresh)));
        body.add(tree);tree.setRect(left,y+26,width,Math.min(166,bottom-27-(y+26)));
        float after=tree.bottom()+7;
        if(after+9<bottom-26)label("레벨은 능력치를 올리지 않음 · 레벨당 3 P",6,left,after,width,GREEN);
    }
    private void node(final int n){
        ExtractionProfile p=ExtractionProfile.get();boolean learned=p.nodes.contains(ExtractionProfile.IDS[n]);
        boolean unlocked=p.unlocked(n);
        String state=learned?"이미 습득했습니다.":!unlocked?"선행: "+p.prerequisites(n):p.active?"거점으로 돌아온 뒤 배울 수 있습니다.":"비용 "+ExtractionProfile.COSTS[n]+" P · 보유 "+p.points+" P";
        boolean can=!learned&&unlocked&&!p.active&&p.points>=ExtractionProfile.COSTS[n];
        add(new WndOptions(ExtractionProfile.NAMES[n],ExtractionProfile.DESCS[n]+"\n\n선행: "+p.prerequisites(n)+"\n\n"+state,can?new String[]{"습득 · "+ExtractionProfile.COSTS[n]+" P","닫기"}:new String[]{"닫기"}){
            @Override protected void onSelect(int c){if(can&&c==0)try{p.learn(n);refresh();}catch(RuntimeException e){error(e);}}
        });
    }
    private void expedition(float y){
        ExtractionProfile p=ExtractionProfile.get();
        int chapter=p.active?p.raidChapter:p.selectedChapter, difficulty=p.active?p.raidDifficulty:p.selectedDifficulty;
        String[] shortNames={"하수도","감옥","동굴","드워프 도시","악마의 전당"};
        for(int c=1;c<=com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.CHAPTER_COUNT;c++){final int selected=c;
            int columns=c<=3?3:2, column=c<=3?c-1:c-4;float cw=(width-2*(columns-1))/columns;
            button(String.format(java.util.Locale.ROOT,"%02d ",c)+shortNames[c-1]+(p.unlockedDifficulty[c-1]==0?" 잠금":""),left+column*(cw+2),y+(c<=3?0:21),cw,19,()->{
                if(p.active)throw new IllegalStateException("진행 중인 원정을 먼저 마치세요.");
                if(p.unlockedDifficulty[selected-1]==0)throw new IllegalStateException(com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.chapterName(selected-1)+" 보스를 잡고 탈출하면 열립니다.");
                p.selectRaid(selected);refresh();
            },chapter==c);
        }
        centered(p.active&&p.raidRules<3?"이전 원정 · 기존 규칙 유지":chapter==1?"파밍과 성장":"고난도 · 이전 지역에서 준비",7,left,y+47,width,GOLD);
        panel(left,y+62,width,48);
        int start=com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.startDepth(chapter),end=com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.endDepth(chapter);
        label(start+"~"+end+"층 · "+com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.bossName(chapter),8,left+5,y+67,width-10,GOLD);
        int tier=p.active&&p.raidRules<2?com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.maxTier(chapter,difficulty):com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionDifficulty.chapterMaxTier(chapter);
        label("보스 처치 후 다음 계단에서 탈출\n장비 최대 T"+tier+" · "+(chapter==1?"반복 파밍으로 출격 준비":"강화 보스 · 높은 고정 난이도"),6,left+5,y+82,width-10,TEXT);
        label("탈출: 장비 보관 · 포션/스크롤 골드 정산\n사망: 원정 물품 손실 · 경험치/노드 유지",6,left,y+116,width,TEXT);
        button("챕터 / 유물 안내",left,y+139,width,13,this::expeditionGuide,false);
        if(!p.result.isEmpty()&&y+158+35<bottom-27)label(p.result,6,left,y+158,width,GREEN);
    }
    private void expeditionGuide(){
        add(new WndOptions("원정 안내","확인할 내용을 선택하세요.","챕터 / 보상","신규 유물","착용 / 상점"){
            @Override protected void onSelect(int index){
                String[] pages={
                    "각 챕터는 독립된 5층 원정입니다. 앞 챕터 보스를 처치하고 탈출하면 다음 지역이 열립니다.\n\n01 하수도 1~5층 · 구 · 최대 T2\n02 감옥 6~10층 · 텐구 · 최대 T3\n03 동굴 11~15층 · DM-300 · 최대 T4\n04 드워프 도시 16~20층 · 드워프 제왕 · 최대 T5\n05 악마의 전당 21~25층 · 요그제바 · 최대 T5\n\n챕터가 높아질수록 적의 원본 체력 / 공격 피해 배율이 크게 오릅니다.\n하수도 ×1.25 / ×1.1 → 감옥 ×4 / ×2 → 동굴 ×6 / ×2.8 → 도시 ×9 / ×3.8 → 전당 ×13 / ×5. 후반 지역일수록 보상도 높습니다. 장비와 성장 노드로 준비하세요. 보스는 해당 챕터 최상위 티어 무기를 보장합니다.\n\n보스 처치 후 아래 계단에서 정산하여 거점으로 돌아옵니다. 5챕터도 같은 방식으로 완주하고 다시 파밍할 수 있습니다. 일반 원정에는 수식어 정예가 없습니다. 기존 진행 중인 원정은 이전 규칙을 유지합니다.",
                    "피의 등불: 처치 회복 / 물약 회복 감소\n\n탐욕의 주머니: 좋은 장비 / 골드에 따른 피해 증가\n\n깨진 모래시계: 시간 가속 / 이후 둔화\n\n사냥꾼의 표식: 지정 적 피해 증가 / 다른 적 피해 감소\n\n불안정한 나침반: 비밀 감지 / 주변 적 유인",
                    "유물은 최대 2개 착용하며 같은 유물은 중복 착용할 수 없습니다. 창고나 가방에 있는 유물은 효과가 없습니다.\n\n준비 화면에서 유물을 길게 눌러 착용할 물품을 선택하세요. 신규 유물은 적 처치로 성장하며 +15가 상한입니다. 기존 유물은 각자의 성장 방식을 따릅니다.\n\n상점은 보급품과 T1~T5 무기·갑옷·바지·신발을 판매합니다."
                };
                if(index>=0&&index<pages.length)ExtractionHubScene.this.add(new WndMessage(pages[index]));
            }
        });
    }
    private void abandon(){
        add(new WndOptions("원정 포기","출격 물품과 전리품을 잃습니다. 창고·성장 노드·이미 얻은 성장 경험치는 유지됩니다.","원정 포기","취소"){
            @Override protected void onSelect(int index){
                if(index==0)try{ExtractionProfile.get().abandon();refresh();}catch(RuntimeException e){error(e);}
            }
        });
    }
    private void depart(){
        ExtractionProfile p=ExtractionProfile.get();p.begin();
        GamesInProgress.selectedClass=HeroClass.WARRIOR;GamesInProgress.curSlot=1;
        SPDSettings.intro(false);SPDSettings.challenges(0);ActionIndicator.clearAction();
        if(GamesInProgress.check(1)!=null)InterlevelScene.mode=InterlevelScene.Mode.CONTINUE;
        else{Dungeon.hero=null;Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();InterlevelScene.mode=InterlevelScene.Mode.DESCEND;}
        ShatteredPixelDungeon.switchScene(InterlevelScene.class);
    }
    private void error(RuntimeException e){refresh();add(new WndMessage(e.getMessage()==null?"작업을 완료하지 못했습니다.":e.getMessage()));}
    private void panel(float x,float y,float w,float h){ColorBlock bg=new ColorBlock(w,h,0xEC15212D);bg.x=x;bg.y=y;body.add(bg);}
    private RenderedTextBlock label(String s,int size,float x,float y,float w,int color){
        RenderedTextBlock t=renderTextBlock(s,size);t.maxWidth((int)w);t.hardlight(color);t.setPos(x,y);body.add(t);return t;
    }
    private void centered(String s,int size,float x,float y,float w,int color){RenderedTextBlock t=label(s,size,x,y,w,color);t.setPos(x+(w-t.width())/2f,y);}
    private HubButton button(String s,float x,float y,float w,float h,Runnable action,boolean accent){
        HubButton b=new HubButton(s,action,accent);b.setRect(x,y,w,h);body.add(b);return b;
    }
    private class HubButton extends StyledButton {
        private final boolean accent;
        private final Runnable action;
        HubButton(String s,Runnable action,boolean accent){
            super(Chrome.Type.GREY_BUTTON,s,7);this.accent=accent;this.action=action;
            bg.hardlight(accent?0x31594D:0x263541);textColor(accent?0xE4C583:TEXT);
        }
        @Override protected void onPointerUp(){super.onPointerUp();bg.hardlight(accent?0x31594D:0x263541);}
        @Override protected void onClick(){try{action.run();}catch(RuntimeException e){error(e);}}
    }
    @Override protected void onBackPressed(){if(tab!=0){tab=0;refresh();}else Game.instance.finish();}
}
