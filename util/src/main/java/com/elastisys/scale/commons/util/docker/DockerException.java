package com.elastisys.scale.commons.util.docker;

/**
 * Thrown by {@link Docker} to indicate an error condition.
 *
 * @see Docker
 */
public class DockerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DockerException() {
        super();
    }

    public DockerException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

    public DockerException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public DockerException(String arg0) {
        super(arg0);
    }

    public DockerException(Throwable arg0) {
        super(arg0);
    }

}
