/* Extraction fork © 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.*;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfMagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import java.util.ArrayList;
import java.util.function.Supplier;

/** Fixed, identified stock; buying and selling always use permanent profile gold. */
public final class ExtractionShop {
    public static final int UPGRADE_REFUND = 50;
    public static final String[] CATEGORIES = {"보급품", "무기", "갑옷"};
    public static final ArrayList<Offer> OFFERS = new ArrayList<>();
    public static final class Offer {
        public final int category, price;
        private final Supplier<Item> factory;
        private Offer(int category, Supplier<Item> factory, int price) {
            this.category=category;this.factory=factory;this.price=price;
        }
        public Item item() { return factory.get().identify(false); }
        public boolean available(){
            Item item=item();
            int tier=WeaponUpgradeLimit.tier(item);
            if(item instanceof Armor)tier=((Armor)item).tier;
            return tier<=2;
        }
    }
    private static void stock(int category, Supplier<Item> factory, int price) {
        OFFERS.add(new Offer(category,factory,price));
    }
    private static void gear(int category, Supplier<Item> factory) {
        stock(category,factory,Math.max(20,factory.get().identify(false).value()*2));
    }
    static {
        stock(0,SupplyHealingPotion::new,30);
        stock(0,Food::new,20);
        stock(0,SupplyIdentify::new,60);
        stock(0,SupplyRemoveCurse::new,80);
        stock(0,SupplyUpgrade::new,250);
        gear(1,WornShortsword::new);gear(1,Dagger::new);gear(1,Gloves::new);
        gear(1,Shortsword::new);gear(1,HandAxe::new);gear(1,Spear::new);
        gear(1,Dirk::new);gear(1,Quarterstaff::new);gear(1,Rapier::new);
        gear(1,Sword::new);gear(1,Mace::new);gear(1,Scimitar::new);
        gear(1,RoundShield::new);gear(1,Sai::new);gear(1,Whip::new);
        gear(1,Longsword::new);gear(1,BattleAxe::new);gear(1,AssassinsBlade::new);
        gear(1,RunicBlade::new);gear(1,Crossbow::new);gear(1,Katana::new);
        gear(1,Greatsword::new);gear(1,Greataxe::new);gear(1,Glaive::new);
        gear(1,WarHammer::new);gear(1,Greatshield::new);gear(1,Gauntlet::new);
        gear(1,()->new MagesStaff(new WandOfMagicMissile()));
        gear(1,ThrowingKnife::new);gear(1,Shuriken::new);gear(1,Javelin::new);
        gear(1,Tomahawk::new);gear(1,Trident::new);
        gear(2,ClothArmor::new);gear(2,LeatherArmor::new);gear(2,MailArmor::new);
        gear(2,ScaleArmor::new);gear(2,PlateArmor::new);

    }
    public static int salePrice(Item item) {
        int price=item instanceof SupplyHealingPotion ? 15*item.quantity()
                : item instanceof ScrollOfUpgrade ? UPGRADE_REFUND*item.quantity() : Math.max(0,item.value());
        if(item instanceof Bag)for(Item inside:((Bag)item).items)price+=salePrice(inside);
        return price;
    }
    public static boolean canSellOne(Item item) {
        return item.stackable && item.quantity()>1 && !(item instanceof MissileWeapon);
    }
    public static final class Redemption {
        public int gold, potions, scrolls;
    }
    /** Runs only on copied extraction loot, preserving the live run on save failure. */
    public static Redemption redeemConsumables(ArrayList<Item> items) {
        Redemption result=new Redemption();
        java.util.Iterator<Item> it=items.iterator();
        while(it.hasNext()){
            Item item=it.next();
            if(item instanceof Potion||item instanceof Scroll){
                result.gold+=salePrice(item);
                if(item instanceof Potion)result.potions+=item.quantity();else result.scrolls+=item.quantity();
                it.remove();
            }else if(item instanceof Bag){
                Redemption nested=redeemConsumables(((Bag)item).items);
                result.gold+=nested.gold;result.potions+=nested.potions;result.scrolls+=nested.scrolls;
            }
        }
        return result;
    }
    // Anonymous supplies keep their identity across each raid's randomized scroll labels.
    public static class SupplyIdentify extends ScrollOfIdentify {
        public SupplyIdentify(){anonymize();image=ItemSpriteSheet.SCROLL_SOWILO;}
        @Override public String name(){return "보급 식별 스크롤";}
        @Override public String desc(){return "선택한 물품의 성능과 저주를 식별합니다.";}
    }
    public static class SupplyRemoveCurse extends ScrollOfRemoveCurse {
        public SupplyRemoveCurse(){anonymize();image=ItemSpriteSheet.SCROLL_LAGUZ;}
        @Override public String name(){return "보급 저주 해제 스크롤";}
        @Override public String desc(){return "선택한 장비의 저주를 해제합니다.";}
    }
    public static class SupplyUpgrade extends ScrollOfUpgrade {
        public SupplyUpgrade(){anonymize();image=ItemSpriteSheet.SCROLL_KAUNAN;}
        @Override public String name(){return "보급 강화 스크롤";}
        @Override public String desc(){return "장비를 강화합니다. 무기의 티어별 상한이 적용됩니다. 탈출 시 남으면 장당 50 G로 정산됩니다.";}
    }
    private ExtractionShop(){}
}
