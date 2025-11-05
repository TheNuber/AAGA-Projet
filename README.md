## AAGA-Projet — Girvan–Newman & Betweenness Sampling Algorithm (Java)

Ce projet implémente :

* **L’algorithme de Girvan–Newman** pour la détection de communautés dans un graphe,
* **Une version optimisée** appelée *Betweenness Sampling Algorithm* (BSA), qui estime la centralité d’intermédiarité sur un échantillon de nœuds pour accélérer le calcul,
* **Un calculateur de modularité**,
* **Une interface CLI** (ligne de commande) permettant d’exécuter et de comparer les deux approches.

Maven est utilisé uniquement pour la compilation et les tests unitaires.

---

### 📂 Structure du projet

| Dossier / Fichier                 | Description                                |
| --------------------------------- | ------------------------------------------ |
| `src/main/java/com/thenuber/aaga` | Code source principal                      |
| `src/test/java`                   | Tests unitaires                            |
| `data/sample.edgelist`            | Exemple de graphe (petit dataset)          |
| `Makefile`                        | Automatisation (Linux/macOS)               |
| `run.bat`                         | Automatisation (Windows)                   |
| `results/`                        | Dossier de sortie (généré après exécution) |

---

### ⚙️ Prérequis

* **Java 17+** installé et accessible via `java -version`
* **Maven 3+** installé et accessible via `mvn -version`
* (Optionnel) **Make** installé pour Linux/macOS
* (Optionnel) **Git Bash ou PowerShell** sous Windows

---

### 🚀 Compilation rapide

#### 🔸 Avec Maven (recommandé)

```bash
mvn -DskipTests package
```

#### 🔸 Avec Makefile (Linux/macOS)

```bash
make package
```

#### 🔸 Avec le script Windows

```bash
run.bat package
```

---

### ▶️ Exécution du programme

#### 1. **Girvan–Newman standard**

Exécute l’algorithme original sur le petit graphe `data/sample.edgelist` :

```bash
java -jar target/aaga-projet-0.1.0-SNAPSHOT.jar -i data/sample.edgelist -o results/newman
```

ou simplement :

```bash
make run-newman        # sous Linux/macOS
run.bat run-newman     # sous Windows
```

#### 2. **Betweenness Sampling Algorithm (BSA)**

Exécute la version optimisée par échantillonnage :

```bash
java -jar target/aaga-projet-0.1.0-SNAPSHOT.jar -i data/sample.edgelist -o results/sample -a bsa
```

ou :

```bash
make run-sample        # sous Linux/macOS
run.bat run-sample     # sous Windows
```

Le paramètre `-a bsa` indique au programme d’utiliser la version optimisée.

---

### 🧹 Nettoyage du projet

Pour supprimer les fichiers générés et le dossier `results/` :

```bash
make clean        # sous Linux/macOS
run.bat clean     # sous Windows
```

---

### 🧪 Tests unitaires

Exécution des tests Maven :

```bash
mvn test
```

ou

```bash
make test
```

---

### 💡 Notes pour l’évaluateur

* Les scripts (`Makefile` et `run.bat`) couvrent tous les cas d’usage : **compilation, exécution, test, et nettoyage**.
* Aucun prérequis externe n’est nécessaire en dehors de **Java** et **Maven**.
* Les résultats (communautés détectées, modularité, etc.) seront générés dans le dossier `results/`.
* Le projet peut être testé sur des graphes plus grands en remplaçant le chemin d’entrée via l’option `-i`, par exemple :

  ```bash
  java -jar target/aaga-projet-0.1.0-SNAPSHOT.jar -i data/facebook_combined.txt -o results/facebook -d " "
  ```

---

### 📘 Exemple d’utilisation complète (Windows)

```bash
run.bat package
run.bat test
run.bat run-sample
run.bat run-newman
```


### 🧩 Auteurs

Projet développé dans le cadre du cours **AAGA**

