package com.jeefix;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;

import javax.persistence.EntityManagerFactory;

@Component
public class HibernateSecurityListener implements PostLoadEventListener, PreUpdateEventListener, PreLoadEventListener {
    @Autowired
    EntityManagerFactory entityManagerFactory;

    @PostConstruct
    private void init() {
        HibernateEntityManagerFactory hibernateEntityManagerFactory = (HibernateEntityManagerFactory) this.entityManagerFactory;
        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) hibernateEntityManagerFactory.getSessionFactory();
        EventListenerRegistry registry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_LOAD, this);
        registry.appendListeners(EventType.PRE_UPDATE, this);
        registry.appendListeners(EventType.PRE_LOAD,this);
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
        final Object entity = event.getEntity();
        if (entity == null) return;

        // some logic after entity loaded
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        final Object entity = event.getEntity();
        if (entity == null) return false;

        // some logic before entity persist

        return false;
    }

    @Override
    public void onPreLoad(PreLoadEvent event) {
        System.out.println(event);
    }
}