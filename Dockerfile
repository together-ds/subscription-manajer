FROM azul/zulu-openjdk:17
VOLUME [/tmp , /config]
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT [\
"java",\
"${JAVA_OPTS}"\
"-jar",\
"/app.jar",\
"--spring.config.name=application",\
"--spring.config.location=classpath:/application.yaml,file:/config/manajer.yaml"\
]
