/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.plants.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import java.util.ArrayList;
/** Physical ammunition. One seed makes six rounds; elemental recipes also pay energy. */
public class AlchemyBullet extends Item {
    public static final int NORMAL=0,FIRE=1,FROST=2,POISON=3,SHOCK=4,BLIND=5;
    public int element;
    {stackable=true;image=11002;identify(false);}
    public AlchemyBullet(){}
    public AlchemyBullet(int element){this.element=element;}
    public static String elementName(int e){return e==FIRE?"화염":e==FROST?"냉기":e==POISON?"독":e==SHOCK?"전격":e==BLIND?"섬광":"일반";}
    public static ArrayList<Item> guideIngredients(int kind){
        ArrayList<Item> items=new ArrayList<>();
        if(kind==NORMAL){items.add(new Firebloom.Seed());return items;}
        items.add(new AlchemyBullet());
        items.add(kind==FIRE?new Firebloom.Seed():kind==FROST?new Icecap.Seed():kind==POISON?new Sorrowmoss.Seed():kind==SHOCK?new Stormvine.Seed():new Blindweed.Seed());
        return items;
    }
    @Override public String name(){return elementName(element)+" 탄약";}
    @Override public String desc(){return "연금 권총의 소모 탄약입니다. 한 발을 발사할 때마다 탄창에서 한 발을 소비하며, 빗나가도 소모합니다.\n\n씨앗 1개 + 연금 에너지 1 = 일반탄 6발. 일반탄 1발 + 씨앗 1개 + 에너지 2 = 해당 속성탄 1발. 화염초의 씨앗: 화염, 눈꽃송이의 씨앗: 냉기, 슬픔이끼의 씨앗: 독, 폭풍덩굴의 씨앗: 전격, 실명초의 씨앗: 섬광. 연금술 화면의 총탄 제작법 버튼에서 재료·에너지·제작 수량을 확인할 수 있습니다. 그 밖의 씨앗은 일반탄 재료로 씁니다.";}
    @Override public boolean isUpgradable(){return false;}
    @Override public int value(){return quantity;}
    @Override public int energyVal(){return 0;}
    @Override public boolean isSimilar(Item item){return super.isSimilar(item)&&item instanceof AlchemyBullet&&((AlchemyBullet)item).element==element;}
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("bullet_element",element);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);element=Math.max(0,Math.min(BLIND,b.getInt("bullet_element")));}
    public static int seedElement(Item seed){return seed instanceof Firebloom.Seed?FIRE:seed instanceof Icecap.Seed?FROST:seed instanceof Sorrowmoss.Seed?POISON:seed instanceof Stormvine.Seed?SHOCK:seed instanceof Blindweed.Seed?BLIND:0;}
    public static class BasicRecipe extends Recipe {
        @Override public boolean testIngredients(ArrayList<Item> items){return items.size()==1&&items.get(0) instanceof Plant.Seed&&items.get(0).quantity()>0;}
        @Override public int cost(ArrayList<Item> items){return 1;}
        @Override public Item sampleOutput(ArrayList<Item> items){return new AlchemyBullet().quantity(6);}
        @Override public Item brew(ArrayList<Item> items){if(!testIngredients(items))return null;items.get(0).quantity(items.get(0).quantity()-1);return sampleOutput(items);}
    }
    public static class ElementRecipe extends Recipe {
        @Override public boolean testIngredients(ArrayList<Item> items){if(items.size()!=2)return false;Item seed=null,bullet=null;for(Item i:items){if(i.quantity()<1)return false;if(i instanceof Plant.Seed)seed=i;if(i instanceof AlchemyBullet&&((AlchemyBullet)i).element==NORMAL)bullet=i;}return seed!=null&&bullet!=null&&seedElement(seed)!=0;}
        @Override public int cost(ArrayList<Item> items){return 2;}
        @Override public Item sampleOutput(ArrayList<Item> items){for(Item i:items)if(i instanceof Plant.Seed)return new AlchemyBullet(seedElement(i));return null;}
        @Override public Item brew(ArrayList<Item> items){if(!testIngredients(items))return null;Item out=sampleOutput(items);for(Item i:items)i.quantity(i.quantity()-1);return out;}
    }
}
