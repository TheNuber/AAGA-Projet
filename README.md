# AAGA-Projet (Java pur)

Ce projet implémente l’algorithme de Girvan–Newman en Java « pur » (sans framework, ni bibliothèques runtime externes). Il inclut une CLI, le calcul de la modularité Q, et une structure de graphe minimale.

Contenu
- Implémentation Girvan–Newman standard: `src/main/java/com/thenuber/aaga/GirvanNewman.java`
- Modularity Q: `src/main/java/com/thenuber/aaga/Modularity.java`
- Loader edge-list: `src/main/java/com/thenuber/aaga/GraphLoader.java`
- Graphe minimal: `src/main/java/com/thenuber/aaga/SimpleGraph.java`, `Edge.java`
- CLI: `src/main/java/com/thenuber/aaga/Main.java`
- Dataset exemple: `data/sample.edgelist`

Compilation (Windows, cmd.exe)
```
mvn -DskipTests package
```

Exécution (exemple)
```
java -jar target\aaga-projet-0.1.0-SNAPSHOT.jar -i data\sample.edgelist -o results\sample
```

Options CLI
- `-i` chemin du fichier edge-list (2 colonnes par ligne)
- `-d` regex du délimiteur (défaut: `\t`)
- `-o` préfixe de sortie (défaut: `out`)
- `-a` algorithme: `gn` (défaut)

Prochaines étapes
- Variantes d’accélération: approximation par échantillonnage, recomputation périodique/incrémentale.
- Mesure systématique du temps et de la modularité sur jeux de données réels.
- Rapport détaillant méthodologie, complexité et résultats.












To execute it :

mvn -DskipTests package



java -jar target\aaga-projet-0.1.0-SNAPSHOT.jar -i data\sample.edgelist -o results\sample



hello