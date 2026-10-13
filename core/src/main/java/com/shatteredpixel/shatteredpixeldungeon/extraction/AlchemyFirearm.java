/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.*;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.*;
import com.shatteredpixel.shatteredpixeldungeon.items.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.*;
import com.watabou.utils.*;
import java.util.ArrayList;
public class AlchemyFirearm extends Weapon {
    public static final String SHOOT="GUN_SHOOT",RELOAD="GUN_RELOAD";
    public int loaded,element;
    {image=11001;unique=true;bones=false;defaultAction=SHOOT;usesTargeting=true;identify(false);}
    public static boolean unlocked(Hero h){return ExtractionHybridCombat.learned(h,"alchemy",0);}
    private static boolean node(Hero h,int n){return ExtractionHybridCombat.learned(h,"alchemy",n);}
    public int capacity(Hero h){return node(h,10)?6:4;}
    public int range(Hero h){return node(h,9)?8:6;}
    public float reloadTime(Hero h){return node(h,8)?1.5f:2f;}
    @Override public String name(){return "연금 권총";}
    @Override public String desc(){Hero h=Dungeon.hero;return "종류: 총기 · 총포술사 혼합 계통\n\n총포 입문 노드로 지급되는 원거리 무기입니다. 일반탄은 물리 피해이며 속성탄은 명중할 때 해당 효과를 추가합니다.\n피해 "+min(0)+"~"+max(0)+" · 사거리 "+range(h)+"칸 · 사격 최소 1턴 · 재장전 "+reloadTime(h)+"턴\n탄창 "+loaded+" / "+capacity(h)+" · "+AlchemyBullet.elementName(element)+"탄\n\n탄약을 연금술로 만들고 준비해 가져오세요. 빗나가도 한 발을 소비합니다. 근접 사격은 명중이 불리하며 벽·다른 대상에 막힙니다. 다른 속성으로 바꾸려면 탄창을 비워야 합니다. 총 자체의 강화 대신 총포 노드와 범용·원거리 노드로 성장합니다.\n\n첫 총포 입문 보상: 총과 일반탄 12발을 창고에 지급합니다. 사망 후 총은 다시 지급하지만 탄약은 재지급하지 않습니다.";}
    @Override public boolean isUpgradable(){return false;}
    @Override public int value(){return 0;}
    @Override public int energyVal(){return 0;}
    @Override public int STRReq(int level){return 10;}
    @Override public int min(int level){return 4+(node(Dungeon.hero,1)?2:0)+(node(Dungeon.hero,13)?2:0);}
    @Override public int max(int level){return 10+(node(Dungeon.hero,1)?3:0)+(node(Dungeon.hero,13)?3:0);}
    @Override public String status(){return loaded+"/"+capacity(Dungeon.hero);}
    @Override public ArrayList<String> actions(Hero h){ArrayList<String> a=super.actions(h);a.remove(AC_EQUIP);if(unlocked(h)){a.add(SHOOT);a.add(RELOAD);}return a;}
    public static AlchemyFirearm find(Hero h){return h==null?null:h.belongings.getItem(AlchemyFirearm.class);}
    public int reload(Hero h,int kind){
        if(h==null||!unlocked(h)||!h.belongings.contains(this)||kind<0||kind>AlchemyBullet.BLIND||loaded>0&&element!=kind)return 0;
        int need=capacity(h)-loaded,total=0;if(need<=0)return 0;
        ArrayList<AlchemyBullet> stacks=new ArrayList<>();for(Item i:h.belongings)if(i instanceof AlchemyBullet&&((AlchemyBullet)i).element==kind)stacks.add((AlchemyBullet)i);
        for(AlchemyBullet b:stacks){int count=Math.min(need,b.quantity());if(count<=0)continue;b.quantity(b.quantity()-count);if(b.quantity()==0)b.detachAll(h.belongings.backpack);need-=count;total+=count;if(need==0)break;}
        if(total>0){loaded+=total;element=kind;Item.updateQuickslot();}return total;
    }
    public void reloadMenu(Hero h){String[] choices=new String[7];for(int i=0;i<6;i++)choices[i]=AlchemyBullet.elementName(i)+"탄 장전";choices[6]="닫기";GameScene.show(new WndOptions("탄창 "+loaded+" / "+capacity(h),"장전할 탄약을 선택하세요. 재장전은 "+reloadTime(h)+"턴을 소모합니다.",choices){
        @Override protected void onSelect(int i){if(i<6&&h.ready){if(reload(h,i)>0){h.sprite.operate(h.pos);h.spendAndNext(reloadTime(h));}else GLog.w("탄약이 없거나 탄창이 가득 찼습니다. 다른 속성은 탄창을 비운 뒤 장전하세요.");}}
    });}
    @Override public void execute(Hero h,String action){
        if((SHOOT.equals(action)||RELOAD.equals(action))&&!unlocked(h)){GLog.w("총포 입문 노드가 필요합니다.");return;}super.execute(h,action);
        if(RELOAD.equals(action)){reloadMenu(h);return;}if(!SHOOT.equals(action))return;
        GameScene.selectCell(new CellSelector.Listener(){
            @Override public String prompt(){return "사격할 대상을 선택하세요.";}
            @Override public void onSelect(Integer cell){if(cell==null||!h.ready||!unlocked(h)||!h.belongings.contains(AlchemyFirearm.this))return;
                if(loaded<=0){GLog.w("탄창이 비었습니다. 재장전하세요.");return;}
                if(!Dungeon.level.heroFOV[cell]||Dungeon.level.distance(h.pos,cell)>range(h)){GLog.w("사거리와 시야를 확인하세요.");return;}
                int kind=element;loaded--;Item.updateQuickslot();new Shot(AlchemyFirearm.this,kind).cast(h,cell);
            }
        });
    }
    public static void elemental(Hero h,Char enemy,int kind){
        if(h==null||enemy==null||enemy.alignment!=Char.Alignment.ENEMY||!enemy.isAlive()||kind==0)return;
        int duration=node(h,2)?4:3;
        if(kind==AlchemyBullet.FIRE){Burning b=Buff.affect(enemy,Burning.class);if(b!=null)b.reignite(enemy);}
        else if(kind==AlchemyBullet.FROST)Buff.prolong(enemy,Chill.class,duration);
        else if(kind==AlchemyBullet.POISON){Poison b=Buff.affect(enemy,Poison.class);if(b!=null)b.set(duration);}
        else if(kind==AlchemyBullet.SHOCK)enemy.damage(node(h,4)?4:2,new ElementDamage());
        else if(kind==AlchemyBullet.BLIND)Buff.prolong(enemy,Blindness.class,2);
        if(node(h,3)&&enemy.buff(Poison.class)!=null&&kind==AlchemyBullet.FROST)Buff.prolong(enemy,Cripple.class,2);
        if(node(h,11)){boolean prior=enemy.buff(Poison.class)!=null||enemy.buff(Chill.class)!=null;if(prior&&kind==AlchemyBullet.FIRE){enemy.damage(3,new ElementDamage());Buff.affect(h,Barrier.class).setShield(3);}}
        ExtractionHybridCombat.HybridTag tag=Buff.prolong(enemy,ExtractionHybridCombat.HybridTag.class,12);if(tag!=null)tag.mask|=4;
    }
    public static class ElementDamage{}
    public static class Shot extends MissileWeapon {
        private transient AlchemyFirearm gun;private int kind;
        public Shot(){image=11002;spawnedForEffect=true;}
        public Shot(AlchemyFirearm gun,int kind){this();this.gun=gun;this.kind=kind;}
        @Override public int defaultQuantity(){return 1;}
        @Override public float accuracyFactor(Char owner,Char target){float f=super.accuracyFactor(owner,target);return owner instanceof Hero&&node((Hero)owner,12)?f*1.15f:f;}
        @Override public int min(int level){return gun==null?0:gun.min(0);}
        @Override public int max(int level){return gun==null?0:gun.max(0);}
        @Override public int STRReq(int level){return 10;}
        @Override public int damageRoll(Char owner){return gun==null?0:gun.damageRoll(owner);}
        @Override public float delayFactor(Char owner){return Math.max(1,gun==null?1:gun.delayFactor(owner));}
        @Override public int proc(Char owner,Char target,int damage){if(owner instanceof Hero){elemental((Hero)owner,target,kind);if(node((Hero)owner,4)&&kind!=0)damage+=2;}return gun==null?damage:gun.proc(owner,target,damage);}
        @Override protected void rangedHit(Char target,int cell){}
        @Override protected void rangedMiss(int cell){}
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("gun_loaded",loaded);b.put("gun_element",element);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);loaded=Math.max(0,Math.min(6,b.getInt("gun_loaded")));element=Math.max(0,Math.min(AlchemyBullet.BLIND,b.getInt("gun_element")));}
}
