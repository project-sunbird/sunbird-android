package org.sunbird.utils;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.sunbird.ui.MainActivity;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by juspay on 4/17/17.
 */

public class WebSocket {
    public URI uri;
    public WebSocketClient mWebSocketClient;
    private MainActivity activity;

    public WebSocket(MainActivity __activity) {
        activity = __activity;
    }

    public void init(String url) {
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                mWebSocketClient.send("init");
            }

            @Override
            public void onMessage(String s) {
                Log.d("message wsocket", s);
                activity.onWebSocketCallback(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        mWebSocketClient.connect();
    }
}
