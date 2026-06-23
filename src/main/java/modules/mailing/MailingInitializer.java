package modules.mailing;

import EventBus.EventBus;
import EventBus.EventType;
import modules.mailing.events.LoginListener;
import modules.mailing.events.PasswordResetListener;

public class MailingInitializer {

    public static void init(EventBus eventBus){
        eventBus.subscribe(EventType.LOGIN, new LoginListener());
        eventBus.subscribe(EventType.REGISTRATION, new LoginListener());
        eventBus.subscribe(EventType.PASSWORD_RESET, new PasswordResetListener());
    }
}
