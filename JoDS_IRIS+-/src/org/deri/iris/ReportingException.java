/**
 * 
 */
package org.deri.iris;

/**
 * @author Giorgio Orsi <giorgio.orsi@cs.ox.ac.uk> - Department of Computer Science - University of Oxford.
 */
public class ReportingException extends RuntimeException {

	/**
	 * Constructor.
	 * @param message The exception message.
	 */
	public ReportingException(final String message) {
		super(message);
	}

	/** The serial ID */
	private static final long serialVersionUID = 1L;

}
