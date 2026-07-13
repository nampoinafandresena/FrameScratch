package com.framescratch.utils;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;

public class Utilitaire {
    public String nom_package;
    public String annotation;
    public ElementType niveau;

    public Utilitaire(String nom_package, String annotation, ElementType niveau) {
        this.nom_package = nom_package;
        this.annotation = annotation;
        this.niveau = niveau;
    }

    public static List<Class<?>> getAllClassesFromPackage(String nom_package) throws Exception{
        List<Class<?>> classes = new ArrayList<>();

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
        
        return classes;
    }

    public static List<String> recupererClassesAvecAnnotation(Utilitaire utilitaire) throws Exception {
        List<String> listeAvecAnnotation = new ArrayList<>();

        try {
            listeAvecAnnotation = utilitaire.recupererElements(utilitaire);

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la récupération des classes : " + e.getMessage());
        }

        return listeAvecAnnotation;
    }

    public List<String> recupererElements(Utilitaire utilitaire) throws Exception {

        List<String> resultat = new ArrayList<>();

        List<Class<?>> classes = getAllClassesFromPackage(utilitaire.getNom_package());

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

    public static Map<String, Mapping> recupererUrlMapping(Utilitaire utilitaire) throws Exception {
        Map<String, Mapping> urlMapping = new HashMap<>();

        List<Class<?>> classes = getAllClassesFromPackage(utilitaire.getNom_package());

        Class<?> annotationClass = Class.forName(utilitaire.getAnnotation());

        if (!annotationClass.isAnnotation()) {
            throw new Exception("Ce n'est pas une annotation");
        }

        Class<? extends Annotation> annotation = annotationClass.asSubclass(Annotation.class);

        Method valueMethod = annotation.getMethod("value");

        for (Class<?> classe : classes) {
            for (Method method : classe.getDeclaredMethods()) {

                if (method.isAnnotationPresent(annotation)) {

                    Annotation ann = method.getAnnotation(annotation);

                    String url = (String) valueMethod.invoke(ann);

                    urlMapping.put(url, new Mapping(classe, method));
                }
            }
        }

        return urlMapping;
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
