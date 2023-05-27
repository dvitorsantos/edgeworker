package lsdi.edgeworker.Threads;

import com.sun.management.OperatingSystemMXBean;

public class ContextDataReaderThread extends Thread {
    private static final long MEGABYTE = 1024L * 1024L;
    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();

    @Override
    public void run() {
        super.run();
        while (true) {
            try {
                long memory = osBean.getFreeMemorySize();
                double cpu = osBean.getProcessCpuLoad();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
