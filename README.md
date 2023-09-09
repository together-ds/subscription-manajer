# subscription-manajer
A clash subscription manager app. 

start with docker
```shell
docker stop manajer && docker rm manajer
docker pull togetherds/manajer:latest

docker run --user $(id -u):$(id -g) -d \
--name manajer \
-p 8080:8080 \
-v ~/manajer:/config \
--restart unless-stopped \
togetherds/manajer:latest --spring.config.name=application --spring.config.location=classpath:/application.yaml,file:/config/manajer.yaml

docker logs -f manajer
```

start with docker(native-image)

```shell
docker stop manajer0
docker rm manajer0
docker pull togetherds/manajer:native-latest

docker run  --user $(id -u):$(id -g)  -d \
-m 128m \
--name manajer0 \
-p 8080:8080 \
-v ~/manajer:/config \
--restart unless-stopped \
togetherds/manajer:native-latest --spring.config.name=application --spring.config.location=classpath:/application.yaml,file:/config/manajer.yaml
```

```shell
./mvnw clean && ./mvnw -DskipTests=true package &&\
docker buildx build --platform linux/arm64/v8,linux/amd64 -t togetherds/manajer .  --push
```
 
