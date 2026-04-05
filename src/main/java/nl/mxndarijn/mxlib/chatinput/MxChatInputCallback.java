package nl.mxndarijn.mxlib.chatinput;

/**
 * Callback interface for receiving chat input from a player.
 */
public interface MxChatInputCallback {

    /**
     * Called when the player sends a chat message while being tracked by the input manager.
     * @param message the received message
     */
    void textReceived(String message);
}
