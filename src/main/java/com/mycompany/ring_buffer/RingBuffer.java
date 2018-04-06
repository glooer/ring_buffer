/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ring_buffer;

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
	 */
	public synchronized void push(Type value) {
		this.buffer[buffer_index] = value;
		buffer_index++;
		System.out.println("записали " + value + "; " + buffer_index);

		if (buffer_index >= buffer_size) {
			buffer_index = 0;
		}
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
		System.out.println("получили " + value);

		return value;
	}

}
