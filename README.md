JMX Made Easy
=============

**A set of utility classes which simplify publishing of annotated java POJOs through JMX.**

Now publishing of POJOs is easy:

```java
JMXBeanManager.getInstance().register(BEAN_NAME, new TestBeanImpl(SOME_INITIAL_VALUE));
```

Unpublishing is even easier:

```java
JMXBeanManager.getInstance().unregister(BEAN_NAME);
```

Here is our annotated interface:

```java
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
```

And our POJO:

```java
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
```
