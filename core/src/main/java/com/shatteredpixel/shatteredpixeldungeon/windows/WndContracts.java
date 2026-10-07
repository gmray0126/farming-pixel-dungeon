/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.extraction.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.watabou.noosa.*;
import java.util.ArrayList;

/** Three cards per page; all child windows belong to the live scene. */
public class WndContracts extends Window {
    private Group body;private int mode=0,page=0;private final Runnable closed;private final int w,h;
    public WndContracts(Runnable closed){this.closed=closed;w=PixelScene.landscape()?185:136;h=Math.min(220,Camera.main.height-26);resize(w,h);refresh();}
    private void label(String value,int size,int x,int y,int width,int color){RenderedTextBlock t=PixelScene.renderTextBlock(value,size);t.maxWidth(width);t.hardlight(color);t.setPos(x,y);body.add(t);}
    private void button(String value,float x,float y,float width,float height,Runnable action){StyledButton b=new StyledButton(Chrome.Type.GREY_BUTTON,value,6){@Override protected void onClick(){try{action.run();}catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}};b.setRect(x,y,width,height);body.add(b);}
    private void refresh(){
        if(body!=null){erase(body);body.destroy();}body=new Group();add(body);
        ExtractionProfile p=ExtractionProfile.get();label("의뢰 게시판",9,1,1,w-27,0xE4C583);button("닫기",w-25,0,25,13,this::hide);
        label("수락 "+p.contracts.size()+" / 3 · 최초 완료 "+p.completedContracts.size()+" / "+ExtractionContracts.JOBS.length,6,0,17,w,0x83C9B5);
        float bw=(w-4)/3f;String[] tabs={"게시판","진행 중","완료 기록"};
        for(int i=0;i<3;i++){final int tab=i;button(tabs[i],i*(bw+2),29,bw,15,()->{mode=tab;page=0;refresh();});}
        ArrayList<ExtractionContracts.Job> jobs=new ArrayList<>();
        for(ExtractionContracts.Job j:ExtractionContracts.JOBS){if(mode==0&&ExtractionContracts.available(p,j)&&!p.contracts.contains(j.id)||mode==1&&p.contracts.contains(j.id)||mode==2&&p.completedContracts.contains(j.id))jobs.add(j);}
        int pages=Math.max(1,(jobs.size()+2)/3);page=Math.min(page,pages-1);float row=(h-79)/3f;
        for(int i=page*3;i<Math.min(jobs.size(),page*3+3);i++){
            ExtractionContracts.Job j=jobs.get(i);int y=Math.round(49+(i-page*3)*row);button("",0,y,w,row-3,()->details(j));
            label(j.name,7,4,y+3,w-8,0xE4C583);
            String status=mode==1?ExtractionContracts.progress(p,j)+" / "+j.goal+(ExtractionContracts.progress(p,j)>=j.goal?" · 완료 가능":" · 탈출 후 저장"):ExtractionDifficulty.chapterName(j.chapter)+" · "+(p.completedContracts.contains(j.id)?"반복 의뢰":"최초 장비 보상");
            label(status,5,4,y+15,w-8,0xD5DFE4);label(j.gold+" G · 성장 XP "+j.xp,5,4,y+24,w-8,0x83C9B5);
        }
        if(jobs.isEmpty())label(mode==1?"받은 의뢰가 없습니다.":mode==2?"완료한 의뢰가 없습니다.":"지역 또는 혼합 입문 노드를 해금하면\n새 의뢰가 나타납니다.",6,4,59,w-8,0xD5DFE4);
        button("이전",0,h-18,30,15,()->{page=(page+pages-1)%pages;refresh();});label((page+1)+" / "+pages,6,w/2-14,h-15,40,0xD5DFE4);button("다음",w-30,h-18,30,15,()->{page=(page+1)%pages;refresh();});
    }
    private void details(ExtractionContracts.Job j){
        ExtractionProfile p=ExtractionProfile.get();boolean taken=p.contracts.contains(j.id),ready=taken&&ExtractionContracts.progress(p,j)>=j.goal;
        String[] options=p.active?new String[]{"닫기"}:taken?ready?new String[]{"의뢰 완료","의뢰 취소","닫기"}:new String[]{"의뢰 취소","닫기"}:new String[]{"의뢰 수락","닫기"};
        Game.scene().add(new WndOptions(j.name,j.description()+"\n\n저장된 진행도 "+ExtractionContracts.progress(p,j)+" / "+j.goal,options){
            @Override protected void onSelect(int index){try{
                if(p.active)return;
                if(taken&&ready&&index==0)donation(j);
                else if(taken&&index==(ready?1:0))Game.scene().add(new WndOptions("의뢰 취소","이 의뢰의 저장된 진행도를 잃습니다.","취소하기","돌아가기"){@Override protected void onSelect(int c){if(c==0){ExtractionContracts.cancel(p,j.id);refresh();}}});
                else if(!taken&&index==0){ExtractionContracts.accept(p,j.id);mode=1;page=0;refresh();}
            }catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}
        });
    }
    private void donation(ExtractionContracts.Job j){
        ExtractionProfile p=ExtractionProfile.get();if(j.kind!=ExtractionContracts.Kind.DELIVER){reward(j,null);return;}
        ArrayList<Item> eligible=new ArrayList<>();for(Item item:p.stash)if(ExtractionContracts.deliverable(item,j))eligible.add(item);
        chooseDonation(j,eligible,0);
    }
    private void chooseDonation(ExtractionContracts.Job j,ArrayList<Item> items,int page){
        int first=page*4,last=Math.min(items.size(),first+4);ArrayList<String> labels=new ArrayList<>();for(int i=first;i<last;i++)labels.add(items.get(i).name()+" +"+items.get(i).level());if(last<items.size())labels.add("다음");labels.add("닫기");
        Game.scene().add(new WndOptions("납품 무기 선택",items.isEmpty()?"창고에 해당 티어의 저주 없는 근접 무기가 없습니다. 준비한 무기는 창고로 돌려놓으세요.":"선택한 무기 한 자루가 소모됩니다.",labels.toArray(new String[0])){
            @Override protected void onSelect(int i){if(i<last-first)reward(j,items.get(first+i));else if(last<items.size()&&i==last-first)chooseDonation(j,items,page+1);}
        });
    }
    private void reward(ExtractionContracts.Job j,Item donation){
        ExtractionProfile p=ExtractionProfile.get();if(p.completedContracts.contains(j.id)){finish(j,donation,1);return;}
        Game.scene().add(new WndOptions("최초 완료 장비 선택","T"+ExtractionDifficulty.chapterMaxTier(j.chapter)+" 장비를 골라 받습니다."+(donation==null?"":"\n납품: "+donation.name()+" +"+donation.level()),"무기","갑옷","바지","신발","취소"){
            @Override protected void onSelect(int i){if(i<4)finish(j,donation,i+1);}
        });
    }
    private void finish(ExtractionContracts.Job j,Item donation,int category){try{ExtractionContracts.claim(ExtractionProfile.get(),j.id,donation,category);refresh();Game.scene().add(new WndMessage(ExtractionProfile.get().result));}catch(RuntimeException e){Game.scene().add(new WndMessage(e.getMessage()));}}
    @Override public void hide(){super.hide();if(closed!=null)closed.run();}
}
