FROM java

ENV VERTICLE_FILE vtodo-fat.jar

ENV VERTICEL_HOME /opt/verticles

ENV BUILD_DIR /tmp/build/vtodo

EXPOSE 8888

RUN mkdir -p /tmp/build/vtodo
ADD ./ /tmp/build/vtodo

WORKDIR $BUILD_DIR

RUN cd /tmp/build/vtodo && chmod +x gradlew && ./gradlew build

COPY build/libs/$VERTICLE_FILE $VERTICLE_HOME/



ENTRYPOINT ["sh", "-c"]

CMD ["java -jar $VERTICLE_FILE"]
