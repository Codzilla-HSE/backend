# Запуск
для того , чтобы запустить используй : 

mac/linux : 
```shell 
 ./gradlew clean build -x test  
 docker-compose up -d     
  ./gradlew bootRun     
```

windows : 
```shell
gradlew.bat clean build -x test
docker-compose up -d
gradlew.bat bootRun
```

ps : чтобы остановить контейнер используй : 
```shell
docker-compose down -v
```