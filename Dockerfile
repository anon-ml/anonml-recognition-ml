FROM linuxbrew/linuxbrew

ARG user
ARG branch

USER root

RUN apt-get update
RUN apt-get -y upgrade

RUN apt-get install -y --no-install-recommends bsdtar
RUN export tar='bsdtar'

# JAVA
RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
RUN apt-get install -y oracle-java8-installer

# MONGO DB
RUN apt-get install -y dirmngr
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.0.list
RUN apt-get update
RUN mkdir -p /data/db
RUN apt-get install -y  mongodb-org
EXPOSE 27017


# UTIL
RUN apt-get install -y git maven wget liblbfgs-dev tar build-essential

RUN git clone https://${user}@github.com/anon-ml/anonml-core.git
RUN cd anonml-core && git checkout ${branch} &&  mvn clean install -q -DskipTests


#ML
RUN git clone https://github.com/tudarmstadt-lt/GermaNER.git
RUN ls -lh
RUN cd GermaNER/germaner && mvn clean install -q -Drat.skip=true

RUN git clone https://github.com/seyyaw/cleartk.git
RUN cd cleartk && mvn clean install -q -Drat.skip=true -Dcleartk.skipTests=svmlight -DskipTests

USER linuxbrew
RUN brew tap homebrew/science
RUN brew install crfsuite
USER root

#RUN echo "..........."
#RUN git clone https://${user}@github.com/anon-ml/anonml-recognition-ml.git


#RUN cd anonml-recognition-ml/service/src/main/resources/GermaNER && wget https://github.com/tudarmstadt-lt/GermaNER/releases/download/germaNER0.9.1/data.zip

#RUN touch anonml-recognition-ml/service/src/main/resources/GermaNER/temp-file-to-annotate.txt
#RUN cd anonml-recognition-ml && git checkout ${branch} && mvn clean install -q -DskipTests
RUN echo "here6"
COPY /service/target/recognition-machinelearning-service-0.0.1-SNAPSHOT.jar /

RUN mkdir model
RUN mv anonml-recognition-ml/service/src/main/resources/GermaNER/ model/



EXPOSE 9003

COPY start.sh /
RUN ls -lh
ENTRYPOINT /start.sh