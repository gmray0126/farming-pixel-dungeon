/* Extraction fork © 2026. GPL-3.0-or-later; original credits retained. */
package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionProfile;
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
    private static int tab=0, stashPage=0, bagPage=0;
    private Group body;
    private float left, top, width, bottom;
    private int stashRows;
    private float cellStep;
    private static final int TEXT=0xD5DFE4, MUTED=0x8D9FA9, GOLD=0xE4C583, GREEN=0x83C9B5;

    @Override public void create() {
        super.create();uiCamera.visible=false;
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
        label("잔향 원정대",11,left,top,width-34,GOLD);
        button("설정",left+width-30,top,30,15,()->add(new WndSettings()),false);
        label(p.gold+" G  ·  "+p.points+" P  ·  성장 "+(1+p.xp/25),7,left,top+20,width,TEXT);
        float tw=(width-4)/3f;
        String[] titles={"장비 준비","성장","원정"};
        for(int i=0;i<3;i++){
            final int n=i;
            HubButton b=button(titles[i],left+i*(tw+2),top+33,tw,19,()->{tab=n;refresh();},i==tab);
            if(i==tab)b.textColor(GOLD);
        }
        float y=top+58;
        if(tab==0)equipment(y);else if(tab==1)growth(y);else expedition(y);
        button(p.active?"원정 이어하기":"하수도로 출격",left,bottom-22,width,22,this::depart,true);
    }
    private void equipment(float y){
        ExtractionProfile p=ExtractionProfile.get();
        if(p.active){
            label("진행 중인 원정",9,left,y,width,GOLD);
            label("원정 중에는 장비를 바꿀 수 없습니다.\n아래 버튼으로 원정을 이어가세요.",7,left,y+20,width,TEXT);return;
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
        label("출격  "+p.prepared.size()+" / "+p.capacity(),7,left,bagY,width-(p.capacity()>12?56:30),GREEN);
        button("비우기",left+width-29,bagY-2,29,13,()->{p.returnPrepared();bagPage=0;refresh();},false);
        if(p.capacity()>12)button((bagPage+1)+"/2",left+width-54,bagY-2,23,13,()->{bagPage=1-bagPage;refresh();},false);
        bagPage=Math.min(bagPage,Math.max(0,(p.capacity()-1)/12));
        grid(p.prepared,bagPage*12,3,bagY+12,true);
        float after=bagY+12+3*cellStep;
        if(after+10<bottom-26)label("탭: 넣기 / 빼기 · 길게: 착용 / 상세",6,left,after+3,width,MUTED);
        if(after+51<bottom-26){
            panel(left,after+16,width,33);
            Item weapon=p.preparedWeapon(), armor=p.preparedArmor();
            label("착용 무기  "+(weapon==null?"맨손":weapon.title()),6,left+5,after+20,width-10,GOLD);
            label("착용 갑옷  "+(armor==null?"없음":armor.title()),6,left+5,after+34,width-10,GREEN);
        }
    }
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
                if(bag&&(i==weapon||i==armor))label("착용",5,x+2,sy+cellStep-10,cw-4,GREEN);
            }
        }
    }
    private void itemDetails(final Item i,boolean bag){
        ExtractionProfile p=ExtractionProfile.get();
        boolean gear=bag&&(i instanceof KindOfWeapon||i instanceof Armor);
        boolean worn=i==p.preparedWeapon()||i==p.preparedArmor();
        ArrayList<String> options=new ArrayList<>();
        options.add(bag?"창고로 빼기":"출격 가방에 넣기");
        options.add("성능 / 상세 보기");
        if(gear&&!worn)options.add("출격 시 착용하기");
        if(!bag)options.add("판매 · "+i.value()+" G");
        options.add("닫기");
        add(new WndOptions(i.title(),bag?(worn?"출격 시 착용하는 장비입니다.":"출격 가방에 준비한 물품입니다."):"창고 보관 물품입니다. 출격에 가져간 물품은 사망하면 잃습니다.",options.toArray(new String[0])){
            @Override protected void onSelect(int c){try{
                if(c==0){p.prepare(i,!bag);refresh();}
                else if(c==1)ExtractionHubScene.this.add(new WndInfoItem(i));
                else if(c==2&&gear&&!worn){p.selectEquipment(i);refresh();}
                else if(c==2&&!bag){p.sell(i);refresh();}
            }catch(RuntimeException e){error(e);}}
        });
    }
    private void growth(float y){
        ExtractionProfile p=ExtractionProfile.get();
        label("성장 노드  "+p.nodes.size()+" / "+ExtractionProfile.IDS.length+"  ·  "+p.points+" P",8,left,y,width,GOLD);
        label("노드를 눌러 효과와 해금 조건을 확인",6,left,y+12,width,MUTED);
        ExtractionNodeTree tree=new ExtractionNodeTree(this::node);
        tree.setRect(left,y+26,width,Math.min(166,bottom-27-(y+26)));body.add(tree);
        float after=tree.bottom()+7;
        if(after+9<bottom-26)label("습득 노드는 사망해도 유지 · 25 XP = 1 P",6,left,after,width,GREEN);
    }
    private void node(final int n){
        ExtractionProfile p=ExtractionProfile.get();boolean learned=p.nodes.contains(ExtractionProfile.IDS[n]);
        int parent=ExtractionProfile.PARENTS[n];boolean unlocked=parent<0||p.nodes.contains(ExtractionProfile.IDS[parent]);
        String state=learned?"이미 습득했습니다.":!unlocked?"선행: "+ExtractionProfile.NAMES[parent]:p.active?"거점으로 돌아온 뒤 배울 수 있습니다.":"비용 "+ExtractionProfile.COSTS[n]+" P · 보유 "+p.points+" P";
        boolean can=!learned&&unlocked&&!p.active&&p.points>=ExtractionProfile.COSTS[n];
        add(new WndOptions(ExtractionProfile.NAMES[n],ExtractionProfile.DESCS[n]+"\n\n"+state,can?new String[]{"습득 · "+ExtractionProfile.COSTS[n]+" P","닫기"}:new String[]{"닫기"}){
            @Override protected void onSelect(int c){if(can&&c==0)try{p.learn(n);refresh();}catch(RuntimeException e){error(e);}}
        });
    }
    private void expedition(float y){
        ExtractionProfile p=ExtractionProfile.get();
        panel(left,y,width,71);
        label("01  하수도",11,left+8,y+8,width-16,GOLD);
        label("낡은 수로에서 물품을 회수하세요.\n아래 계단이 탈출 지점입니다.",7,left+8,y+27,width-16,TEXT);
        label(p.active?"상태: 원정 진행 중":"상태: 출격 가능",6,left+8,y+57,width-16,GREEN);
        label("원정 규칙",8,left,y+82,width,GOLD);
        label("탈출: 장비와 전리품을 창고로\n사망: 가져간 물품과 전리품 손실\n유지: 창고 · 성장 노드 · 경험치",7,left,y+97,width,TEXT);
        float after=y+135;
        if(!p.result.isEmpty()&&after+30<bottom-27)label(p.result,6,left,after,width,GREEN);
        if(after+45<bottom-27)button("제작자 / 원본 크레딧",left,bottom-46,width,17,()->ShatteredPixelDungeon.switchScene(AboutScene.class),false);
    }
    private void depart(){
        ExtractionProfile p=ExtractionProfile.get();p.begin();
        GamesInProgress.selectedClass=HeroClass.WARRIOR;GamesInProgress.curSlot=1;
        SPDSettings.intro(false);SPDSettings.challenges(0);ActionIndicator.clearAction();
        if(GamesInProgress.check(1)!=null)InterlevelScene.mode=InterlevelScene.Mode.CONTINUE;
        else{Dungeon.hero=null;Dungeon.daily=Dungeon.dailyReplay=false;Dungeon.customSeedText="";Dungeon.initSeed();InterlevelScene.mode=InterlevelScene.Mode.DESCEND;}
        ShatteredPixelDungeon.switchScene(InterlevelScene.class);
    }
    private void error(RuntimeException e){add(new WndMessage(e.getMessage()==null?"작업을 완료하지 못했습니다.":e.getMessage()));}
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
