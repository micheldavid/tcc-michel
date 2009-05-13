Instale as bibliotecas localmente para fazer o empacotamento pelo maven.
Para instalar basta executar este comando a partir desta pasta:

mvn install:install-file -DgroupId=exehda -DartifactId=exehda-core -Dversion=0.0.2006-02-09 -Dpackaging=jar -Dfile=lib/exehda-core.jar

Não sei a versão do exehda-core por isso deixei 0.0.2006-02-09.

Para empacotar apenas, use "mvn package".
Para disponibilizar no exehda pode ser usado "mvn pre-integration-test",
"mvn integration-test", ou "mvn install".
