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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.ui.Component;

public class WndMessage extends Window {

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	private static final int MARGIN = 4;
	private ScrollPane scroll;
	
	public WndMessage( String text ) {
		
		super();

		int limit = Math.min(WIDTH_MAX, maxContentWidth());
		int width = Math.min(WIDTH_MIN, limit);
		
		RenderedTextBlock info = PixelScene.renderTextBlock( text, 6 );
		info.maxWidth(width - MARGIN * 2);
		info.setPos(MARGIN, MARGIN);

		while (PixelScene.landscape()
				&& info.height() > 120
				&& width < limit){
			width = Math.min(limit, width + 20);
			info.maxWidth(width - MARGIN * 2);
		}

		int contentHeight = (int)Math.ceil(info.height()) + MARGIN * 2;
		int height = Math.min(contentHeight, maxContentHeight());
		if (contentHeight > height){
			Component content = new Component();
			content.add(info);
			content.setSize(width, contentHeight);
			scroll = new ScrollPane(content);
			add(scroll);
			resize(width, height);
			scroll.setRect(0, 0, width, height);
		} else {
			add(info);
			resize(width, contentHeight);
		}
	}

	@Override public void offset(int x, int y){
		super.offset(x, y);
		if (scroll != null) scroll.setRect(scroll.left(), scroll.top(), scroll.width(), scroll.height());
	}
}
