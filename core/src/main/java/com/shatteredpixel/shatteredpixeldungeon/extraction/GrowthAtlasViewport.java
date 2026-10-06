/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.extraction;

/** Viewport math in UI units; gestures remain independent of device pixel density. */
public final class GrowthAtlasViewport {
    public float width,height,scale,centerX=GrowthAtlasLayout.CENTER,centerY=GrowthAtlasLayout.CENTER;
    private boolean initial=true;
    public float fitScale(){return Math.min(width,height)/GrowthAtlasLayout.SIZE;}
    public void bounds(float width,float height){
        this.width=Math.max(1,width);this.height=Math.max(1,height);
        if(initial){fit();initial=false;}else{scale=Math.max(fitScale(),scale);clamp();}
    }
    public void fit(){scale=fitScale();centerX=centerY=GrowthAtlasLayout.CENTER;}
    public void origin(){scale=Math.max(fitScale(),0.65f);centerX=centerY=GrowthAtlasLayout.CENTER;clamp();}
    public float worldX(float localX){return centerX+(localX-width/2f)/scale;}
    public float worldY(float localY){return centerY+(localY-height/2f)/scale;}
    public float localX(float worldX){return width/2f+(worldX-centerX)*scale;}
    public float localY(float worldY){return height/2f+(worldY-centerY)*scale;}
    public void zoomAt(float nextScale,float localX,float localY){
        float wx=worldX(localX),wy=worldY(localY);
        scale=Math.max(fitScale(),Math.min(2f,nextScale));
        centerX=wx-(localX-width/2f)/scale;centerY=wy-(localY-height/2f)/scale;clamp();
    }
    public void anchor(float worldX,float worldY,float localX,float localY){
        centerX=worldX-(localX-width/2f)/scale;centerY=worldY-(localY-height/2f)/scale;clamp();
    }
    public void pan(float dx,float dy){centerX-=dx/scale;centerY-=dy/scale;clamp();}
    public void focus(float worldX,float worldY){scale=Math.max(scale,0.7f);centerX=worldX;centerY=worldY;clamp();}
    private void clamp(){centerX=clampAxis(centerX,width/scale);centerY=clampAxis(centerY,height/scale);}
    private float clampAxis(float value,float visible){
        if(visible>=GrowthAtlasLayout.SIZE)return GrowthAtlasLayout.CENTER;
        return Math.max(visible/2f-24,Math.min(GrowthAtlasLayout.SIZE-visible/2f+24,value));
    }
}
