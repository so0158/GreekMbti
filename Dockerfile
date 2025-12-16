# 1) 빌드 단계: JDK 17 + Gradle로 빌드
FROM gradle:8.7-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test

# 2) 실행 단계: 가벼운 JRE 17에서 실행
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Render는 PORT 환경변수를 줌. Spring이 그 포트로 떠야 함.
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh","-c","java -Dserver.port=${PORT} -jar app.jar"]
