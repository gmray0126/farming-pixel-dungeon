/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.*;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.*;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.*;
import java.util.*;
import java.util.function.Supplier;

/** Original recipes, using copied stash ingredients and a single profile transaction. */
public final class ExtractionAlchemy {
    public static final int ENERGY_PRICE=10;
    private static void atHub(ExtractionProfile p){if(p.active)throw new IllegalStateException("원정을 마친 뒤 로비에서 연금술을 이용하세요.");}
    /** Recipes sometimes consult the hero; never expose or mutate the last expedition hero. */
    private static <T>T context(Supplier<T> action){
        Potion.ensureHubColors();Scroll.ensureHubLabels();
        Hero old= Dungeon.hero;int cooking= Dungeon.LimitedDrops.COOKING_HP.count;
        Hero hub=new Hero();hub.extractionRaidID=-1;hub.HP=0;Dungeon.hero=hub;Dungeon.LimitedDrops.COOKING_HP.count=0;
        try{return action.get();}finally{for(Buff buff:hub.buffs())buff.detach();Dungeon.hero=old;Dungeon.LimitedDrops.COOKING_HP.count=cooking;}
    }
    private static Item known(Item item){
        if(item instanceof Potion)((Potion)item).anonymize();
        if(item instanceof Scroll)((Scroll)item).anonymize();
        return item.identify(false);
    }
    private static Item ingredient(Item source){
        Item copy=source instanceof SupplyHealingPotion?new PotionOfHealing():source instanceof ExtractionShop.SupplyIdentify?new ScrollOfIdentify():source instanceof ExtractionShop.SupplyRemoveCurse?new ScrollOfRemoveCurse():source instanceof ExtractionShop.SupplyUpgrade?new ScrollOfUpgrade():source.duplicate();
        return known(copy.quantity(1));
    }
    public static ArrayList<Item> ingredients(ExtractionProfile p,List<Item> selected){
        atHub(p);if(selected==null||selected.size()>3)throw new IllegalArgumentException("재료는 최대 3개입니다.");
        IdentityHashMap<Item,Integer> counts=new IdentityHashMap<>();ArrayList<Item> copies=new ArrayList<>();
        for(Item item:selected){
            if(!p.stash.contains(item)||!Recipe.usableInRecipe(item))throw new IllegalStateException("창고의 저주 없는 제작 재료를 선택하세요.");
            int used=counts.getOrDefault(item,0)+1;counts.put(item,used);
            if(used>item.quantity())throw new IllegalStateException("선택한 재료 수량이 부족합니다.");
            copies.add(ingredient(item));
        }
        return copies;
    }
    public static ArrayList<Recipe> recipes(ExtractionProfile p,List<Item> selected){
        ArrayList<Item> copies=ingredients(p,selected);
        if(copies.size()==1&&selected.get(0) instanceof TrinketCatalyst){
            TrinketCatalyst catalyst=(TrinketCatalyst)selected.get(0);
            if(!catalyst.hasRolledTrinkets())p.change(catalyst::hubOptions);
            ArrayList<Recipe> choices=new ArrayList<>();for(Trinket item:catalyst.hubOptions())choices.add(new CatalystChoice(item));return choices;
        }
        return context(()->Recipe.findRecipes(copies));
    }
    public static Item preview(ExtractionProfile p,List<Item> selected,Recipe recipe){ArrayList<Item> copies=ingredients(p,selected);return context(()->known(recipe.sampleOutput(copies)));}
    public static int cost(ExtractionProfile p,List<Item> selected,Recipe recipe){ArrayList<Item> copies=ingredients(p,selected);return context(()->recipe.cost(copies));}
    private static class CatalystChoice extends Recipe {
        final Trinket output;
        CatalystChoice(Trinket output){this.output=output;}
        @Override public boolean testIngredients(ArrayList<Item> items){return items.size()==1&&items.get(0) instanceof TrinketCatalyst;}
        @Override public int cost(ArrayList<Item> items){return 6;}
        @Override public Item sampleOutput(ArrayList<Item> items){return output.duplicate();}
        @Override public Item brew(ArrayList<Item> items){if(!testIngredients(items))return null;items.get(0).quantity(0);return output.duplicate();}
    }
    private static boolean same(Recipe a,Recipe b){return a.getClass()==b.getClass()&&(!(a instanceof CatalystChoice)||((CatalystChoice)a).output.getClass()==((CatalystChoice)b).output.getClass());}
    public static void craft(ExtractionProfile p,List<Item> selected,Recipe requested){
        ArrayList<Item> selection=new ArrayList<>(selected);Recipe matched=null;
        for(Recipe candidate:recipes(p,selection))if(same(candidate,requested)){matched=candidate;break;}
        if(matched==null)throw new IllegalStateException("선택한 재료로 만들 수 없는 제작법입니다.");
        final Recipe recipe=matched;final int energy=cost(p,selection,recipe);
        if(energy<0||p.alchemyEnergy<energy)throw new IllegalStateException("연금 에너지가 부족합니다.");
        p.change(()->{
            ArrayList<Item> copies=ingredients(p,selection);Item result=context(()->recipe.brew(copies));
            if(result==null||result.quantity()<1)throw new IllegalStateException("제작 결과가 없습니다. 재료는 보존됩니다.");
            int used=0;boolean paidFood=true;
            for(int i=0;i<copies.size();i++){
                int consumed=1-copies.get(i).quantity();if(consumed<0||consumed>1)throw new IllegalStateException("잘못된 제작 재료 수량입니다.");
                Item source=selection.get(i);if(source instanceof Food)paidFood&=source.extractionPurchasedFood;
                if(consumed>0){source.quantity(source.quantity()-consumed);used+=consumed;}
            }
            if(used==0)throw new IllegalStateException("재료가 소비되지 않았습니다.");
            p.stash.removeIf(item->item.quantity()<=0);p.alchemyEnergy-=energy;
            if(result instanceof Food)result.extractionPurchasedFood=paidFood;
            p.stash.add(known(result));p.result="로비 연금술: "+result.title()+" 제작 · 에너지 -"+energy;
        });
    }
    public static void buyEnergy(ExtractionProfile p,int amount){
        atHub(p);if(amount<=0||amount>1000)throw new IllegalArgumentException("잘못된 에너지 수량입니다.");
        int gold=Math.multiplyExact(amount,ENERGY_PRICE);if(p.gold<gold)throw new IllegalStateException("골드가 부족합니다.");
        p.change(()->{p.gold-=gold;p.alchemyEnergy=Math.addExact(p.alchemyEnergy,amount);});
    }
    public static int energyValue(Item item,int quantity){
        if(quantity<=0||quantity>item.quantity()||item.cursed)return 0;
        return Math.max(0,item.duplicate().quantity(quantity).energyVal());
    }
    public static void energize(ExtractionProfile p,Item item,int quantity){
        atHub(p);if(!p.stash.contains(item))throw new IllegalStateException("창고 재료를 선택하세요.");
        int energy=energyValue(item,quantity);if(energy<=0)throw new IllegalStateException("에너지로 분해할 수 없는 재료입니다.");
        p.change(()->{item.quantity(item.quantity()-quantity);if(item.quantity()==0)p.stash.remove(item);p.alchemyEnergy=Math.addExact(p.alchemyEnergy,energy);p.result="재료 분해: 연금 에너지 +"+energy;});
    }
    private ExtractionAlchemy(){}
}
