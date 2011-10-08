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
package net.hardisonbrewing.signingserver.service.narst;

import net.hardisonbrewing.signingserver.service.Properties;
import net.hardisonbrewing.signingserver.service.store.narst.CSKStore;
import net.hardisonbrewing.signingserver.service.store.narst.NarstKeyStoreManager;
import net.rim.device.api.crypto.PrivateKey;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.PasswordEditField;

public class Signer extends net.hardisonbrewing.signingserver.narst.Signer {

    private static final Object PASSWORD_LOCK = new Object();
    private static final Object PRIVATE_KEY_LOCK = new Object();

    private static String password;
    private static PrivateKey privateKey;

    public static void reset() {

        password = null;
        privateKey = null;
    }

    protected Properties getCSKProperties() throws Exception {

        return CSKStore.get();
    }

    protected String getPassword() {

        synchronized (PASSWORD_LOCK) {
            if ( password == null ) {
                password = requestPassword();
            }
            return password;
        }
    }

    protected void loadPrivateKey() throws Exception {

        synchronized (PRIVATE_KEY_LOCK) {
            super.loadPrivateKey();
        }
    }

    protected PrivateKey loadPrivateKey( String clientId ) throws Exception {

        if ( privateKey == null ) {
            privateKey = NarstKeyStoreManager.getStore().get( clientId );
        }
        return privateKey;
    }

    protected void putPrivateKey( String clientId, PrivateKey privateKey ) throws Exception {

        NarstKeyStoreManager.getStore().put( clientId, privateKey );
    }

    private String requestPassword() {

        final PasswordDialog dialog = new PasswordDialog();

        UiApplication.getUiApplication().invokeAndWait( new Runnable() {

            public void run() {

                UiApplication.getUiApplication().pushModalScreen( dialog );
            }
        } );

        if ( dialog.getSelectedValue() == Dialog.OK ) {
            return dialog.getPassword();
        }

        return null;
    }

    private final class PasswordDialog extends Dialog {

        private final PasswordEditField passwordEditField;

        public PasswordDialog() {

            super( Dialog.D_OK_CANCEL, "The signing key password is required to continue" + Characters.HORIZONTAL_ELLIPSIS, Dialog.OK, null, 0 );

            add( passwordEditField = new PasswordEditField() );
        }

        public String getPassword() {

            return passwordEditField.getText();
        }
    }
}
