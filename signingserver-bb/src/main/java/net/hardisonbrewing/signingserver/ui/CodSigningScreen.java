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

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;

import net.hardisonbrewing.signingserver.SigservApplication;
import net.hardisonbrewing.signingserver.model.JAD;
import net.hardisonbrewing.signingserver.model.JAD.COD;
import net.hardisonbrewing.signingserver.model.SigningAuthority;
import net.hardisonbrewing.signingserver.service.Properties;
import net.hardisonbrewing.signingserver.service.narst.Signer;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.container.MainScreen;

import org.apache.commons.threadpool.DefaultThreadPool;
import org.apache.commons.threadpool.ThreadPool;
import org.metova.mobile.util.io.IOUtility;

public class CodSigningScreen extends MainScreen {

    private ListField listField;

    private final JAD jad;
    private final SigningAuthority[] signingAuthorities;

    private final Hashtable authoritySigningAttempts;

    private SigningAttempt[] signingAttempts;

    private boolean startedThreads;

    public CodSigningScreen(JAD jad, SigningAuthority[] signingAuthorities) {

        this.jad = jad;
        this.signingAuthorities = signingAuthorities;

        COD[] cods = jad.getCODs();

        signingAttempts = new SigningAttempt[cods.length * signingAuthorities.length];

        authoritySigningAttempts = new Hashtable();

        for (int i = 0, k = 0; i < cods.length; i++) {

            for (int j = 0; j < signingAuthorities.length; j++) {

                SigningAttempt signingAttempt = new SigningAttempt();
                signingAttempt.cod = cods[i];
                signingAttempt.signingAuthority = signingAuthorities[j];

                String signingAuthorityKey = signingAttempt.signingAuthority.key;

                Vector vector = (Vector) authoritySigningAttempts.get( signingAuthorityKey );
                if ( vector == null ) {
                    vector = new Vector();
                    authoritySigningAttempts.put( signingAuthorityKey, vector );
                }
                vector.addElement( signingAttempt );

                signingAttempts[k++] = signingAttempt;
            }
        }

        listField = new ListField();
        listField.setCallback( new MyListFieldCallback() );
        listField.setSize( signingAttempts.length );
        listField.setRowHeight( 30 );
        add( listField );
    }

    protected void onVisibilityChange( boolean visible ) {

        super.onVisibilityChange( visible );

        if ( visible && !startedThreads ) {
            startedThreads = true;
            ThreadPool threadPool = new DefaultThreadPool( signingAuthorities.length );
            for (int i = 0; i < signingAuthorities.length; i++) {
                threadPool.invokeLater( new SignCodsTask( signingAuthorities[i] ) );
            }
        }
    }

    private String getSigningStatusText( int status ) {

        switch (status) {
            case SigningAttempt.STATUS_SENDING:
                return "Sending";
            case SigningAttempt.STATUS_COMPLETE:
                return "Signed";
            case SigningAttempt.STATUS_FAILED:
                return "Failed";
            case SigningAttempt.STATUS_WAITING:
                return "Waiting";
            default:
                return "Unknown";
        }
    }

    private int getSigningAuthorityKeyWidth( Font font ) {

        int result = 0;
        for (int i = 0; i < signingAuthorities.length; i++) {
            result = Math.max( result, font.getAdvance( signingAuthorities[i].key ) );
        }
        return result;
    }

    private int getSigningStatusWidth( Font font ) {

        int result = 0;
        for (int i = 0; i <= SigningAttempt.STATUS_COMPLETE; i++) {
            result = Math.max( result, font.getAdvance( getSigningStatusText( i ) ) );
        }
        return result;
    }

    private final class MyListFieldCallback implements ListFieldCallback {

        public void drawListRow( ListField listField, Graphics graphics, int index, int y, int width ) {

            int rowHeight = listField.getRowHeight( index );

            SigningAttempt signingAttempt = signingAttempts[index];
            COD cod = signingAttempt.cod;
            SigningAuthority signingAuthority = signingAttempt.signingAuthority;

            Font font = graphics.getFont();

            String status = getSigningStatusText( signingAttempt.status );
            int statusWidth = getSigningStatusWidth( font );
            int statusX = width - ( statusWidth + 10 );
            graphics.drawText( status, statusX, y );

            int keyDividerX = statusX - 10;
            graphics.drawLine( keyDividerX, y, keyDividerX, y + rowHeight );

            int keyWidth = getSigningAuthorityKeyWidth( font );
            int keyX = keyDividerX - ( keyWidth + 10 );
            graphics.drawText( signingAuthority.key, keyX, y );

            int filenameDividerX = keyX - 10;
            graphics.drawLine( filenameDividerX, y, filenameDividerX, y + rowHeight );

            int filenameWidth = filenameDividerX - 20;
            int filenameX = 10;
            graphics.drawText( cod.filename, filenameX, y, DrawStyle.ELLIPSIS, filenameWidth );
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

    private final class SigningAttempt {

        public static final int STATUS_WAITING = 0;
        public static final int STATUS_SENDING = 1;
        public static final int STATUS_FAILED = 2;
        public static final int STATUS_COMPLETE = 3;

        public COD cod;
        public SigningAuthority signingAuthority;
        public int status = STATUS_WAITING;

        public void updateStatus( int status ) {

            this.status = status;
            invalidate();
        }
    }

    private final class SignCodsTask implements Runnable {

        private SigningAuthority signingAuthority;

        public SignCodsTask(SigningAuthority signingAuthority) {

            this.signingAuthority = signingAuthority;
        }

        public void run() {

            Signer signer = new Signer();
            signer.url = signingAuthority.url;
            signer.signerId = signingAuthority.key;
            signer.clientId = Long.toString( signingAuthority.clientId );

            Vector signingAttempts = (Vector) authoritySigningAttempts.get( signingAuthority.key );

            Enumeration enumerator = signingAttempts.elements();
            while (enumerator.hasMoreElements()) {

                SigningAttempt signingAttempt = (SigningAttempt) enumerator.nextElement();
                COD cod = signingAttempt.cod;

                String jadDirectory = jad.filePath.substring( 0, jad.filePath.lastIndexOf( '/' ) );
                String filePath = jadDirectory + "/" + cod.filename;

                for (int i = 0; i < 5; i++) {
                    InputStream inputStream = null;
                    try {
                        signingAttempt.updateStatus( SigningAttempt.STATUS_SENDING );
                        inputStream = Connector.openInputStream( filePath );
                        Properties response = signer.requestSignature( inputStream );
                        synchronized (signingAttempt.cod) {
                            signer.applySignature( response, filePath );
                        }
                        signingAttempt.updateStatus( SigningAttempt.STATUS_COMPLETE );
                        break;
                    }
                    catch (Throwable t) {
                        IOUtility.safeClose( inputStream );
                        SigservApplication.logEvent( t.toString() );
                    }
                }

                if ( signingAttempt.status != SigningAttempt.STATUS_COMPLETE ) {
                    signingAttempt.updateStatus( SigningAttempt.STATUS_FAILED );
                }
            }
        }
    }
}
