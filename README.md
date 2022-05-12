# Component Name

An SQS-driven lambda. This generic project is purposefully designed for new Lambda components to be produced (generated in GitHub) from sqs-lambda-hello-world.

## Inputs

An sqs message (possibly containing multiple events)
- a just-configuration json file 
- environment variables (defined as part of the stack)

## Outputs 
- aws log comments
- ispy messages
- sns message written to topic

## Build

```
./build
```

(The slight hello-world / helloworld naming confusion is due to registering the project as a cosmos service before realising it should be a cosmos lambda. I've renamed all the build and packaging aspects to use the newer helloworld, but the github repo and actual source is still hello-world)

## Deploying

Use Jenkins to build and deploy to INT, promote to TEST and LIVE via cosmos console.

HOWEVER, when deploying FOR THE FIRST TIME it will use a zip file containing a dummy Lambda.
You must deploy a proper release via Jenkins / Cosmos before using this stack.

### Deploying Manually (Don't do this)


The Makefile defines a deploy-int target which copies the test just-config file and uploads the build.zip file. 
it also prints the S3 path (useful if updating the lambda via lambda console, see below)
and the CODE\_KEY (which you'll need when deploying the component stack)

go to amazon console, lambda, choose the correct lambda.

in "Function Code" box
choose "Upload a File from Amazon S3"
in the "S3 Link URL" box enter the S3 path printed by the make deploy-int stage
press "Save" at the top

create an alias if required (Jenkins / Cosmos does this)

now you can use ./sendMessage.sh to send the lambda a message (but make sure the queue is correct beforehand)

## Stacks

1. `poetry install`
2. `bundle install`
3. `poetry run bundle exec modav-spud prepare int`

(command replaces the need to create yet another virtual env... though
you can use `clean-modav-spud`, if you want to :)

4. `poetry run bundle exec modav-spud apply int`

## Diagnostics

splunk events start with "sqs-lambda-hello-world"

logs are on the "Monitoring" tab of the lambda page or in "Cloud Watch - Log Groups" (filter "/aws/lambda/IntModavSqsLambdaHello")

it's easiest to find the log you want from the splunk message which contains the log stream and the aws request id
(if searching for the request id use only the first 8 characters because the hyphens confuse it)
