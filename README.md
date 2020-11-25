# GraphQL API
GraphQL API to expose the XWiki model for usecases where REST is too verbose or produces too much back and forward between the client and the server.

## Usage

Once installed you can request XWiki using curl:
```
curl -X POST -H "Content-Type: application/json" -d '{"query": "{ document(documentReference: \"Main.WebHome\") { title, content }}"}' http://localhost:8080/xwiki/graphql
```

You can also check the whole schema by going on:
```
http://localhost:8080/xwiki/graphql/schema.graphql
```

* Project Lead: [Eduard Moraru](https://xwiki.org/xwiki/bin/view/XWiki/enygma) 
* [Documentation & Download](https://extensions.xwiki.org/xwiki/bin/view/Extension/XWiki%20GraphQL%20API/)
* [Issue Tracker](https://jira.xwiki.org/browse/GQL)
* Communication: [Forum](https://forum.xwiki.org/), [IRC](https://dev.xwiki.org/xwiki/bin/view/Community/Chat) 
* [Development Practices](http://dev.xwiki.org/) 
* Minimal XWiki version supported: 12.6 
* License: LGPL 2.1
* Translations: N/A
* Sonar Dashboard: N/A
* Continuous Integration Status: N/A
