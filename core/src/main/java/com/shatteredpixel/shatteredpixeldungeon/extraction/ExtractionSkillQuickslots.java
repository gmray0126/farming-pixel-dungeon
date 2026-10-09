/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Toolbar;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;
import java.util.ArrayList;

/** Saved ability references, never inventory items or loot. Execution uses the original skill handlers. */
public final class ExtractionSkillQuickslots {
    private ExtractionSkillQuickslots(){}
    public static ExtractionClassSkills.Action find(Hero hero,String key){
        if(hero==null||key==null)return null;
        for(ExtractionClassSkills.Action action:ExtractionClassSkills.shortcuts(hero))if(key.equals(action.key))return action;
        return null;
    }
    public static boolean usableReference(Hero hero,Item item){
        if(item instanceof Shortcut)return find(hero,((Shortcut)item).key)!=null;
        return hero!=null&&item!=null&&hero.belongings.contains(item);
    }
    public static void restore(Hero hero){
        if(hero==null||hero.extractionRaidID==0)return;
        ExtractionProfile profile=ExtractionProfile.get();
        for(int slot=0;slot<QuickSlot.SIZE;slot++){
            Item old=Dungeon.quickslot.getItem(slot);
            if(old instanceof Shortcut&&!usableReference(hero,old))Dungeon.quickslot.clearSlot(slot);
            String key=profile.skillQuickslots[slot];
            if(key!=null&&find(hero,key)!=null&&(old==null||old instanceof Shortcut))Dungeon.quickslot.setSlot(slot,new Shortcut(key));
        }
        Item.updateQuickslot();
    }
    public static void chooseBinding(int slot,Runnable itemPicker){
        if(Dungeon.hero==null||!Dungeon.hero.ready)return;
        if(Dungeon.hero.extractionRaidID==0){itemPicker.run();return;}
        GameScene.show(new WndOptions("퀵슬롯 "+(slot+1),"등록할 종류를 선택하세요. 스킬은 가방 칸을 차지하지 않습니다.","스킬 등록","아이템 등록","비우기","닫기"){
            @Override protected void onSelect(int i){
                if(i==0)chooseSkill(slot,0);else if(i==1)itemPicker.run();
                else if(i==2)QuickSlotButton.set(slot,null);
            }
        });
    }
    public static void chooseSkill(int slot,int page){
        Hero hero=Dungeon.hero;ArrayList<ExtractionClassSkills.Action> all=ExtractionClassSkills.shortcuts(hero);
        int first=page*5,last=Math.min(all.size(),first+5);boolean more=last<all.size();
        ArrayList<String> names=new ArrayList<>();for(int i=first;i<last;i++)names.add(all.get(i).name);
        if(more)names.add("다음 스킬");names.add("닫기");
        GameScene.show(new WndOptions("배운 스킬 등록",all.isEmpty()?"사용 가능한 스킬 노드를 먼저 습득하세요.":"충전·재사용 조건은 사용 시 확인합니다. 대상을 고르는 스킬은 퀵슬롯에서 바로 대상 선택으로 넘어갑니다.",names.toArray(new String[0])){
            @Override protected boolean hasInfo(int i){return i<last-first;}
            @Override protected void onInfo(int i){GameScene.show(new WndMessage(all.get(first+i).desc));}
            @Override protected void onSelect(int i){
                if(i<last-first){String key=all.get(first+i).key;if(slot>=0)assign(slot,key);else chooseSlot(key,0);}
                else if(more&&i==last-first)chooseSkill(slot,page+1);
            }
        });
    }
    private static void chooseSlot(String key,int page){
        String[] names=new String[7];int first=page*6;
        for(int i=0;i<6;i++){
            Item old=Dungeon.quickslot.getItem(first+i);
            names[i]="퀵슬롯 "+(first+i+1)+" · "+(old==null?"빈칸":old.name());
        }
        names[6]=page==0?"7~12번 보기":"1~6번 보기";
        GameScene.show(new WndOptions("등록할 위치","선택한 칸을 스킬로 교체합니다.",names){
            @Override protected void onSelect(int i){if(i<6)assign(first+i,key);else chooseSlot(key,1-page);}
        });
    }
    private static void assign(int slot,String key){
        if(find(Dungeon.hero,key)==null){GLog.w("이 스킬을 아직 배우지 않았습니다.");return;}
        QuickSlotButton.set(slot,new Shortcut(key));
        if(!(Dungeon.quickslot.getItem(slot) instanceof Shortcut)||!key.equals(((Shortcut)Dungeon.quickslot.getItem(slot)).key))return;
        Dungeon.quickslot.showSlot(slot,QuickSlot.pageSize(PixelScene.uiCamera.width));Toolbar.updateLayout();
        GLog.p("퀵슬롯 "+(slot+1)+"에 스킬을 등록했습니다.");
    }
    public static class Shortcut extends Item {
        private static final String USE="QUICK_SKILL";
        public String key="";
        public Shortcut(){unique=true;keptThoughLostInvent=true;defaultAction=USE;image=ItemSpriteSheet.ARTIFACT_SPELLBOOK;}
        public Shortcut(String key){this();this.key=key;setImage();}
        private void setImage(){
            image=key.startsWith("spell_")||key.startsWith("holy_")?ItemSpriteSheet.ARTIFACT_TOME
                :key.startsWith("monk_")?ItemSpriteSheet.SAI
                :key.equals("stealth")||key.equals("hybrid_shadow")?ItemSpriteSheet.ARTIFACT_CLOAK
                :key.startsWith("coat_")?ItemSpriteSheet.BREW_SHOCKING
                :key.equals("blood_oath")?ItemSpriteSheet.ARTIFACT_CHALICE1
                :key.equals("hybrid_storm")?ItemSpriteSheet.ARTIFACT_HOURGLASS:ItemSpriteSheet.ARTIFACT_SPELLBOOK;
        }
        @Override public String name(){ExtractionClassSkills.Action a=find(Dungeon.hero,key);return a==null?"미습득 스킬":a.name;}
        @Override public String desc(){ExtractionClassSkills.Action a=find(Dungeon.hero,key);return a==null?"현재 성장 배분에서는 사용할 수 없는 스킬입니다.":a.desc;}
        @Override public boolean isIdentified(){return true;}
        @Override public boolean isUpgradable(){return false;}
        @Override public int value(){return 0;}
        @Override public int energyVal(){return 0;}
        @Override public boolean isSimilar(Item other){return other instanceof Shortcut&&key.equals(((Shortcut)other).key);}
        @Override public ArrayList<String> actions(Hero hero){ArrayList<String> a=new ArrayList<>();if(find(hero,key)!=null)a.add(USE);return a;}
        @Override public String actionName(String action,Hero hero){return "스킬 사용";}
        @Override public void execute(Hero hero,String action){
            if(!USE.equals(action))return;
            ExtractionClassSkills.Action a=find(hero,key);
            if(a==null){GLog.w("이 스킬을 아직 배우지 않았습니다.");return;}
            if(!ExtractionClassSkills.ready(hero)||hero.buff(MagicImmune.class)!=null||!a.enabled){GLog.w("자신의 차례인지, 충전·재사용 시간·사용 조건을 확인하세요.");return;}
            a.use();Item.updateQuickslot();
        }
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("skill_key",key);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);key=b.getString("skill_key");setImage();}
    }
}
