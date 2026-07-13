package com.framescratch.servlet;

import java.io.*;
import java.lang.annotation.*;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import com.framescratch.utils.*;

public class FrontControllerServlet extends HttpServlet {
    List<String> listeControllers = new ArrayList<>();
    Map<String, Mapping> urlMapping = new HashMap<>();
    
    // fonction init
    public void init() throws ServletException {
        try {
            String nom_package = getServletConfig().getInitParameter("nomPackage");
            listeControllers = Utilitaire.recupererClassesAvecAnnotation(new Utilitaire(nom_package,
                    "com.framescratch.annotation.Controller", ElementType.METHOD));
                    urlMapping = Utilitaire.recupererUrlMapping(
                    new Utilitaire(nom_package, "com.framescratch.annotation.UrlMapping", ElementType.METHOD));
        } catch (Exception e) {
            System.out.println("Erreur lors de la recuperation des controllers : " + e.getMessage());
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI().substring(request.getContextPath().length());

        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL recue : " + url + "</p>");

        afficher(url, request, response);

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        processRequest(req, res);
    }

    protected void afficher(String url, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        Mapping mapping = urlMapping.get(url);

        if (mapping != null) {
                out.println("Url trouvee : " + url);
                out.println("Classe: " + mapping.getClasse().getName());
                out.println("Méthode: " + mapping.getMethode().getName());
        } else {
            out.println("Url non trouvee : " + url);
            out.println("<h2>Liste des URL disponibles :</h2>");
            for (String urlDisponible : urlMapping.keySet()) {
                Mapping mappingDisponible = urlMapping.get(urlDisponible);
                out.println("<p>URL: " + urlDisponible + " | Classe: " + mappingDisponible.getClasse().getName() + " | Méthode: "
                        + mappingDisponible.getMethode().getName() + "</p>");
            }
        }
    }
}
