package com.grivera.jconvo.commons.user.message;

import java.io.Serializable;

/**
 *
 * Represents all of the possible operations that a message may intend
 *
 * @author grivera
 * @version 1.0
 *
 */
public enum MessageIntent implements Serializable {
    CREATE,
    SEND,
    STATUS_SUCCESS,
    STATUS_FAILURE
}
