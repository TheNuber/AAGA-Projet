to compile the project :
mvn -DskipTests package

=> TO RUN THE GIRVAN NEWMAN ALGO :

java -jar target\aaga-projet-0.1.0-SNAPSHOT.jar -i data\sample.edgelist -o results\sample

=> TO RUN BETWENNESS SAMPLING ALGO :

java -jar target\aaga-projet-0.1.0-SNAPSHOT.jar -i data\sample.edgelist -o results\sample -a bsa

