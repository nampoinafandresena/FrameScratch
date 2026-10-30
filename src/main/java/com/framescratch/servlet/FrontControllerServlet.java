package com.framescratch.servlet;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.MultipartConfig;

import com.framescratch.utils.*;
import com.framescratch.view.ModelAndView;
import com.framescratch.view.ViewResolver;

@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 50 * 1024 * 1024)
public class FrontControllerServlet extends HttpServlet implements ServletContextListener {
    List<String> listeControllers = new ArrayList<>();
    Map<UrlMethod, Mapping> urlMapping = new HashMap<>();
    Object applicationContext;

    // fonction init
    public void init() throws ServletException {
        listeControllers = (List<String>) getServletContext().getAttribute("listeControllers");
        urlMapping = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("urlMapping");
        if (getServletContext().getAttribute("springContext") != null) {
            applicationContext = getServletContext().getAttribute("springContext");
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Avant toute lecture de parametre : sinon les accents des formulaires POST sont casses
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        afficher(url, method, request, response, out);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void afficher(String url, String method, HttpServletRequest request, HttpServletResponse response,
            PrintWriter out)
            throws ServletException, IOException {

        UrlMethod urlMethod = new UrlMethod(url, method);
        Mapping mapping = urlMapping.get(urlMethod);

        if (mapping != null) {
            try {
                Object instance = mapping.getClasse().getDeclaredConstructor().newInstance();
                Method methode = mapping.getMethode();
                boolean is_apiRest = Utilitaire.estApiRest(methode);
                response.setContentType(is_apiRest ? "application/json" : "text/html");

                // Injection (request, response, contexte Spring) + BINDING des parametres du formulaire
                Object[] arguments = new Object[methode.getParameters().length];
                Utilitaire.creerArguments(methode, arguments, applicationContext, request, response);
                Object resultat = methode.invoke(instance, arguments);
                Utilitaire.synchroniserSessions(arguments);   // ← AJOUTER
                
                    if (resultat instanceof ModelAndView) {
                    ModelAndView mv = (ModelAndView) resultat;
                    ViewResolver viewResolver = new ViewResolver();
                    viewResolver.setNom_vue(mv.getNom_vue());
                    viewResolver.setPrefix_vue(getServletContext().getInitParameter("prefixVue"));
                    viewResolver.setExtension_vue(getServletContext().getInitParameter("suffixVue"));

                    for (Map.Entry<String, Object> entry : mv.getAttributs().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = request.getRequestDispatcher(viewResolver.getCheminCompletVue());
                    dispatcher.forward(request, response);
                } else if (is_apiRest) {
                    if (resultat instanceof String) {
                        out.println((String) resultat);
                    } else {
                        out.println(Utilitaire.toJson(resultat));
                    }
                }

            } catch (IllegalArgumentException e) {
                // valeur envoyee invalide (ex: age=abc pour un int) -> erreur 400
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                out.println("<p>Erreur lors de l'invocation de la méthode : " + cause + "</p>");
            }
        } else {
            out.println("Url non trouvee : " + url);
            out.println("<h2>Liste des URL disponibles :</h2>");
            for (UrlMethod urlMethodDisponible : urlMapping.keySet()) {
                Mapping mappingDisponible = urlMapping.get(urlMethodDisponible);
                out.println("<p>URL: " + urlMethodDisponible.getUrl() + " avec la methode : "
                        + urlMethodDisponible.getMethod() + "| Classe: " + mappingDisponible.getClasse().getName()
                        + " | Fonction: "
                        + mappingDisponible.getMethode().getName() + "</p>");
            }
        }
    }
}