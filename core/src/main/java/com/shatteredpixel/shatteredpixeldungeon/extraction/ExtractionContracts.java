/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.*;
import java.util.*;

/** Persistent accepted jobs, run-local progress, and atomic hub reward claims. */
public final class ExtractionContracts {
    public enum Kind { HUNT, RECOVER, DELIVER, HYBRID }
    public static final class Job {
        public final String id,name,target;public final int chapter,goal,gold,xp,hybrid;public final Kind kind;
        Job(String id,String name,int chapter,Kind kind,String target,int goal,int hybrid){
            this.id=id;this.name=name;this.chapter=chapter;this.kind=kind;this.target=target;this.goal=goal;this.hybrid=hybrid;
            gold=40+40*chapter+(kind==Kind.DELIVER?40:0);xp=10+5*chapter;
        }
        public String description(){
            String text=kind==Kind.HUNT?ExtractionDifficulty.chapterName(chapter)+"에서 "+target+" "+goal+"마리를 처치하고 탈출하세요.":
                kind==Kind.RECOVER?ExtractionDifficulty.chapterName(chapter)+"의 "+(ExtractionDifficulty.startDepth(chapter)+2)+"층에서 의뢰 기록을 찾아 회수하고 탈출하세요. 기록은 의뢰 주머니에 따로 보관합니다.":
                kind==Kind.DELIVER?ExtractionDifficulty.chapterName(chapter)+"에서 탈출한 뒤 T"+ExtractionDifficulty.chapterMaxTier(chapter)+" 근접 무기 한 자루를 납품하세요. 창고에서 납품할 무기를 직접 선택하며 해당 무기는 소모됩니다.":
                ExtractionGrowth.BRANCHES[25+hybrid]+"의 연계 효과를 적용한 적 "+goal+"마리를 처치하고 탈출하세요. 해당 혼합 트리를 배운 뒤 수행할 수 있습니다.";
            return text+"\n\n탈출한 원정의 진행도만 저장됩니다. 사망·포기 시 이번 원정의 진행도는 잃지만 의뢰와 이전 탈출의 진행도는 유지됩니다.\n\n보상 "+gold+" G · 성장 XP "+xp+". 최초 완료 시 해당 지역 장비 선택 보상을 추가로 받습니다.";
        }
    }
    public static final Job[] JOBS;
    static {
        ArrayList<Job> jobs=new ArrayList<>();String[] targets={"쥐","해골","박쥐","구울","서큐버스"};
        for(int c=1;c<=5;c++){
            jobs.add(new Job("hunt_"+c,targets[c-1]+" 토벌",c,Kind.HUNT,targets[c-1],c==1?6:4,-1));
            jobs.add(new Job("record_"+c,"분실된 원정 기록 "+c,c,Kind.RECOVER,"",1,-1));
            jobs.add(new Job("supply_"+c,"지역 무기 납품 "+c,c,Kind.DELIVER,"",1,-1));
        }
        for(int i=0;i<6;i++)jobs.add(new Job("hybrid_"+i,ExtractionGrowth.BRANCHES[25+i]+" 실전",1,Kind.HYBRID,"",3,i));
        JOBS=jobs.toArray(new Job[0]);
    }
    public static Job job(String id){for(Job job:JOBS)if(job.id.equals(id))return job;throw new IllegalArgumentException("없는 의뢰입니다.");}
    public static class Run implements Bundlable {
        public String[] ids=new String[0];public int[] progress=new int[0];public int spawnedRecords;
        @Override public void storeInBundle(Bundle b){b.put("ids",ids);b.put("progress",progress);b.put("records",spawnedRecords);}
        @Override public void restoreFromBundle(Bundle b){ids=b.getStringArray("ids");progress=b.getIntArray("progress");spawnedRecords=b.getInt("records");if(progress.length!=ids.length)progress=new int[ids.length];}
    }
    public static Run run(Hero h){
        if(h.extractionContracts==null){h.extractionContracts=new Run();h.extractionContracts.ids=ExtractionProfile.get().contracts.toArray(new String[0]);h.extractionContracts.progress=new int[h.extractionContracts.ids.length];}
        return h.extractionContracts;
    }
    public static boolean available(ExtractionProfile p,Job job){return p.unlockedDifficulty[job.chapter-1]>0&&(job.kind!=Kind.HYBRID||p.nodes.contains(ExtractionHybridTrees.PREFIXES[job.hybrid]+"_0"));}
    public static int progress(ExtractionProfile p,Job job){return Math.min(job.goal,p.contractProgress.getOrDefault(job.id,0));}
    public static void accept(ExtractionProfile p,String id){
        Job j=job(id);if(p.active)throw new IllegalStateException("의뢰는 거점에서 받을 수 있습니다.");
        if(p.contracts.contains(id))return;
        if(p.contracts.size()>=3)throw new IllegalStateException("의뢰는 최대 3개까지 받을 수 있습니다.");
        if(!available(p,j))throw new IllegalStateException("해당 지역 또는 혼합 입문 노드를 먼저 해금하세요.");
        p.change(()->{p.contracts.add(id);p.contractProgress.put(id,0);});
    }
    public static void cancel(ExtractionProfile p,String id){
        if(p.active)throw new IllegalStateException("원정 중에는 의뢰를 취소할 수 없습니다.");
        p.change(()->{p.contracts.remove(id);p.contractProgress.remove(id);});
    }
    public static void killed(Hero h,Mob mob,int mask){
        if(h==null||h.extractionRaidID==0||mob.alignment!=Char.Alignment.ENEMY||mob.EXP<=0)return;
        Run r=run(h);String type=mob.getClass().getSimpleName();
        String[] types={"Rat","Skeleton","Bat","Ghoul","Succubus"};
        for(int i=0;i<r.ids.length;i++){
            Job j=job(r.ids[i]);boolean matches=j.kind==Kind.HUNT&&ExtractionDifficulty.chapter()==j.chapter&&(type.equals(types[j.chapter-1])||j.chapter==1&&type.equals("Albino"));
            if(j.kind==Kind.HYBRID)matches=(mask&(1<<j.hybrid))!=0;
            if(matches&&r.progress[i]<j.goal){r.progress[i]++;if(h.sprite!=null)GLog.i("의뢰: "+j.name+" "+Math.min(j.goal,progress(ExtractionProfile.get(),j)+r.progress[i])+" / "+j.goal);}
        }
    }
    public static void floor(Hero h){
        if(h==null||h.extractionRaidID==0||Dungeon.level==null||Dungeon.branch!=0)return;
        Run r=run(h);
        for(int i=0;i<r.ids.length;i++){
            Job j=job(r.ids[i]);if(j.kind!=Kind.RECOVER||Dungeon.depth!=ExtractionDifficulty.startDepth(j.chapter)+2||r.progress[i]>0||progress(ExtractionProfile.get(),j)>0||(r.spawnedRecords&(1<<i))!=0)continue;
            int cell=Dungeon.level.randomRespawnCell(null);
            if(cell<0)cell=Dungeon.level.entrance();
            Dungeon.level.drop(new Record(j.id,h.extractionRaidID),cell);r.spawnedRecords|=1<<i;
            if(h.sprite!=null)GLog.i("의뢰 기록이 이 층에 있습니다. 바닥의 기록 두루마리를 찾아 회수하세요.");
        }
    }
    public static boolean collect(Hero h,String id,int raid){
        if(h==null||h.extractionRaidID==0||h.extractionRaidID!=raid)return false;
        Run r=run(h);for(int i=0;i<r.ids.length;i++)if(r.ids[i].equals(id)){r.progress[i]=1;return true;}return false;
    }
    /** Called within the profile settlement transaction, once per successful raid. */
    public static void extracted(ExtractionProfile p,Hero h){
        Run r=run(h);
        for(int i=0;i<r.ids.length;i++)if(p.contracts.contains(r.ids[i])){
            Job j=job(r.ids[i]);int amount=r.progress[i];
            if(j.kind==Kind.DELIVER&&p.raidChapter==j.chapter)amount=1;
            p.contractProgress.put(j.id,Math.min(j.goal,progress(p,j)+amount));
        }
    }
    public static boolean deliverable(Item item,Job j){return item instanceof MeleeWeapon&&!(item instanceof BasicExpeditionSword)&&WeaponUpgradeLimit.tier(item)==ExtractionDifficulty.chapterMaxTier(j.chapter)&&!item.cursed;}
    public static Item reward(Job j,int category){
        if(category<1||category>4)throw new IllegalArgumentException("없는 장비 보상입니다.");
        if(category==1){int tier=ExtractionDifficulty.chapterMaxTier(j.chapter);return (tier==2?new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shortsword():tier==3?new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Sword():tier==4?new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Longsword():new com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Greatsword()).identify(false);}
        for(ExtractionShop.Offer offer:ExtractionShop.OFFERS){if(offer.category==category){Item item=offer.item();if((item instanceof com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor?((com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor)item).tier:WeaponUpgradeLimit.tier(item))==ExtractionDifficulty.chapterMaxTier(j.chapter))return item;}}
        throw new IllegalStateException("장비 보상을 찾지 못했습니다.");
    }
    public static void claim(ExtractionProfile p,String id,Item donation,int rewardCategory){
        Job j=job(id);if(p.active||!p.contracts.contains(id)||progress(p,j)<j.goal)throw new IllegalStateException("목표를 채운 뒤 거점에서 완료하세요.");
        if(j.kind==Kind.DELIVER&&(!p.stash.contains(donation)||!deliverable(donation,j)))throw new IllegalStateException("납품할 저주 없는 근접 무기를 창고에서 선택하세요.");
        boolean first=!p.completedContracts.contains(id);Item gear=first?reward(j,rewardCategory):null;
        p.change(()->{
            if(j.kind==Kind.DELIVER)p.stash.remove(donation);
            p.contracts.remove(id);p.contractProgress.remove(id);p.completedContracts.add(id);
            p.gold=Math.addExact(p.gold,j.gold);p.awardGrowthXP(j.xp);if(gear!=null)p.stash.add(gear);
            p.result="의뢰 완료: "+j.name+" · +"+j.gold+" G · 성장 XP +"+j.xp+(first?"\n최초 보상: "+gear.name():"");
            if(!p.contractSealAwarded&&p.completedContracts.contains("hunt_1")&&p.completedContracts.contains("record_1")&&p.completedContracts.contains("supply_1")){
                p.contractSealAwarded=true;p.stash.add(new CommissionSeal().identify(false));p.result+="\n하수도 연속 의뢰 완료: 의뢰인의 인장 획득";
            }
        });
    }
    public static String tracker(Hero h){
        if(h==null||h.extractionRaidID==0)return "진행 중인 원정이 없습니다.";
        Run r=run(h);StringBuilder s=new StringBuilder("현재 원정 진행도 · 탈출해야 저장됩니다.\n");
        for(int i=0;i<r.ids.length;i++){Job j=job(r.ids[i]);s.append('\n').append(j.name).append(" · ").append(j.kind==Kind.DELIVER?"해당 지역 탈출 후 창고에서 납품":Math.min(j.goal,progress(ExtractionProfile.get(),j)+r.progress[i])+" / "+j.goal);}
        if(r.ids.length==0)s.append("\n받은 의뢰가 없습니다. 로비의 의뢰 게시판을 확인하세요.");return s.toString();
    }
    public static class Record extends Item {
        public String contract="";public int raid;
        public Record(){image=ItemSpriteSheet.SCROLL_HOLDER;unique=true;}
        Record(String id,int raid){this();contract=id;this.raid=raid;}
        @Override public String name(){return "의뢰용 원정 기록";}
        @Override public String desc(){return "분실된 원정 기록입니다. 해당 의뢰를 받은 원정에서 회수하면 의뢰 주머니에 보관됩니다. 탈출해야 의뢰 진행도로 인정됩니다.";}
        @Override public boolean isIdentified(){return true;}
        @Override public boolean isUpgradable(){return false;}
        @Override public boolean doPickUp(Hero h,int pos){
            if(ExtractionContracts.collect(h,contract,raid)){GLog.p("원정 기록을 의뢰 주머니에 보관했습니다.");if(h.sprite!=null){GameScene.pickUp(this,pos);h.spendAndNext(pickupDelay());}return true;}
            GLog.w("현재 원정의 의뢰 기록이 아닙니다.");return false;
        }
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("contract",contract);b.put("contract_raid",raid);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);contract=b.getString("contract");raid=b.getInt("contract_raid");}
    }
    public static class CommissionSeal extends ExpeditionArtifacts.Relic {
        public CommissionSeal(){image=ItemSpriteSheet.ARTIFACT_TALISMAN;}
        @Override public String name(){return "의뢰인의 인장";}
        @Override public String desc(){return "하수도 연속 의뢰의 보상. 착용 중 주변 적을 처치하면 보호막 "+(3+level()/5)+"을 얻습니다. 보호막은 누적되지 않습니다."+progress();}
        @Override public com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing glowing(){return new com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing(0xE3C579);}
    }
    private ExtractionContracts(){}
}
