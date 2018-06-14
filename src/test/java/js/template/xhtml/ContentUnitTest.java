package js.template.xhtml;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import js.util.Classes;

public class ContentUnitTest extends TestCaseEx {
	public void testDirectPrimitiveValue() throws Throwable {
		Pojo object = new Pojo();
		Content content = new Content(object);
		assertEquals(object.title, Classes.invoke(content, "getValue", object, "title"));
	}

	public void testIndirectPrimitiveValue() throws Throwable {
		NestedObject object = new NestedObject();
		Content content = new Content(object);
		assertEquals(object.nested.title, Classes.invoke(content, "getValue", object, "nested.title"));
	}

	private volatile int failsCount;

	public void testConcurentDateScriptFormating() throws Throwable {
		final int THREADS_COUNT = 1000;

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		final Pojo object = new Pojo();
		object.date = df.parse("1964-03-15T13:40:00Z");

		final Content content = new Content(object);

		Thread[] threads = new Thread[THREADS_COUNT];
		failsCount = 0;
		for (int i = 0; i < THREADS_COUNT; ++i) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						assertEquals("Sun Mar 15 1964 13:40:00 UTC", Classes.invoke(content, "getString", object, "date", null));
					} catch (Throwable t) {
						++failsCount;
					}
				}
			});
			threads[i].start();
		}

		for (Thread thread : threads) {
			thread.join();
		}
		assertEquals(String.format("Concurent date script parsing test not passed. There are %d failing threads.", failsCount), 0, failsCount);
	}

	// ------------------------------------------------------
	// fixture initialization and helpers

	private static class Pojo {
		String title;
		@SuppressWarnings("unused")
		Date date;
	}

	@SuppressWarnings("unused")
	private static class NestedObject {
		String title;
		Nested nested = new Nested();

		static class Nested {
			String title;
			Pojo object = new Pojo();
		}
	}
}
