package com.framescratch.utils;

import jakarta.servlet.http.HttpSession;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Session {
    // nom de l'attribut -> valeur   (ex: "role" -> "ADMIN", "user" -> un Employee)
    private Map<String, Object> attributs = new HashMap<>();

    private HttpSession httpSession;                       // la vraie session (pour la mise a jour)
    private Set<String> nomsInitiaux = new HashSet<>();    // noms presents au depart (pour detecter les suppressions)

    public Session() {
    }

    public Session(HttpSession httpSession) {
        this.httpSession = httpSession;

        Enumeration<String> noms = httpSession.getAttributeNames();
        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            attributs.put(nom, httpSession.getAttribute(nom));
        }
        nomsInitiaux.addAll(attributs.keySet());
    }

    public void ajouterAttribut(String nom, Object valeur) {
        attributs.put(nom, valeur);
    }

    public Object getAttribut(String nom) {
        return attributs.get(nom);
    }

    public void modifierAttribut(String nom, Object valeur) {
        if (!attributs.containsKey(nom)) {
            throw new IllegalArgumentException("Impossible de modifier : l'attribut '" + nom
                    + "' n'existe pas dans la session. Utilise ajouterAttribut(...).");
        }
        attributs.put(nom, valeur);
    }

    public void supprimerAttribut(String nom) {
        attributs.remove(nom);
    }

    public void viderAttributs() {
        attributs.clear();
    }

    public Map<String, Object> getAttributs() {
        return attributs;
    }

    public void synchroniser() {
        if (httpSession == null) {
            return;
        }

        // attributs supprimes par le developpeur -> supprimes de la HttpSession
        for (String nom : nomsInitiaux) {
            if (!attributs.containsKey(nom)) {
                httpSession.removeAttribute(nom);
            }
        }

        // attributs ajoutes ou modifies -> ecrits dans la HttpSession
        for (Map.Entry<String, Object> entree : attributs.entrySet()) {
            httpSession.setAttribute(entree.getKey(), entree.getValue());
        }

        nomsInitiaux = new HashSet<>(attributs.keySet());
    }
}
