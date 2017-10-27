public class SolutionWithSynchronization implements MemoryWrapper {

    private int readers;
    private int writers;
    private int wantWrite;
    private MemorySegment _memory = null;

    public SolutionWithSynchronization() {
        _memory = new MemorySegment();
        readers = 0;
        writers = 0;
    }

    @Override
    public void read(Process p) {
        p.setState("wantread");
        synchronized (this) {
            while (wantWrite > 0 || writers > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            readers++;
        }
        p.setState("reading");
        _memory.read();
        synchronized (this){
            readers--;
            notifyAll();
        }
        p.setState("idle");
    }

    @Override
    public void write(Process p) {
        p.setState("wantwrite");
        wantWrite++;
        synchronized (this) {
            while (writers > 0 || readers > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            writers++;
            wantWrite--;
        }
        p.setState("writing");
        _memory.write();
        synchronized (this){
            writers--;
            notifyAll();
        }
        p.setState("idle");
    }
}