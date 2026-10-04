package com.framescratch.listener;

import java.lang.annotation.*;
import java.util.*;

import com.framescratch.utils.*;

import jakarta.servlet.*;

public class ApplicationListener implements ServletContextListener{
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();
    List<String> listeControllers = new ArrayList<>();

    // --- Spring (par réflexion) ---
    try {
        Class<?> utils = Class.forName("org.springframework.web.context.support.WebApplicationContextUtils");
        Object ctx = utils.getMethod("getWebApplicationContext", ServletContext.class)
                          .invoke(null, servletContext);
        servletContext.setAttribute("springContext", ctx);
    } catch (Exception e) {
        System.err.println("Spring non détecté : " + e.getMessage());
    }

    try {
        String nomPackage = servletContext.getInitParameter("nomPackage");

        Utilitaire.recupererClassesAvecAnnotation(new Utilitaire(nomPackage,
                "com.framescratch.annotation.Controller", ElementType.TYPE), listeControllers);
        servletContext.setAttribute("listeControllers", listeControllers);

        Map<UrlMethod, Mapping> urlMapping = Utilitaire.recupererUrlMapping(new Utilitaire(nomPackage,
                "com.framescratch.annotation.UrlMapping", ElementType.METHOD));
        servletContext.setAttribute("urlMapping", urlMapping);
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
}

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().log("## Arrêt de l'application ##");
    }
}
