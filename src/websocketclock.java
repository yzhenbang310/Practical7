/**
 * Created by 152595y on 11/28/2016.
 */
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ServerEndpoint("/clock")
public class websocketclock {

    static ScheduledExecutorService timer =
            Executors.newSingleThreadScheduledExecutor();

    private static Set<Session> allSessions;

    DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    @OnOpen
    public void showTime(Session session){
        allSessions = session.getOpenSessions();
        System.out.println("WebSocket opened: " + session.getId());
        // start the scheduler on the very first connection
        // to call sendTimeToAll every second
        if (allSessions.size()<=1){
            System.out.println("Session: " + session.getId());
            timer.scheduleAtFixedRate(
                    () -> sendTimeToAll(session),0,1,TimeUnit.SECONDS);
        }
    }

    private void sendTimeToAll(Session session){
        allSessions = session.getOpenSessions();
        for (Session sess: allSessions){
            try{
                String dataToSend = "Local time: " + LocalTime.now().format(timeFormatter);
                sess.getBasicRemote().sendText(dataToSend);
                System.out.println(dataToSend);
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }


    @OnMessage
    public void onMessage(String txt, Session session) throws IOException {
        System.out.println("Message received: " + txt);
        session.getBasicRemote().sendText(txt.toUpperCase());
    }

    @OnClose
    public void onClose(CloseReason reason, Session session) {
        System.out.println("Closing a WebSocket due to " + reason.getReasonPhrase());

    }
}