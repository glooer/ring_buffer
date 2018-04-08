/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.ring_buffer;

import java.util.Iterator;

/**
 *
 * @author polenkov-kp
 */
public class RingBufferIterator implements Iterator {

	private RingBuffer rb;

	public RingBufferIterator(RingBuffer rb) {
		this.rb = rb;
	}

	@Override
	public boolean hasNext() {
		return !rb.isEmpty();
	}

	@Override
	public Object next() {
		return rb.poll();
	}

}
