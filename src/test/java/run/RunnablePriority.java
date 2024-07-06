package run;

public class RunnablePriority implements Runnable, Comparable<RunnablePriority> {

    private Integer priority;

    public Integer getPriority() {
        return priority;
    }

    public RunnablePriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 优先级比较
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(RunnablePriority o) {
        if (this.getPriority() < o.priority) {
            return 1;
        } else if (this.getPriority() > o.priority) {
            return -1;
        }
        return 0;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " prioroty " + priority + " 开始执行任务 ...");
    }
}