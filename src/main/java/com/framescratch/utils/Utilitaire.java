package com.framescratch.utils;

import java.io.File;
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.URL;

import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    public static void creerArguments(Method methode, Object[] arguments, Object applicationContext,
            HttpServletRequest request, HttpServletResponse response) {
        Parameter[] params = methode.getParameters();

        for (int i = 0; i < params.length; i++) {
            Parameter p = params[i];
            Class<?> type = p.getType();

            if (type == HttpServletRequest.class) {
                arguments[i] = request;
            } else if (type == HttpServletResponse.class) {
                arguments[i] = response;
            } else if (applicationContext != null && type != Object.class && type.isInstance(applicationContext)) {
                arguments[i] = applicationContext;
            } else if (estTypeSimple(type)) {
                if (!p.isNamePresent()) {
                    throw new IllegalStateException("Les noms des parametres de "
                            + methode.getDeclaringClass().getSimpleName() + "." + methode.getName()
                            + "(...) ne sont pas disponibles : compile le projet test avec 'javac -parameters'.");
                }
                String nom = p.getName();
                arguments[i] = convertir(nom, request.getParameter(nom), type);
            } else {
                // BINDING OBJET : Employee employee -> on cree l'objet et on remplit ses attributs
                String prefixe = p.isNamePresent() ? p.getName() : null;
                arguments[i] = binderObjet(type, prefixe, request);
            }
        }
    }

    private static boolean estTypeSimple(Class<?> type) {
        return type.isPrimitive() || type == String.class
                || type == Integer.class || type == Long.class || type == Double.class
                || type == Float.class || type == Short.class || type == Byte.class
                || type == Boolean.class || type == Character.class;
    }

    private static Object binderObjet(Class<?> type, String prefixe, HttpServletRequest request) {
        if (type.isInterface() || type.isArray() || type.isEnum()
                || Modifier.isAbstract(type.getModifiers()) || type.getName().startsWith("java.")) {
            return null;
        }

        Object objet;
        try {
            java.lang.reflect.Constructor<?> constructeur = type.getDeclaredConstructor();
            constructeur.setAccessible(true); 
            objet = constructeur.newInstance();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("La classe " + type.getName()
                    + " doit avoir un constructeur sans argument pour etre remplie automatiquement.");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Impossible de creer " + type.getName() + " : " + e, e);
        }

        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field champ : c.getDeclaredFields()) {
                int mod = champ.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || !estTypeSimple(champ.getType())) {
                    continue;
                }

                String nomChamp = champ.getName();
                String valeur = null;
                if (prefixe != null) {
                    valeur = request.getParameter(prefixe + "." + nomChamp);
                }
                if (valeur == null) {
                    valeur = request.getParameter(nomChamp);
                }
                if (valeur == null) {
                    continue; 
                }

                try {
                    champ.setAccessible(true);
                    champ.set(objet, convertir(nomChamp, valeur, champ.getType()));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Impossible de remplir " + type.getSimpleName()
                            + "." + nomChamp + " : " + e, e);
                }
            }
        }
        return objet;
    }

    private static Object convertir(String nom, String valeur, Class<?> type) {
        if (valeur == null || (valeur.isEmpty() && type != String.class)) {
            return type.isPrimitive() ? valeurParDefaut(type) : null;
        }

        try {
            if (type == String.class) {
                return valeur;
            }
            String v = valeur.trim();
            if (type == int.class || type == Integer.class) return Integer.parseInt(v);
            if (type == long.class || type == Long.class) return Long.parseLong(v);
            if (type == double.class || type == Double.class) return Double.parseDouble(v);
            if (type == float.class || type == Float.class) return Float.parseFloat(v);
            if (type == short.class || type == Short.class) return Short.parseShort(v);
            if (type == byte.class || type == Byte.class) return Byte.parseByte(v);
            if (type == boolean.class || type == Boolean.class) {
                return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("on") || v.equals("1");
            }
            if (type == char.class || type == Character.class) return v.charAt(0);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parametre '" + nom + "' invalide : '" + valeur
                    + "' ne peut pas etre converti en " + type.getSimpleName());
        }

        return null; 
    }

    private static Object valeurParDefaut(Class<?> typePrimitif) {
        return java.lang.reflect.Array.get(java.lang.reflect.Array.newInstance(typePrimitif, 1), 0);
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