FROM linuxbrew/linuxbrew

ARG user
USER root
RUN apt-get update
RUN apt-get -y upgrade
RUN apt-get install -y software-properties-common
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
RUN apt-get install -y oracle-java8-installer
RUN apt-get install -y git
RUN apt-get install -y maven
ARG user



RUN git clone https://github.com/tudarmstadt-lt/GermaNER.git
RUN ls -lh
RUN cd GermaNER/germaner && mvn clean install -Drat.skip=true

RUN git clone https://github.com/seyyaw/cleartk.git
RUN cd cleartk && mvn clean install -Drat.skip=true -Dcleartk.skipTests=svmlight -DskipTests

#RUN git clone https://${user}@github.com/anon-ml/anonml-core.git
#RUN cd anonml-core && mvn clean install -DskipTests

#RUN git clone https://${user}@github.com/anon-ml/anonml-recognition-ml.git
#RUN cd anonml-recognition-ml && git checkout dockerize

RUN apt-get install wget

#RUN cd anonml-recognition-ml && cd src/main/resources/GermaNER && wget https://github.com/tudarmstadt-lt/GermaNER/releases/download/germaNER0.9.1/data.zip
#RUN touch anonml-recognition-ml/src/main/resources/GermaNER/temp-file-to-annotate.txt
#RUN cd anonml-recognition-ml && mvn clean install -DskipTests

RUN apt-get install -y liblbfgs-dev
RUN apt-get install -y tar
RUN apt-get install -y build-essential
#RUN apt-get update && apt-get install -y curl file g++ git make ruby2.3 ruby2.3-dev uuid-runtime && ln -sf ruby2.3 /usr/bin/ruby  && ln -sf gem2.3 /usr/bin/gem

USER linuxbrew
#RUN ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Linuxbrew/install/master/install)"
#RUN PATH="/home/linuxbrew/.linuxbrew/bin:$PATH"
#RUN echo 'export PATH="/home/linuxbrew/.linuxbrew/bin:$PATH"' >>~/.bash_profile
RUN brew tap homebrew/science
RUN brew install crfsuite
#RUN wget https://github.com/downloads/chokkan/crfsuite/crfsuite-0.12.tar.gz && tar xvzf crfsuite-0.12.tar.gz && cd crfsuite-0.12 && ./configure && make && make install

RUN echo "12"
COPY /target/recognition-ml-0.0.1-SNAPSHOT.jar anonml-recognition-ml/target/

EXPOSE 9003

COPY start.sh /
ENTRYPOINT /start.sh

