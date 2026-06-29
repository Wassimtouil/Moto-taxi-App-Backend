<div align="center">
  
  <img src="https://img.icons8.com/?size=100&id=107446&format=png&color=000000" alt="Moto-Taxi Logo" width="120"/>

  # 🏍️ E-Moto-Taxi Platform (Backend)

  **Un backend robuste, sécurisé et performant pour une application de réservation de Moto-Taxis.**

  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.java.net/)
  [![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
  [![JWT Security](https://img.shields.io/badge/Security-JWT-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)](https://jwt.io/)
  
</div>

## 📖 Description du Projet

Ce projet constitue l'API backend principale pour une application **E-Moto-Taxi**, permettant de relier des passagers à des conducteurs de moto-taxis. Il est développé avec **Spring Boot 3** et offre une gamme complète de fonctionnalités allant de la gestion des utilisateurs au traitement des paiements, en passant par le suivi en temps réel et l'intégration de fonctionnalités d'IA et de commandes vocales.

## ✨ Fonctionnalités Principales

*   **🔐 Authentification & Sécurité :** Inscription, connexion et gestion des rôles (Admin, Chauffeur, Passager) sécurisées par JWT.
*   **🗺️ Gestion des Trajets (Ride Management) :** Création, acceptation, suivi et historique des courses.
*   **📍 Géolocalisation & Suivi en temps réel :** WebSockets intégrés pour le suivi de la position des chauffeurs et l'intégration de services de géocodage.
*   **💳 Système de Paiement :** Gestion de portefeuilles virtuels (Wallets), paiements par carte et configuration des tarifs.
*   **⭐ Évaluations & Réclamations :** Système de notation des chauffeurs et traitement des réclamations via un dashboard administrateur.
*   **🤖 IA & NLP (Commandes Vocales) :** Assistance via un Chatbot IA et module de traitement du langage naturel (NLP) pour la réservation vocale.
*   **💬 Chat en Temps Réel :** Communication intégrée entre le passager et le chauffeur.
*   **📊 Tableau de Bord Administrateur :** Outils complets de supervision (Utilisateurs, Paiements, Trajets, Évaluations, Statistiques).
*   **📄 Génération de Rapports :** Création automatique de factures et rapports au format PDF (OpenPDF).

## 🛠️ Technologies Utilisées

| Catégorie | Technologie |
| :--- | :--- |
| **Langage** | Java 17 |
| **Framework Principal**| Spring Boot 3.2.5 |
| **Sécurité** | Spring Security, JWT (JSON Web Tokens) |
| **Base de Données** | MySQL, Spring Data JPA |
| **Communication Temps Réel**| WebSockets, Spring WebFlux |
| **Documentation API** | Swagger / OpenAPI 3 |
| **Génération PDF** | OpenPDF, OpenHTMLToPDF |
| **Utilitaires** | Lombok, MapStruct, JavaMailSender, Thymeleaf |

## 🏗️ Architecture du Projet

Le projet suit une architecture MVC (Modèle-Vue-Contrôleur) orientée domaine pour garantir la maintenabilité et la scalabilité :

```text
src/main/java/com/example/taximotoapp_backend/
├── Admin/               # Fonctionnalités et Dashboard Administrateur
├── Authentification/    # Gestion des logins, signups et sécurité JWT
├── User/                # Gestion des profils (Passagers et Chauffeurs)
├── trajet/              # Logique de réservation et cycle de vie des courses
├── paiement/            # Wallets, Cartes et facturation
├── location/            # Tracking GPS des véhicules
├── chat/                # Messagerie instantanée Passager/Chauffeur
├── ai/ & nlpVocale/     # Fonctionnalités d'Intelligence Artificielle et Voix
├── reclamation/         # Gestion des plaintes
└── Evaluation/          # Système de notation
```

## 🚀 Prérequis

Avant de lancer le projet, assurez-vous d'avoir installé les éléments suivants :

*   **Java Development Kit (JDK) 17**
*   **Maven** (ou utilisez le wrapper `mvnw` inclus)
*   **MySQL Server** en cours d'exécution

## ⚙️ Installation & Lancement

1.  **Cloner le dépôt :**
    ```bash
    git clone https://github.com/votre-utilisateur/Moto-taxi-App-Backend.git
    cd Moto-taxi-App-Backend
    ```

2.  **Configuration de la Base de Données :**
    Créez une base de données MySQL. Puis, mettez à jour le fichier `src/main/resources/application.properties` avec vos identifiants :
    ```properties
    spring.datasource.url=jdbc:mysql://localhost:3306/votre_nom_de_bdd
    spring.datasource.username=votre_utilisateur
    spring.datasource.password=votre_mot_de_passe
    ```

3.  **Compiler et lancer l'application :**
    ```bash
    # Sous Windows
    mvnw.cmd spring-boot:run
    
    # Sous Linux/Mac
    ./mvnw spring-boot:run
    ```

4.  **Accéder à l'API :**
    Le serveur démarrera par défaut sur le port `8080`.
    L'interface Swagger pour explorer et tester les endpoints est disponible sur :
    `http://localhost:8080/swagger-ui.html`

## 📡 Endpoints Principaux (Exemples)

Voici un aperçu des routes disponibles (consultez Swagger pour la documentation complète) :

*   `POST /api/auth/login` : Authentification et récupération du token JWT
*   `POST /api/auth/register` : Création d'un nouveau compte
*   `GET /api/trajets/` : Liste des trajets disponibles
*   `POST /api/trajets/reserver` : Créer une nouvelle course
*   `POST /api/location/update` : Mise à jour de la position du chauffeur
*   `POST /api/paiement/process` : Traitement d'un paiement

## 📸 Captures d'écran

*(Ajoutez ici des captures d'écran de Swagger, de la structure de la BDD ou du Dashboard Admin Frontend connecté)*

| Swagger UI | Base de données / Architecture |
| :---: | :---: |
| <img src="[URL_IMAGE_SWAGGER]" width="400" alt="Swagger UI"/> | <img src="[URL_IMAGE_BDD]" width="400" alt="Architecture BDD"/> |

## 👥 Auteurs

*   **[Votre Nom/Pseudo]** - *Développeur Backend* - [Lien vers votre GitHub](https://github.com/votre-utilisateur)

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE.md](LICENSE.md) pour plus de détails.
