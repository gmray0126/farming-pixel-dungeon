/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Random;

/** Two independent equipment slots with tiered, permanently farmable items. */
public abstract class ExpeditionClothing extends EquipableItem {
    public final int tier;
    protected ExpeditionClothing(int tier,boolean boots) {
        this.tier=tier;
        image=ItemSpriteSheet.EXPEDITION_CLOTHING+(boots?5:0)+tier-1;
        defaultAction=AC_EQUIP;
    }
    public abstract boolean boots();
    public int STRReq(){return STRReq(level());}
    public int STRReq(int level){return 8+2*tier-(int)(Math.sqrt(8*Math.max(0,level)+1)-1)/2;}
    public int missingStrength(Hero hero){return Math.max(0,STRReq()-hero.STR());}
    @Override public String name(){
        String[] material={"천","가죽","사슬","미늘","판금"};
        return material[tier-1]+(boots()?" 신발":" 바지");
    }
    @Override public String desc(){
        int shownLevel=levelKnown?Math.max(0,level()):0;
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor base=new com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor(tier);
        String text=(boots()?"신발":"바지")+" 장비칸에 착용합니다. 같은 티어·강화 갑옷의 기본 방어 성능 중 "+(boots()?20:30)+"%를 담당합니다.";
        text+="\n\n물리 피해 방어 "+String.format(java.util.Locale.ROOT,"%.1f~%.1f",base.DRMin(shownLevel)*share(),base.DRMax(shownLevel)*share())+". 세 부위의 방어량을 합산한 뒤 반올림합니다.";
        text+="\n\n요구 힘 "+STRReq(shownLevel)+". 힘이 부족해도 착용할 수 있습니다. 부족한 힘에 따른 방어 감소·회피·이동 부담도 이 부위의 비율만큼 적용됩니다.";
        text+=WeaponUpgradeLimit.description(this);
        if(cursedKnown&&cursed)text+="\n\n저주받은 장비입니다. 저주를 해제하기 전에는 벗을 수 없습니다.";
        return text;
    }
    @Override public int value(){return Math.max(1,15*tier*(levelKnown?Math.max(1,level()+1):1)/(cursedKnown&&cursed?2:1));}
    @Override public Item random(){
        level(Random.Int(4)==0?(Random.Int(5)==0?2:1):0);
        cursed=Random.Float()<.25f;
        return this;
    }
    @Override public Item upgrade(){
        if(!WeaponUpgradeLimit.canIncrease(this))return this;
        cursed=false;return super.upgrade();
    }
    @Override public boolean isEquipped(Hero hero){return hero!=null&&(boots()?hero.belongings.boots():hero.belongings.pants())==this;}
    @Override public boolean doEquip(Hero hero){
        if(isEquipped(hero))return true;
        ExpeditionClothing old=boots()?hero.belongings.boots:hero.belongings.pants;
        detach(hero.belongings.backpack);
        if(old!=null&&!old.doUnequip(hero,true,false)){collect(hero.belongings.backpack);return false;}
        if(boots())hero.belongings.boots=this;else hero.belongings.pants=this;
        identify(false);
        if(cursed){if(hero.sprite!=null)equipCursed(hero);GLog.n("저주받은 "+name()+"이 몸에 달라붙었습니다.");}
        Talent.onItemEquipped(hero,this);
        updateQuickslot();hero.spendAndNext(timeToEquip(hero));return true;
    }
    @Override public boolean doUnequip(Hero hero,boolean collect,boolean single){
        if(!super.doUnequip(hero,collect,single))return false;
        if(boots())hero.belongings.boots=null;else hero.belongings.pants=null;
        updateQuickslot();return true;
    }
    public float share(){return boots()?.2f:.3f;}
    public float defenseRoll(Hero hero){
        com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor base=new com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor(tier);
        return Math.max(0,Random.NormalIntRange(base.DRMin(buffedLvl()),base.DRMax(buffedLvl()))-2*missingStrength(hero))*share();
    }
    public float evasionFactor(Hero hero){return (float)Math.pow(1.5,-missingStrength(hero)*share());}
    public float movementFactor(Hero hero){return (float)Math.pow(1.2,-missingStrength(hero)*share());}
    public static int bodyDefense(Hero hero){
        float defense=0;
        if(hero.belongings.armor()!=null){
            com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor armor=hero.belongings.armor();
            defense=Math.max(0,Random.NormalIntRange(armor.DRMin(),armor.DRMax())-2*Math.max(0,armor.STRReq()-hero.STR()))*.5f;
        }
        if(hero.belongings.pants()!=null)defense+=hero.belongings.pants().defenseRoll(hero);
        if(hero.belongings.boots()!=null)defense+=hero.belongings.boots().defenseRoll(hero);
        return Math.round(defense);
    }
    public static ExpeditionClothing create(boolean boots,int tier){
        switch(tier){
            case 1:return boots?new ClothShoes():new ClothPants();
            case 2:return boots?new LeatherBoots():new LeatherPants();
            case 3:return boots?new MailBoots():new MailPants();
            case 4:return boots?new ScaleBoots():new ScalePants();
            case 5:return boots?new PlateBoots():new PlatePants();
            default:throw new IllegalArgumentException("Unknown clothing tier");
        }
    }
    public abstract static class Pants extends ExpeditionClothing{protected Pants(int tier){super(tier,false);}@Override public boolean boots(){return false;}}
    public abstract static class Boots extends ExpeditionClothing{protected Boots(int tier){super(tier,true);}@Override public boolean boots(){return true;}}
    public static class ClothPants extends Pants{public ClothPants(){super(1);}}
    public static class LeatherPants extends Pants{public LeatherPants(){super(2);}}
    public static class MailPants extends Pants{public MailPants(){super(3);}}
    public static class ScalePants extends Pants{public ScalePants(){super(4);}}
    public static class PlatePants extends Pants{public PlatePants(){super(5);}}
    public static class ClothShoes extends Boots{public ClothShoes(){super(1);}}
    public static class LeatherBoots extends Boots{public LeatherBoots(){super(2);}}
    public static class MailBoots extends Boots{public MailBoots(){super(3);}}
    public static class ScaleBoots extends Boots{public ScaleBoots(){super(4);}}
    public static class PlateBoots extends Boots{public PlateBoots(){super(5);}}
}
