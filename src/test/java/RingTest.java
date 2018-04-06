/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.mycompany.ring_buffer.RingBuffer;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
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

		for (int i = 0; i < 100; i++) {
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

		try {
			sleep(1000 * 2);
		} catch (Exception e) {
		}

		List result = new ArrayList();

		for (int i = 0; i < ring_size; i++) {
			result.add(rb.pop());
		}

		System.out.println(result);
	}
}
