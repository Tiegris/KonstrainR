FROM openjdk:17-bullseye AS BuildDsl
COPY Konstrainer-Dsl/ /app
WORKDIR /app
RUN ./gradlew build


FROM openjdk:17-bullseye AS Final
RUN apt update && apt install -y python3-pip && apt clean
RUN pip3 install requests

COPY KonstraineR-Builder/framework /app/framework
COPY --from=BuildDsl /app/build/libs/*.jar /app/dsl.jar
COPY KonstraineR-Builder/builder.py /app/builder.py

WORKDIR /app
#ENTRYPOINT [ "sleep", "infinity" ]
ENTRYPOINT [ "python3", "/app/builder.py" ]
