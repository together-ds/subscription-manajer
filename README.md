# subscription-manajer
A clash subscription manager app. 

```shell
docker run -d \
 --name manajer \
 -p 8080:8080 \
 -v /etc/manajer:/config \
 --restart unless-stopped \
 together/manajer:v1
```

```shell
./mvnw clean && ./mvnw -DskipTests=true package &&\
docker buildx build --platform linux/arm64/v8,linux/amd64 -t togetherds/manajer:v0.1 .  --push
```