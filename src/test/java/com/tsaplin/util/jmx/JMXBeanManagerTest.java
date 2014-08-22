package com.tsaplin.util.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A test case.
 */
public class JMXBeanManagerTest {

	private static final int SOME_INITIAL_VALUE1 = 1;
	private static final int SOME_OTHER_VALUE2 = 10;
	private static final int RESULT_OF_ADDITION = 11;
	
	private static final JMXBeanName BEAN_NAME1 = new JMXBeanName("TEST_BEAN1", "TEST_DOMAIN");
	private static final JMXBeanName BEAN_NAME2 = new JMXBeanName("TEST_BEAN2", "TEST_DOMAIN");
	
	private MBeanServerConnection serverConnection = ManagementFactory.getPlatformMBeanServer();

	@Before
	public void setUp() throws JMXBeanException {
		JMXBeanManager.getInstance().register(BEAN_NAME1, new TestBeanImpl(SOME_INITIAL_VALUE1));
	}

	@After
	public void tearDown() throws JMXBeanException {
		JMXBeanManager.getInstance().unregister(BEAN_NAME1);
	}
	
	@Test
	public void shouldRegisterManagedBeans() throws MalformedObjectNameException, JMXBeanException, IOException {
		JMXBeanManager.getInstance().register(BEAN_NAME2, new TestBeanImpl());
		assertTrue("Managed bean should be registered", serverConnection.isRegistered(BEAN_NAME1.getObjectName()));
		assertTrue("Managed bean should be registered", serverConnection.isRegistered(BEAN_NAME2.getObjectName()));
		JMXBeanManager.getInstance().unregister(BEAN_NAME2);
	}

	@Test
	public void shouldUnregisterManagedBean() throws JMXBeanException, MalformedObjectNameException, IOException {
		JMXBeanManager.getInstance().register(BEAN_NAME2, new TestBeanImpl());
		JMXBeanManager.getInstance().unregister(BEAN_NAME2);
		assertFalse("Managed bean should be unregistered", serverConnection.isRegistered(BEAN_NAME2.getObjectName()));
	}

	@Test
	public void shouldGetManagedProperty() throws JMXBeanException, MalformedObjectNameException {
		TestBean beanProoxy = JMX.newMBeanProxy(serverConnection, BEAN_NAME1.getObjectName(), TestBean.class);
		assertEquals("Should return an actual value", SOME_INITIAL_VALUE1, beanProoxy.getValue());
	}
	
	@Test
	public void shouldSetManagedProperty() throws JMXBeanException, MalformedObjectNameException {
		TestBean beanProoxy = JMX.newMBeanProxy(serverConnection, BEAN_NAME1.getObjectName(), TestBean.class);
		beanProoxy.setValue(SOME_OTHER_VALUE2);
		assertEquals("Should return a new value", SOME_OTHER_VALUE2, beanProoxy.getValue());
	}
	
	@Test
	public void shouldCallManagedMethod() throws JMXBeanException, MalformedObjectNameException {
		TestBean beanProoxy = JMX.newMBeanProxy(serverConnection, BEAN_NAME1.getObjectName(), TestBean.class);
		assertEquals("Should return a result of addition of " + SOME_INITIAL_VALUE1 + " and " + SOME_OTHER_VALUE2,  
				RESULT_OF_ADDITION,	beanProoxy.computeResultOfAddition(SOME_OTHER_VALUE2));
	}

	/**
	 * An annotated interface of a managed bean.
	 */
	@JMXBean(description = "Test bean")
	public static interface TestBean {

		/**
		 * A managed property (either setter or getter should be annotated).   
		 */
		@JMXProperty(description = "Test property")
		public int getValue();

		public void setValue(int value);

		/**
		 * A managed method.
		 */
		@JMXMethod(description = "Add other value")
		public int computeResultOfAddition(@JMXParameter(name = "Other value", description = "Some other value") int otherValue);

	}

	/**
	 * A managed bean implementation.
	 */
	private static class TestBeanImpl implements TestBean {

		private int value = 0;

		public TestBeanImpl() {
		}
		
		public TestBeanImpl(int value) {
			this.value = value;
		}

		@Override
		public int getValue() {
			return value;
		}

		@Override
		public void setValue(int value) {
			this.value = value;
		}

		@Override
		public int computeResultOfAddition(int otherValue) {
			return this.value + otherValue;
		}

	}

}
