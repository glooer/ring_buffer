/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.ring_buffer.RingBuffer;
import static java.lang.Thread.sleep;
import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
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
		RingBuffer<Integer> rb = new RingBuffer();

		// заполняем список значениями от 0 до 11, соотсветсвенно элементы списка будут 8, 9, 10, 11, 4, 5, 6, 7
		for (int i = 0; i < 12; i++) {
			rb.push(i);
		}

		// вытаскивание будет идти по порядку
		for (int i = 11; i >= 4; i--) {
			assertEquals(rb.pop(), (Integer) i);
		}

		// когда мы вытащили все элементы список будет пуст
		for (int i = 0; i < 100; i++) {
			assertEquals(rb.pop(), null);
		}
	}

	@Test
	public void nullTest() {
		RingBuffer<String> rb = new RingBuffer();

		for (int i = 0; i < 10; i++) {
			assertEquals(rb.pop(), null);
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
					rb.push(i);
				}
			}
		};

		Thread th2 = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 5; i++) {
					rb.push(i);
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
		while ((item = rb.pop()) != null) {
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
				rb.push(i);
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

		for (int i = 0; i < ring_size + 1; i++) {
			rb.push(i);
		}

		assertEquals(rb.pop(), (Integer) 1);
	}

	// ещё одна проверка многопоточности, пробуем одновременно читать и писать
	@Test
	public void threadTestWithRead() {
		int ring_size = 100;

		RingBuffer<Integer> rb = new RingBuffer(ring_size);

		Thread writer = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 50; i++) {
					rb.push(i);
					try {
						this.sleep(new Random(System.currentTimeMillis()).nextInt(50));
					} catch (InterruptedException e) {
					}
				}
			}
		};

		Thread writer_two = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 50; i++) {
					rb.push(i);
					try {
						this.sleep(new Random(System.currentTimeMillis()).nextInt(50));
					} catch (InterruptedException e) {
					}
				}
			}
		};

		List<Integer> result = new ArrayList();

		Thread reader = new Thread() {
			@Override
			public void run() {
				for (int i = 0; i < 50; i++) {
					Object item = rb.pop();
					if (item != null) {
						result.add((Integer) item);
					}
					try {
						this.sleep(new Random(System.currentTimeMillis()).nextInt(100));
					} catch (InterruptedException e) {
					}
				}
			}
		};

		writer.start();
		writer_two.start();
		reader.start();

		while (writer.isAlive() || writer_two.isAlive()) {
			try {
				sleep(10);
			} catch (InterruptedException e) {
			}
		}
		// на случай если что то осталось
		Object item;
		while ((item = rb.pop()) != null) {
			result.add((Integer) item);
		}

		Collections.sort(result);

		List<Integer> result_expect = new ArrayList();

		for (int i = 0; i < 50; i++) {
			result_expect.add(i);
			result_expect.add(i);
		}

		assertEquals(result, result_expect);
	}
}
