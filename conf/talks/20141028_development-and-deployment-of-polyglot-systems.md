---
title = "Development and Deployment of Polyglot Systems"
speaker = David Pichsenmeister
twitter = 3x14159265
meetupEventId = 212002172
meetupMemberId = 92597072
homepage = "http://www.pichsenmeister.com"
slides = "http://www.slideshare.net/3x14159265/development-and-deployment-of-polyglot-systems"
tags = "Scala,Play Framework,Slick"
---
At [orat.io](https://www.orat.io/) we are developing a comment plugin for online bloggers and publishers. Since the uptime of our software is very important, we try to apply best practices to our development and deployment workflow. Our system is based on different stacks, which includes the use of different languages like PHP, Scala, and TypeScript. This talk is about how we manage the consistency of our data-models through the different stacks, how the connection of our services is done through APIs and the deployment strategy of our system to get a nearly zero-downtime continuous integration pipeline. I'll also show, how we use code generators and shell scripts to automate code creation and tasks. Last, I'll show how we handle our database migrations. 