FROM openjdk:15-jdk-alpine

RUN apk --no-cache add curl

ARG MAVEN_VERSION=3.6.3
ARG SHA=c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0
ARG BASE_URL=https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries
RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
    && echo "Downloading maven" \
    && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    \
    && echo "Checking download hash" \
    && echo "${SHA}  /tmp/apache-maven.tar.gz" | sha512sum -c - \
    \
    && echo "Unziping maven" \
    && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
    \
    && echo "Cleaning and setting links" \
    && rm -f /tmp/apache-maven.tar.gz \
    && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG /root/.m2

RUN mkdir -p /starwarsdb
RUN ln -s /starwarsdb ~/starwarsdb

COPY ./ /root/starwars-search
RUN mvn clean package -f /root/starwars-search/pom.xml -DskipTests

RUN cp /root/starwars-search/target/*.jar /starwars-search.jar

RUN rm -rf /root/.m2
RUN rm -rf /root/starwars-search
RUN rm -rf /starwarsdb/*

ENTRYPOINT ["java","-jar","/starwars-search.jar"]