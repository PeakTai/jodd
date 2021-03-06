// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.servlet.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Implementation of <b>HttpServletResponseWrapper</b> that works with
 * the {@link GzipResponseStream} implementation.
 */
public class GzipResponseWrapper extends HttpServletResponseWrapper {

	/**
	 * Calls the parent constructor which creates a ServletResponse adaptor
	 * wrapping the given response object.
	 */
	public GzipResponseWrapper(HttpServletResponse response) {
		super(response);
		origResponse = response;
	}

	/**
	 * Original response.
	 */
	protected HttpServletResponse origResponse;

	/**
	 * The ServletOutputStream that has been returned by
	 * <code>getOutputStream()</code>, if any.
	 */
	protected ServletOutputStream stream;

	/**
	 * The PrintWriter that has been returned by <code>getWriter()</code>, if any.
	 */
	protected PrintWriter writer;

	/**
	 * The threshold number to compress.
	 */
	protected int threshold;

	/**
	 * Content type.
	 */
	protected String contentType;

	// ---------------------------------------------------------------- public
	
	/**
	 * Set content type
	 */
	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
		origResponse.setContentType(contentType);
	}

	/**
	 * Set threshold number
	 */
	public void setCompressionThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * Creates and returns a ServletOutputStream to write the content associated
	 * with this Response.
	 */
	public ServletOutputStream createOutputStream() throws IOException {
		GzipResponseStream gzstream = new GzipResponseStream(origResponse);
		gzstream.setBuffer(threshold);
		return gzstream;
	}

	/**
	 * Finishes a response.
	 */
	public void finishResponse() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				if (stream != null) {
					stream.close();
				}
			}
		} catch (IOException e) {
			// ignore
		}
	}

	// ---------------------------------------------------------------- ServletResponse

	/**
	 * Flushes the buffer and commit this response.
	 */
	@Override
	public void flushBuffer() throws IOException {
		if (stream != null) {
			stream.flush();
		}
	}

	/**
	 * Returns the servlet output stream associated with this Response.
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException {

		if (writer != null) {
			throw new IllegalStateException("getWriter() has already been called for this response");
		}
		if (stream == null) {
			stream = createOutputStream();
		}
		return(stream);
	}

	/**
	 * Returns the writer associated with this Response.
	 */
	@Override
	public PrintWriter getWriter() throws IOException {

		if (writer != null) {
			return writer;
		}

		if (stream != null) {
			throw new IllegalStateException("getOutputStream() has already been called for this response");
		}

		stream = createOutputStream();
		
		String charEnc = origResponse.getCharacterEncoding();
		if (charEnc != null) {
			writer = new PrintWriter(new OutputStreamWriter(stream, charEnc));
		} else {
			writer = new PrintWriter(stream);
		}
		return(writer);
	}

	@Override
	public void setContentLength(int length) {
	}

	/**
	 * Servlets v3.1 introduce this method, so we need to have it here
	 * in case they are used.
	 * See: https://github.com/oblac/jodd/issues/189
	 */
	public void setContentLengthLong(long length) {
	}

}