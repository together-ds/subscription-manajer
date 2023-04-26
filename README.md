# subscription-manajer
A clash subscription manager app. 

start with docker
```shell
docker stop manajer
docker rm manajer
docker run -d \
-e JAVA_OPTS="-Xmx64m -XX:MaxMetaspaceSize=64m -Xss1m -XX:+UseCompressedClassPointers " \
--name manajer \
-p 8080:8080 \
-v /etc/manajer:/config \
--restart unless-stopped \
togetherds/manajer
```


```shell
./mvnw clean && ./mvnw -DskipTests=true package &&\
docker buildx build --platform linux/arm64/v8,linux/amd64 -t togetherds/manajer:v0.1 .  --push
```
