package EventBus;

public interface Listener {
    public void invokeListener(EventType eventType, String message);
}
