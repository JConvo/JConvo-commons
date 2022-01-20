package com.grivera.jconvo.commons.user;

import com.grivera.jconvo.commons.user.message.Message;
import com.grivera.jconvo.commons.user.message.MessageIntent;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 *
 * Represents a User in JConvo.
 *
 * @author grivera
 * @version 1.0
 *
 */
public class User implements Runnable {

    private Socket socket;
    private String username;
    private ObjectOutputStream outWriter;
    private ObjectInputStream inReader;
    private Consumer<Message> onReceived = message -> {};
    private Consumer<User> onDie = User::close;
    private Consumer<Message> onFailure = message -> System.out.printf("%s\n", message.getRaw());

    /**
     *
     * Creates a newly accepted user.
     *
     * @param socket a client socket received by the server.
     * @throws IOException if non-CREATE request isn't sent.
     *
     * @see Socket
     *
     */
    public User(Socket socket) throws IOException {

        this.setupSocket(socket);
        this.setupUsername();

    }

    /**
     *
     * Creates a User and sends a CREATE request to the connected server.
     *
     * @param socket a client socket.
     * @param username the user's displayed name for other connections.
     * @throws IOException if a disconnected socket is provided.
     *
     * @see Socket
     *
     */
    public User(Socket socket, String username) throws IOException {

        this.setupSocket(socket);
        this.username = username;
        this.sendMessage(new Message(username, MessageIntent.CREATE, username));

        Message status = this.receiveMessage();

        while (status.author().equals("SYSTEM") && status.intent() == MessageIntent.STATUS_FAILURE) {

            this.onFailure.accept(status);
            this.setupUsername();

        }

    }

    /**
     *
     * Attaches all socket information from the provided socket to the user.
     *
     * @param socket a user's socket.
     * @throws IOException if the socket is null.
     * @see Socket
     *
     */
    public void setupSocket(Socket socket) throws IOException {

        /* Setup Socket IO */
        this.socket = socket;
        this.outWriter = new ObjectOutputStream(new DataOutputStream(socket.getOutputStream()));
        this.inReader = new ObjectInputStream(new DataInputStream(socket.getInputStream()));

    }

    /**
     *
     * Accepts a username sent from the Client.
     *
     * @throws IOException if a CREATE Message intent isn't received.
     *
     */
    public void setupUsername() throws IOException {

        Message usernameMessage = this.receiveMessage();

        if (usernameMessage == null || usernameMessage.intent() != MessageIntent.CREATE) {

            throw new IOException("Username not provided!");

        }

        this.username = usernameMessage.author();

    }

    /**
     *
     * Displays to the user all received messages while connected.
     *
     */
    @Override
    public void run() {

        while (this.isConnected()) {

            Message message = this.receiveMessage();
            if (message == null) break;

            MessageIntent intent = message.intent();
            if (intent == MessageIntent.SEND) {

                this.onReceived.accept(message);

            }

        }

        this.onDie.accept(this);

    }

    /**
     *
     * Sets an action when a message is sent
     *
     * @param onReceived
     */
    public void setOnReceived(Consumer<Message> onReceived) {

        this.onReceived = onReceived.andThen(this.onReceived);

    }

    public void setOnDie(Consumer<User> onDie) {

        this.onDie = onDie.andThen(this.onDie);

    }

    public void setOnFailure(Consumer<Message> onFailure) {

        this.onFailure = onFailure;

    }

    /**
     *
     * Sends a Message to the server.
     *
     * @param message a constructed Message object to send to the server.
     * @return status of the message.
     *
     * @apiNote Blocks until the message is sent.
     * @see Message
     *
     */
    public boolean sendMessage(Message message) {

        try {

            this.outWriter.writeObject(message);
            return true;

        } catch (IOException e) {

            return false;

        }

    }

    /**
     *
     * Sends a Message to the output stream.
     *
     * @param content a message to send to the server.
     * @return The status of the message.
     *
     * @apiNote Blocks until the message is sent.
     *
     */
    public boolean sendMessage(String content) {

        Message message = new Message(this.getUsername(), MessageIntent.SEND, content);
        return this.sendMessage(message);

    }

    /**
     *
     * Receives a Message from the server.
     *
     * @return the Message object received.
     *
     * @apiNote blocks until a message is received.
     * @see Message
     *
     */
    public Message receiveMessage() {

        try {

            return (Message) this.inReader.readObject();

        } catch (IOException | ClassNotFoundException e) {

            return null;

        }

    }

    /**
     *
     * Verifies if the user is still connected.
     *
     * @return the status of the user's connection.
     *
     */
    public boolean isConnected() {

        return this.socket.isConnected();

    }

    /**
     *
     * Closes the user's connection.
     *
     */
    public void close() {

        if (!this.isConnected()) return;

        try {

            if (this.isConnected()) {

                this.socket.close();
                this.inReader.close();
                this.outWriter.close();

            }

        } catch (IOException e) {

            e.printStackTrace();

        }

    }

    /**
     *
     * Returns the user's username.
     *
     * @return the user's username.
     *
     */
    public String getUsername() {

        return this.username;

    }

    /**
     *
     * Returns the data of the User as a String.
     *
     * @return string representation of the user.
     *
     */
    @Override
    public String toString() {

        return String.format("User%s[Username: %s, Socket information %s]",
                super.toString(), this.getUsername(), this.socket);

    }

}
