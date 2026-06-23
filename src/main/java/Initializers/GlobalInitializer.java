package Initializers;

import EventBus.EventBus;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import modules.mailing.MailingInitializer;

@WebListener
public class GlobalInitializer implements ServletContextListener {
    public static final String EVENT_BUS = "eventBus";

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent){
        ServletContext servletContext = servletContextEvent.getServletContext();
        EventBus eventBus = EventBus.getInstance();
        MailingInitializer.init(eventBus);
        servletContext.setAttribute(EVENT_BUS,eventBus);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent){
        ServletContext servletContext = servletContextEvent.getServletContext();

        EventBus eventBus = (EventBus) servletContext.getAttribute(EVENT_BUS);

        if(eventBus != null){
            servletContext.removeAttribute(EVENT_BUS);
        }
    }
}
