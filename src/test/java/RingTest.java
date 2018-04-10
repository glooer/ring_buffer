/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.ring_buffer.RingBuffer;
import static java.lang.Thread.sleep;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author polenkov-kp
 */
public class RingTest {

	// проверяем возможность создания списка с отрицательным количеством
	@Test(expected = NegativeArraySizeException.class)
	public void bufferCreate() {
		new RingBuffer(-4);
	}

	// проверяем адекватность работы списка
	@Test
	public void bufferFill() {
		RingBuffer<Integer> rb = new RingBuffer(8);

		// заполняем список значениями от 0 до 11, соотсветсвенно элементы списка будут 8, 9, 10, 11, 4, 5, 6, 7
		for (int i = 0; i < 12; i++) {
			rb.offer(i);
		}

		// вытаскивание будет идти по порядку
		for (int i = 4; i < 12; i++) {
			assertEquals(rb.poll(), (Integer) i);
		}

		// когда мы вытащили все элементы список будет пуст
		for (int i = 0; i < 100; i++) {
			assertEquals(rb.poll(), null);
		}
	}

	@Test
	public void nullTest() {
		RingBuffer<String> rb = new RingBuffer();

		for (int i = 0; i < 10; i++) {
			assertEquals(rb.poll(), null);
		}
	}

