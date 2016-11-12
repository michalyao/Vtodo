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
