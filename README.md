# Component Name

An SQS-driven lambda to set the legid via the Ribbon API.

## Inputs

An sqs message from event bridge
- environment variables (defined as part of the stack)

## Outputs 
- aws log comments
- ispy messages

## Build

```
./build
```

## Deploying

Use Jenkins to build and deploy to INT, promote to TEST and LIVE via cosmos console.

## Stacks

1. `poetry install`
2. `bundle install`
3. `poetry run bundle exec modav-spud prepare int`

(command replaces the need to create yet another virtual env... though
you can use `clean-modav-spud`, if you want to :)

4. `poetry run bundle exec modav-spud apply int`

## Diagnostics

splunk events start with "lsp-legid-setter"

logs are on the "Monitoring" tab of the lambda page or in "Cloud Watch - Log Groups" (filter "/aws/lambda/IntModavLspLegidSetter")

it's easiest to find the log you want from the splunk message which contains the log stream and the aws request id
(if searching for the request id use only the first 8 characters because the hyphens confuse it)
