package com.grivera.jconvo.commons.user.message;

import java.io.Serializable;

/**
 *
 * Represents an immutable message sent through JConvo sockets.
 *
 * @apiNote Requires jdk16+.
 *
 * @param author Represents the author of the message.
 * @param intent The message's intent for sending the message.
 * @param content The message's content
 *
 * @author grivera
 * @version 1.0
 */
public record Message(String author, MessageIntent intent, String content) implements Serializable {

    public String getRaw() {

        return String.format("%s: %s", this.author(), this.content());

    }

    @Override
    public String toString() {

        return String.format("Message[author: %s, intent: %s, content: %s]",
                this.author(), this.intent(), this.content());

    }

    @Override
    public boolean equals(Object o) {

        /* Pattern Variable from jdk-16 */
        if (!(o instanceof Message tmp)) return false;

        return tmp.author().equals(this.author());

    }

}
