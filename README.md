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
- Configure the Meetup API authentication (see [Getting a Meetup API Key](https://secure.meetup.com/meetup_api/key/)):
  - `export MEETUP_API_KEY=<your API key>`
- Optional: configure a different Meetup group ID in the meetup.groupId key in the application.conf file
- Configure GitHub API authentication. It you are part of the [Scala Vienna GitHub organization](https://github.com/scala-vienna) you will be able to access 
the `client` id and the `client secret` keys in the ["Applications" section](https://github.com/organizations/scala-vienna/settings/applications/119475). 
Otherwise asks us to be added or generate your own keys.
  - `export GITHUB_AUTH_CLIENT_ID=<the GitHub application client id>`
  - `export GITHUB_AUTH_CLIENT_SECRET=<the GitHub application secret>`
- Use `activator` to start the app

## Deployment

We use [Travis CI](https://travis-ci.org/scala-vienna/scala-vienna-web) to automatically deploy to Heroku:
 
- **the master branch** to the **production** app: http://www.scala-vienna.org
- **the develop branch** to the **dev/test/stage** app: http://dev.scala-vienna.org

# Feature Pipeline

We have even more cool stuff coming up! ;-) https://github.com/scala-vienna/scala-vienna-web/issues?state=open

## License

- This software is licensed under the terms of the Apache 2.0 License  which you find in the file named `LICENSE.txt` in this repository.
- **Aggregated content** (blog posts, talk slides, ...) are copyright to their respective owners.
