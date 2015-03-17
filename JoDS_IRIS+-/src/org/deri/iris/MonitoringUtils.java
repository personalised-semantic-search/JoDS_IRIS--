/**
 * 
 */
package org.deri.iris;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.List;

/**
 * @author jd
 */
public class MonitoringUtils {

	public static int getHeapUsage() {
		
		
		for (int i = 0; i < 150; i++) {	
			System.runFinalization();
			System.gc();
		}

		// Get a memory monitor
		final List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();

		int usage = 0;
		for (final MemoryPoolMXBean memoryPoolBean : memoryPoolBeans) {
			if (memoryPoolBean.getType().equals(MemoryType.HEAP)) {
				usage += memoryPoolBean.getUsage().getUsed() / 1024;
			}
		}
		return usage;
	}

	public static void resetPeakUsage() {
		for (int i = 0; i < 50; i++) {
			System.gc();
		}
		// Get a memory monitor
		final List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		for (final MemoryPoolMXBean memoryPoolBean : memoryPoolBeans) {
			if (memoryPoolBean.getType().equals(MemoryType.HEAP)) {
				memoryPoolBean.resetPeakUsage();
			}
		}
	}

	public static long getPeakUsage() {

		// Get a memory monitor
		final List<MemoryPoolMXBean> memoryPoolBeans = ManagementFactory.getMemoryPoolMXBeans();
		int peakUsage = 0;
		for (final MemoryPoolMXBean memoryPoolBean : memoryPoolBeans) {
			if (memoryPoolBean.getType().equals(MemoryType.HEAP)) {
				peakUsage += memoryPoolBean.getPeakUsage().getUsed() / 1024;
			}
		}
		return peakUsage;
	}
}
