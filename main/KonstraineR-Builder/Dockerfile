FROM tiegris/konstrainer-agent:stage1 AS Final
RUN apt update && apt install -y python3-pip && apt clean
RUN pip3 install requests

COPY KonstraineR-Agent /app/compiler
COPY KonstraineR-Builder/builder.py /app/builder.py

WORKDIR /app
#ENTRYPOINT [ "sleep", "infinity" ]
ENTRYPOINT [ "python3", "/app/builder.py" ]
