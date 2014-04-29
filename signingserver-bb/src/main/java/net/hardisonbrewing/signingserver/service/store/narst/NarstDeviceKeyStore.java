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
package net.hardisonbrewing.signingserver.service.store.narst;

import java.util.Enumeration;

import net.rim.device.api.crypto.PrivateKey;
import net.rim.device.api.crypto.certificate.KeyUsage;
import net.rim.device.api.crypto.keystore.AssociatedData;
import net.rim.device.api.crypto.keystore.AssociatedDataKeyStoreIndex;
import net.rim.device.api.crypto.keystore.DeviceKeyStore;
import net.rim.device.api.crypto.keystore.KeyStore;
import net.rim.device.api.crypto.keystore.KeyStoreData;
import net.rim.device.api.crypto.keystore.KeyStoreDataTicket;
import net.rim.device.api.crypto.keystore.KeyStoreTicket;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.util.Arrays;

public class NarstDeviceKeyStore implements NarstKeyStore {

    private static final long MODULE_NAME = AssociatedData.ISSUER;
    private static final long CLIENT_ID = AssociatedData.SERIAL_NUMBER;

    private KeyStoreTicket keyStoreTicket;
    private KeyStoreDataTicket keyStoreDataTicket;

    public void put( String clientId, PrivateKey privateKey ) throws Exception {

        KeyStore keyStore = DeviceKeyStore.getInstance();

        if ( keyStoreTicket == null || !keyStore.checkTicket( keyStoreTicket ) ) {
            keyStoreTicket = keyStore.getTicket( " Your code signing key will now be placed in the BlackBerry secure storage." );
        }

        int securityLevel = KeyStore.SECURITY_LEVEL_HIGH;
        long keyUsage = KeyUsage.CODE_SIGNING | KeyUsage.DIGITAL_SIGNATURE;
        AssociatedData[] associatedData = getAssociatedData( clientId );
        keyStore.set( associatedData, "NARST", privateKey, null, securityLevel, null, keyUsage, keyStoreTicket );
    }

    public PrivateKey get( String clientId ) throws Exception {

        KeyStoreData keyStoreData = getKeyStoreData( clientId );

        if ( keyStoreData == null ) {
            return null;
        }

        if ( keyStoreDataTicket == null || !keyStoreData.checkTicket( keyStoreDataTicket ) ) {
            keyStoreDataTicket = keyStoreData.getTicket( " The code signing key is currently locked and not accessible." );
        }

        return keyStoreData.getPrivateKey( keyStoreDataTicket );
    }

    private KeyStoreData getKeyStoreData( String clientId ) {

        KeyStore keyStore = DeviceKeyStore.getInstance();

        AssociatedDataKeyStoreIndex keyStoreIndex = new AssociatedDataKeyStoreIndex( MODULE_NAME );
        keyStore.addIndex( keyStoreIndex );

        Enumeration enumerator = keyStore.elements( keyStoreIndex.getID(), getModuleHandle() );
        while (enumerator.hasMoreElements()) {
            KeyStoreData keyStoreData = (KeyStoreData) enumerator.nextElement();
            byte[][] _clientId = keyStoreData.getAssociatedData( CLIENT_ID );
            if ( _clientId == null || _clientId.length == 0 ) {
                continue;
            }
            if ( Arrays.equals( _clientId[0], clientId.getBytes() ) ) {
                return keyStoreData;
            }
        }

        return null;
    }

    private byte[] getModuleHandle() {

        int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();
        String moduleName = CodeModuleManager.getModuleName( moduleHandle );
        return moduleName.getBytes();
    }

    private AssociatedData[] getAssociatedData( String clientId ) {

        AssociatedData[] associatedData = new AssociatedData[2];
        associatedData[0] = new AssociatedData( MODULE_NAME, getModuleHandle() );
        associatedData[1] = new AssociatedData( CLIENT_ID, clientId.getBytes() );
        return associatedData;
    }
}
