package iuh.fit.core.net.server.dispatch;

import iuh.fit.core.net.protocol.MessageEnvelope;
import iuh.fit.core.net.server.session.ClientSession;

public interface CommandHandler {
    MessageEnvelope handle(ClientSession session, MessageEnvelope request);
}
