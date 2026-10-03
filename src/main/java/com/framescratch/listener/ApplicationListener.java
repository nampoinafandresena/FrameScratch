package com.framescratch.listener;

import java.lang.annotation.*;
import java.util.*;

import com.framescratch.utils.*;

import jakarta.servlet.*;

public class ApplicationListener implements ServletContextListener{
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        List<String> listeControllers = new ArrayList<>();
        Map<UrlMethod, Mapping> urlMapping;

        try {
                Class<?> utils = Class.forName("org.springframework.web.context.support.WebApplicationContextUtils");
                Object ctx = utils
                                .getMethod("getWebApplicationContext", ServletContext.class)
                                .invoke(null, servletContext);

                servletContext.setAttribute("springContext", ctx);
            } catch (Exception e) {
                System.err.println("ERROR: Spring non trouvé: " + e.getMessage() + "!!");
            }

        try {
            String nom_package = servletContext.getInitParameter("nomPackage");

            Utilitaire.recupererClassesAvecAnnotation(new Utilitaire(nom_package,
                    "mg.itu.annotation.Controller", ElementType.METHOD), listeControllers);

            servletContext.setAttribute("listeControllers", listeControllers);

            urlMapping = Utilitaire.recupererUrlMapping(
                    new Utilitaire(nom_package, "mg.itu.annotation.UrlMapping", ElementType.METHOD));

            servletContext.setAttribute("urlMapping", urlMapping);
        } catch (Exception e) {
            System.err.println("---- Erreur lors de l'initialisation: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().log("## Arrêt de l'application ##");
    }
}
