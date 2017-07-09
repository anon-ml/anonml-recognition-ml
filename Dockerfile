FROM ubuntu:17.04

ARG user

RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get install -y openjdk-8-jdk
RUN apt-get install -y git
RUN apt-get install -y maven
RUN apt-get install unzip
ARG user



RUN git clone https://github.com/tudarmstadt-lt/GermaNER.git
RUN ls -lh
RUN cd GermaNER/germaner && mvn clean install -Drat.skip=true

RUN git clone https://github.com/seyyaw/cleartk.git
RUN cd cleartk && mvn clean install -Drat.skip=true -Dcleartk.skipTests=svmlight -DskipTests

RUN git clone https://${user}@github.com/anon-ml/anonml-core.git
RUN cd anonml-core && mvn clean install -DskipTests


RUN git clone https://${user}@github.com/anon-ml/anonml-recognition-ml.git
RUN apt-get install wget

RUN cd anonml-recognition-ml && cd src/main/resources/GermaNER && wget https://github.com/tudarmstadt-lt/GermaNER/releases/download/germaNER0.9.1/data.zip && unzip data.zip && pwd && ls -lh
RUN touch anonml-recognition-ml/src/main/resources/GermaNER/temp-file-to-annotate.txt
RUN cd anonml-recognition-ml && mvn clean install -DskipTests



EXPOSE 9003

COPY start.sh /
ENTRYPOINT /start.sh

