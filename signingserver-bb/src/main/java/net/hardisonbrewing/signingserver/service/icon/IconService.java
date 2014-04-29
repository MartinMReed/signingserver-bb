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
package net.hardisonbrewing.signingserver.service.icon;

import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.m2g.SVGImage;

import net.hardisonbrewing.signingserver.SigservApplication;
import net.hardisonbrewing.signingserver.service.store.push.PushPPGStatusStore;
import net.hardisonbrewing.signingserver.service.store.push.PushSIGStatusStore;
import net.hardisonbrewing.signingserver.service.store.sig.SigStatusStore;
import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
import net.rim.blackberry.api.push.PushApplicationStatus;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.ApplicationManagerException;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.XYDimension;
import net.rim.device.api.ui.XYEdges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGSVGElement;

public class IconService extends IconServicePlatform {

    private static final Logger log = LoggerFactory.getLogger( IconService.class );

    public static void updateIcon() {

        Hashtable sigStatus = SigStatusStore.get();

        if ( sigStatus == null || sigStatus.isEmpty() ) {
            updateIcon( Icons.COLOR_UNKNOWN, false );
            return;
        }

        if ( !SigStatusStore.isSuccess() ) {
            updateIcon( Icons.COLOR_FAILURE, true );
            return;
        }

        updateIcon( Icons.COLOR_SUCCESS, false );
    }

    private static ApplicationIndicator getApplicationIndicator() {

        ApplicationIndicatorRegistry applicationIndicatorRegistry = ApplicationIndicatorRegistry.getInstance();
        ApplicationIndicator applicationIndicator = applicationIndicatorRegistry.getApplicationIndicator();

        if ( applicationIndicator != null ) {
            return applicationIndicator;
        }

        SVGImage svgImage = SVGService.getImage( Icons.LOGO_URL );
        int dimension = Icons.APPLICATION_INDICATOR_SIZE;

        do {
            ApplicationIcon applicationIcon = getApplicationIcon( svgImage, dimension );
            try {
                // this may throw an exception is the byte[] length is over 1k or so
                // even if the image size is 64x64 pixels
                applicationIndicator = applicationIndicatorRegistry.register( applicationIcon, true, false );
            }
            catch (IllegalArgumentException e) {
                dimension = (int) Math.floor( dimension * 0.80 );
                continue;
            }
        }
        while (applicationIndicator == null && dimension > 0);

        if ( applicationIndicator == null ) {
            throw new IllegalStateException( "Unable to create a new ApplicationIcon." );
        }

        return applicationIndicator;
    }

    private static ApplicationIcon getApplicationIcon( SVGImage svgImage, int size ) {

        XYDimension image = new XYDimension( size, size );
        changeColor( svgImage, Icons.COLOR_FAILURE );
        EncodedImage encodedImage = SVGService.convert( svgImage, image, null );
        return new ApplicationIcon( encodedImage );
    }

    public static void setIndicatorVisible( boolean visible ) {

        ApplicationIndicator applicationIndicator = getApplicationIndicator();
        applicationIndicator.setVisible( visible );
    }

    public static void setIconNewState( boolean newState ) {

        ApplicationDescriptor[] applicationDescriptors = getVisibleApplicationDescriptors();

        for (int i = 0; i < applicationDescriptors.length; i++) {

            ApplicationDescriptor applicationDescriptor = applicationDescriptors[i];
            ApplicationManager applicationManager = ApplicationManager.getApplicationManager();

            if ( applicationManager.getProcessId( applicationDescriptor ) != -1 ) {
                IconService.setIconNewState( newState, applicationDescriptor );
                continue;
            }

            String[] startupArguments = { SigservApplication.UPDATE_ICON_STATE, String.valueOf( newState ) };
            ApplicationDescriptor updateApplicationDescriptor = new ApplicationDescriptor( applicationDescriptor, startupArguments );

            try {
                applicationManager.runApplication( updateApplicationDescriptor );
            }
            catch (ApplicationManagerException e) {
                log.error( "Unable to update icon state" );
            }
        }
    }

