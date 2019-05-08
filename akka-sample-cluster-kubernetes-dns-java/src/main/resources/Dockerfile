FROM openjdk:8-jdk-alpine
# FROM alpine
COPY . /bin
COPY . /lib

EXPOSE 8080
EXPOSE 8558
EXPOSE 2552
ENTRYPOINT exec /bin/bin/akka-sample-cluster-kubernetes-dns-java
# For Spring-Boot project, use the entrypoint below to reduce Tomcat startupLocalCluster time.
#ENTRYPOINT /bin/sh
#ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar akka-kubernetes-sample-1.0-snapshot.jar
