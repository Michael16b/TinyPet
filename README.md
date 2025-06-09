# Tiny Pet - Web Application for Petitions 
ATALLA FADIA  
BESILY MICHAEL  
JAN CHARLÈNE  

## Description
Une application web permettant de créer et de signer des pétitions, inspirée des plateformes telles qu'Avaaz ou Change.org. L'application prend en charge l'authentification des utilisateurs et la gestion de millions de pétitions et signatures.

## Fonctionnalités
- **Création de pétition** : Les utilisateurs peuvent rédiger et publier des pétitions.
- **Signature de pétition** : Un utilisateur ne peut signer une pétition qu'une seule fois.
- **Liste des pétitions signées** : Affichage des pétitions signées par un utilisateur, triées par date.
- **Classement des pétitions populaires** : Affichage des 100 pétitions les plus populaires triées par date.
- **Recherche par tags** : Trouver des pétitions via des tags, triées par date de création.
- **Liste des signataires** : Affichage de toutes les personnes ayant signé une pétition.

## Objectifs
L'application sera développée en tant qu'application **Google App Engine**, avec :
- Une interface utilisateur développée en **Mithril** (ou Vue, React, Angular…).
- Des services REST écrits avec **Google Cloud Endpoints (Java)**.
- Une base de données utilisant **Google Datastore** pour stocker et gérer les données.

## Installation

## Prérequis

Assurez-vous que **Maven** est installé sur votre machine.  
Vous pouvez soit :

- Installer Maven manuellement : [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- Ou utiliser **IntelliJ IDEA**, qui installe et configure Maven automatiquement si nécessaire.

## Compilation du projet


A REVOIR
Depuis un terminal ouvert à la racine du projet, exécutez la commande suivante :

```bash
mvn package
```

Cela va compiler et packager l'ensemble du projet.

## Lancement du backend en local

Naviguez dans le dossier /backend, puis lancez la commande suivante :

```bash
mvn appengine:run
```
