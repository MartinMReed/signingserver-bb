/**
 * Copyright (c) 2011 Martin M Reed
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
