public class Message {
    int uid;
    int maxId;
    String type;    // 1.init; 2.ack; 3.nack

    //Constructor of Message
    public Message(int uid, int maxId, String type) {
        this.uid = uid;
        this.maxId = maxId;
        this.type = type;
    }
}
