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
package net.hardisonbrewing.signingserver.service.network;

import net.hardisonbrewing.signingserver.SigservApplication;
import net.rim.device.api.system.RadioStatusListener;
import net.rim.device.api.system.WLANConnectionListener;

public abstract class AbstractRadioStatusListener implements RadioStatusListener, WLANConnectionListener {

    public void baseStationChange() {

        SigservApplication.logEvent( "baseStationChange" );
    }

    public void networkScanComplete( boolean success ) {

        SigservApplication.logEvent( "networkScanComplete success[" + success + "]" );
    }

    public void networkServiceChange( int networkId, int service ) {

        SigservApplication.logEvent( "networkServiceChange networkId[" + networkId + "], service[" + service + "]" );
    }

    public void networkStarted( int networkId, int service ) {

        SigservApplication.logEvent( "networkStarted networkId[" + networkId + "], service[" + service + "]" );
    }

    public void networkStateChange( int state ) {

        SigservApplication.logEvent( "networkStateChange state[" + state + "]" );
    }

    public void pdpStateChange( int apn, int state, int cause ) {

        SigservApplication.logEvent( "pdpStateChange apn[" + apn + "], state[" + state + "], cause[" + cause + "]" );
    }

    public void radioTurnedOff() {

        SigservApplication.logEvent( "radioTurnedOff" );
    }

    public void signalLevel( int level ) {

        SigservApplication.logEvent( "signalLevel level[" + level + "]" );
    }

    public void networkConnected() {

        SigservApplication.logEvent( "networkConnected" );
    }

    public void networkDisconnected( int reason ) {

        SigservApplication.logEvent( "networkDisconnected reason[" + reason + "]" );
    }
}
