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