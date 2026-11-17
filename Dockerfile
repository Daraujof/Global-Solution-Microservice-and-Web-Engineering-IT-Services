# ---------- Etapa 1: build com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagem final só com o JRE ----------
FROM eclipse-temurin:21-jre-alpine
LABEL org.opencontainers.image.title="to-no-direito-api" \
      org.opencontainers.image.description="Tô no Direito - API REST Spring Boot + SQL Server" \
      org.opencontainers.image.source="https://github.com/Enzo-Miletta/to-no-direito-api"
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /build/target/app.jar app.jar
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=America/Sao_Paulo -Djava.security.egd=file:/dev/./urandom"
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
