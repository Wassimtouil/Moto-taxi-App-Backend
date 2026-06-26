# 🏍️ E-Moto-Taxi – Backend

Backend développé avec **Spring Boot** pour la plateforme intelligente **E-Moto-Taxi**, permettant la gestion complète des réservations de moto-taxi, des paiements, de la géolocalisation en temps réel et des fonctionnalités d'intelligence artificielle.

---

# 📖 Description

E-Moto-Taxi est une plateforme intelligente développée dans le cadre d'un **Projet de Fin d'Études (PFE)**.

Le backend expose une API REST sécurisée qui permet de gérer les différents acteurs de la plateforme (clients, chauffeurs et administrateurs) ainsi que l'ensemble des fonctionnalités métier : réservation des trajets, géolocalisation, paiements, chat en temps réel, réclamations, évaluations et administration.

---

# ✨ Fonctionnalités

## 🔐 Authentification et sécurité

* Inscription et connexion
* Authentification sécurisée avec JWT
* Gestion des rôles (Administrateur, Chauffeur, Client)
* Protection des API avec Spring Security

---

## 🛵 Gestion des trajets

* Création d'une réservation
* Acceptation ou refus d'une course
* Affectation d'un chauffeur
* Historique des trajets
* Annulation d'une réservation

---

## 📍 Géolocalisation

* Localisation GPS des chauffeurs
* Recherche des chauffeurs à proximité
* Calcul des itinéraires
* Suivi en temps réel

---

## 💬 Communication

* Chat entre client et chauffeur
* Notifications en temps réel
* Communication via WebSocket

---

## 💳 Paiement

* Paiement électronique
* Gestion du portefeuille (Wallet)
* Gestion des cartes bancaires
* Historique des transactions

---

## ⭐ Évaluations et réclamations

* Évaluation des chauffeurs
* Gestion des avis
* Traitement des réclamations

---

## 🤖 Intelligence artificielle

* Chatbot intelligent
* Analyse des réclamations
* Traitement automatique du langage naturel (NLP)

---

## 📊 Administration

* Gestion des utilisateurs
* Gestion des chauffeurs
* Gestion des trajets
* Gestion des paiements
* Tableau de bord
* Statistiques

---

# 🏗️ Architecture

Le backend est développé selon une **architecture en couches (Layered Architecture)**.

Chaque module métier est organisé de manière indépendante afin de garantir une bonne séparation des responsabilités.

Chaque module contient généralement les couches suivantes :

* **Controller** : expose les API REST.
* **Service** : implémente la logique métier.
* **Repository** : assure l'accès aux données.
* **Model (Entity)** : représente les entités de la base de données.
* **DTO** : transporte les données entre le client et le serveur.
* **Mapper** : conversion entre les entités et les DTO.

Cette organisation facilite la maintenance, l'évolution et les tests de l'application.

---

# 📁 Structure du projet

```text
src/main/java

├── admin
│   ├── controller
│   ├── service
│   ├── repository
│   └── model
│
├── authentification
│   ├── controller
│   ├── service
│   ├── repository
│   └── model
│
├── trajet
│   ├── controller
│   ├── service
│   ├── repository
│   ├── dto
│   └── model
│
├── paiement
├── wallet
├── chat
├── location
├── reclamation
├── evaluation
├── notification
├── websocket
├── security
└── configuration
```

---

# 🛠️ Technologies utilisées

## Backend

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Hibernate
* Maven

## Base de données

* MySQL

## Sécurité

* JWT

## Communication

* WebSocket
* STOMP

## Documentation

* Swagger / OpenAPI

## Outils

* IntelliJ IDEA
* Git
* GitHub
* Postman

---

# 🚀 Installation

### 1. Cloner le projet

```bash
git clone https://github.com/votre-utilisateur/votre-repository.git
```

### 2. Configurer la base de données

Modifier le fichier :

```
application.properties
```

avec vos paramètres MySQL.

### 3. Lancer le projet

```bash
mvn spring-boot:run
```

---

# 📚 Documentation de l'API

Une fois l'application démarrée, la documentation Swagger est accessible à l'adresse :

```
http://localhost:8080/swagger-ui/index.html
```

---

# 📸 Captures d'écran

Ajoutez ici quelques captures d'écran :

* Swagger
* Base de données
* Structure du projet
* Tests Postman

---

# 📄 Licence

Projet académique réalisé dans le cadre d'un Projet de Fin d'Études.
