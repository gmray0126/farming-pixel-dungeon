/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.extraction.ExtractionClassSkills;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.ColorBlock;
import com.watabou.utils.GameMath;

/** Shows skill energy within the existing button, without expanding the HUD. */
public class ExtractionSkillButton extends StyledButton {

    private final BitmapText energyText;
    private final ColorBlock energyTrack, energyFill;
    private boolean showingEnergy;
    private int lastEnergy = -1;
    private float charge;

    public ExtractionSkillButton() {
        super(Chrome.Type.GREY_BUTTON, "스킬", 6);
        energyTrack = new ColorBlock(1, 2, 0xFF20282B);
        add(energyTrack);
        energyFill = new ColorBlock(1, 2, 0xFFFFFFFF);
        add(energyFill);
        energyText = new BitmapText(PixelScene.pixelFont);
        add(energyText);
        refreshEnergy();
    }

    private void refreshEnergy() {
        Hero hero = Dungeon.hero;
        boolean available = hero != null && hero.extractionRaidID != 0
                && hero.extractionSkills != null && hero.extractionSkills.armor != null;
        boolean changed = available != showingEnergy;
        showingEnergy = available;
        energyText.visible = energyTrack.visible = available;
        if (available) {
            charge = GameMath.gate(0, hero.extractionSkills.armor.charge, 100);
            // Do not round up: 19.9 energy cannot pay for a 20-energy skill.
            int remaining = (int) charge;
            if (remaining != lastEnergy) {
                lastEnergy = remaining;
                energyText.text(remaining + "/100");
                energyText.measure();
                int color = remaining < 20 ? 0xFF8070 : remaining < 50 ? 0xFFD275 : 0x64D9E8;
                energyText.hardlight(color);
                energyFill.hardlight(color);
                changed = true;
            }
        }
        energyFill.visible = available && charge > 0;
        if (changed) layout();
        if (available) energyFill.size(Math.max(0, width - 4) * charge / 100f, 2);
    }

    @Override
    public void update() {
        super.update();
        refreshEnergy();
    }

    @Override
    protected void layout() {
        super.layout();
        // StyledButton can lay out before this subclass has finished construction.
        if (energyText == null || !showingEnergy) return;
        text.setPos(x + (width - text.width()) / 2f, y + (height >= 24 ? 4 : 1));
        PixelScene.align(text);
        energyText.x = x + (width - energyText.width()) / 2f;
        energyText.y = bottom() - 8;
        PixelScene.align(energyText);
        energyTrack.x = energyFill.x = x + 2;
        energyTrack.y = energyFill.y = bottom() - 3;
        PixelScene.align(energyTrack);
        PixelScene.align(energyFill);
        energyTrack.size(Math.max(0, width - 4), 2);
        energyFill.size(Math.max(0, width - 4) * charge / 100f, 2);
    }

    @Override
    protected void onClick() {
        ExtractionClassSkills.open();
    }

    @Override
    protected String hoverText() {
        return showingEnergy ? "스킬 · 기력 " + lastEnergy + " / 100" : "원정 스킬";
    }

    @Override
    public void alpha(float value) {
        super.alpha(value);
        if (energyText != null) {
            energyText.alpha(value);
            energyTrack.alpha(value);
            energyFill.alpha(value);
        }
    }
}
