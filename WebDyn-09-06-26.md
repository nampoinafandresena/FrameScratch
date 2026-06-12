- Technique d'acces reseau
- Clustering

- S6:
  - Web service
  - framework

# Objectif - Spring MVC
- Hanamboatra framework
- Décomposition de problème



# Spring MVC
- Controller, view,....

## Sprint 0(fin 14/06/26)
=> Forcer tous les requêtes à passer sur FrontController
**ex:** localhost:8080 => vers a page principal de tomcat
- Hanamboatra servlet ray: FrontControllerServlet
  - doGet appel ProcessRequest
  - doPost appel ProcessRequest
  - out.writer
  - out.println(url)
=> FrontController => .jar, .class



- Mandeha ve? :
    - code: Tsy maintsy mandeha ny FrontController
    - test:
      - déclarer le controller dans web.xml
      - chemin, fullName de FrontController
      - urlParam: / ou /* (tous les requêtes)
      - web.xml

- Amboarina ny script pour avoir du .jar
- La mettre dans lib, classpath

=> FrontController dans git + nom package

## Nécessite
- Avoir 2 espaces de travail:
  - code framework: jar
  - test: .jar classpath
- Utiliser github (code et test)
  - Créer une branche pour chaque S0-descFonctionnalite (issue de main)
  - Clone branche
  - Miasa
  - Push vers S0
  - Pull Request (14Sprint -> 14 pullrequest)
  - Supprime branche