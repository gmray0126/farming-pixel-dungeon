/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.mage.WarpBeacon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.rogue.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.huntress.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.duelist.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.*;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HolyTome;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.utils.*;
import java.util.*;

/** Native abilities and spells with saved resources, independent of equipment slots. */
public final class ExtractionClassSkills {
    private static final LinkedHashMap<String,Class<? extends ArmorAbility>> ABILITIES=new LinkedHashMap<>();
    private static final LinkedHashMap<Talent,ClericSpell> SPELLS=new LinkedHashMap<>();
    static {
        ABILITIES.put("endure",Endure.class);ABILITIES.put("leap",HeroicLeap.class);
        ABILITIES.put("beacon",WarpBeacon.class);ABILITIES.put("smoke",SmokeBomb.class);
        ABILITIES.put("clone",ShadowClone.class);ABILITIES.put("hawk",SpiritHawk.class);
        ABILITIES.put("nature",NaturesPower.class);ABILITIES.put("feint",Feint.class);
        ABILITIES.put("challenge",Challenge.class);ABILITIES.put("ascend",AscendedForm.class);
        ABILITIES.put("trinity",Trinity.class);ABILITIES.put("many",PowerOfMany.class);
        SPELLS.put(Talent.HOLY_INTUITION,HolyIntuition.INSTANCE);SPELLS.put(Talent.SHIELD_OF_LIGHT,ShieldOfLight.INSTANCE);
        SPELLS.put(Talent.RECALL_INSCRIPTION,RecallInscription.INSTANCE);SPELLS.put(Talent.SUNRAY,Sunray.INSTANCE);
        SPELLS.put(Talent.DIVINE_SENSE,DivineSense.INSTANCE);SPELLS.put(Talent.BLESS,BlessSpell.INSTANCE);
        SPELLS.put(Talent.CLEANSE,Cleanse.INSTANCE);SPELLS.put(Talent.HALLOWED_GROUND,HallowedGround.INSTANCE);
        SPELLS.put(Talent.MNEMONIC_PRAYER,MnemonicPrayer.INSTANCE);SPELLS.put(Talent.LAY_ON_HANDS,LayOnHands.INSTANCE);
        SPELLS.put(Talent.AURA_OF_PROTECTION,AuraOfProtection.INSTANCE);SPELLS.put(Talent.WALL_OF_LIGHT,WallOfLight.INSTANCE);
        SPELLS.put(Talent.DIVINE_INTERVENTION,DivineIntervention.INSTANCE);SPELLS.put(Talent.FLASH,Flash.INSTANCE);
        SPELLS.put(Talent.BODY_FORM,BodyForm.INSTANCE);SPELLS.put(Talent.MIND_FORM,MindForm.INSTANCE);
        SPELLS.put(Talent.SPIRIT_FORM,SpiritForm.INSTANCE);SPELLS.put(Talent.LIFE_LINK,LifeLinkSpell.INSTANCE);
        SPELLS.put(Talent.STASIS,Stasis.INSTANCE);SPELLS.put(Talent.HOLY_LANCE,HolyLance.INSTANCE);
    }
    public static boolean learned(Hero h,String skill){return h!=null&&h.extractionRaidID!=0&&ExtractionProfile.get().nodes.contains("skill_"+skill);}
    public static boolean hasSkills(Hero h){
        for(String root:ExtractionClassUtilities.ROOTS)if(h!=null&&h.extractionRaidID!=0&&ExtractionProfile.get().nodes.contains(root))return true;
        return false;
    }
    public static final class State implements Bundlable {
        public SkillArmor armor=new SkillArmor(); public SkillPrayer prayer=new SkillPrayer();
        private final LinkedHashMap<String,ArmorAbility> abilities=new LinkedHashMap<>();
        public ArmorAbility ability(String key){
            if(!ABILITIES.containsKey(key))throw new IllegalArgumentException(key);
            if(!abilities.containsKey(key))abilities.put(key,Reflection.newInstance(ABILITIES.get(key)));
            return abilities.get(key);
        }
        @Override public void storeInBundle(Bundle b){
            b.put("armor",armor);b.put("prayer",prayer);
            Bundle a=new Bundle();for(Map.Entry<String,ArmorAbility> entry:abilities.entrySet())a.put(entry.getKey(),entry.getValue());b.put("abilities",a);
        }
        @Override public void restoreFromBundle(Bundle b){
            armor=(SkillArmor)b.get("armor");prayer=(SkillPrayer)b.get("prayer");
            if(armor==null)armor=new SkillArmor();if(prayer==null)prayer=new SkillPrayer();
            abilities.clear();Bundle a=b.getBundle("abilities");
            for(String key:ABILITIES.keySet())if(a.contains(key)){ArmorAbility restored=(ArmorAbility)a.get(key);if(restored!=null&&ABILITIES.get(key).isInstance(restored))abilities.put(key,restored);}
            armor.charge=GameMath.gate(0,armor.charge,100);
        }
    }
    /** Resource holder only: never equipped and never contributes armor stats. */
    public static class SkillArmor extends ClassArmor {public SkillArmor(){charge=100;}}
    /** Original spell casting, without an artifact slot or automatic item-level growth. */
    public static class SkillPrayer extends HolyTome {
        public SkillPrayer(){levelCap=0;charge=chargeCap=3+Math.round(ExtractionProfile.get().bonus(ExtractionGrowth.Stat.PRAYER_CAPACITY));partialCharge=0;}
        public int capacity(){return chargeCap;}
        public void capacity(int n){chargeCap=n;charge=Math.min(charge,chargeCap);}
        @Override public boolean isEquipped(Hero h){return h!=null&&h.extractionSkills!=null&&h.extractionSkills.prayer==this&&learned(h,"prayer");}
        @Override public boolean canCast(Hero h,ClericSpell spell){return allowedSpell(h,spell)&&super.canCast(h,spell);}
        @Override public void spendCharge(float spent){
            partialCharge-=spent;while(partialCharge<0){charge--;partialCharge++;}charge=Math.max(0,charge);Item.updateQuickslot();
        }
        public float remaining(){return charge+partialCharge;}
        public void prepare(Hero h){super.execute(h,"NODE_SKILL");}
    }
    public static ArrayList<ClericSpell> spells(Hero h){
        ArrayList<ClericSpell> result=new ArrayList<>();if(!learned(h,"prayer"))return result;
        result.add(GuidingLight.INSTANCE);result.add(HolyWeapon.INSTANCE);result.add(HolyWard.INSTANCE);
        if(h.hasSubclass(HeroSubClass.PRIEST))result.add(Radiance.INSTANCE);if(h.hasSubclass(HeroSubClass.PALADIN))result.add(Smite.INSTANCE);
        for(Map.Entry<Talent,ClericSpell> entry:SPELLS.entrySet())if(h.hasTalent(entry.getKey()))result.add(entry.getValue());return result;
    }
    private static boolean allowedSpell(Hero h,ClericSpell spell){return spells(h).contains(spell);}
    public static ArmorAbility ability(Hero h,String key){ensure(h);return h.extractionSkills.ability(key);}
    public static void ensure(Hero h){
        if(!hasSkills(h))return;
        if(h.extractionSkills==null)h.extractionSkills=new State();
        h.extractionSkills.prayer.capacity(3+Math.round(ExtractionProfile.get().bonus(ExtractionGrowth.Stat.PRAYER_CAPACITY)));
        Buff.affect(h,SkillRecharge.class);
        if(h.hasSubclass(HeroSubClass.MONK))Buff.affect(h,MonkEnergy.class);
        if(h.hasWeaponAbilities())Buff.affect(h,MeleeWeapon.Charger.class);
        if(h.hasTalent(Talent.IRON_WILL))Buff.affect(h,BrokenSeal.WarriorShield.class);
    }
    /** Called once the native scene exists, including for old saves. No visual effects on load threads. */
    public static void sceneReady(Hero h){
        ensure(h);if(!hasSkills(h))return;
        if(h.belongings.weapon()!=null)Talent.onItemEquipped(h,h.belongings.weapon());
        if(h.belongings.armor()!=null)Talent.onItemEquipped(h,h.belongings.armor());
        if(h.belongings.ring()!=null)Talent.onItemEquipped(h,h.belongings.ring());
        if(h.belongings.misc()!=null)Talent.onItemEquipped(h,h.belongings.misc());
    }
    public static class SkillRecharge extends Buff {
        @Override public boolean act(){
            Hero h=(Hero)target;
            if(h.extractionSkills!=null&&h.isAlive()&&Regeneration.regenOn()){
                h.extractionSkills.armor.charge=Math.min(100,h.extractionSkills.armor.charge+0.2f*RingOfEnergy.armorChargeMultiplier(h));
                h.extractionSkills.prayer.directCharge(1f/50f);
            }
            spend(TICK);return true;
        }
    }
    public static boolean ready(Hero h){return h!=null&&h.isAlive()&&h.ready&&h.extractionRaidID!=0&&Dungeon.level!=null;}
    public static boolean stealth(Hero h){
        ensure(h);if(!ready(h)||!learned(h,"stealth")||h.buff(MagicImmune.class)!=null||h.extractionSkills.armor.charge<20)return false;
        h.extractionSkills.armor.charge-=20;Buff.prolong(h,Invisibility.class,6);h.spendAndNext(1);return true;
    }
    public static void open(){
        Hero h=Dungeon.hero;if(!ready(h)){GameScene.show(new WndMessage("자신의 차례에 스킬을 사용할 수 있습니다."));return;}
        ensure(h);
        GameScene.show(new WndOptions("원정 스킬",hasSkills(h)?"기력 "+Math.round(h.extractionSkills.armor.charge)+" / 100 · 기도 "+MessagesDecimal(h.extractionSkills.prayer.remaining())+" / "+h.extractionSkills.prayer.capacity()+"\n\n기력은 500턴, 기도 충전은 50턴마다 1 회복됩니다. 허기 회복이 중단된 상태에서는 충전되지 않습니다. 스킬 옆 정보 버튼에서 효과를 볼 수 있습니다.":"성장 지도 바깥의 기술 계통에서 스킬을 해금하세요.","수호","비전","그림자","자연","기동","기도","닫기"){
            @Override protected void onSelect(int i){if(i<6)page(h,19+i,0);}
        });
    }
    private static String MessagesDecimal(float n){return String.format(java.util.Locale.ROOT,"%.1f",n);}
    private interface Command{void use();}
    private static class Action {String name,desc;Command command;boolean enabled;Action(String n,String d,boolean e,Command c){name=n;desc=d;enabled=e;command=c;}}
    private static ArrayList<Action> actions(Hero h,int group){
        ArrayList<Action> out=new ArrayList<>();if(!hasSkills(h))return out;
        for(ExtractionGrowth.Node n:ExtractionGrowth.NODES){
            if(n.branch!=group||n.skill==null||!learned(h,n.skill))continue;
            if(n.skill.equals("prayer"))continue;
            if(n.skill.equals("stealth"))out.add(new Action(n.name+" · 기력 20",n.description(),h.buff(MagicImmune.class)==null&&h.extractionSkills.armor.charge>=20,()->stealth(h)));
            else {
                ArmorAbility a=ability(h,n.skill);float cost=a.chargeUse(h);
                out.add(new Action(n.name+" · 기력 "+Math.round(cost),n.description(),h.buff(MagicImmune.class)==null&&h.extractionSkills.armor.charge>=cost,()->{
                    if(!ready(h)||!learned(h,n.skill)||h.extractionSkills.armor.charge<a.chargeUse(h)||h.buff(MagicImmune.class)!=null)return;
                    h.extractionSkills.armor.execute(h,"NODE_SKILL");a.use(h.extractionSkills.armor,h);
                }));
            }
        }
        if(group==23&&h.hasSubclass(HeroSubClass.MONK))out.add(new Action("수도승 기술","연타·집중·질주·용의 발차기·명상을 사용합니다. 적 처치로 기를 얻습니다.",true,()->{if(ready(h))GameScene.show(new WndMonkAbilities(Buff.affect(h,MonkEnergy.class)));}));
        if(group==19&&h.hasTalent(Talent.RUNIC_TRANSFERENCE))out.add(new Action("상형문자 이전 · 기력 10","기존 갑옷의 문자를 제거해 다른 갑옷으로 옮깁니다. 1단계는 일반·희귀, 2단계는 모든 문자를 옮깁니다. 대상 갑옷은 문자가 없는 갑옷이어야 합니다.",h.extractionSkills.armor.charge>=10,()->selectRune(h)));
        if(group==24)for(ClericSpell spell:spells(h))out.add(new Action(spell.name()+" · 기도 "+MessagesDecimal(spell.chargeUse(h)),spell.desc(),h.extractionSkills.prayer.canCast(h,spell),()->{
            if(ready(h)&&h.extractionSkills.prayer.canCast(h,spell)){h.extractionSkills.prayer.prepare(h);spell.onCast(h.extractionSkills.prayer,h);}
        }));
        return out;
    }
    private static void page(Hero h,int group,int page){
        ArrayList<Action> list=actions(h,group);int first=page*5,last=Math.min(list.size(),first+5);
        ArrayList<String> labels=new ArrayList<>();for(int i=first;i<last;i++)labels.add(list.get(i).name);
        boolean more=last<list.size();if(more)labels.add("다음");labels.add("돌아가기");
        GameScene.show(new WndOptions(ExtractionGrowth.BRANCHES[group],list.isEmpty()?"이 계통의 사용 스킬을 아직 배우지 않았습니다.":"기력 "+Math.round(h.extractionSkills.armor.charge)+" / 100 · 기도 "+MessagesDecimal(h.extractionSkills.prayer.remaining())+" / "+h.extractionSkills.prayer.capacity(),labels.toArray(new String[0])){
            @Override protected boolean enabled(int i){return i>=last-first||list.get(first+i).enabled;}
            @Override protected boolean hasInfo(int i){return i<last-first;}
            @Override protected void onInfo(int i){GameScene.show(new WndMessage(list.get(first+i).desc));}
            @Override protected void onSelect(int i){if(i<last-first)list.get(first+i).command.use();else if(more&&i==last-first)page(h,group,page+1);else open();}
        });
    }
    public static boolean canTransferRune(Hero h,Armor source,Armor destination){
        if(h==null||!h.hasTalent(Talent.RUNIC_TRANSFERENCE)||source==null||destination==null||source==destination||source.glyph==null||destination.glyph!=null||source.checkSeal()!=null||destination.checkSeal()!=null)return false;
        return h.pointsInTalent(Talent.RUNIC_TRANSFERENCE)>=2||Arrays.asList(Armor.Glyph.common).contains(source.glyph.getClass())||Arrays.asList(Armor.Glyph.uncommon).contains(source.glyph.getClass());
    }
    public static boolean transferRune(Hero h,Armor source,Armor destination){
        ensure(h);if(!ready(h)||h.extractionSkills==null||h.extractionSkills.armor.charge<10||!h.belongings.contains(source)||!h.belongings.contains(destination)||!canTransferRune(h,source,destination))return false;
        Armor.Glyph glyph=source.glyph;source.inscribe(null);destination.inscribe(glyph);h.extractionSkills.armor.charge-=10;Item.updateQuickslot();h.spendAndNext(1);return true;
    }
    private static void selectRune(Hero h){GameScene.selectItem(new WndBag.ItemSelector(){
        @Override public String textPrompt(){return "상형문자를 가져올 갑옷";}
        @Override public boolean itemSelectable(Item i){return i instanceof Armor&&((Armor)i).glyph!=null;}
        @Override public void onSelect(Item i){if(!(i instanceof Armor))return;Armor source=(Armor)i;GameScene.selectItem(new WndBag.ItemSelector(){
            @Override public String textPrompt(){return "상형문자를 받을 갑옷";}
            @Override public boolean itemSelectable(Item i){return i instanceof Armor&&canTransferRune(h,source,(Armor)i);}
            @Override public void onSelect(Item i){if(i instanceof Armor&&!transferRune(h,source,(Armor)i))GLog.w("상형문자를 옮길 수 없습니다.");}
        });}
    });}
    private ExtractionClassSkills(){}
}
