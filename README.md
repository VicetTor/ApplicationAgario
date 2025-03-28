# 🧬✨ Agario - Projet en JavaFX ✨🧬

Bienvenue dans le projet **Agario** !
Une version inspirée du célèbre jeu **Agario**, développée avec **JavaFX**.
Ce projet a été conçu pour mettre en pratique des concepts de programmation en Java, ainsi que l'utilisation de JavaFX pour créer des interfaces graphiques interactives.

---

## 📝 Description du projet

Le jeu **Agario** est un jeu multijoueur où les joueurs contrôlent une cellule qui grandit en mangeant d'autres cellules plus petites.
Le but du jeu est de devenir la plus grande cellule tout en évitant de se faire manger par d'autres joueurs.

Ce projet propose une version simplifiée du jeu en utilisant **JavaFX** pour l'interface graphique, avec un gameplay à la fois amusant et stratégique.

---

### 🌟 Fonctionnalités principales

- 🕹️ **Gameplay en temps réel :** Les joueurs contrôlent leur cellule avec la souris.
- 🌱 **Mécanisme de croissance :** Les cellules mangent les plus petites cellules pour grandir.
- 🛑 **Éviter les plus grosses cellules :** Les joueurs doivent éviter de se faire manger par des cellules plus grandes.
- 💻 **Interface graphique :** Une interface conviviale utilisant JavaFX pour une expérience utilisateur optimale.

---

## ⚙️ Prérequis

Avant de pouvoir exécuter le projet, assurez-vous d'avoir installé les éléments suivants :

- ☕ **Java 17** ou une version plus récente
- 📦 **JavaFX SDK :** Téléchargez et ajoutez le SDK JavaFX à votre projet.
- 🛠️ **IDE compatible :** Utilisez un IDE comme **IntelliJ IDEA**, **Eclipse**, ou **NetBeans**.

---

## 🚀 Installation

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/votre-utilisateur/agario-javafx.git
   ```
2. Ouvrez le projet dans votre IDE.
3. Ajoutez la bibliothèque JavaFX aux paramètres du projet :
   - Sous IntelliJ : Allez dans **File -> Project Structure -> Libraries** et ajoutez le dossier lib de JavaFX.
   - Sous Eclipse : Allez dans **Build Path -> Configure Build Path -> Libraries -> Add External JARs**.
4. Configurez les arguments VM pour l'exécution :
   ```bash
   --module-path /chemin/vers/javafx/lib --add-modules=javafx.controls,javafx.fxml
   ```
5. Lancez le projet depuis votre IDE.

---

##▶️ Démarrage du jeu

Pour démarrer le jeu, lancez le launcher.

Ensuite, cliquez sur Jouer Local pour une partie en solo ou Jouer en Ligne pour rejoindre un serveur multijoueur.

