FROM tiegris/konstrainer-agent:stage1

RUN apt update && apt install -y ca-certificates && apt clean

COPY ./startup.sh /startup.sh
ENTRYPOINT [ "/startup.sh" ]
#ENTRYPOINT [ "sleep", "infinity" ]
CMD [ "java", "-cp", "@/app/jib-classpath-file", "me.btieger.ApplicationKt" ]
