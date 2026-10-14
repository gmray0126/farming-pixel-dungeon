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
    public transient boolean firing;
    {image=11001;unique=true;bones=false;defaultAction=SHOOT;usesTargeting=true;identify(false);}
    public static boolean unlocked(Hero h){return ExtractionHybridCombat.learned(h,"alchemy",0);}
    private static boolean node(Hero h,int n){return ExtractionHybridCombat.learned(h,"alchemy",n);}
    public int capacity(Hero h){return node(h,10)?6:4;}
    public int range(Hero h){return node(h,9)?8:6;}
    public float reloadTime(Hero h){return node(h,8)?1.5f:2f;}
    @Override public String name(){return "연금 권총";}
    @Override public String desc(){Hero h=Dungeon.hero;return "종류: 총기 · 마탄학자 혼합 계통\n\n총포 입문 노드로 지급되는 장착 가능한 원거리 무기입니다. 탄창이 비면 인접한 적에게 맨손으로 공격하며 무투 계열 노드가 적용됩니다. 다시 장전하면 원거리 사격으로 돌아갑니다. 주무기 슬롯에 장착하고 적을 누르면 사격합니다. 일반탄은 투척 계열 물리 피해입니다. 속성탄은 명중 시 기본 사격 피해 평균의 30%를 별도 마법 피해로 더하며 마법 저항을 따릅니다.\n피해 "+min()+"~"+max()+" · 사거리 "+range(h)+"칸 · 사격 최소 1턴 · 재장전 "+reloadTime(h)+"턴\n탄창 "+loaded+" / "+capacity(h)+" · "+AlchemyBullet.elementName(element)+"탄\n\n탄약을 연금술로 만들고 준비해 가져오세요. 빗나가도 한 발을 소비합니다. 근접 사격은 명중이 불리하며 벽·다른 대상에 막힙니다. 다른 속성으로 바꾸려면 탄창을 비워야 합니다. 강화 스크롤로 최대 +15까지 강화할 수 있습니다. 투척의 반지 피해 보너스와 총포·범용·원거리 노드가 적용됩니다.\n\n첫 총포 입문 보상: 총과 일반탄 12발을 창고에 지급합니다. 사망 후 총은 다시 지급하지만 탄약은 재지급하지 않습니다.";}
    @Override public boolean isUpgradable(){return true;}
    @Override public int value(){return 0;}
    @Override public int energyVal(){return 0;}
    @Override public int STRReq(int level){return 10;}
    @Override public int min(int level){return Math.max(0,4+level+(node(Dungeon.hero,1)?2:0)+(node(Dungeon.hero,13)?2:0));}
    @Override public int max(int level){return Math.max(0,10+2*level+(node(Dungeon.hero,1)?3:0)+(node(Dungeon.hero,13)?3:0));}
    @Override public int min(){return min(buffedLvl()+com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting.levelDamageBonus(Dungeon.hero));}
    @Override public int max(){return max(buffedLvl()+com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting.levelDamageBonus(Dungeon.hero));}
    public int magicDamage(Hero h){int amount=Math.max(1,Math.round((min()+max())*.15f));return h!=null&&h.extractionRaidID!=0?ExtractionProfile.get().magicDamage(amount):amount;}
    public void bulletEffects(Hero h,Char target,int kind){
        if(kind!=AlchemyBullet.NORMAL&&target!=null&&target.isAlive()&&target.alignment==Char.Alignment.ENEMY){
            target.damage(magicDamage(h),new ElementDamage(Math.max(1,Math.round((min()+max())*.15f))+(h.extractionRaidID!=0?ExtractionProfile.get().bonus(ExtractionGrowth.Stat.WAND_DAMAGE,ExtractionGrowth.MAGIC):0)));
            if(target.isAlive())elemental(h,target,kind);
        }
    }
    @Override public String status(){return loaded+"/"+capacity(Dungeon.hero);}
    @Override public String actionName(String action,Hero hero){
        if(SHOOT.equals(action))return "사격하기";
        if(RELOAD.equals(action))return "장전하기";
        return super.actionName(action,hero);
    }
    @Override public ArrayList<String> actions(Hero h){ArrayList<String> a=super.actions(h);if(unlocked(h)){a.add(SHOOT);a.add(RELOAD);}return a;}
    @Override public int reachFactor(Char owner){return owner instanceof Hero?range((Hero)owner):6;}
    @Override public boolean canReach(Char owner,int cell){if(!(owner instanceof Hero)||Dungeon.level==null||Dungeon.level.distance(owner.pos,cell)>range((Hero)owner))return false;return new com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica(owner.pos,cell,com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica.PROJECTILE).collisionPos==cell;}
    @Override public float delayFactor(Char owner){return Math.max(1,super.delayFactor(owner));}
    @Override public float accuracyFactor(Char owner,Char target){float factor=super.accuracyFactor(owner,target);if(Dungeon.level!=null&&Dungeon.level.adjacent(owner.pos,target.pos))factor=ExtractionScaling.merge(owner,factor,.5f);if(owner instanceof Hero&&node((Hero)owner,12))factor=ExtractionScaling.merge(owner,factor,1.15f);return factor;}
    public boolean consumeRound(Hero h,Char target){if(!unlocked(h)||loaded<=0)return false;loaded--;Item.updateQuickslot();if(h.sprite!=null&&h.sprite.parent!=null&&target.sprite!=null)((com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite)h.sprite.parent.recycle(com.shatteredpixel.shatteredpixeldungeon.sprites.MissileSprite.class)).reset(h.sprite,target.sprite,new AlchemyBullet(element),()->{});return true;}
    @Override public int proc(Char owner,Char target,int damage){if(owner instanceof Hero&&((Hero)owner).belongings.attackingWeapon()==this){bulletEffects((Hero)owner,target,element);if(target.alignment==Char.Alignment.ENEMY){ExtractionHybridCombat.HybridTag tag=Buff.prolong(target,ExtractionHybridCombat.HybridTag.class,12);if(tag!=null)tag.mask|=4;}if(node((Hero)owner,4)&&element!=0)damage+=2;}return super.proc(owner,target,damage);}
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
                if(loaded<=0){
                    Char target=Actor.findChar(cell);
                    if(h.belongings.weapon()==AlchemyFirearm.this&&target!=null&&target.alignment==Char.Alignment.ENEMY&&Dungeon.level.adjacent(h.pos,cell)&&Dungeon.level.heroFOV[cell]){if(h.handle(cell))h.next();}
                    else GLog.w("탄창이 비었습니다. 총을 장착한 상태에서 인접한 적에게 맨손 공격하거나 재장전하세요.");return;
                }
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
    public static class ElementDamage{public float base;public ElementDamage(){}public ElementDamage(float base){this.base=base;}}
    public static class Shot extends MissileWeapon {
        private transient AlchemyFirearm gun;private int kind;
        public Shot(){image=11002;spawnedForEffect=true;}
        public Shot(AlchemyFirearm gun,int kind){this();this.gun=gun;this.kind=kind;}
        @Override public int defaultQuantity(){return 1;}
        @Override public float accuracyFactor(Char owner,Char target){float f=super.accuracyFactor(owner,target);return owner instanceof Hero&&node((Hero)owner,12)?ExtractionScaling.merge(owner,f,1.15f):f;}
        @Override public int min(int level){return gun==null?0:gun.min();}
        @Override public int max(int level){return gun==null?0:gun.max();}
        @Override public int STRReq(int level){return 10;}
        @Override public int damageRoll(Char owner){return gun==null?0:gun.damageRoll(owner);}
        @Override public float delayFactor(Char owner){return Math.max(1,gun==null?1:gun.delayFactor(owner));}
        @Override public int proc(Char owner,Char target,int damage){if(owner instanceof Hero){if(target.alignment==Char.Alignment.ENEMY){ExtractionHybridCombat.HybridTag tag=Buff.prolong(target,ExtractionHybridCombat.HybridTag.class,12);if(tag!=null)tag.mask|=4;}if(gun!=null)gun.bulletEffects((Hero)owner,target,kind);if(node((Hero)owner,4)&&kind!=0)damage+=2;}return gun==null?damage:gun.proc(owner,target,damage);}
        @Override protected void rangedHit(Char target,int cell){}
        @Override protected void rangedMiss(int cell){}
    }
    @Override public void storeInBundle(Bundle b){super.storeInBundle(b);b.put("gun_loaded",loaded);b.put("gun_element",element);}
    @Override public void restoreFromBundle(Bundle b){super.restoreFromBundle(b);loaded=Math.max(0,Math.min(6,b.getInt("gun_loaded")));element=Math.max(0,Math.min(AlchemyBullet.BLIND,b.getInt("gun_element")));}
}
