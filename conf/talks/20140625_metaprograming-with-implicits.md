---
title = Metaprogramming with implicits
speaker = Barnabas Kralik
meetupEventId = 183865162
meetupMemberId = 111872622
tags = "Scala, implicit“
---
Scala's type inference algorithm tries hard to automatically infer generic type parameters' actual values. If there are implicit parameters to a function that depend on yet underspecified type parameters, it can use this as a source of type constraints. This yields a variant of logic programming on types.

In this lightning talk, I will show the main powers and limitations of this method of metaprogramming through a series of short examples.