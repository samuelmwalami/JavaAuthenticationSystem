package modules.mailing;

import EventBus.EventBus;
import EventBus.EventType;
import modules.mailing.events.*;

public class MailingInitializer {

    public static void init(EventBus eventBus){
        eventBus.subscribe(EventType.LOGIN, new LoginListener());
        eventBus.subscribe(EventType.REGISTRATION, new RegistrationListener());
        eventBus.subscribe(EventType.PASSWORD_RESET, new PasswordResetListener());
        eventBus.subscribe(EventType.DELETE_ACCOUNT, new DeleteAccountListener());
        eventBus.subscribe(EventType.OTP, new OTPListener());
    }
}
