package run;

import org.junit.Test;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RunExecuteTask {
    @Test
    public void aaa() throws InterruptedException {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 1, TimeUnit.DAYS, new PriorityBlockingQueue<>());
        executor.execute(new RunnablePriority(1));
        executor.execute(new RunnablePriority(10));
        executor.execute(new RunnablePriority(11));
        executor.execute(new RunnablePriority(2));
        executor.execute(new RunnablePriority(3));
        executor.execute(new RunnablePriority(12));
        executor.execute(new RunnablePriority(4));
        executor.execute(new RunnablePriority(13));
        Thread.sleep(20000);
        executor.shutdown();
    }
}
