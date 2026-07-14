package com.framescratch.servlet;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import com.framescratch.utils.*;

public class FrontControllerServlet extends HttpServlet implements ServletContextListener{
    List<String> listeControllers = new ArrayList<>();
    Map<UrlMethod, Mapping> urlMapping = new HashMap<>();
    
    // fonction init
    public void init() throws ServletException {
        listeControllers = (List<String>) getServletContext().getAttribute("listeControllers");
        urlMapping = (Map<UrlMethod, Mapping>) getServletContext().getAttribute("urlMapping");
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        out.println("<h1>Front Controller</h1>");
        out.println("<p>URL recue : " + url + "</p>");

        afficher(url, method, request, response, out);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        processRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        processRequest(req, res);
    }

    protected void afficher(String url, String method, HttpServletRequest request, HttpServletResponse response, PrintWriter out)
            throws ServletException, IOException {

        UrlMethod urlMethod = new UrlMethod(url, method);
        Mapping mapping = urlMapping.get(urlMethod);

        if (mapping != null) {
            out.println("<p>URL: " + urlMethod.getUrl() 
            + " avec la methode : " + urlMethod.getMethod() 
            + " | Classe: " + mapping.getClasse().getName() 
            + " | Fonction: " + mapping.getMethode().getName() + "</p>");
            try {
                Object instance = mapping.getClasse().getDeclaredConstructor().newInstance();
                Method methode = mapping.getMethode();
                Object resultat = methode.invoke(instance);

                out.print("<script>console.log('" + resultat.toString() + "');</script>");
            } catch (Exception e) {
                out.println("<p>Erreur lors de l'invocation de la méthode : " + e.getMessage() + "</p>");
            }

        } else {
            out.println("Url non trouvee : " + url);
            out.println("<h2>Liste des URL disponibles :</h2>");
            for (UrlMethod urlMethodDisponible : urlMapping.keySet()) {
                Mapping mappingDisponible = urlMapping.get(urlMethodDisponible);
                out.println("<p>URL: " + urlMethodDisponible.getUrl() 
                            + " avec la methode : " + urlMethodDisponible.getMethod() 
                            + " | Classe: " + mappingDisponible.getClasse().getName() 
                            + " | Fonction: " + mappingDisponible.getMethode().getName() + "</p>");
            }
        }
    }
}
