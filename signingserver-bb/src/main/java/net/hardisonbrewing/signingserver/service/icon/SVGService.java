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

import java.io.InputStream;

import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.PNGEncodedImage;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYDimension;
import net.rim.device.api.ui.XYEdges;

import org.metova.mobile.util.io.IOUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.svg.SVGSVGElement;

public class SVGService {

    private static final Logger log = LoggerFactory.getLogger( SVGService.class );

    public static SVGImage getImage( String name ) {

        InputStream inputStream = null;
        try {
            inputStream = SVGService.class.getResourceAsStream( name );
            return (SVGImage) SVGImage.createImage( inputStream, null );
        }
        catch (Exception e) {
            log.error( "Execption loading SVG image", e );
            return null;
        }
        finally {
            IOUtility.safeClose( inputStream );
        }
    }

    public static EncodedImage convert( SVGImage svgImage, XYDimension image, XYEdges padding ) {

        int paddingTop = padding == null ? 0 : padding.top;
        int paddingRight = padding == null ? 0 : padding.right;
        int paddingBottom = padding == null ? 0 : padding.bottom;
        int paddingLeft = padding == null ? 0 : padding.left;

        svgImage.setViewportWidth( image.width - ( paddingLeft + paddingRight ) );
        svgImage.setViewportHeight( image.height - ( paddingTop + paddingBottom ) );

        SVGSVGElement svgElement = (SVGSVGElement) svgImage.getDocument().getDocumentElement();
        svgElement.setFloatTrait( "width", svgImage.getViewportWidth() );
        svgElement.setFloatTrait( "height", svgImage.getViewportHeight() );

        Bitmap bitmap = new Bitmap( image.width, image.height );
        bitmap.setARGB( new int[image.width * image.height], 0, image.width, 0, 0, image.width, image.height );

        Graphics graphics = Graphics.create( bitmap );

        ScalableGraphics scalableGraphics = ScalableGraphics.createInstance();
        scalableGraphics.bindTarget( graphics );
        scalableGraphics.render( paddingLeft, paddingTop, svgImage );
        scalableGraphics.releaseTarget();

        return PNGEncodedImage.encode( bitmap );
    }
}
