import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Process extends Thread {
    int uid;
    int maxId;
    int parent;
    ArrayList<MessageQueue> outgoingLinks;
    ArrayList<MessageQueue> incomingLinks;
    ArrayList<Integer> listOfChild;
    Set<Integer> setOfAck;
    boolean termination = false;
    boolean initialization = true;
    Marker marker;
    int numOfAck;
    int timeUnit = 10;
    CyclicBarrier cb;

    // Constructor of Process
    public Process(int uid, Marker marker, CyclicBarrier cb) {
        this.uid = uid;
        this.maxId = uid;
        this.parent = -1;
        this.cb = cb;
        this.outgoingLinks = new ArrayList<>();
        this.incomingLinks = new ArrayList<>();
        this.listOfChild = new ArrayList<>();
        this.setOfAck = new HashSet<>();
        this.marker = marker;
    }

    public void run() {
        while (!marker.termination) {
            // At the beginning of the algorithm， every process initializes its own id as the max id it has received.
            if (initialization) {
                Message message = new Message(uid, uid, "init");

                // Send message to all its neighbors.
                for (int i = 0; i < outgoingLinks.size(); i++) {
                    marker.addNumOfMessage();
                    outgoingLinks.get(i).send(message);
                }
                initialization = false;
            }

            // Receive message.
            try {
                receiveMessage();
                cb.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    private void receiveMessage() throws InterruptedException {
        // Generate random transmission time for the message in the channel.
        Map<Integer, Integer> transTimeMap = new HashMap<>();
        for (int i = 0; i < incomingLinks.size(); i++) {
            int transTime = new Random().nextInt(11);
            transTimeMap.put(i, transTime);
        }

        // Sort the random transmission time for the message in the channel.
        List<Map.Entry<Integer, Integer>> timeInfo = new ArrayList<>(transTimeMap.entrySet());
        timeInfo.sort((t1, t2) -> t1.getValue() - t2.getValue());

        int time = 0;
        for (int i = 0; i < timeInfo.size(); i++) {
            int index = timeInfo.get(i).getKey();
            int value = timeInfo.get(i).getValue();

            // After transmission time units, receive the message.
            Thread.sleep((value - time) * timeUnit);
            time += value - time;
            Message message = incomingLinks.get(index).receive();


            if (message != null) {
                String type = message.type;
                if (type.equals("init")) {
                    // If the process receives a larger id, it let the sender as its parent and broadcast this new
                    // max id to all its neighbor except for its new parent.
                    if (this.maxId < message.maxId) {
                        this.maxId = message.maxId;
                        this.parent = message.uid;
                        this.listOfChild.clear();
                        broadcast(new Message(this.uid, this.maxId, "init"), this.parent);
                        // also send to the parent
                        sendTo(new Message(this.uid, -1, "dummy"), this.parent);
                        numOfAck = 0;
                        //If the process receives a smaller id, it will reply the sender a nack message.
                    } else {
                        sendTo(new Message(this.uid, message.maxId, "nack"), message.uid);
                        broadcast(new Message(this.uid, -1, "dummy"), message.uid);
                    }
                } else if (type.equals("ack")) {
                    // After receiving the ack message, it adds the number of ack and let the sender as its child.
                    if (this.maxId == message.maxId) {
                        numOfAck++;
                        listOfChild.add(message.uid);
                        // send a dummy message to all its neighbors
                        broadcast(new Message(this.uid, -1, "dummy"), -1);
                    }
                } else if (type.equals("nack")) {
                    // After receiving the nack message, it adds the number of ack.
                    if (this.maxId == message.maxId) {
                        numOfAck++;
                        // send a dummy message to its neighbors
                        broadcast(new Message(this.uid, -1, "dummy"), -1);
                    }
                }
//                else if (type.equals("dummy")) {
//                    System.out.println("received a dummy message");
//                    broadcast(new Message(this.uid, -1, "dummy"), -1);
//                }
            }
        }
        checkResult();
    }

    private void broadcast(Message message, int parent) {
        for (int i = 0; i < outgoingLinks.size(); i++) {
            MessageQueue queue = outgoingLinks.get(i);
            if (queue.receiver != parent) {
                marker.addNumOfMessage();
                queue.send(message);
            }
        }
    }

    private void sendTo(Message message, int target) {
        for (MessageQueue queue : outgoingLinks) {
            if (queue.receiver == target) {
                marker.addNumOfMessage();
                queue.send(message);
            }
        }
    }

    // Check if the leaf or the intermediate or the root node satisfies the termination condition.
    private void checkResult() {
        if (parent != -1 && (numOfAck == (outgoingLinks.size() - 1))) {
            if (setOfAck.add(this.maxId)) {
                Message message = new Message(this.uid, this.maxId, "ack");
                sendTo(message, this.parent);
            }
            if (!termination) {
                marker.addNumOfTerminatedProc();
                termination = true;
            }
        } else if (parent == -1 && (numOfAck == outgoingLinks.size())) {
            System.out.println(this.uid + " is the leader.");
            marker.addNumOfTerminatedProc();
        }
    }
}
