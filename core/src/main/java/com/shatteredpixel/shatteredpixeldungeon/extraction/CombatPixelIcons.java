/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;
import com.watabou.gltextures.*;
import com.watabou.glwrap.Texture;
/** Small code-drawn pixel icons; transparent background and nearest filtering. */
public final class CombatPixelIcons {
    public static SmartTexture get(int image){
        SmartTexture tx=TextureCache.create("farming-combat-icon-"+image,16,16);tx.filter(Texture.NEAREST,Texture.NEAREST);
        String[] gun={"................","................","................",".......kkkkkkkk.","...kkkksssssssk.","...ksccccccccsk.","...kkkkkkkkkkkk.","...kwwk.kssk....","...kwwk..kk.....","...kwwk.........","...kwwk.........","...kkkk.........","................","................","................","................"};
        String[] bullet={"................","................","................","................","................",".......gg.......","......gyyg......","......gyyg......","......gwwg......","......gwwg......","......gwwg......","......gggg......","................","................","................","................"};
        String[] pixels=image==11001?gun:bullet;
        for(int y=0;y<16;y++)for(int x=0;x<16;x++){char c=pixels[y].charAt(x);int color=c=='k'?0x171D26FF:c=='s'?0x738999FF:c=='c'?0xBBDFEAFF:c=='w'?0x8D5734FF:c=='g'?0x826324FF:c=='y'?0xEDCB65FF:0;tx.bitmap.drawPixel(x,y,color);}
        return tx;
    }
    private CombatPixelIcons(){}
}
