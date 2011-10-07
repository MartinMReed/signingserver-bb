/**
 * Copyright (c) 2011 Martin M Reed
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.hardisonbrewing.signingserver.ui;

import net.hardisonbrewing.signingserver.model.JAD;
import net.hardisonbrewing.signingserver.model.JAD.COD;
import net.hardisonbrewing.signingserver.model.SigningAuthority;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.MainScreen;

public class CodSigningScreen extends MainScreen {

    private ListField listField;

    private SigningAttempt[] signingAttempts;

    public CodSigningScreen(JAD jad, SigningAuthority[] signingAuthorities) {

        COD[] cods = jad.getCODs();

        signingAttempts = new SigningAttempt[cods.length * signingAuthorities.length];

        for (int i = 0, k = 0; i < cods.length; i++) {
            for (int j = 0; j < signingAuthorities.length; j++) {
                SigningAttempt signingAttempt = new SigningAttempt();
                signingAttempt.cod = cods[i];
                signingAttempt.signingAuthority = signingAuthorities[j];
                signingAttempts[k++] = signingAttempt;
            }
        }

        listField = new ListField();
        listField.setCallback( new MyListFieldCallback() );
        listField.setSize( signingAttempts.length );
        listField.setRowHeight( 30 );
        add( listField );
    }

    private String getStatusText( int status ) {

        switch (status) {
            case SigningAttempt.STATUS_SENDING:
                return "Sending" + Characters.HORIZONTAL_ELLIPSIS;
            case SigningAttempt.STATUS_COMPLETE:
                return "Complete";
            default:
            case SigningAttempt.STATUS_WAITING:
                return "Waiting" + Characters.HORIZONTAL_ELLIPSIS;
        }
    }

    private final class MyListFieldCallback implements ListFieldCallback {

        public void drawListRow( ListField listField, Graphics graphics, int index, int y, int width ) {

            SigningAttempt signingAttempt = signingAttempts[index];
            COD cod = signingAttempt.cod;
            SigningAuthority signingAuthority = signingAttempt.signingAuthority;

            Font font = graphics.getFont();

            int filenameWidth = font.getAdvance( cod.filename );
            int keyWidth = font.getAdvance( signingAuthority.key );

            String status = getStatusText( signingAttempt.status );
            int statusWidth = font.getAdvance( status );

            if ( filenameWidth + keyWidth + statusWidth > width ) {
                filenameWidth = width - ( 5 + keyWidth + 10 + statusWidth + 5 );
            }

            int keyX = 5;
            graphics.drawText( signingAuthority.key, keyX, y );

            int filenameX = keyX + keyWidth + 5;
            graphics.drawText( cod.filename, filenameX, y, DrawStyle.ELLIPSIS, filenameWidth );

            int statusX = width - ( statusWidth + 5 );
            graphics.drawText( status, statusX, y );
        }

        public Object get( ListField listField, int index ) {

            return signingAttempts[index];
        }

        public int getPreferredWidth( ListField listField ) {

            return Display.getWidth();
        }

        public int indexOfList( ListField listField, String prefix, int start ) {

            return -1;
        }
    }

    private static final class SigningAttempt {

        public static final int STATUS_WAITING = 0;
        public static final int STATUS_SENDING = 1;
        public static final int STATUS_COMPLETE = 2;

        public COD cod;
        public SigningAuthority signingAuthority;
        public int status = STATUS_WAITING;
    }
}
