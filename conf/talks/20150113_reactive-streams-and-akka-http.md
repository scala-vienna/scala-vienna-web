---
title = "Reactive Streams & Akka HTTP"
speaker = Mathias Doenitz
twitter = sirthias
meetupEventId = 218798044
homepage = "http://decodified.com"
slides = "http://spray.io/vienna/"
tags = "Scala,Reactive Streams,Akka,spray,Akka HTTP,Akka Streams"
---
If you want to provide or consume HTTP-based APIs with Scala (or Java) then this talk is for you: Akka HTTP takes reactive high-performance web interactions to the next level. Fully asynchronous and non-blocking, a type-safe and very powerful declarative API, an immutable HTTP model without compromises, all these are perfectly fitted to the Actor world.

Among many smaller improvements over its predecessor library [spray.io](http://spray.io), akka-http comes with one major advantage: it is based on a new abstraction called "[Reactive Streams](http://www.reactive-streams.org)” which provides a number of important benefits, not only for working with HTTP.

In this session we’ll first take a closer look into the reactive streams effort, the problems it solves and what programming with reactive streams looks like.

After a short break we’ll then turn to akka-http, how it is used, what improvements it brings over spray and why you will not want to use anything else after having tried it. And of course I'll share the latest roadmap up until making akka-http the new "driver" layer for the Play framework.