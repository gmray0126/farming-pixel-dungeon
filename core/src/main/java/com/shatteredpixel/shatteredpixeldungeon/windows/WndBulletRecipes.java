/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.extraction.AlchemyBullet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;
import java.util.ArrayList;

/** Read-only recipes; costs and yields come from the crafting implementations. */
public class WndBulletRecipes extends Window {
    public WndBulletRecipes(){
        int w=Math.min(PixelScene.landscape()?200:144,maxContentWidth());
        int h=Math.min(248,Camera.main.height-26);resize(w,h);
        RenderedTextBlock title=PixelScene.renderTextBlock("총탄 제작법",9);
        title.maxWidth(w-28);title.hardlight(0xE4C583);title.setPos(0,2);add(title);
        StyledButton close=new StyledButton(Chrome.Type.GREY_BUTTON,"닫기",6){@Override protected void onClick(){hide();}};
        close.setRect(w-25,0,25,15);add(close);
        Component content=new Component();float y=1;
        for(int kind=AlchemyBullet.NORMAL;kind<=AlchemyBullet.BLIND;kind++){
            ArrayList<Item> ingredients=AlchemyBullet.guideIngredients(kind);
            Recipe recipe=kind==AlchemyBullet.NORMAL?new AlchemyBullet.BasicRecipe():new AlchemyBullet.ElementRecipe();
            Item output=recipe.sampleOutput(ingredients);
            ItemSprite icon=new ItemSprite(output);icon.x=0;icon.y=y;content.add(icon);
            RenderedTextBlock name=text(content,AlchemyBullet.elementName(kind)+"탄 · "+output.quantity()+"발 제작",7,22,y,w-22,0xE4C583);
            String material=kind==AlchemyBullet.NORMAL?"아무 씨앗 1개":"일반탄 1발 + "+ingredients.get(1).name()+" 1개";
            RenderedTextBlock input=text(content,material,6,22,name.bottom()+4,w-22,0xD5DFE4);
            RenderedTextBlock energy=text(content,"연금 에너지 "+recipe.cost(ingredients)+" E",6,22,input.bottom()+3,w-22,0x83C9B5);
            y=energy.bottom()+8;
            ColorBlock line=new ColorBlock(w,1,0xFF394957);line.y=y;content.add(line);y+=8;
        }
        RenderedTextBlock note=text(content,"재료는 슬롯마다 1개씩 넣으세요. 연금 에너지는 별도로 소비됩니다. 일반탄은 씨앗만 넣고, 속성탄은 일반탄과 해당 씨앗을 함께 넣어 제작합니다.",6,0,y,w,0xA8B5BD);
        content.setSize(w,note.bottom()+4);
        ScrollPane scroll=new ScrollPane(content);scroll.setRect(0,22,w,h-22);add(scroll);
        com.badlogic.gdx.Gdx.app.log("Farming","Farming ammo recipe guide ready");
    }
    private static RenderedTextBlock text(Component parent,String value,int size,float x,float y,int width,int color){
        RenderedTextBlock t=PixelScene.renderTextBlock(value,size);t.maxWidth(width);t.hardlight(color);t.setPos(x,y);parent.add(t);return t;
    }
}
