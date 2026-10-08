/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.watabou.noosa;

/** Render-only clock: UI and gameplay keep the normal frame delta. */
public final class AnimationClock {
    private static int speed=1, depth=0;
    private static float normalDelta, visualTime;
    public static void speed(int value){speed=value==3||value==5?value:1;}
    public static int speed(){return speed;}
    public static void reset(int value){speed(value);depth=0;visualTime=Game.timeTotal;}
    public static void advance(float delta){visualTime+=delta*speed;}
    public static float time(){return visualTime;}
    public static void scaled(Runnable update){
        float saved=Game.elapsed;
        if(depth++==0){normalDelta=saved;Game.elapsed=saved*speed;}
        try{update.run();}finally{depth--;Game.elapsed=saved;}
    }
    public static void unscaled(Runnable update){
        float saved=Game.elapsed;
        if(depth>0)Game.elapsed=normalDelta;
        try{update.run();}finally{Game.elapsed=saved;}
    }
    public static class AnimatedGroup extends Group {
        public AnimatedGroup(){}
        public AnimatedGroup(Group content){add(content);}
        @Override public void update(){scaled(super::update);}
    }
    private AnimationClock(){}
}
