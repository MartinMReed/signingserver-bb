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
