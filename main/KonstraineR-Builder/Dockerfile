FROM tiegris/konstrainer-agent:stage1 AS Final

COPY KonstraineR-Builder/framework /app/framework
WORKDIR /app/framework
RUN ./gradlew jar

COPY KonstraineR-Builder/builder.sh /app/builder.sh
WORKDIR /app
#ENTRYPOINT [ "sleep", "infinity" ]
ENTRYPOINT [ "/app/builder.sh" ]
