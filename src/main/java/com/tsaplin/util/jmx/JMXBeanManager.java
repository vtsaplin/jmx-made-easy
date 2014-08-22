package com.tsaplin.util.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

/**
 * Allows to publish annotated beans as managed beans. 
 */
public final class JMXBeanManager {

	private MBeanServer beanServer;

	private JMXBeanManager() {
		beanServer = ManagementFactory.getPlatformMBeanServer();
	}

	public synchronized void register(JMXBeanName beanName, Object bean) throws JMXBeanException {
		try {
			beanServer.registerMBean(new JMXBeanWrapper(bean), beanName.getObjectName());
		} catch (Exception e) {
			throw new JMXBeanException("Error while registering managed bean", e);
		}
	}

	public synchronized void unregister(JMXBeanName beanName) throws JMXBeanException {
		try {
			beanServer.unregisterMBean(beanName.getObjectName());
		} catch (Exception e) {
			throw new JMXBeanException("Error while unregistering managed bean", e);
		}
	}

	private static class LazyHolder {
		private static final JMXBeanManager INSTANCE = new JMXBeanManager();
	}

	public static JMXBeanManager getInstance() {
		return LazyHolder.INSTANCE;
	}

}