    public static void updateIcon( String color, boolean indicatorVisible ) {

        setIndicatorVisible( indicatorVisible );
        updateIcon( color );
    }

    public static Bitmap getPreferredIcon( String color ) {

        int width = HomeScreen.getPreferredIconWidth();
        int height = HomeScreen.getPreferredIconHeight();

        XYDimension image = new XYDimension( width, height );
        XYEdges padding = new XYEdges( 10, 10, 10, 10 );

        EncodedImage encodedImage = getImage( Icons.LOGO_URL, image, padding, color );
        return encodedImage.getBitmap();
    }

    public static void updateIcon( String color ) {

        Bitmap bitmap = getPreferredIcon( color );

        ApplicationDescriptor[] applicationDescriptors = getVisibleApplicationDescriptors();

        for (int i = 0; i < applicationDescriptors.length; i++) {

            ApplicationDescriptor applicationDescriptor = applicationDescriptors[i];
            ApplicationManager applicationManager = ApplicationManager.getApplicationManager();

            if ( applicationManager.getProcessId( applicationDescriptor ) != -1 ) {
                HomeScreen.updateIcon( bitmap, applicationDescriptor );
                continue;
            }

            String[] startupArguments = { SigservApplication.UPDATE_ICON_BITMAP, color };
            ApplicationDescriptor updateApplicationDescriptor = new ApplicationDescriptor( applicationDescriptor, startupArguments );

            try {
                applicationManager.runApplication( updateApplicationDescriptor );
            }
            catch (ApplicationManagerException e) {
                log.error( "Unable to update icon bitmap" );
            }
        }
    }

    private static ApplicationDescriptor[] getVisibleApplicationDescriptors() {

        ApplicationDescriptor currentApplicationDescriptor = ApplicationDescriptor.currentApplicationDescriptor();
        ApplicationDescriptor[] applicationDescriptors = CodeModuleManager.getApplicationDescriptors( currentApplicationDescriptor.getModuleHandle() );

        Vector results = new Vector();

        for (int i = 0; i < applicationDescriptors.length; i++) {

            ApplicationDescriptor applicationDescriptor = applicationDescriptors[i];

            int flags = applicationDescriptor.getFlags();
            if ( ( flags & ApplicationDescriptor.FLAG_SYSTEM ) == ApplicationDescriptor.FLAG_SYSTEM ) {
                continue;
            }

            results.addElement( applicationDescriptor );
        }

        ApplicationDescriptor[] _results = new ApplicationDescriptor[results.size()];
        results.copyInto( _results );
        return _results;
    }

    public static EncodedImage getImage( String name, XYDimension image, XYEdges padding, String color ) {

        SVGImage svgImage = SVGService.getImage( name );
        changeColor( svgImage, color );

        boolean pushPPGRegistered = PushPPGStatusStore.get() == PushApplicationStatus.STATUS_ACTIVE;
        adjustPushColor( svgImage, 0, pushPPGRegistered, color, Icons.COLOR_UNKNOWN );

        boolean pushSIGRegistered = PushSIGStatusStore.get();
        adjustPushColor( svgImage, 1, pushSIGRegistered, color, Icons.COLOR_UNKNOWN );

        return SVGService.convert( svgImage, image, padding );
    }

    private static void adjustPushColor( SVGImage svgImage, int index, boolean on, String onColor, String offColor ) {

        SVGSVGElement svgElement = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
        NodeList paths = svgElement.getElementsByTagName( "path" );

        SVGElement element = (SVGElement) paths.item( index );
        element.setTrait( "fill", on ? onColor : offColor );
    }

    private static void changeColor( SVGImage svgImage, String color ) {

        SVGSVGElement svgElement = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
        NodeList paths = svgElement.getElementsByTagName( "path" );

        for (int i = 0; i < paths.getLength(); i++) {
            SVGElement element = (SVGElement) paths.item( i );
            element.setTrait( "fill", color );
        }
    }
}
