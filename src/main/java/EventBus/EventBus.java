package EventBus;

import java.util.HashMap;
import java.util.HashSet;

public class EventBus {
    private static EventBus eventBus = null;

    private EventBus(){

    }

    HashMap<EventType, HashSet<Listener>> subscribers = new HashMap<>();

    public void subscribe(EventType eventType, Listener listener){
        HashSet<Listener> listeners = new HashSet<>();
        if(subscribers.containsKey(eventType)){
            subscribers.get(eventType).add(listener);
        }
        listeners.add(listener);
        subscribers.put(eventType, listeners);

    }

    public void unsubscribe(EventType eventType, Listener listener){
        if(subscribers.containsKey(eventType)){
            subscribers.get(eventType).remove(listener);
        }

    }

    public void publish(EventType eventType, String message){
        if(subscribers.containsKey(eventType)){
            for(Listener listener : subscribers.get(eventType)){
                IO.println(String.format("Invoking %s", listener.toString()));
                IO.println(String.format("For eventType %s and message %s",eventType,message));
                listener.invokeListener(eventType, message);
            }
        }
    }

    public static EventBus getInstance(){
        if(eventBus == null){
            eventBus = new EventBus();
        }
        return eventBus;
    }
}
