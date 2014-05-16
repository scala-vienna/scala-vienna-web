# Scala Vienna UG Webapp

[![Build Status](https://travis-ci.org/scala-vienna/scala-vienna-web.png)](https://travis-ci.org/scala-vienna/scala-vienna-web)

This is the [Play](http://www.playframework.com) application that powers the web presence of the [Vienna Scala User Group](http://scala-vienna.org)!

##Feature Highlights

- Integration with the [Meetup API](http://www.meetup.com/meetup_api/) to display events, photos, ...
- Talk detail pages rendered from Markdown files with metadata and related tagged photos from extracted from the Meetup API
- "Blog" section aggregates user group member blog entries via their RSS feeds

## Getting started with development

- Grab and install [Play](http://www.playframework.com)
- Install the [Chrome tools](https://chrome.google.com/webstore/detail/play-framework-tools/dchhggpgbommpcjpogaploblnpldbmen) to get the auto-reloading benefits
- Check your meetup.com API key here http://www.meetup.com/meetup_api/key/
- Create a "Personal Access Token" for the app on your GitHub account: https://github.com/settings/applications
- Create the following environment variables:
  - `export MEETUP_API_KEY=<your API key>`
  - `export GITHUB_API_KEY=<your GITHUB application access token>`
- Optional: configure a different Meetup group ID in the meetup.groupId key in the application.conf file
- Run Play with `play ~ run` to enable continuous reloading during development

## Deployment

- We use [Travis CI](https://travis-ci.org/scala-vienna/scala-vienna-web) to automagically deploy **the master branch** to Heroku.

# Feature Pipeline

We have even more cool stuff coming up! ;-) https://github.com/scala-vienna/scala-vienna-web/issues?state=open

## License

- This software is licensed under the terms of the Apache 2.0 License  which you find in the file named `LICENSE.txt` in this repository.
- **Aggregated content** (blog posts, talk slides, ...) are copyright to their respective owners.
