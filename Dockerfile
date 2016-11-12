FROM java

MAINTAINER michal<yaoyao0777@gmail.com>

ENV VERTICLE_FILE vtodo-fat.jar

ENV VERTICLE_HOME /opt/verticles

ENV BUILD_DIR /tmp/build/vtodo

EXPOSE 8888

RUN mkdir -p $BUILD_DIR && mkdir -p $VERTICLE_HOME

ADD ./ $BUILD_DIR

WORKDIR $BUILD_DIR

RUN cd $BUILD_DIR && chmod +x gradlew; sync           \
    && ./gradlew build                                \
    && mv build/libs/vtodo-fat.jar $VERTICLE_HOME/    \
    && rm -rf $BUILD_DIR

ENTRYPOINT ["sh", "-c"]

CMD ["java -jar $VERTICLE_HOME/$VERTICLE_FILE"]
