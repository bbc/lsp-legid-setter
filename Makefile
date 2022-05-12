.PHONY: default zip deploy-int

default:
	@echo "This Makefile does nothing by default.  You must specify a specific goal."
	@exit 2

clean:
	-rm ./target/sqs-lambda-helloworld*.jar
	-rm ./build.zip
	-rm ./test-classes.zip

jar:
	mvn clean install

./target/sqs-lambda-helloworld-%-SNAPSHOT.jar: $(wildcard src/*/*/*/*/*/sqs_lambda_hello_world/*.java)
	mvn package

zip: 
	cp ./target/sqs-lambda-helloworld*SNAPSHOT.jar ./build.zip
	cp ./target/sqs-lambda-helloworld*SNAPSHOT-fat-tests.jar ./test-classes.zip

build.zip: $(wildcard ./target/sqs-lambda-helloworld-*-SNAPSHOT.jar)
	cp ./target/sqs-lambda-helloworld*SNAPSHOT.jar ./build.zip
	cp ./target/sqs-lambda-helloworld*SNAPSHOT-fat-tests.jar ./test-classes.zip

test: build.zip
	with-aws modav-tmp ./component-test/cucumber/test

deploy-int: build.zip
	wormhole modav-development \
		aws s3 cp \
		./build.zip \
		s3://intmodavsharedresources-configurationbucket-1cimkikp09qru/lambda/sqs-lambda-helloworld/sqs-lambda-helloworld-0.0.1.zip
	echo "S3 is intmodavsharedresources-configurationbucket-1cimkikp09qru/lambda/sqs-lambda-helloworld/sqs-lambda-helloworld-0.0.1.zip"
	echo "CODE_KEY is lambda/sqs-lambda-helloworld/sqs-lambda-helloworld-0.0.1.zip"

deploy-test: build.zip
	wormhole modav-development \
		aws s3 cp \
		./build.zip \
		s3://testmodavsharedresources-configurationbucket-1q0wv0c15cah2/lambda/sqs-lambda-helloworld/sqs-lambda-helloworld-0.0.1.zip
	echo "S3 is testmodavsharedresources-configurationbucket-1q0wv0c15cah2/lambda/sqs-lambda-helloworld/sqs-lambda-helloworld-0.0.1.zip"
	echo "CODE_KEY is lambda/sqs-lambda-helloworld/sqs-lambda-helloworld-0.0.1.zip"
