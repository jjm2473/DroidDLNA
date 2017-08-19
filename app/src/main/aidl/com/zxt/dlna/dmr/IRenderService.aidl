// IDeviceList.aidl
package com.zxt.dlna.dmr;

interface IRenderService {
    String getDeviceName();
    boolean isRunning();
    void start();
    void stop();
    void updateName(String name);
}
