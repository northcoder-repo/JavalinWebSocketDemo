package org.ajames.javalinwebsocketdemo;

import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.get;
import io.javalin.http.Handler;
import io.javalin.websocket.WsContext;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Ticker {

    private static final Map<WsContext, String> userUsernameMap = new ConcurrentHashMap<>();
    private static int nextUserNumber = 1; // not really used for this demo

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.addStaticFiles("/public");
        }).start(7070);
        
        app.get("/ticker", ctx -> ctx.render("index.html"));
        
        app.ws("/tickerfeed", ws -> {
            ws.onConnect(ctx -> {
                String username = "User" + nextUserNumber++;
                userUsernameMap.put(ctx, username);
                processTicker();
            });
            ws.onClose(ctx -> {
                userUsernameMap.remove(ctx);
            });
            ws.onMessage(ctx -> {
                broadcastMessage(ctx.message());
            });
        });
    }
    
    private static void broadcastMessage(String message) {
        userUsernameMap.keySet().stream().filter(ctx -> ctx.session.isOpen())
                .forEach(session -> {
            session.send(message);
        });
    }

    private static void processTicker() {
        final List<String> symbols = Arrays.asList("AAPL", "TSLA", "NKE", "AMZN", "WMT");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final int i = new Random().nextInt(5);
                final int j = new Random().nextInt(100);
                final String message = String.format("{\"symbol\": \"%s\", \"price\": %s}",
                        symbols.get(i), j);
                broadcastMessage(message);
            }
        }, 2000, 2000);
    }

}
