.PHONY: default zip deploy-int

default:
	@echo "This Makefile does nothing by default.  You must specify a specific goal."
	@exit 2

clean:
	-rm ./target/lsp-legid-setter*.jar
	-rm ./build.zip
	-rm ./test-classes.zip

jar:
	mvn clean install

zip: 
	cp ./target/lsp-legid-setter*SNAPSHOT.jar ./build.zip
	cp ./target/lsp-legid-setter*SNAPSHOT-fat-tests.jar ./test-classes.zip

build.zip: $(wildcard ./target/lsp-legid-setter-*-SNAPSHOT.jar)
	cp ./target/lsp-legid-setter*SNAPSHOT.jar ./build.zip
	cp ./target/lsp-legid-setter*SNAPSHOT-fat-tests.jar ./test-classes.zip

test: build.zip
	with-aws modav-tmp ./component-test/cucumber/test

deploy-int: build.zip
	wormhole modav-development \
		aws s3 cp \
		./build.zip \
		s3://intmodavsharedresources-configurationbucket-1cimkikp09qru/lambda/lsp-legid-setter/lsp-legid-setter-0.0.1.zip
	echo "S3 is intmodavsharedresources-configurationbucket-1cimkikp09qru/lambda/lsp-legid-setter/lsp-legid-setter-0.0.1.zip"
	echo "CODE_KEY is lambda/lsp-legid-setter/lsp-legid-setter-0.0.1.zip"

deploy-test: build.zip
	wormhole modav-development \
		aws s3 cp \
		./build.zip \
		s3://testmodavsharedresources-configurationbucket-1q0wv0c15cah2/lambda/lsp-legid-setter/lsp-legid-setter-0.0.1.zip
	echo "S3 is testmodavsharedresources-configurationbucket-1q0wv0c15cah2/lambda/lsp-legid-setter/lsp-legid-setter-0.0.1.zip"
	echo "CODE_KEY is lambda/lsp-legid-setter/lsp-legid-setter-0.0.1.zip"
