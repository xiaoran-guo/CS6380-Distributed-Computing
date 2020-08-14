import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {
    private Queue<Message> queue;
    int sender;
    int receiver;

    // Constructer of MessageQueue
    public MessageQueue(int sender, int receiver) {
        this.queue = new LinkedList<>();
        this.sender = sender;
        this.receiver = receiver;
    }

    // Send message to the queue of the link.
    public synchronized void send(Message message) {
        queue.add(message);
    }

    // Receive message from the queue of the link.
    public synchronized Message receive() {
        if (!queue.isEmpty())
            return queue.poll();
        return null;
    }
}
