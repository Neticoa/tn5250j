package org.tn5250j.event;

import java.util.EventListener;

public interface FTPStatusListener extends EventListener {

    void statusReceived(FTPStatusEvent statusevent);

    void commandStatusReceived(FTPStatusEvent statusevent);

    void fileInfoReceived(FTPStatusEvent statusevent);
}
