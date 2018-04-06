/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ring_buffer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author polenkov-kp
 * @param <Type>
 */
public class RingBuffer<Type> {

	// ага, final, мыж объекты внутри меняем а не саму ссылку
	private final Object[] buffer; // собсвтенно хранилище
	private int buffer_size = 8; // размер хранилища
	private int buffer_index = 0; // текущий индекс
	private boolean is_save_mode = false; // кидать ли ексепшн при переполнении
	private static final Logger LOG = Logger.getLogger(RingBuffer.class.getName());

	public RingBuffer(int buffer_size) {
		this.buffer_size = buffer_size;
		this.buffer = new Object[this.buffer_size];
	}

	public RingBuffer() {
		this.buffer = new Object[this.buffer_size];
	}

	public RingBuffer setSaveMode(boolean is) {
		this.is_save_mode = is;
		return this;
	}

	/**
	добавляет элемент в список
	@param value
	 */
	public synchronized RingBuffer push(Type value) {

		if (is_save_mode && this.buffer[buffer_index] != null) {
			throw new RuntimeException("Переполнение списка");
		}

		this.buffer[buffer_index] = value;

		LOG.log(Level.INFO, "записали " + value + "; " + buffer_index);

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
	public synchronized Type pop() {
		buffer_index--;

		if (buffer_index < 0) {
			buffer_index = buffer_size - 1;
		}

		Type value = (Type) this.buffer[buffer_index];
		this.buffer[buffer_index] = null;

		LOG.log(Level.INFO, "получили " + value);

		return value;
	}

}
