# Tiny Pet - Web Application for Petitions

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

### Prérequis
Assurez-vous que **Node.js** est installé sur votre machine (Windows, Linux, etc.).

Vous pouvez le télécharger depuis [nodejs.org](https://nodejs.org/).

### Vérification de l'installation
Ouvrez un terminal et exécutez les commandes suivantes pour vérifier que Node.js et npm sont installés correctement :

```bash
node --version
npm --version
ng --version
```

Si les versions s'affichent, cela signifie que l'installation a réussi.

### Installation d'une dépendance

Pour installer une dépendance, utilisez la commande suivante dans le terminal à la racine de votre projet :

```bash
npm install
```

### Lancer l'application

Pour lancer l'application, exécutez la commande suivante dans le terminal à la racine de votre projet :

```bash
npm start
```
