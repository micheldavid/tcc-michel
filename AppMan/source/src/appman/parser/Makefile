#JAVACC_HOME="../javacc-3.2/bin/"
JAVACC_HOME=""
all:
	${JAVACC_HOME}jjtree SimpleParser.jjt
	${JAVACC_HOME}javacc SimpleParser.jj

tar:
	tar czvf parser.tgz *.jj *.jjt Makefile *.java

