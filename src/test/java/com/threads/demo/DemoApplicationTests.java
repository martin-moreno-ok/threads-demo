package com.threads.demo;

import com.threads.demo.model.Pool;
import org.jmock.lib.concurrent.Blitzer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
class DemoApplicationTests {

	static Blitzer blitzer;
	static Pool<String> pool;
	static String resource;
	static int ACTION_COUNT = 50000;
	static int THREAD_COUNT = 100;

	@BeforeAll
	public static void beforeAll() {
		blitzer = new Blitzer(ACTION_COUNT, THREAD_COUNT);
		pool = new Pool<>();
		pool.open();
		resource = "Hello-World";
	}

	@Test
	public void addResourceTest() throws InterruptedException {

		blitzer.blitz(() -> {
			try {
				pool.add(resource);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		Assertions.assertEquals(1 , pool.size());
	}

	@Test
	public void removeResourceTest() throws InterruptedException {
		pool.add(resource);
		blitzer.blitz(() -> {
			try {
				pool.remove(resource);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Assertions.assertEquals(0 , pool.size());
	}

	@Test
	public void removeNowResourceTest() throws InterruptedException {
		pool.add(resource);
		blitzer.blitz(() -> {
			try {
				pool.removeNow(resource);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Assertions.assertEquals(0 , pool.size());
	}


	@Test
	public void acquireReleaseResourceTest() throws InterruptedException {
		AtomicInteger count = new AtomicInteger(0);
		pool.add(resource);
		blitzer.blitz(() -> {
			try {
				String resourceAcquired = pool.acquire();
				if (resourceAcquired != null) {
					pool.release(resourceAcquired);
					count.incrementAndGet();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Assertions.assertEquals(ACTION_COUNT , count.get());
	}

	@Test
	public void acquireTimeOutResourceTest() throws InterruptedException {
		AtomicBoolean timeout = new AtomicBoolean(false);
		pool.add(resource);
		blitzer.blitz(() -> {
			try {
				String resourceAcquired = pool.acquire(1, TimeUnit.NANOSECONDS);
				if (resourceAcquired != null) {
					TimeUnit.NANOSECONDS.sleep(2);
					pool.release(resourceAcquired);
				} else {
					timeout.set(true);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		Assertions.assertTrue(timeout.get());
	}

	@Test
	public void isOpenTest() throws InterruptedException {
		AtomicBoolean isOpen = new AtomicBoolean(false);
		blitzer.blitz(() -> {
			try {
				if (pool.isOpen()) {
					isOpen.set(true);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});
		Assertions.assertTrue(isOpen.get());
	}

	@Test
	public void isNotOpenTest() throws InterruptedException {
		AtomicBoolean isOpen = new AtomicBoolean(false);
		Pool<String> tmpPool = new Pool<>();
		tmpPool.open();
		tmpPool.close();

		blitzer.blitz(() -> {
			try {
				if (!tmpPool.isOpen()) {
					isOpen.set(true);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		});

		Assertions.assertTrue(isOpen.get());
	}

}
