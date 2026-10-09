/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.utils.*;

/** Hit-triggered hybrid mechanics. No changes to class selection or automatic level stats. */
public final class ExtractionHybridCombat {
    public static class State implements Bundlable {
        public boolean spellReady,bladeReady,bloodOath;
        public float spellUntil,bladeUntil,rangeUntil,coatReadyAt,stepReadyAt,shadowReadyAt;
        public int spellChain,lastSpellKind,rangeChain,lastRangeKind,coating,coatedShots,faith,oathTurns;
        public transient boolean castEmpowered,castHit,castExtraCharge;
        public transient float castMultiplier=1;
        @Override public void storeInBundle(Bundle b){
            b.put("spell_ready",spellReady);b.put("blade_ready",bladeReady);b.put("blood_oath",bloodOath);
            b.put("spell_until",spellUntil);b.put("blade_until",bladeUntil);b.put("range_until",rangeUntil);
            b.put("coat_ready",coatReadyAt);b.put("step_ready",stepReadyAt);b.put("shadow_ready",shadowReadyAt);
            b.put("spell_chain",spellChain);b.put("spell_kind",lastSpellKind);b.put("range_chain",rangeChain);b.put("range_kind",lastRangeKind);
            b.put("coating",coating);b.put("shots",coatedShots);b.put("faith",faith);b.put("oath_turns",oathTurns);
        }
        @Override public void restoreFromBundle(Bundle b){
            spellReady=b.getBoolean("spell_ready");bladeReady=b.getBoolean("blade_ready");bloodOath=b.getBoolean("blood_oath");
            spellUntil=b.getFloat("spell_until");bladeUntil=b.getFloat("blade_until");rangeUntil=b.getFloat("range_until");
            coatReadyAt=b.getFloat("coat_ready");stepReadyAt=b.getFloat("step_ready");shadowReadyAt=b.getFloat("shadow_ready");
            spellChain=b.getInt("spell_chain");lastSpellKind=b.getInt("spell_kind");rangeChain=b.getInt("range_chain");lastRangeKind=b.getInt("range_kind");
            coating=b.getInt("coating");coatedShots=Math.max(0,b.getInt("shots"));faith=Math.max(0,b.getInt("faith"));oathTurns=Math.max(0,b.getInt("oath_turns"))%5;
            castEmpowered=castHit=castExtraCharge=false;castMultiplier=1;
        }
    }
    public static State state(Hero h){if(h.extractionHybrid==null)h.extractionHybrid=new State();return h.extractionHybrid;}
    public static boolean learned(Hero h,String tree,int node){return h!=null&&h.extractionRaidID!=0&&ExtractionProfile.get().nodes.contains(tree+"_"+node);}
    private static boolean target(Hero h,Char c,int damage){return h!=null&&h.extractionRaidID!=0&&h.isAlive()&&c!=null&&c.isAlive()&&c.alignment==Char.Alignment.ENEMY&&damage>0;}
    private static void heal(Hero h,int hp){h.HP=Math.min(h.HT,h.HP+hp);}
    private static void shield(Hero h,int amount){Buff.affect(h,Barrier.class).setShield(amount);}
    private static void clean(Hero h){Buff.detach(h,Poison.class);Buff.detach(h,Burning.class);Buff.detach(h,Bleeding.class);}
    private static void tag(Char c,int tree){HybridTag t=Buff.prolong(c,HybridTag.class,12);if(t!=null)t.mask|=1<<tree;}
    public static class HybridTag extends FlavourBuff {
        public int mask;
        @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("hybrid_mask",mask);}
        @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);mask=b.getInt("hybrid_mask");}
    }
    public static class ShadowMark extends FlavourBuff {
        {type=buffType.NEGATIVE;}
        @Override public int icon(){return BuffIndicator.MARK;}
        @Override public String name(){return "암영 표식";}
        @Override public String desc(){return "단검과 완드로 표식을 폭발시키거나 그림자 밟기로 접근할 수 있습니다.";}
    }
    private static void mark(Hero h,Char c){Buff.prolong(c,ShadowMark.class,learned(h,"shadow",12)?12:8);}
    public static int physical(Hero h,Char enemy,int damage){
        if(!target(h,enemy,damage))return damage;
        State s=state(h);int family=ExtractionGrowth.family(h.belongings.attackingWeapon());boolean ranged=family==10;
        float now=Actor.now();boolean empowered=false;
        if(!ranged&&learned(h,"spell",0)){
            if(s.bladeReady&&now<=s.bladeUntil){
                s.spellChain=s.lastSpellKind==2?Math.min(3,s.spellChain+1):0;
                damage=Math.round(damage*(learned(h,"spell",12)?1.4f:1.2f)*(1+(learned(h,"spell",13)?s.spellChain*.05f:0)));
                if(learned(h,"spell",1))damage+=2;
                if(learned(h,"spell",6))shield(h,learned(h,"spell",8)?6:3);
                if(learned(h,"spell",9))Buff.prolong(h,Haste.class,2);
                if(learned(h,"spell",11))Buff.prolong(h,Recharging.class,3);
                s.bladeReady=false;empowered=true;tag(enemy,0);
            }else s.spellChain=0;
            s.lastSpellKind=1;s.spellReady=true;s.spellUntil=now+(learned(h,"spell",14)?10:6);
        }
        if(family==5&&learned(h,"shadow",0)){
            if(enemy.buff(ShadowMark.class)!=null&&learned(h,"shadow",11))damage=Math.round(damage*1.15f);
            mark(h,enemy);
        }
        if(learned(h,"storm",0)){
            int kind=ranged?2:1;boolean alternating=s.lastRangeKind!=0&&s.lastRangeKind!=kind&&now<=s.rangeUntil;
            if(alternating){
                s.rangeChain=Math.min(3,s.rangeChain+1);
                float boost=ranged?(learned(h,"storm",10)?.35f:.2f):(learned(h,"storm",4)?.4f:.2f);
                damage=Math.round(damage*(1+boost+(learned(h,"storm",13)?s.rangeChain*.05f:0)));
                if(!ranged){if(learned(h,"storm",1))damage+=2;if(learned(h,"storm",2))Buff.prolong(h,Haste.class,2);if(learned(h,"storm",3))Buff.prolong(enemy,Vulnerable.class,3);}
                else {if(learned(h,"storm",5))Buff.prolong(enemy,Cripple.class,3);if(learned(h,"storm",6))Buff.prolong(h,Haste.class,2);}
                if(learned(h,"storm",9))shield(h,learned(h,"storm",14)?6:3);
                tag(enemy,3);
            }else s.rangeChain=0;
            s.lastRangeKind=kind;s.rangeUntil=now+(learned(h,"storm",11)?10:6);
        }
        if(ranged&&learned(h,"alchemy",0)&&s.coatedShots>0){
            boolean poison=enemy.buff(Poison.class)!=null,chill=enemy.buff(Chill.class)!=null,burn=enemy.buff(Burning.class)!=null;
            boolean reacting=(s.coating==1&&(chill||burn))||(s.coating==2&&(poison||burn))||(s.coating==3&&(poison||chill));
            if(learned(h,"alchemy",13)&&(poison||chill||burn))damage=Math.round(damage*1.2f);
            if(reacting&&learned(h,"alchemy",4))damage+=4;
            if(s.coating==2&&poison&&learned(h,"alchemy",9))Buff.prolong(enemy,Cripple.class,2);
            if(s.coating==3&&chill&&learned(h,"alchemy",10))damage+=4;
            if(reacting&&learned(h,"alchemy",11))shield(h,4);
            if(poison&&learned(h,"alchemy",12))Buff.detach(h,Poison.class);
            if(s.coating==1){Poison p=Buff.affect(enemy,Poison.class);if(p!=null)p.set(learned(h,"alchemy",1)?5:3);}
            else if(s.coating==2)Buff.prolong(enemy,Chill.class,4);
            else if(s.coating==3){Burning b=Buff.affect(enemy,Burning.class);if(b!=null)b.reignite(enemy);}
            s.coatedShots--;tag(enemy,2);
        }
        if(!ranged&&learned(h,"holy",0)){
            int cap=faithCap(h);s.faith=Math.min(cap,s.faith+1);
            if(s.faith>=cap&&learned(h,"holy",1))damage+=2;
        }
        if(!ranged&&learned(h,"blood",0)){
            boolean bleeding=enemy.buff(Bleeding.class)!=null;
            if(bleeding){
                if(learned(h,"blood",2))damage=Math.round(damage*1.15f);
                if(learned(h,"blood",5))heal(h,(learned(h,"blood",6)?2:1)+(learned(h,"blood",14)?1:0));
            }
            int cost=Math.max(1,(int)Math.ceil(h.HT*(learned(h,"blood",8)?.03f:.05f)));
            if(s.bloodOath&&h.HP>cost){
                h.HP-=cost;damage=Math.round(damage*(learned(h,"blood",4)?1.45f:1.3f)*(bleeding&&learned(h,"blood",13)?1.1f:1));
                Bleeding b=Buff.affect(enemy,Bleeding.class);if(b!=null)b.set(learned(h,"blood",1)?3:2);
                if(bleeding&&learned(h,"blood",3))Buff.prolong(enemy,Vulnerable.class,2);
                if(learned(h,"blood",7))shield(h,learned(h,"blood",14)?6:3);
                if(h.HP<=h.HT*.35f&&learned(h,"blood",12))shield(h,learned(h,"blood",14)?6:5);
                tag(enemy,5);
            }
        }
        return damage;
    }
    public static void beginCast(Hero h,Wand wand,int baseCost){
        if(h==null||h.extractionRaidID==0)return;State s=state(h);s.castHit=false;s.castEmpowered=false;s.castExtraCharge=false;s.castMultiplier=1;
        if(h.buff(MagicImmune.class)!=null||!learned(h,"spell",0)||!s.spellReady||Actor.now()>s.spellUntil)return;
        s.castEmpowered=true;
        s.castExtraCharge=learned(h,"spell",4)&&wand.curCharges>=baseCost+1;
        s.castMultiplier=s.castExtraCharge?1.55f:learned(h,"spell",2)?1.35f:1.2f;
        int chain=s.lastSpellKind==1?Math.min(3,s.spellChain+1):0;
        if(learned(h,"spell",13))s.castMultiplier+=chain*.05f;
    }
    public static int magic(Hero h,Char enemy,int damage){
        if(!target(h,enemy,damage)||h.buff(MagicImmune.class)!=null)return damage;
        State s=state(h);boolean first=!s.castHit;s.castHit=true;
        if(s.castEmpowered){
            damage=Math.round(damage*s.castMultiplier);tag(enemy,0);
            if(learned(h,"spell",3))Buff.prolong(enemy,Vulnerable.class,3);
            if(learned(h,"spell",10))Buff.prolong(enemy,Chill.class,3);
            if(first){s.spellReady=false;s.spellChain=s.lastSpellKind==1?Math.min(3,s.spellChain+1):0;if(learned(h,"spell",5))shield(h,learned(h,"spell",8)?6:3);if(learned(h,"spell",7))clean(h);}
        }
        if(learned(h,"shadow",0)&&enemy.buff(ShadowMark.class)!=null){
            Buff.detach(enemy,ShadowMark.class);damage+=(learned(h,"shadow",1)?7:4)+(learned(h,"shadow",13)?4:0);tag(enemy,1);
            if(learned(h,"shadow",2))Buff.prolong(enemy,Vulnerable.class,3);
            if(learned(h,"shadow",3))Buff.prolong(enemy,Blindness.class,3);
            if(learned(h,"shadow",8))Buff.prolong(h,Haste.class,3);
            if(learned(h,"shadow",9))shield(h,3);
            if(learned(h,"shadow",10))heal(h,2);
            if(learned(h,"shadow",4)&&Dungeon.level!=null)for(Mob mob:Dungeon.level.mobs)if(mob!=enemy&&mob.alignment==Char.Alignment.ENEMY&&Dungeon.level.distance(enemy.pos,mob.pos)<=2)mark(h,mob);
        }
        return damage;
    }
    public static void endCast(Hero h,Wand wand){
        if(h==null||h.extractionRaidID==0)return;State s=state(h);
        if(s.castHit&&learned(h,"spell",0)){
            if(!s.castEmpowered)s.spellChain=0;
            s.lastSpellKind=2;s.bladeReady=true;s.bladeUntil=Actor.now()+(learned(h,"spell",14)?10:6);
            if(s.castExtraCharge&&wand.curCharges>0){wand.curCharges--;com.shatteredpixel.shatteredpixeldungeon.items.Item.updateQuickslot();}
        }
        s.castHit=s.castEmpowered=s.castExtraCharge=false;s.castMultiplier=1;
    }
    /** Regeneration advances once per dungeon turn, including waiting and resting. */
    public static void bloodTick(Hero h){
        if(h==null||h.extractionRaidID==0||!h.isAlive())return;
        State s=state(h);if(!s.bloodOath)return;
        if(!learned(h,"blood",0)||h.HP<=1){s.bloodOath=false;if(h.sprite!=null)GLog.w("체력이 부족해 피의 서약이 해제되었습니다.");return;}
        if(++s.oathTurns>=5){s.oathTurns=0;h.HP--;h.resting=false;if(h.HP<=1){s.bloodOath=false;if(h.sprite!=null)GLog.w("체력이 부족해 피의 서약이 해제되었습니다.");}}
    }
    public static int incoming(Hero h,int damage,Object source){
        if(h==null||h.extractionRaidID==0)return damage;
        if(learned(h,"alchemy",7)&&state(h).coatedShots>0&&(source instanceof Poison||source instanceof Burning))damage=(int)Math.ceil(damage*.75f);
        if(learned(h,"holy",11)&&state(h).faith>=3&&source instanceof Char)damage=(int)Math.ceil(damage*.9f);
        return damage;
    }
    public static void defeated(Hero h,Mob mob){
        if(h==null||h.extractionRaidID==0)return;HybridTag t=mob.buff(HybridTag.class);int mask=t==null?0:t.mask;
        if((mask&1)!=0&&learned(h,"spell",14))heal(h,3);
        if((mask&8)!=0){if(learned(h,"storm",12))heal(h,2);if(learned(h,"storm",14))Buff.prolong(h,Haste.class,3);}
        if((mask&4)!=0&&learned(h,"alchemy",14))heal(h,3);
        if(mob.buff(ShadowMark.class)!=null&&learned(h,"shadow",14)){heal(h,3);Buff.prolong(h,Invisibility.class,2);}
        if(mob.buff(Bleeding.class)!=null){
            if(learned(h,"blood",9))heal(h,2+(learned(h,"blood",13)?2:0));
            if(learned(h,"blood",10))clean(h);if(learned(h,"blood",11))Buff.prolong(h,Haste.class,2);
        }
        if(learned(h,"holy",10))state(h).faith=Math.min(faithCap(h),state(h).faith+1);
        ExtractionContracts.killed(h,mob,mask);
    }
    public static int faithCap(Hero h){return (learned(h,"holy",9)?5:3)+(learned(h,"holy",13)?1:0);}
    public static int faithCost(Hero h){return learned(h,"holy",13)?2:3;}
    public static boolean mend(Hero h){
        if(!learned(h,"holy",0)||state(h).faith<faithCost(h)||h.HP>=h.HT)return false;
        state(h).faith-=faithCost(h);heal(h,learned(h,"holy",5)?7:4);
        if(learned(h,"holy",6))clean(h);if(learned(h,"holy",7))shield(h,4);
        if(learned(h,"holy",8)&&Dungeon.level!=null)for(Char ally:Actor.chars())if(ally!=h&&ally.alignment==Char.Alignment.ALLY&&Dungeon.level.distance(h.pos,ally.pos)<=3)ally.HP=Math.min(ally.HT,ally.HP+4);
        return true;
    }
    public static boolean coat(Hero h,int element){
        if(!learned(h,"alchemy",0)||Actor.now()<state(h).coatReadyAt||element<1||element>3||element==2&&!learned(h,"alchemy",2)||element==3&&!learned(h,"alchemy",3))return false;
        State s=state(h);s.coating=element;s.coatedShots=(learned(h,"alchemy",5)?5:3)+(learned(h,"alchemy",14)?2:0);
        s.coatReadyAt=Actor.now()+(learned(h,"alchemy",8)?6:learned(h,"alchemy",6)?8:12);return true;
    }
    public static class Judgement {}
    public static boolean judge(Hero h,Char enemy){
        if(!learned(h,"holy",2)||state(h).faith<faithCost(h)||enemy==null||enemy.alignment!=Char.Alignment.ENEMY||!enemy.isAlive()||Dungeon.level==null)return false;
        int range=learned(h,"holy",12)?7:5;
        if(!Dungeon.level.heroFOV[enemy.pos]||Dungeon.level.distance(h.pos,enemy.pos)>range||enemy.isInvulnerable(Judgement.class))return false;
        state(h).faith-=faithCost(h);tag(enemy,4);
        if(learned(h,"holy",3))Buff.prolong(enemy,Vulnerable.class,3);
        if(learned(h,"holy",12))Buff.prolong(enemy,Blindness.class,3);
        enemy.damage(learned(h,"holy",4)?12:8,new Judgement());
        if(!enemy.isAlive()&&learned(h,"holy",14)){state(h).faith=Math.min(faithCap(h),state(h).faith+1);shield(h,5);}
        return true;
    }
    private static boolean ready(Hero h){return ExtractionClassSkills.ready(h)&&h.buff(MagicImmune.class)==null;}
    public static String status(Hero h){
        State s=state(h);float now=Actor.now();
        return "마검 준비: 주문 "+(s.spellReady&&now<=s.spellUntil?"강화":"대기")+" · 근접 "+(s.bladeReady&&now<=s.bladeUntil?"강화":"대기")
            +"\n혈기사 서약: "+(s.bloodOath?"켜짐 · 5턴마다 체력 -1":"꺼짐")+"\n코팅: "+(s.coating==1?"독":s.coating==2?"냉기":s.coating==3?"화염":"없음")+" · 남은 명중 "+s.coatedShots
            +"\n성력 "+s.faith+" / "+faithCap(h)+"\n행동 버튼 옆 정보에서 조건과 재사용 시간을 확인하세요.";
    }
    public static java.util.ArrayList<ExtractionClassSkills.Action> shortcuts(Hero h){
        java.util.ArrayList<ExtractionClassSkills.Action> out=new java.util.ArrayList<>();
        if(h==null||h.extractionRaidID==0)return out;
        if(learned(h,"shadow",5))out.add(new ExtractionClassSkills.Action("hybrid_shadow","그림자 밟기","표식한 적 옆 안전한 칸으로 이동합니다.\n"+status(h),Actor.now()>=state(h).shadowReadyAt&&!h.rooted,()->shadowStep(h)));
        if(learned(h,"alchemy",0))for(int e=1;e<=3;e++){
            if(e==2&&!learned(h,"alchemy",2)||e==3&&!learned(h,"alchemy",3))continue;
            final int element=e;String name=e==1?"독":e==2?"냉기":"화염";
            out.add(new ExtractionClassSkills.Action("coat_"+e,name+" 코팅","투척 공격에 코팅을 준비합니다. 준비 1턴.\n"+status(h),Actor.now()>=state(h).coatReadyAt,()->{if(ready(h)&&coat(h,element)){GLog.p("약품 코팅 준비: "+state(h).coatedShots+"회 명중");h.spendAndNext(1);}}));
        }
        if(learned(h,"storm",7))out.add(new ExtractionClassSkills.Action("hybrid_storm","사선 확보","안전한 빈칸으로 이동합니다.\n"+status(h),Actor.now()>=state(h).stepReadyAt&&!h.rooted,()->windStep(h)));
        if(learned(h,"holy",0))out.add(new ExtractionClassSkills.Action("holy_mend","성력 회복","성력을 소모해 체력을 회복합니다.\n"+status(h),state(h).faith>=faithCost(h)&&h.HP<h.HT,()->{if(ready(h)&&mend(h))h.spendAndNext(1);}));
        if(learned(h,"holy",2))out.add(new ExtractionClassSkills.Action("holy_judge","심판","보이는 적을 심판합니다.\n"+status(h),state(h).faith>=faithCost(h),()->GameScene.selectCell(new CellSelector.Listener(){
            @Override public String prompt(){return "심판할 적을 선택하세요.";}
            @Override public void onSelect(Integer cell){if(cell!=null&&ready(h)&&judge(h,Actor.findChar(cell)))h.spendAndNext(1);else if(cell!=null)GLog.w("성력과 대상의 거리·시야를 확인하세요.");}
        })));
        if(learned(h,"blood",0))out.add(new ExtractionClassSkills.Action("blood_oath","피의 서약 "+(state(h).bloodOath?"끄기":"켜기"),"서약을 켜거나 끕니다. 활성 중 5턴마다 체력 1 소모.\n"+status(h),true,()->{if(ready(h)){state(h).bloodOath=!state(h).bloodOath;GLog.i("피의 서약 "+(state(h).bloodOath?"활성화":"해제"));h.spendAndNext(1);}}));
        return out;
    }
    public static void open(){
        Hero h=Dungeon.hero;if(!ready(h)){GameScene.show(new WndMessage("자신의 차례에 혼합 스킬을 사용할 수 있습니다. 마법 면역 중에는 사용할 수 없습니다."));return;}
        GameScene.show(new WndOptions("혼합 스킬",status(h),"마검사 상태","그림자 밟기","약품 코팅","사선 확보","성전사 기술","피의 서약 "+(state(h).bloodOath?"끄기":"켜기"),"닫기"){
            @Override protected boolean enabled(int i){return i==6||learned(h,ExtractionHybridTrees.PREFIXES[i],0)&&(i!=1||learned(h,"shadow",5))&&(i!=3||learned(h,"storm",7));}
            @Override protected boolean hasInfo(int i){return i<6;}
            @Override protected void onInfo(int i){GameScene.show(new WndMessage(ExtractionGrowth.NODES[ExtractionGrowth.index(ExtractionHybridTrees.PREFIXES[i]+"_0")].description()+"\n\n"+status(h)));}
            @Override protected void onSelect(int i){
                if(i==0)GameScene.show(new WndMessage(status(h)));
                else if(i==1)shadowStep(h);
                else if(i==2)coatings(h);
                else if(i==3)windStep(h);
                else if(i==4)holyMenu(h);
                else if(i==5&&ready(h)&&learned(h,"blood",0)){state(h).bloodOath=!state(h).bloodOath;GLog.i("피의 서약 "+(state(h).bloodOath?"활성화":"해제"));h.spendAndNext(1);}
            }
        });
    }
    private static void coatings(Hero h){GameScene.show(new WndOptions("약품 코팅","준비 1턴 · 재사용까지 "+Math.max(0,(int)Math.ceil(state(h).coatReadyAt-Actor.now()))+"턴","독","냉기","화염","돌아가기"){
        @Override protected boolean enabled(int i){return i==3||i==0||i==1&&learned(h,"alchemy",2)||i==2&&learned(h,"alchemy",3);}
        @Override protected void onSelect(int i){if(i==3)open();else if(ready(h)&&coat(h,i+1)){GLog.p("약품 코팅 준비: "+state(h).coatedShots+"회 명중");h.spendAndNext(1);}else GameScene.show(new WndMessage("코팅을 아직 준비할 수 없습니다. 재사용 시간을 확인하세요."));}
    });}
    private static void holyMenu(Hero h){GameScene.show(new WndOptions("성전사 기술","성력 "+state(h).faith+" / "+faithCap(h)+" · 비용 "+faithCost(h),"회복","심판","돌아가기"){
        @Override protected boolean enabled(int i){return i==2||state(h).faith>=faithCost(h)&&(i==0&&h.HP<h.HT||i==1&&learned(h,"holy",2));}
        @Override protected void onSelect(int i){if(i==2)open();else if(i==0&&ready(h)&&mend(h))h.spendAndNext(1);else if(i==1)GameScene.selectCell(new CellSelector.Listener(){
            @Override public String prompt(){return "심판할 적을 선택하세요.";}
            @Override public void onSelect(Integer cell){if(cell!=null&&ready(h)&&judge(h,Actor.findChar(cell)))h.spendAndNext(1);else if(cell!=null)GLog.w("성력과 대상의 거리·시야를 확인하세요.");}
        });}
    });}
    private static boolean safeCell(Hero h,int cell){return Dungeon.level!=null&&Dungeon.level.insideMap(cell)&&Dungeon.level.passable[cell]&&!Dungeon.level.solid[cell]&&Dungeon.level.heroFOV[cell]&&Actor.findChar(cell)==null&&Dungeon.level.traps.get(cell)==null;}
    private static void move(Hero h,int cell){h.pos=cell;h.sprite.place(cell);Dungeon.observe();GameScene.updateFog();h.spendAndNext(1);}
    private static void windStep(Hero h){GameScene.selectCell(new CellSelector.Listener(){
        @Override public String prompt(){return "이동할 안전한 빈칸을 선택하세요.";}
        @Override public void onSelect(Integer cell){if(cell==null)return;
            if(!ready(h)||!learned(h,"storm",7)||Actor.now()<state(h).stepReadyAt||!safeCell(h,cell)||Dungeon.level.distance(h.pos,cell)>(learned(h,"storm",8)?5:3)||h.rooted){GLog.w("거리·재사용 시간·안전한 빈칸을 확인하세요.");return;}
            state(h).stepReadyAt=Actor.now()+(learned(h,"storm",8)?6:10);move(h,cell);
        }
    });}
    private static void shadowStep(Hero h){GameScene.selectCell(new CellSelector.Listener(){
        @Override public String prompt(){return "표식한 적을 선택하세요.";}
        @Override public void onSelect(Integer cell){if(cell==null)return;Char enemy=Actor.findChar(cell);
            if(!ready(h)||!learned(h,"shadow",5)||Actor.now()<state(h).shadowReadyAt||enemy==null||enemy.alignment!=Char.Alignment.ENEMY||enemy.buff(ShadowMark.class)==null||!Dungeon.level.heroFOV[cell]||Dungeon.level.distance(h.pos,cell)>(learned(h,"shadow",6)?7:5)||h.rooted){GLog.w("표식·거리·재사용 시간을 확인하세요.");return;}
            int best=-1,distance=Integer.MAX_VALUE;
            for(int p=0;p<Dungeon.level.length();p++)if(safeCell(h,p)&&Dungeon.level.distance(p,cell)==1){int d=Dungeon.level.distance(h.pos,p);if(d<distance){best=p;distance=d;}}
            if(best<0){GLog.w("대상 주변에 안전한 빈칸이 없습니다.");return;}
            state(h).shadowReadyAt=Actor.now()+(learned(h,"shadow",13)?6:10);
            if(learned(h,"shadow",7))Buff.prolong(h,Invisibility.class,2);move(h,best);
        }
    });}
    private ExtractionHybridCombat(){}
}
