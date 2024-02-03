FROM tomcat:latest
ARG WAR_FILE=build/libs/petDoctor-0.0.1-SNAPSHOT.war
COPY ${WAR_FILE} /usr/local/tomcat/webapps/ROOT.war
CMD ["catalina.sh", "run"]

