package com.browserhorde.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentPipeStream {
	private final Queue<byte[]> q;
	private final AtomicInteger available = new AtomicInteger(0);
	private final AtomicBoolean closed = new AtomicBoolean(false);

	private final InputStream is;
	private final OutputStream os;

	public ConcurrentPipeStream() {
		q = new ConcurrentLinkedQueue<byte[]>();

		is = new ConcurrentInputStream(q, available, closed);
		os = new ConcurrentOutputStream(q, available, closed);
	}

	public InputStream getInputStream() {return is;}
	public OutputStream getOutputStream() {return os;}

	private final static class ConcurrentInputStream extends InputStream {
		private static final byte[] EMPTY_BUFFER = new byte[0];

		private final Queue<byte[]> q;
		private final AtomicBoolean closed;
		private final AtomicInteger available;

		private int offset = 0;
		private byte buffer[] = EMPTY_BUFFER;

		public ConcurrentInputStream(Queue<byte[]> q, AtomicInteger available, AtomicBoolean closed) {
			this.q = q;
			this.closed = closed;
			this.available = available;
		}

		@Override
		public int read() throws IOException {
			return -1;
		}
		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if(buffer == null) {
				if(closed.get()) {
					return -1;
				}
				buffer = EMPTY_BUFFER;
			}

			int total = 0;
			int remaining = len; 
			while(total < len) {
				while(buffer != null && buffer.length <= offset) {
					buffer = q.poll();
					offset = 0;
				}
				if(buffer == null) {
					break;
				}

				int copy = Math.min(buffer.length - offset, remaining);
				System.arraycopy(buffer, offset, b, off, copy);

				available.addAndGet(-copy);
				total += copy;
				remaining -= copy;
				offset += copy;
				off += copy;
			}

			return total;
		}
		
		@Override
		public int available() throws IOException {
			return super.available();
		}
		
		@Override
		public void close() throws IOException {
			closed.compareAndSet(false, true);
		}
	}

	private final static class ConcurrentOutputStream extends OutputStream {
		private final Queue<byte[]> q;
		private final AtomicBoolean closed;
		private final AtomicInteger available;

		public ConcurrentOutputStream(Queue<byte[]> q, AtomicInteger available, AtomicBoolean closed) {
			this.q = q;
			this.closed = closed;
			this.available = available;
		}

		@Override
		public void write(int b) throws IOException {
			write(new byte[]{(byte)b}, 0, 1);
		}
		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if(closed.get()) {
				return;
			}
			if(len < 1) {
				return;
			}
			if(b.length < off + len) {
				throw new IOException();
			}

			byte buffer[] = Arrays.copyOfRange(b, off, len);
			if(buffer.length > 0) {
				available.addAndGet(buffer.length);
				q.add(buffer);
			}
		}

		@Override
		public void close() throws IOException {
			closed.compareAndSet(false, true);
		}
	}
}
