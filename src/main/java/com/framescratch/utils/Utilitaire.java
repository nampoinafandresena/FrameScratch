package com.framescratch.utils;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;

import com.google.gson.Gson;

public class Utilitaire {
    public String nom_package;
    public String annotation;
    public ElementType niveau;

    public Utilitaire(String nom_package, String annotation, ElementType niveau) {
        this.nom_package = nom_package;
        this.annotation = annotation;
        this.niveau = niveau;
    }

    public static void getAllClassesFromPackage(String nom_package, List<Class<?>> classes) throws Exception{
        String classpath = nom_package.replace('.', '/');

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL ressource = classLoader.getResource(classpath);

        if (ressource == null){
            throw new IllegalArgumentException("package" + nom_package + "introuvable!");
        }

        File dossier = new File(ressource.toURI());

        if (dossier.exists() && dossier.isDirectory()) {
            File[] files = dossier.listFiles();
            if (files != null){
                for (File file : files){
                    if (file.isFile() && file.getName().endsWith(".class")) {
                        String nomClasse = nom_package + '.'
                                + file.getName().substring(0, file.getName().length() - 6); //-6 amzay le anaran le classe tsy misy .class no azo

                        classes.add(Class.forName(nomClasse));
                    }
                }
            }
        }
    }

    public static void recupererClassesAvecAnnotation(Utilitaire utilitaire, List<String> listeAvecAnnotation) throws Exception {
        try {
            listeAvecAnnotation = utilitaire.recupererElements(utilitaire);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la récupération des classes : " + e.getMessage());
        }
    }

    public List<String> recupererElements(Utilitaire utilitaire) throws Exception {

        List<String> resultat = new ArrayList<>();

        List<Class<?>> classes = new ArrayList<>();
        getAllClassesFromPackage(utilitaire.getNom_package(), classes);

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        switch (utilitaire.getNiveau()) {

            case TYPE:
                for (Class<?> classe : classes) {
                    if (classe.isAnnotationPresent(annotation)) {
                        resultat.add(classe.toString());
                    }
                }
                break;

            case FIELD:
                for (Class<?> classe : classes) {
                    for (Field field : classe.getDeclaredFields()) {
                        if (field.isAnnotationPresent(annotation)) {
                            resultat.add(field.toString());
                        }
                    }
                }
                break;

            case METHOD:
                for (Class<?> classe : classes) {
                    for (Method method : classe.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotation)) {
                            resultat.add(method.toString());
                        }
                    }
                }
                break;
        }

        return resultat;
    }

    public static Map<UrlMethod, Mapping> recupererUrlMapping(Utilitaire utilitaire) throws Exception {
        Map<UrlMethod, Mapping> urlMapping = new HashMap<>();

        List<Class<?>> classes = new ArrayList<>();
        getAllClassesFromPackage(utilitaire.getNom_package(), classes);

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        Method valueMethod = annotation.getMethod("value");
        Method methodUrl = annotation.getMethod("method");

        for (Class<?> classe : classes) {
            for (Method method : classe.getDeclaredMethods()) {

                if (method.isAnnotationPresent(annotation)) {

                    Annotation ann = method.getAnnotation(annotation);

                    String url = (String) valueMethod.invoke(ann);
                    String methodOfUrl = (String) methodUrl.invoke(ann);

                    UrlMethod urlMethod = new UrlMethod(url, methodOfUrl);


                    if(urlMapping.get(urlMethod) != null){
                        throw new Exception("URL Deja utilise par un autre controller : " + urlMethod.getUrl() + " avec la methode : " + urlMethod.getMethod());
                    }

                    urlMapping.put(urlMethod, new Mapping(classe, method));
                }
            }
        }

        return urlMapping;
    }

    public static void creerArguments(Method methode, Object[] arguments, Object applicationContext) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
            if (applicationContext != null && p.getType().isAssignableFrom(applicationContext.getClass())) {
                arguments[i] = applicationContext;
            }

        }
    }

    public static void creerArguments(Method methode, Object[] arguments) {
        for (int i = 0; i < methode.getParameters().length; i++) {
            Parameter p = methode.getParameters()[i];
        }
    }

    public static boolean estApiRest(Method methode) {
        return methode.isAnnotationPresent(com.framescratch.annotation.ApiRest.class);
    }

    public static String toJson(Object object) {
        Gson gson = new Gson();
        return gson.toJson(object);
    }


    public String getNom_package() {
        return nom_package;
    }
    public void setNom_package(String nom_package) {
        this.nom_package = nom_package;
    }
    public String getAnnotation() {
        return annotation;
    }
    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }
    public ElementType getNiveau() {
        return niveau;
    }
    public void setNiveau(ElementType niveau) {
        this.niveau = niveau;
    }
}
