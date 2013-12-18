# Scala Vienna UG Webapp

[![Build Status](https://travis-ci.org/scala-vienna/scala-vienna-web.png)](https://travis-ci.org/scala-vienna/scala-vienna-web)

This is the [Play](http://www.playframework.com) application that powers the web presence of the [Vienna Scala User Group](http://scala-vienna.org)!

## Highlighted Features

- Integration with the [Meetup API](http://www.meetup.com/meetup_api/) to display events, photos, ...
- Create talk detail information in Markdown format and render it together with related tagged photos from meetup.com
- Aggregate UG member blogs via RSS feeds

## Getting started with development

- Grab and install [Play](http://www.playframework.com)
- Install the [Chrome tools](https://chrome.google.com/webstore/detail/play-framework-tools/dchhggpgbommpcjpogaploblnpldbmen) to get the auto-reloading benefits
- Check your meetup.com API key here http://www.meetup.com/meetup_api/key/
- Create an environment variable with `export MEETUP_API_KEY=<your API key>`
- Add "Personal Access Tokens" here https://github.com/settings/applications
- Create an environment variable with `export GITHUB_API_KEY=<your API key>`
- Run Play with `play ~ run` to enable continuous reloading during development

## LICENSE

- This software is licensed under the terms of the Apache 2.0 License  which you find in the file named `LICENSE.txt` in this repository.
- **Aggregated content** (blog posts, talk slides, ...) are copyright to their respective owners.