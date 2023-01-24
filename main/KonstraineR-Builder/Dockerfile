FROM gradle:7.6-jdk17-jammy AS BuildDsl
COPY Konstrainer-Dsl/ /app
WORKDIR /app
RUN ./gradlew build


FROM gradle:7.6-jdk17-jammy AS Final

COPY KonstraineR-Builder/framework /app/framework
COPY --from=BuildDsl /app/framework/build/libs/*.jar /app/dsl.jar
COPY KonstraineR-Builder/builder.py /app/builder.py

WORKDIR /app
ENTRYPOINT [ "/app/builder.py" ]
