# Build

gradle build --info

# Deploy

```shell
# local; build container
docker build -f Dockerfile -t schedule-bot:latest .
docker tag schedule-bot:latest myregistry.com/schedule-bot:latest
docker push myregistry.com/schedule-bot:latest

# on server; pull and start container
docker ps | grep schedule-bot | awk '{print $1}' | xargs docker stop || true
docker pull myregistry.com/schedule-bot:latest
docker run -d --restart always -p 30170:8080 --mount source=sqlite,target=/appl/db --env-file=schedule-bot.env myregistry.com/schedule-bot:latest
docker ps | grep schedule-bot | awk '{print $1}' | xargs docker logs -f || true
# expects a schedule-bot.env file with the DISCORD_TOKEN variable
```
