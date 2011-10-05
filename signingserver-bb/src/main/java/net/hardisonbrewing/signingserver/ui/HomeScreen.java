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

import java.util.Hashtable;
import java.util.Vector;

import net.hardisonbrewing.signingserver.SigservBBMApplication;
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

        MenuItem[] _menuItems = new MenuItem[menuItems.size()];
        menuItems.copyInto( _menuItems );
        return _menuItems;
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
