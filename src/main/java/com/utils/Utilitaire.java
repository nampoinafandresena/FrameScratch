package com.utils;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;

public class Utilitaire {
    public String nom_package;
    public String annotation;
    public ElementType niveau;

    public Utilitaire(String nom_package, String annotation, ElementType niveau) {
        this.nom_package = nom_package;
        this.annotation = annotation;
        this.niveau = niveau;
    }

    // - getAllClassesFromPackage(String package)
    public static List<Class<?>> getAllClassesFromPackage(String nom_package){
        List<Class<?>> classes = new ArrayList<>();

        String classpath = nom_package.replace('.', '/');

        ClassLoader classLoader = 
        return classes;
    }
    // - getAllClassesAnnotation(Utilitaire utils)

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