	// пробуем писать в список из разных потоков, в надежде что всё будет хорошо
	@Test
	public void threadTest() {
		int ring_size = 16;

		RingBuffer<Integer> rb = new RingBuffer(ring_size);

		Thread th1 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					rb.offer(i);
				}
			}
		};

		Thread th2 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					rb.offer(i);
				}
			}
		};

		th1.start();
		th2.start();

		// немного подождём пока потоки отработают, возможно тут есть смысл в th1.isAlive(), но так проще
		try {
			sleep(100);
		} catch (Exception e) {
		}

		List result = new ArrayList();

		// получаем все данные из списка
		Object item;
		while ((item = rb.poll()) != null) {
			result.add(item);
		}

		// т.к. потоки могли записать их в любом порядке то отсортируем
		Collections.sort(result);

		assertEquals(result, Arrays.asList(0, 0, 1, 1, 2, 2, 3, 3, 4, 4));
	}

	// проверяем требование: 1.	Буфер должен работать при переполнении: Выбрасывать исключения
	@Test
	public void saveMode() {
		int ring_size = 5;

		RingBuffer<Integer> rb = new RingBuffer(ring_size);
		rb.setSaveMode(true);

		try {
			for (int i = 0; i < ring_size + 1; i++) {
				rb.add(i);
			}
			fail("Переполнения списка не произошло");
		} catch (Exception e) {
			// все ок
		}

	}

	// проверяем требование: 1.	Буфер должен работать при переполнении: Перезаписывать
	@Test
	public void unSaveMode() {
		int ring_size = 1;

		RingBuffer<Integer> rb = new RingBuffer(ring_size);
		rb.setSaveMode(false);

		for (int i = 0; i < ring_size + 1; i++) {
			rb.add(i);
		}

		assertEquals(rb.poll(), (Integer) 1);
	}

	// ещё одна проверка многопоточности, пробуем одновременно читать и писать
	@Test
	public void threadTestWithRead() {
		int ring_size = 10_000;

		RingBuffer<Integer> rb = new RingBuffer(ring_size);

		Thread writer = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < ring_size / 2; i++) {
					rb.add(i);
				}
			}
		};

		Thread writer_two = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < ring_size / 2; i++) {
					rb.add(i);
				}
			}
		};

		List<Integer> result = new ArrayList();

		Thread reader = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < ring_size; i++) {
					Object item = rb.poll();
					if (item != null) {
						result.add((Integer) item);
					}
				}
			}
		};

		writer.start();
		writer_two.start();
		reader.start();

		while (writer.isAlive() || writer_two.isAlive() || reader.isAlive()) {
			try {
				sleep(10);
			} catch (InterruptedException e) {
			}
		}
		// на случай если что то осталось
		Object item;
		while ((item = rb.poll()) != null) {
			result.add((Integer) item);
		}

		Collections.sort(result);

		List<Integer> result_expect = new ArrayList();

		for (int i = 0; i < ring_size / 2; i++) {
			result_expect.add(i);
			result_expect.add(i);
		}

		assertEquals(result, result_expect);
	}

	@Test(expected = NoSuchElementException.class)
	public void removeNoSuchElementException() {
		new RingBuffer().remove();
	}

	@Test(expected = NoSuchElementException.class)
	public void elementNoSuchElementException() {
		new RingBuffer().element();
	}

	@Test
	public void peek() {
		RingBuffer<Integer> rb = new RingBuffer(2);

		rb.add(11);
		rb.add(42);

		assertEquals(rb.peek(), 11);
		assertEquals(rb.peek(), 11);
	}

	@Test
	public void isEmpty() {
		RingBuffer<Integer> rb = new RingBuffer(2);

		assertTrue(rb.isEmpty());

		rb.add(42);

		assertFalse(rb.isEmpty());
	}

	@Test
	public void contains() {
		RingBuffer<Integer> rb = new RingBuffer(2);

		assertFalse(rb.contains(42));

		rb.add(42);

		assertTrue(rb.contains(42));
	}

	@Test
	public void removeByObject() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(42);
		rb.add(11);
		rb.add(32);
		rb.add(42);
		assertTrue(rb.remove(42));

		assertFalse(rb.contains(42));

		List result = new ArrayList();
		Object temp;

		while ((temp = rb.poll()) != null) {
			result.add(temp);
		}

		assertEquals(result, Arrays.asList(11, 32));
	}

	@Test
	public void noRemove() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(11);
		rb.add(32);

		assertFalse(rb.remove(42));

		List result = new ArrayList();
		Object temp;

		while ((temp = rb.poll()) != null) {
			result.add(temp);
		}

		assertEquals(result, Arrays.asList(11, 32));

	}

	@Test
	public void iterator() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(1);
		rb.add(2);
		rb.add(3);

		List result = new ArrayList();

		for (Iterator iterator = rb.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}

		assertEquals(result, Arrays.asList(1, 2, 3));
		assertEquals(rb.poll(), 1);
		assertEquals(rb.poll(), 2);
	}

	@Test
	public void addAll() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(1);
		rb.add(2);
		rb.add(3);

		RingBuffer<Integer> rb_two = new RingBuffer();

		rb.add(4);

		rb_two.addAll(rb);

		List result = new ArrayList();

		for (Iterator iterator = rb_two.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}

		assertEquals(result, Arrays.asList(1, 2, 3, 4));
	}

	@Test
	public void removeAll() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(1);
		rb.add(2);
		rb.add(3);
		rb.add(4);

		RingBuffer<Integer> rb_two = new RingBuffer();

		rb_two.add(2);
		rb_two.add(4);

		rb.removeAll(rb_two);

		List result = new ArrayList();

		for (Iterator iterator = rb.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}

		assertEquals(result, Arrays.asList(1, 3));
	}

	@Test
	public void removeTwoElement() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(1);
		rb.add(2);
		rb.add(3);
		rb.add(4);
		rb.add(5);

		rb.remove(2);
		rb.remove(4);

		List result = new ArrayList();

		for (Iterator iterator = rb.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}

		assertEquals(result, Arrays.asList(1, 3, 5));
	}

	@Test
	public void retainAll() {
		RingBuffer<Integer> rb = new RingBuffer(10);

		rb.add(1);
		rb.add(2);
		rb.add(3);
		rb.add(4);
		rb.add(5);
		rb.add(6);

		RingBuffer<Integer> rb_two = new RingBuffer();

		rb_two.add(2);
		rb_two.add(4);
		rb_two.add(5);

		rb.retainAll(rb_two);

		List result = new ArrayList();

		for (Iterator iterator = rb.iterator(); iterator.hasNext();) {
			result.add(iterator.next());
		}

		assertEquals(result, Arrays.asList(2, 4, 5));
	}

	@Test
	public void containsAll() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(1);
		rb.add(2);
		rb.add(3);
		rb.add(4);
		rb.add(5);
		rb.add(6);

		RingBuffer<Integer> rb_two = new RingBuffer();

		rb_two.add(2);
		rb_two.add(4);
		rb_two.add(5);

		assertTrue(rb.containsAll(rb_two));

		rb_two.add(42);

		assertFalse(rb.containsAll(rb_two));

	}

	@Test
	public void fifo() {
		RingBuffer<Integer> rb = new RingBuffer();

		rb.add(1);
		rb.add(2);
		rb.add(3);

		assertEquals(rb.poll(), 1);
		assertEquals(rb.poll(), 2);
		assertEquals(rb.poll(), 3);
		assertEquals(rb.poll(), null);
	}

	@Test
	public void fifoSuperMini() {
		RingBuffer<Integer> rb = new RingBuffer(1);

		rb.add(1);
		rb.add(2);
		rb.add(3);

		assertEquals(rb.poll(), 3);
	}

	@Test
	public void fifoOverflow() {
		RingBuffer<Integer> rb = new RingBuffer(2);

		rb.add(1);
		rb.add(2);
		rb.add(3);

		assertEquals(rb.poll(), 2);
		assertEquals(rb.poll(), 3);
	}

	@Test(expected = NullPointerException.class)
	public void addNull() {
		RingBuffer<Integer> rb = new RingBuffer(2);

		rb.add(null);
	}

//	пока не знаю как реализовать проверку типов, возможно в текущей реализации никак..
	@Test(expected = ClassCastException.class)
	@Ignore
	public void addCheckTypes() {
		RingBuffer<Integer> rb = new RingBuffer(2);

		rb.add("42");
	}

}
