/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

/** Percent bonuses share one base; nested attacks restore their own budgets. */
public final class ExtractionScaling {
    private static final ThreadLocal<Budget> PHYSICAL=new ThreadLocal<>(), INCOMING=new ThreadLocal<>();
    private static final class Budget {Char owner;float base=Float.NaN,percent;Budget(Char c){owner=c;}}
    public static final class Scope implements AutoCloseable {
        private final ThreadLocal<Budget> channel;private final Budget previous;
        private Scope(ThreadLocal<Budget> c,Budget next){channel=c;previous=c.get();c.set(next);}
        @Override public void close(){if(previous==null)channel.remove();else channel.set(previous);}
    }
    public static boolean raid(Char c){return c instanceof Hero&&((Hero)c).extractionRaidID!=0;}
    public static float linear(double base,double rank){return (float)Math.max(base<1?.2:.1,1+(base-1)*rank);}
    public static float sum(float... factors){float result=1;for(float f:factors)result+=f-1;return Math.max(.1f,result);}
    public static float merge(Char c,float current,float factor){return raid(c)?sum(current,factor):current*factor;}
    public static float accuracy(Char c,float current,float factor){return raid(c)?Math.max(1,current+((Hero)c).baseAttackRating()*(factor-1)):current*factor;}
    public static float evasion(Char c,float current,float factor){return raid(c)?Math.max(0,current+((Hero)c).baseDefenseRating()*(factor-1)):current*factor;}
    public static Scope attack(Char c){return new Scope(PHYSICAL,raid(c)?new Budget(c):null);}
    public static Scope defence(Char c){return new Scope(INCOMING,raid(c)?new Budget(c):null);}
    public static Scope incoming(Char c,float damage){Budget b=INCOMING.get();if(b==null||b.owner!=c){b=raid(c)?new Budget(c):null;}if(b!=null&&Float.isNaN(b.base))b.base=damage;return new Scope(INCOMING,b);}
    public static void seedIncoming(Char c,float damage){Budget b=INCOMING.get();if(b!=null&&b.owner==c&&Float.isNaN(b.base))b.base=damage;}
    public static void rebaseIncoming(Char c,float ratio){Budget b=INCOMING.get();if(b!=null&&b.owner==c&&!Float.isNaN(b.base))b.base*=ratio;}
    public static float weapon(Char c,float damage,float factor){Budget b=PHYSICAL.get();if(b!=null&&b.owner==c&&Float.isNaN(b.base)){b.percent+=factor-1;return damage;}return damage*factor;}
    public static float seedPhysical(Char c,float base,float factor){Budget b=PHYSICAL.get();if(b!=null&&b.owner==c){b.base=base;b.percent+=factor-1;return base*Math.max(0,1+b.percent);}return base*factor;}
    public static float damage(Char c,float current,float factor){Budget b=PHYSICAL.get();if(b==null||b.owner!=c||Float.isNaN(b.base))return current*factor;return Math.max(0,current+b.base*(factor-1));}
    public static float received(Char c,float current,float factor){Budget b=INCOMING.get();if(b==null||b.owner!=c||Float.isNaN(b.base))return current*factor;float next=Math.max(-.8f,b.percent+factor-1);float result=current+b.base*(next-b.percent);b.percent=next;return Math.max(0,result);}
    public static float amplified(Char target,Object source,float current,float factor){
        if(raid(target))return received(target,current,factor);
        if(source instanceof Hero&&raid((Hero)source))return damage((Hero)source,current,factor);
        if(source instanceof com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand){
            float base=((com.shatteredpixel.shatteredpixeldungeon.items.wands.DamageWand)source).additiveBase;
            if(base>0)return Math.max(0,current+base*(factor-1));
        }
        if(source instanceof AlchemyFirearm.ElementDamage){
            float base=((AlchemyFirearm.ElementDamage)source).base;if(base>0)return Math.max(0,current+base*(factor-1));
        }
        return current*factor;
    }
    private ExtractionScaling(){}
}
