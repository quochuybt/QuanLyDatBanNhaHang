package iuh.fit.core.net.client;

import iuh.fit.core.net.protocol.MessageEnvelope;

public interface ClientEventListener {
    void onEvent(MessageEnvelope event);

    default void onDisconnected(Throwable cause) {
    }
}
