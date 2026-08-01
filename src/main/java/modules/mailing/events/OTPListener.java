package modules.mailing.events;

import EventBus.EventOtpDTO;
import EventBus.EventType;
import EventBus.Listener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import modules.mailing.Services.MailingService;

public class OTPListener implements Listener {

    ObjectMapper mapper = new ObjectMapper();
    MailingService mailingService = new MailingService();


    @Override
    public void invokeListener(EventType eventType, String message){
        EventOtpDTO otpDTO = new EventOtpDTO();
        try{
            IO.println(String.format("Deserializing event %s message: %s",eventType,message ));
            otpDTO = mapper.readValue(message, EventOtpDTO.class);
            IO.println(String.format("Finished deserializing event %s message: %s",eventType,message ));
        }
        catch(JsonProcessingException e){
            e.printStackTrace();
        }

        IO.println(String.format("Processing the mail for event %s message: %s",eventType,message ));
        String messageBody = mailingService.buildGenericOtpEmail(otpDTO.getOtp(), otpDTO.getMailTo());
        String subject = "Verify Registration Email";

        IO.println(String.format("Processing the mail for event %s message: %s",eventType,message ));
        mailingService.sendMail(otpDTO.getMailTo(),subject,messageBody);
        IO.println(String.format("Finished sending the mail for event %s message: %s",eventType,message ));
    }
}
