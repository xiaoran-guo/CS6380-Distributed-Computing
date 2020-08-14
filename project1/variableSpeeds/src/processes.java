public class processes extends Thread {
    private masterThread master;
    private int id;
    private int index;
    private int numOfProc;

    // Constructor
    processes(masterThread master, int id, int index, int numOfProc) {
        this.master = master;
        this.id = id;
        this.index = index;
        this.numOfProc = numOfProc;
    }

    // Run until finding the leader and then terminate.
    @Override
    public void run() {
        while (true) {
            if (master.signal(id, index, numOfProc)) {
                return;
            }
        }
    }
}
