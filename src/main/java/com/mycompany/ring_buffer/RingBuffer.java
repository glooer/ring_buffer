/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ring_buffer;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author polenkov-kp
 * @param <Type>
 * @see https://docs.oracle.com/javase/7/docs/api/java/util/Queue.html
 */
public class RingBuffer<Type> implements Queue {

	private static ReentrantLock locker = new ReentrantLock();

	/**
	устанавливает режим вывода ошибок в случае переполнения буфера, true - валится исключение, false - данные перезаписываются
	@param is
	@return
	 */
	public RingBuffer setSaveMode(boolean is) {
		this.is_save_mode = is;
		return this;
	}

	@Override
	public boolean add(Object value) {
		if (value == null) {
			throw new NullPointerException();
		}

		if (is_save_mode && this.buffer[buffer_index] != null) {
			throw new IllegalStateException("Переполнение списка");
		}

		push(value);

		return true;
	}

	@Override
	public boolean offer(Object value) {
		try {
			this.add(value);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public Object remove() {
		Object value = this.pop();

		if (value == null) {
			throw new NoSuchElementException();
		}

		return value;
	}

	@Override
	public Object poll() {
		return this.pop();
	}

	@Override
	public Object element() {
		Object value = peek();

		if (value == null) {
			throw new NoSuchElementException();
		}

		return value;
	}

	@Override
	public Object peek() {
		Object value = pop(false);

		return value;
	}

	@Override
	public int size() {
		return this.buffer_size;
	}

	@Override
	public boolean isEmpty() {
		return peek() == null;
	}

	@Override
	public boolean contains(Object value) {
		for (Object object : buffer) {
			if (object == null) {
				continue;
			}

			if (object.equals(value)) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		RingBuffer<Type> clone = new RingBuffer(this.buffer_size);
		RingBuffer<Type> clone_test = new RingBuffer(this.buffer_size);

		Object temp;
		while ((temp = this.poll()) != null) {
			clone.add(temp);
			clone_test.add(temp);
		}

		while ((temp = clone_test.poll()) != null) {
			this.add(temp);
		}

		return clone;
	}

	@Override
	public Iterator iterator() {
		try {
			return new RingBufferIterator(this);
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(RingBuffer.class.getName()).log(Level.SEVERE, null, ex);
		}

		return null;
	}

	@Override
	public Object[] toArray() {
		return this.buffer;
	}

	@Override
	public Object[] toArray(Object[] a) {
		// a ?
		return this.buffer;
	}

	// ага, final, мыж объекты внутри меняем а не саму ссылку
	private final Object[] buffer; // собсвтенно хранилище
	private int buffer_size = 8; // размер хранилища
	public int buffer_index = 0; // текущий индекс
	private static final Logger LOG = Logger.getLogger(RingBuffer.class.getName());
	private boolean is_save_mode = false; // кидать ли ексепшн при переполнении

	public RingBuffer(int buffer_size) {
		this.buffer_size = buffer_size;
		this.buffer = new Object[this.buffer_size];
	}

	public RingBuffer() {
		this.buffer = new Object[this.buffer_size];
	}

	/**
	добавляет элемент в список
	@param value
	 * @return
	 */
	private RingBuffer push(Object value) {
		locker.lock();
		try {
			this.buffer[buffer_index] = value;

//		LOG.log(Level.INFO, "записали {0} в ячейку {1}", new Object[]{value, buffer_index});
			buffer_index++;

			if (buffer_index >= buffer_size) {
				buffer_index = 0;
			}
		} finally {
			locker.unlock();
		}

		return this;
	}

	/**
	возвращает следующий индекс списка
	@param i
	@param size
	@return
	 */
	private int indexNext(int i, int size) {
		return i + 1 < size ? i + 1 : 0;
	}

	/**
	извлекает элемент из списка
	@return
	 */
	private Type pop() {
		return pop(true);
	}

	/**
	извлекает элемент из списка
	@return
	 */
	private Type pop(boolean is_delete) {
		locker.lock();

		Type value;
		try {
			int i = buffer_index;

			while (this.buffer[i] == null && indexNext(i, buffer_size) != buffer_index) {
				i = indexNext(i, buffer_size);
			}

			value = (Type) this.buffer[i];
			if (is_delete) {
				this.buffer[i] = null;
			}
		} finally {
			locker.unlock();
		}

//		LOG.log(Level.INFO, "Получили {0}", value);
		return value;
	}

	@Override
	public boolean remove(Object value) {

		if (value == null) {
			throw new NullPointerException();
		}

		boolean is_remove = false;
		RingBuffer temp_rb = new RingBuffer(this.buffer_size + 1);

		Object temp_value;

		while ((temp_value = this.poll()) != null) {
			if (value.equals(temp_value)) {
				is_remove = true;
				continue;
			}

			temp_rb.add(temp_value);
		}

		while ((temp_value = temp_rb.poll()) != null) {
			this.add(temp_value);
		}

		return is_remove;
	}

	@Override
	public boolean containsAll(Collection c) {

		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			Object next = iterator.next();

			if (!this.contains(next)) {
				return false;
			}

		}

		return true;
	}

	@Override
	public boolean addAll(Collection c) {
		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			try {
				this.add(iterator.next());
			} catch (Exception e) {
				return false;
			}
		}

		return true;

	}

	@Override
	public boolean removeAll(Collection c) {
		boolean is_remove = false;

		for (Iterator iterator = c.iterator(); iterator.hasNext();) {
			if (this.remove(iterator.next())) {
				is_remove = true;
			}
		}

		return is_remove;
	}

	@Override
	public boolean retainAll(Collection c) {
		for (Iterator iterator = this.iterator(); iterator.hasNext();) {
			Object item = iterator.next();

			if (!c.contains(item)) {
				this.remove(item);
			}
		}

		return true;
	}

	@Override
	public void clear() {
		Object value;

		do {
			value = poll();
		} while (value != null);
	}

}
