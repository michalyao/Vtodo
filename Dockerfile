FROM java:8u77-jre-alpine

ENV VERTICLE_FILE vtodo-fat.jar

ENV VERTICEL_HOME /opt/verticles

EXPOSE 8888

ADD . /tmp/build/vtodo

RUN cd /tmp/build/vtodo
RUN chmod +x ./gradlew
RUN ./gradlew

COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/

WORKDIR $VERTICLE_HOME

ENTRYPOINT ["sh", "-c"]

CMD ["java -jar $VERTICLE_FILE"]
