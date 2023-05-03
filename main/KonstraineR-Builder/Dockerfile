FROM openjdk:17-bullseye AS Final
RUN apt update && apt install -y python3-pip && apt clean
RUN pip3 install requests

COPY KonstraineR-Builder/framework /app/framework
COPY KonstraineR-Builder/builder.py /app/builder.py

WORKDIR /app
#ENTRYPOINT [ "sleep", "infinity" ]
ENTRYPOINT [ "python3", "/app/builder.py" ]
