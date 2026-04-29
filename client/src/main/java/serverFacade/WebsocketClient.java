package serverFacade;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
//import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;

public class WebsocketClient extends Endpoint {
    public Session session;

    public WebsocketClient(String serverUrl) throws Exception {
        URI uri = new URI("ws://localhost:8080/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
            public void onMessage(String message) {
                System.out.println(message);
                System.out.println("\nEnter another message you want to echo:");
            }
        });
    }

    public void send(String message) throws IOException {
        session.getBasicRemote().sendText(message);
    }

    // This method must be overridden, but we don't have to do anything with it
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}
