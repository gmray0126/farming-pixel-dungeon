/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;
import com.shatteredpixel.shatteredpixeldungeon.extraction.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.watabou.noosa.*;
import java.util.*;

/** Stash crafting stays on the hub scene, with no expedition inventory or floor required. */
public class WndHubAlchemy extends Window {
    private Group body;private final int w,h;private final Runnable closed;private int page;
    private final ArrayList<Item> selected=new ArrayList<>();
    public WndHubAlchemy(Runnable closed){this.closed=closed;w=PixelScene.landscape()?185:136;h=Math.min(220,Camera.main.height-26);resize(w,h);refresh();}
    private void label(String value,int size,float x,float y,int width,int color){RenderedTextBlock t=PixelScene.renderTextBlock(value,size);t.maxWidth(width);t.hardlight(color);t.setPos(x,y);body.add(t);}
    private void button(String value,float x,float y,float width,float height,Runnable action){StyledButton b=new StyledButton(Chrome.Type.GREY_BUTTON,value,6){@Override protected void onClick(){run(action);}};b.setRect(x,y,width,height);body.add(b);}
    private void run(Runnable action){try{action.run();}catch(RuntimeException e){selected.clear();refresh();Game.scene().add(new WndMessage(e.getMessage()==null?"제작을 완료하지 못했습니다.":e.getMessage()));}}
    private void refresh(){
        if(body!=null){erase(body);body.destroy();}body=new Group();add(body);
        ExtractionProfile p=ExtractionProfile.get();selected.removeIf(i->!p.stash.contains(i));
        label("로비 연금술",9,0,1,w-27,0xE4C583);button("닫기",w-25,0,25,13,this::hide);
        label(p.gold+" G · 연금 에너지 "+p.alchemyEnergy,6,0,17,w,0x83C9B5);
        button("에너지 구매",0,29,(w-2)/2f,16,this::energyMenu);button("재료 분해",(w+2)/2f,29,(w-2)/2f,16,()->chooseItem(true,0));
        button("창고 재료 넣기 ("+selected.size()+" / 3)",0,50,w,17,()->{if(selected.size()<3)chooseItem(false,0);});
        float sw=(w-4)/3f;
        for(int i=0;i<3;i++){final int slotIndex=i;ItemButton cell=new ItemButton(){@Override protected void onClick(){run(()->{if(slotIndex<selected.size()){selected.remove(slotIndex);page=0;refresh();}else chooseItem(false,0);});}};
            cell.setRect(i*(sw+2),71,sw,26);if(i<selected.size())cell.item(selected.get(i));body.add(cell);
            if(i<selected.size())label(selected.get(i).name(),5,i*(sw+2),99,Math.round(sw),0xD5DFE4);
        }
        label("재료는 슬롯마다 1개 · 탭하면 빼기",5,0,111,w,0x88969D);
        ArrayList<Recipe> recipes=selected.isEmpty()?new ArrayList<>():ExtractionAlchemy.recipes(p,selected);
        int count=Math.max(1,(h-148)/24),pages=Math.max(1,(recipes.size()+count-1)/count);page=Math.min(page,pages-1);
        for(int i=page*count;i<Math.min(recipes.size(),page*count+count);i++){
            Recipe recipe=recipes.get(i);Item output=ExtractionAlchemy.preview(p,selected,recipe);int energy=ExtractionAlchemy.cost(p,selected,recipe);
            button(output.name()+" · "+energy+" E",0,123+(i-page*count)*24,w,22,()->confirm(recipe,output,energy));
        }
        if(recipes.isEmpty())label(selected.isEmpty()?"창고 재료를 선택하세요.\n기존 연금술 제작법을 사용합니다.":"이 조합으로 만들 수 있는 제작법이 없습니다.",6,0,129,w,0xD5DFE4);
        button("이전",0,h-18,29,15,()->{page=(page+pages-1)%pages;refresh();});label((page+1)+" / "+pages,6,w/2f-12,h-15,40,0xD5DFE4);button("다음",w-29,h-18,29,15,()->{page=(page+1)%pages;refresh();});
    }
    private void energyMenu(){Game.scene().add(new WndOptions("연금 에너지 구매","에너지 1당 "+ExtractionAlchemy.ENERGY_PRICE+" G. 로비에 보관되어 사망해도 유지됩니다.","+1 · 10 G","+5 · 50 G","+10 · 100 G","닫기"){@Override protected void onSelect(int i){if(i<3)run(()->{ExtractionAlchemy.buyEnergy(ExtractionProfile.get(),i==0?1:i==1?5:10);refresh();});}});}
    private void chooseItem(boolean energy,int page){
        ExtractionProfile p=ExtractionProfile.get();ArrayList<Item> items=new ArrayList<>();
        for(Item item:p.stash){int used=Collections.frequency(selected,item);if(energy?ExtractionAlchemy.energyValue(item,1)>0:Recipe.usableInRecipe(item)&&item.quantity()>used)items.add(item);}
        int first=page*4,last=Math.min(items.size(),first+4);ArrayList<String> names=new ArrayList<>();for(int i=first;i<last;i++)names.add(items.get(i).title());if(last<items.size())names.add("다음");names.add("닫기");
        Game.scene().add(new WndOptions(energy?"분해할 창고 재료":"창고 재료 선택",energy?"선택한 재료는 소모됩니다. 준비 가방의 물품은 먼저 창고로 돌려놓으세요.":"재료 1개를 슬롯에 놓습니다. 같은 재료도 보유 수량만큼 선택할 수 있습니다.",names.toArray(new String[0])){
            @Override protected void onSelect(int i){run(()->{if(i<last-first){Item item=items.get(first+i);if(energy)energize(item);else if(selected.size()<3){selected.add(item);WndHubAlchemy.this.page=0;refresh();}}else if(last<items.size()&&i==last-first)chooseItem(energy,page+1);});}
        });
    }
    private void energize(Item item){Game.scene().add(new WndOptions("재료 분해",item.title()+"\n1개: +"+ExtractionAlchemy.energyValue(item,1)+" E · 전부: +"+ExtractionAlchemy.energyValue(item,item.quantity())+" E","1개 분해","전부 분해","닫기"){@Override protected void onSelect(int i){if(i<2)run(()->{ExtractionAlchemy.energize(ExtractionProfile.get(),item,i==0?1:item.quantity());selected.clear();refresh();});}});}
    private void confirm(Recipe recipe,Item output,int energy){Game.scene().add(new WndOptions(output.title(),"선택한 재료를 소비하여 창고에 완성품을 보관합니다.\n연금 에너지 "+energy+" 필요.","제작","완성품 설명","닫기"){
        @Override protected boolean enabled(int i){return i!=0||ExtractionProfile.get().alchemyEnergy>=energy;}
        @Override protected void onSelect(int i){if(i==0)run(()->{ExtractionAlchemy.craft(ExtractionProfile.get(),selected,recipe);selected.clear();page=0;refresh();});else if(i==1)Game.scene().add(new WndInfoItem(output));}
    });}
    @Override public void hide(){super.hide();if(closed!=null)closed.run();}
}
