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
