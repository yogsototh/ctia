#!/usr/bin/env zsh

command -v docker-compose >/dev/null 2>&1 || err "please install docker-compose"

print -- "Init dev environment"

command -v docker-machine >/dev/null 2>&1
DOCKER_MACHINE=$?
if $DOCKER_MACHINE; then
  docker-machine start default
  eval $(docker-machine env default)
  DOCKER_IP="$(docker-machine ip)"
else
  DOCKER_IP="localhost"
fi

# Update ctia.properties
cat > resources/ctia.properties <<END
ctia.store.actor=es
ctia.store.feedback=es
ctia.store.campaign=es
ctia.store.coa=es
ctia.store.events=redis
ctia.store.exploit-target=es
ctia.store.identity=es
ctia.store.incident=es
ctia.store.indicator=es
ctia.store.judgement=es
ctia.store.sighting=es
ctia.store.ttp=es
ctia.store.es.uri=http://$DOCKER_IP:9200
ctia.store.redis.uri=redis://$DOCKER_IP:6379
END

docker-compose -f containers/dev/docker-compose.yml build
docker-compose -f containers/dev/docker-compose.yml up

