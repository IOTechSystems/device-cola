FROM alpine:3.4
MAINTAINER Cloud Tsai <Cloud.Tsai@Dell.com>

RUN apk --update add openjdk8-jre

# environment variables
ENV APP_DIR=/edgex/device-cola
ENV APP=device-cola.jar
ENV APP_PORT=49977

#copy JAR and property files to the image
COPY *.jar $APP_DIR/$APP
COPY *.properties $APP_DIR/

#expose core data port
EXPOSE $APP_PORT

#set the working directory
WORKDIR $APP_DIR

#kick off the micro service
ENTRYPOINT java -jar -Djava.security.egd=file:/dev/urandom -Xmx100M $APP
