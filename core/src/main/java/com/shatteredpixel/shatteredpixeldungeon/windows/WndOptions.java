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
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.ui.Component;
import com.watabou.noosa.Image;

public class WndOptions extends Window {

	protected static final int WIDTH_P = 120;
	protected static final int WIDTH_L = 144;

	protected static final int MARGIN 		= 2;
	protected static final int BUTTON_HEIGHT	= 18;
	private ScrollPane messageScroll;

	public WndOptions(Image icon, String title, String message, String... options) {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = 0;
		if (title != null) {
			IconTitle tfTitle = new IconTitle(icon, title);
			tfTitle.setRect(0, pos, width, 0);
			add(tfTitle);

			pos = tfTitle.bottom() + 2*MARGIN;
		}

		layoutBody(pos, message, options);
	}
	
	public WndOptions( String title, String message, String... options ) {
		super();

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		float pos = MARGIN;
		if (title != null) {
			RenderedTextBlock tfTitle = PixelScene.renderTextBlock(title, 9);
			tfTitle.hardlight(TITLE_COLOR);
			tfTitle.setPos(MARGIN, pos);
			tfTitle.maxWidth(width - MARGIN * 2);
			add(tfTitle);

			pos = tfTitle.bottom() + 2*MARGIN;
		}
		
		layoutBody(pos, message, options);
	}

	protected void layoutBody(float pos, String message, String... options){
		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		RenderedTextBlock tfMesage = PixelScene.renderTextBlock( 6 );
		tfMesage.text(message, width);
		tfMesage.setPos( 0, pos );
		int available = Math.max(20, maxContentHeight()-(int)Math.ceil(pos)-options.length*(BUTTON_HEIGHT+MARGIN)-2*MARGIN);
		if (tfMesage.height() > available){
			tfMesage.setPos(0, 0);
			Component content = new Component();content.add(tfMesage);content.setSize(width, tfMesage.height());
			messageScroll = new ScrollPane(content);add(messageScroll);
			messageScroll.setRect(0, pos, width, available);
			pos += available + 2*MARGIN;
		} else {
			add(tfMesage);
			pos = tfMesage.bottom() + 2*MARGIN;
		}

		for (int i=0; i < options.length; i++) {
			final int index = i;
			RedButton btn = new RedButton( options[i] ) {
				@Override
				protected void onClick() {
					hide();
					onSelect( index );
				}
			};
			if (hasIcon(i)) btn.icon(getIcon(i));
			btn.multiline = true;
			add( btn );

			if (!hasInfo(i)) {
				btn.setRect(0, pos, width, BUTTON_HEIGHT);
			} else {
				btn.setRect(0, pos, width - BUTTON_HEIGHT, BUTTON_HEIGHT);
				IconButton info = new IconButton(Icons.get(Icons.INFO)){
					@Override
					protected void onClick() {
						onInfo( index );
					}
				};
				info.setRect(width-BUTTON_HEIGHT, pos, BUTTON_HEIGHT, BUTTON_HEIGHT);
				add(info);
			}

			btn.enable(enabled(i));

			pos += BUTTON_HEIGHT + MARGIN;
		}

		resize( width, (int)(pos - MARGIN) );
		if (messageScroll != null) messageScroll.setRect(messageScroll.left(), messageScroll.top(), messageScroll.width(), messageScroll.height());
	}

	@Override public void offset(int x, int y){
		super.offset(x, y);
		if (messageScroll != null) messageScroll.setRect(messageScroll.left(), messageScroll.top(), messageScroll.width(), messageScroll.height());
	}

	protected boolean enabled( int index ){
		return true;
	}
	
	protected void onSelect( int index ) {}

	protected boolean hasInfo( int index ) {
		return false;
	}

	protected void onInfo( int index ) {}

	protected boolean hasIcon( int index ) {
		return false;
	}

	protected Image getIcon( int index ) {
		return null;
	}
}
