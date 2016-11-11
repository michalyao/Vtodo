FROM java

ENV VERTICLE_FILE vtodo-fat.jar

ENV VERTICLE_HOME /opt/verticles

ENV BUILD_DIR /tmp/build/vtodo

EXPOSE 8888

RUN mkdir -p /tmp/build/vtodo && mkdir -p /opt/verticles
ADD ./ /tmp/build/vtodo

WORKDIR $BUILD_DIR

RUN cd /tmp/build/vtodo && chmod +x gradlew \
    && ./gradlew build && mv build/libs/vtodo-fat.jar /opt/verticles/

ENTRYPOINT ["sh", "-c"]

CMD ["java -jar $VERTICLE_HOME/VERTICLE_FILE"]
