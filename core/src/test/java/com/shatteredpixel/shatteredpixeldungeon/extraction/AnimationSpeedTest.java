package com.shatteredpixel.shatteredpixeldungeon.extraction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.backends.headless.HeadlessPreferences;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.AnimationClock;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.tweeners.PosTweener;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

public class AnimationSpeedTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    @Before public void setup(){
        Gdx.files=new HeadlessFiles();GdxNativesLoader.load();new Game(com.watabou.noosa.Scene.class,null);
        SPDSettings.set(new HeadlessPreferences("animation.xml",folder.getRoot().getAbsolutePath()));
        AnimationClock.reset(1);Game.elapsed=.021f;
    }
    @After public void restore(){AnimationClock.reset(1);Game.elapsed=0;}
    private static class Frames extends CharSprite {
        Frames(){
            RectF[] frames={new RectF(),new RectF(),new RectF(),new RectF()};
            run=new MovieClip.Animation(10,true).frames(frames);
            attack=new MovieClip.Animation(10,false).frames(frames);
        }
        @Override public void frame(RectF rect){}
        void walking(){play(run);}
        void attacking(){play(attack);}
        void tick(){super.updateAnimation();}
        int index(){return curFrame;}
    }
    @Test public void walkingFramesAndTravelStayNormalWhileAttackAndProjectileSpeedUp(){
        for(int multiplier:new int[]{1,3,5}){
            AnimationClock.reset(multiplier);Game.elapsed=.021f;
            Frames walk=new Frames(),attack=new Frames();walk.walking();attack.attacking();
            AnimationClock.scaled(()->{walk.tick();attack.tick();});
            assertEquals(0,walk.index());assertEquals(multiplier==5?1:0,attack.index());
            PosTweener movement=new PosTweener(walk,new PointF(10,0),1);movement.unscaled=true;
            PosTweener projectile=new PosTweener(attack,new PointF(10,0),1);
            AnimationClock.scaled(()->{movement.update();projectile.update();});
            assertEquals(.21f,walk.x,.0001f);assertEquals(.21f*multiplier,attack.x,.0001f);
            assertEquals(.021f,Game.elapsed,.0001f);
        }
    }
    @Test public void nestedEffectsDoNotMultiplyTwiceAndClockRestoresAfterFailure(){
        AnimationClock.reset(5);
        AnimationClock.scaled(()->AnimationClock.scaled(()->assertEquals(.105f,Game.elapsed,.0001f)));
        assertEquals(.021f,Game.elapsed,.0001f);
        try{AnimationClock.scaled(()->{throw new IllegalStateException("test");});fail();}catch(IllegalStateException expected){}
        assertEquals(.021f,Game.elapsed,.0001f);
        AnimationClock.scaled(()->AnimationClock.unscaled(()->assertEquals(.021f,Game.elapsed,.0001f)));
        AnimationClock.advance(.2f);assertEquals(Game.timeTotal+1,AnimationClock.time(),.0001f);
    }
    @Test public void selectedMultiplierPersistsAndUnsupportedValuesFallBackToNormal(){
        assertEquals(1,SPDSettings.animationSpeed());
        for(int multiplier:new int[]{3,5,1}){
            SPDSettings.animationSpeed(multiplier);
            SPDSettings.set(new HeadlessPreferences("animation.xml",folder.getRoot().getAbsolutePath()));
            assertEquals(multiplier,SPDSettings.animationSpeed());assertEquals(multiplier,AnimationClock.speed());
        }
        SPDSettings.animationSpeed(2);assertEquals(1,SPDSettings.animationSpeed());assertEquals(1,AnimationClock.speed());
    }
}
