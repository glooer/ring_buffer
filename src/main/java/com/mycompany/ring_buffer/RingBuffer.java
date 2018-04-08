/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ring_buffer;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author polenkov-kp
 * @param <Type>
 * @see https://docs.oracle.com/javase/7/docs/api/java/util/Queue.html
 */
public class RingBuffer<Type> implements Queue {

	@Override
	public boolean add(Object value) {
		if (this.buffer[buffer_index] != null) {
			throw new IllegalStateException("Переполнение списка");
		}

		return offer(value);
	}

	@Override
	public boolean offer(Object value) {
		try {
			this.push(value);
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
		int last_index = (buffer_index - 1 >= 0 ? buffer_index : buffer_size) - 1;
		Object value = buffer[last_index];

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
		RingBuffer<Type> clone = new RingBuffer();

		for (Object object : this.toArray()) {
			if (object == null) {
				continue;
			}

			clone.add(object);
		}

		return clone;
	}

	@Override
	public Iterator iterator() {

		RingBuffer clone = this;
		try {
			clone = (RingBuffer) this.clone();
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(RingBuffer.class.getName()).log(Level.SEVERE, null, ex);
		}

		return new RingBufferIterator(clone);
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
	private int buffer_index = 0; // текущий индекс
	private static final Logger LOG = Logger.getLogger(RingBuffer.class.getName());

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
	private synchronized RingBuffer push(Object value) {
		this.buffer[buffer_index] = value;

		LOG.log(Level.INFO, "записали {0} в ячейку {1}", new Object[]{value, buffer_index});

		buffer_index++;

		if (buffer_index >= buffer_size) {
			buffer_index = 0;
		}

		return this;
	}

	/**
	извлекает элемент из списка
	@return
	 */
	private synchronized Type pop() {
		buffer_index--;

		if (buffer_index < 0) {
			buffer_index = buffer_size - 1;
		}

		Type value = (Type) this.buffer[buffer_index];
		this.buffer[buffer_index] = null;

		LOG.log(Level.INFO, "Получили {0}", value);

		return value;
	}

	@Override
	public boolean remove(Object value) {

		if (value == null) {
			throw new NullPointerException();
		}

		boolean is_remove = false;
		Object[] temp_list = new Object[this.buffer_size];
		int iterator = 0;

		Object temp_value = poll();

		while (temp_value != null) {
			System.out.println(temp_value);
			if (value.equals(temp_value)) {
				is_remove = true;
				temp_value = pop();
				continue;
			}

			temp_list[iterator++] = temp_value;

			temp_value = pop();
		}

		while ((--iterator) >= 0) {
			this.add(temp_list[iterator]);
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
