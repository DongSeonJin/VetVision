# 톰캣 베이스 이미지 사용
FROM tomcat:9.0
# 애플리케이션 WAR 파일을 톰캣의 webapps 디렉토리로 복사
COPY build/libs/petDoctor-0.0.1-SNAPSHOT.war /usr/local/tomcat/webapps/app.war
# 톰캣 실행
CMD ["catalina.sh", "run"]
