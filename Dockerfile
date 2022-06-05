FROM konradboniecki/budget:java-base-image-latest
ENV JAVA_TOOL_OPTIONS \
    "-Dspring.config.import=configserver:http://config-server:8888" \
    "-Dspring.cloud.config.username=${CONFIG_SERVER_BA_USERNAME}" \
    "-Dspring.cloud.config.password=${CONFIG_SERVER_BA_PASSWORD}"
ARG ARTIFACT=mvc-*.jar
ADD /target/$ARTIFACT app.jar
ENTRYPOINT [ "java", "-jar", \
    "-Djava.security.egd=file:/dev/./urandom ", "app.jar", \
    "-Dencrypt.key=${CONFIG_ENCRYPT_KEY}", \
    "--spring.profiles.active=default" \
]
