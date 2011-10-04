Notes
=====
This is a sample BlackBerry application for tracking the RIM signing server status.  
This project is mainly just for providing sample code.

Build
-----------------
To build this you need to use Maven.

The [Metova Mobile SDK](http://metova.com/display/PUB/SDK+Licensing) and [Metova Maven Plugin](http://metova.com/display/PUB/SDK+Licensing) are required dependencies. For more information please visit [http://metova.com/display/PUB/SDK+Licensing](http://metova.com/display/PUB/SDK+Licensing).

Settings for communication with the tracking server are abstracted to a closed library.

The NARST (Not A Real SignatureTool, but close enough) library is a required dependency, however it is not publicly available.  
The NARST library is responsible for the actual signing attempts made to the RIM server and has been ported to J2ME/BlackBerry for on-device signing.

API Usage
-----------------
* BBM SDK (5.0.0)
* Push SDK (5.0.0)
* SVG (4.6.0)
* ConnectionFactory (5.0.0)
* ApplicationIndicator (4.6.0)
* JSON (6.0.0)
* OptionsProvider (3.6.0)
* ApplicationManager (3.6.0)
* HomeScreen "new state" (6.0.0)
* RuntimeStore (3.6.0)
* PersistentStore (3.6.0)
* DeviceKeyStore (3.6.0)
* PrivateKey (3.6.0)
* ... etc