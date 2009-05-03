Instale as bibliotecas localmente para fazer o empacotamento pelo maven.
Para instalar basta executar estes comandos a partir desta pasta:

mvn install:install-file -DgroupId=att.grapa -DartifactId=att-grappa -Dversion=1.2 -Dpackaging=jar -Dfile=lib/grappa1_2.jar
mvn install:install-file -DgroupId=exehda -DartifactId=exehda-core -Dversion=0.0.2006-02-09 -Dpackaging=jar -Dfile=exehda/lib/exehda-core.jar
mvn install:install-file -DgroupId=edu.berkley.guir.prefuse -DartifactId=berkley-prefuse -Dversion=0.0.2006-02-09 -Dpackaging=jar -Dfile=lib/prefuse.jar
mvn install:install-file -DgroupId=org.ggf.drmaa -DartifactId=jdrmaa -Dversion=0.0.2006-02-09 -Dpackaging=jar -Dfile=lib/jdrmaa.jar

Não sei a versão do exehda-core, prefuse, nem do jdrmaa por isso deixei 0.0.2006-02-09.

Para empacotar apenas, use "mvn package".
Para disponibilizar no exehda pode ser usado "mvn pre-integration-test",
"mvn integration-test", ou "mvn install".
