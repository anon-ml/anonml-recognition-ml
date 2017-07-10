# anonml-germaner-rec

calling germaner, manage temporary blacklists, retrain germaner over night


# Set Up 

1. Clone https://github.com/tudarmstadt-lt/GermaNER and run "mvn clean install -Drat.skip=true"

2. Clone https://github.com/seyyaw/cleartk and run "clean install -Dmaven.test.skip=true"

3. Place the data.zip (contains feature files) in ./src/main/resources/GermaNER
   (data.zip: https://github.com/tudarmstadt-lt/GermaNER/releases/download/germaNER0.9.1/data.zip)
   
# Retrain

The training file needs to be in the resources folder (named "trainingsFile.txt")
https://docs.docker.com/engine/userguide/storagedriver/selectadriver/#other-considerations