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
package net.hardisonbrewing.signingserver.ui;

import java.util.Hashtable;
import java.util.Vector;

import net.hardisonbrewing.signingserver.SigservBBMApplication;
import net.hardisonbrewing.signingserver.model.JAD;
import net.hardisonbrewing.signingserver.model.SigningAuthority;
import net.hardisonbrewing.signingserver.service.Files;
import net.hardisonbrewing.signingserver.service.icon.IconService;
import net.hardisonbrewing.signingserver.service.icon.Icons;
import net.hardisonbrewing.signingserver.service.narst.NarstService;
import net.hardisonbrewing.signingserver.service.store.bbm.BBMApplicationStore;
import net.hardisonbrewing.signingserver.service.store.narst.CSKStore;
import net.hardisonbrewing.signingserver.service.store.narst.DBStore;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusChangeListener;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusChangeListenerStore;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusStore;
import net.hardisonbrewing.signingserver.widget.SubMenuPlatform;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYDimension;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.MainScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomeScreen extends MainScreen {

    private static final Logger log = LoggerFactory.getLogger( HomeScreen.class );

    private EncodedImage encodedImage;

    private SigStatusChangeListener sigStatusChangeListener;

    public HomeScreen() {

        sigStatusChangeListener = new MyStatusChangeListener();
        SigStatusChangeListenerStore.put( sigStatusChangeListener );

        Hashtable status = SigStatusStore.get();
        updateBackground( status );

        add( new NullField( FOCUSABLE ) );
    }

    public boolean onClose() {

        UiApplication.getUiApplication().requestBackground();
        return false;
    }

    protected void paint( Graphics graphics ) {

        super.paint( graphics );

        graphics.setBackgroundColor( Color.BLACK );
        graphics.clear();

        int imageWidth = encodedImage.getScaledWidth();
        int imageHeight = encodedImage.getScaledHeight();
        int imageX = (int) ( ( getWidth() - imageWidth ) * 0.50 );
        int imageY = (int) ( ( getHeight() - imageHeight ) * 0.50 );
        graphics.drawImage( imageX, imageY, imageWidth, imageHeight, encodedImage, 0, 0, 0 );
    }

    private MenuItem[] makeNarstSubMenu() {

        Vector menuItems = new Vector();

        boolean hasCSK = CSKStore.get() != null;
        boolean hasDB = DBStore.get() != null;

        String loadCSKLabel = hasCSK ? "Switch Keys (CSK)" : "Load Keys (CSK)";
        menuItems.addElement( new MenuItem( loadCSKLabel, 0, 0 ) {

            public void run() {

                NarstService.loadCSKFile();
            }
        } );

        if ( hasCSK ) {
            menuItems.addElement( new MenuItem( "Reset Keys (CSK)", 0, 0 ) {

                public void run() {

                    CSKStore.put( null );
                }
            } );
        }

        String loadDBLabel = hasDB ? "Switch Authorities (DB)" : "Load Authorities (DB)";
        menuItems.addElement( new MenuItem( loadDBLabel, 0, 0 ) {

            public void run() {

                NarstService.loadDBFile();
            }
        } );

        if ( hasDB ) {
            menuItems.addElement( new MenuItem( "Reset Authorities (DB)", 0, 0 ) {

                public void run() {

                    DBStore.put( null );
                }
            } );
        }

        menuItems.addElement( new MenuItem( "Sign JAD", 0, 0 ) {

            public void run() {

                SigningAuthority[] signingAuthorities = getSigningAuthorities();

                JAD jad;
                try {
                    jad = NarstService.requestJADFile();
                }
                catch (Exception e) {
                    log.error( "Exception loading JAD file", e );
                    Dialog.inform( Files.LOAD_FILE_FAIL );
                    return;
                }

                UiApplication.getUiApplication().pushScreen( new CodSigningScreen( jad, signingAuthorities ) );
            }
        } );

        menuItems.addElement( new MenuItem( "Sign COD", 0, 0 ) {

            public void run() {

                SigningAuthority[] signingAuthorities = getSigningAuthorities();

                JAD jad;
                try {
                    jad = NarstService.requestCodFilePath();
                }
                catch (Exception e) {
                    log.error( "Exception loading COD files", e );
                    Dialog.inform( Files.LOAD_FILE_FAIL );
                    return;
                }

                UiApplication.getUiApplication().pushScreen( new CodSigningScreen( jad, signingAuthorities ) );
            }
        } );

        MenuItem[] _menuItems = new MenuItem[menuItems.size()];
        menuItems.copyInto( _menuItems );
        return _menuItems;
    }

    private SigningAuthority[] getSigningAuthorities() {

        SigningAuthority[] signingAuthorities = DBStore.get();

        while (signingAuthorities == null) {

            String message = "This feature requires you to load a signing authorities file. The file is typically named with the extension DB. Would you like to load it now?";
            int result = Dialog.ask( Dialog.D_YES_NO, message, Dialog.YES );
            if ( result != Dialog.YES ) {
                return null;
            }

            signingAuthorities = NarstService.loadDBFile();
        }

        return signingAuthorities;
    }

    protected void makeMenu( Menu menu, int instance ) {

        super.makeMenu( menu, instance );

        MenuItem[] narstSubMenu = makeNarstSubMenu();
        SubMenuPlatform.addSubMenu( menu, narstSubMenu, "Narst", 0, 0 );

        SigservBBMApplication bbmApplication = BBMApplicationStore.get();
        if ( bbmApplication.isSupported() ) {

            boolean bbmRegistered = bbmApplication.isRegistered();

            if ( !bbmRegistered ) {
                menu.add( new MenuItem( "BBM Connect", 0, 0 ) {

                    public void run() {

                        SigservBBMApplication bbmApplication = BBMApplicationStore.get();
                        bbmApplication.register();
                    }
                } );
            }

            menu.add( new MenuItem( "Tell A Friend", 0, 0 ) {

                public void run() {

                    SigservBBMApplication bbmApplication = BBMApplicationStore.get();

                    if ( bbmApplication.isRegistered( true ) ) {
                        bbmApplication.sendDownloadInvitation();
                    }
                }
            } );
        }
    }

    private void updateBackground( Hashtable status ) {

        String color;

        if ( status == null || status.isEmpty() ) {
            color = Icons.COLOR_UNKNOWN;
        }
        else if ( !SigStatusStore.isSuccess( status ) ) {
            color = Icons.COLOR_FAILURE;
        }
        else {
            color = Icons.COLOR_SUCCESS;
        }

        int size = Math.min( Display.getWidth(), Display.getHeight() );

        XYDimension image = new XYDimension( size, size );
        XYEdges padding = new XYEdges( 50, 50, 50, 50 );

        encodedImage = IconService.getImage( Icons.LOGO_URL, image, padding, color );
    }

    private final class MyStatusChangeListener implements SigStatusChangeListener {

        public void onStatusChange( Hashtable status ) {

            updateBackground( status );
            invalidate();
        }
    }
}
