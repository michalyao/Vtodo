# Vtodo
[![Build Status](https://travis-ci.org/michalyao/Vtodo.svg?branch=master)](https://travis-ci.org/michalyao/Vtodo)


A backend for todo applicaiton built by vert.x. 

## Build

``` shell
git clone https://github.com/michalyao/Vtodo.git

cd Vtodo

./gradlew
```
## Run

``` shell
cd build/libs

# need redis service. see config/config.json for detail.
java -jar vtodo-fat.jar
```

## Test
Open the browser and test the api.

Or import the vtodo.yaml to Postman.

**Remember to replace the host in the yaml file**

## API
See [vtodo.yaml](./vtodo.yaml)

## Build With Docker

#### Build DIY
``` shell
docker build -t "michalix/vtodo" .
```

#### Pull From Dockerhub
``` shell
docker pull michalix/vtodo # use redis default config.
```

#### Run Container
``` shell
## start redis service
docker run -p 6379:6379 --name redis -d redis
## link the container
docker run -p 8888:8888 --link redis:db -name vtodo -d michalix/vtodo
```
