FROM openjdk:21-jdk-slim

WORKDIR /app

# JAR 파일 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]