/* Farming Pixel Dungeon. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.badlogic.gdx.Gdx;
import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSkin;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PlatformSupport;

public class WndHeroSkin extends WndOptions {
    public WndHeroSkin() {
        super("캐릭터 스킨", (HeroSkin.enabled() ? "현재: 등록한 PNG 스킨" : "현재: 기본 외형")
                + "\n\n원본 사냥꾼과 같은 256×128 PNG를 등록합니다. 적용 전에 움직임을 확인할 수 있습니다."
                + "\n외형만 바뀌며 능력치와 직업은 바뀌지 않습니다.",
                "PNG 파일 등록", "원본 사냥꾼 미리보기", "기본 외형으로 복구", "닫기");
    }
    @Override protected void onSelect(int index) {
        if (index == 0) Game.platform.selectPng(new PlatformSupport.PngSelection() {
            @Override public void selected(byte[] bytes) { preview(bytes); }
            @Override public void canceled() { Game.scene().add(new WndHeroSkin()); }
            @Override public void failed(String message) { Game.scene().add(new WndMessage(message)); }
        });
        else if (index == 1) preview(Gdx.files.internal(Assets.Sprites.HUNTRESS).readBytes());
        else if (index == 2) try {
            HeroSkin.restoreDefault(); refreshHero();
            Game.scene().add(new WndMessage("기본 외형으로 복구했습니다."));
        } catch (RuntimeException error) { Game.scene().add(new WndMessage("기본 외형을 저장하지 못했습니다.")); }
    }
    private static void preview(byte[] bytes) {
        HeroSkin.Candidate candidate = null;
        try { candidate = HeroSkin.inspect(bytes); Game.scene().add(new Preview(candidate)); }
        catch (RuntimeException error) {
            if (candidate != null) candidate.close();
            Game.scene().add(new WndMessage(error.getMessage()));
        }
    }
    private static void refreshHero() {
        if (Game.scene() instanceof GameScene && Dungeon.hero != null) {
            if (Dungeon.hero.sprite instanceof HeroSprite) ((HeroSprite)Dungeon.hero.sprite).updateArmor();
            GameScene.updateAvatar();
        }
    }
    private static final class Preview extends Window {
        private final HeroSkin.Candidate candidate;
        private final MovieClip[] clips = new MovieClip[3];
        private int tier;
        private RedButton tierButton;
        Preview(HeroSkin.Candidate candidate) {
            this.candidate = candidate;
            int w = Math.min(144,maxContentWidth());
            RenderedTextBlock title = PixelScene.renderTextBlock("스킨 미리보기",9);
            title.maxWidth(w); title.hardlight(TITLE_COLOR); add(title);
            RenderedTextBlock info = PixelScene.renderTextBlock("256×128 PNG · 2배 확대\n장비별 8개 행을 유지해 주세요.",6);
            info.maxWidth(w); info.setPos(0,title.bottom()+4); add(info);
            float top = info.bottom()+4;
            ColorBlock background = new ColorBlock(w,52,0xFF000000);
            background.y = top; add(background);
            String[] labels = {"대기","걷기","공격"};
            Object texture = candidate.texture();
            for (int i = 0; i < clips.length; i++) {
                RenderedTextBlock label = PixelScene.renderTextBlock(labels[i],6); add(label);
                label.setPos(w*(i+0.5f)/3-label.width()/2,top+3);
                clips[i] = new MovieClip(texture); clips[i].scale.set(2); add(clips[i]);
                clips[i].x = w*(i+0.5f)/3-12; clips[i].y = top+15;
            }
            float pos = top+56;
            tierButton = new RedButton("장비 외형 0 / 7",6) {
                @Override protected void onClick() { tier = (tier+1)%HeroSkin.ROWS; updateAnimations(); }
            };
            tierButton.setRect(0,pos,w*0.64f-2,18); add(tierButton);
            RedButton flip = new RedButton("좌우 반전",6) {
                @Override protected void onClick() { for (MovieClip clip : clips) clip.flipHorizontal = !clip.flipHorizontal; }
            };
            flip.setRect(tierButton.right()+2,pos,w-tierButton.right()-2,18); add(flip);
            RedButton apply = new RedButton("이 스킨 적용",8) {
                @Override protected void onClick() {
                    try {
                        HeroSkin.apply(candidate); refreshHero(); hide();
                        Game.scene().add(new WndMessage("스킨을 적용했습니다. 다음 원정과 재실행 후에도 유지됩니다."));
                    } catch (RuntimeException error) { Game.scene().add(new WndMessage(error.getMessage())); }
                }
            };
            apply.setRect(0,pos+22,w,22); add(apply);
            RedButton cancel = new RedButton("취소",7) { @Override protected void onClick() { hide(); } };
            cancel.setRect(0,apply.bottom()+3,w,18); add(cancel);
            resize(w,(int)cancel.bottom()); updateAnimations();
        }
        private void updateAnimations() {
            TextureFilm film = new TextureFilm(HeroSprite.tiers(),tier,HeroSkin.FRAME_WIDTH,HeroSkin.FRAME_HEIGHT);
            clips[0].play(new MovieClip.Animation(1,true).frames(film,0,0,0,1,0,0,1,1));
            clips[1].play(new MovieClip.Animation(20,true).frames(film,2,3,4,5,6,7));
            clips[2].play(new MovieClip.Animation(15,true).frames(film,13,14,15,0,0,0));
            tierButton.text("장비 외형 " + tier + " / 7");
        }
        @Override public void destroy() { super.destroy(); candidate.close(); }
    }
}
