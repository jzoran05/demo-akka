FROM openjdk:8-jdk-alpine
# FROM alpine
COPY . /bin
COPY . /lib

#WORKDIR /bin
ENTRYPOINT exec /bin/bin/akka-kotlin-sample 2551
# For Spring-Boot project, use the entrypoint below to reduce Tomcat startup time.
#ENTRYPOINT /bin/sh
#ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar akka-kubernetes-sample-1.0-snapshot.jar
