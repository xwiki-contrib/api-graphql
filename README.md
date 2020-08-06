# api-graphql
GraphQL API to expose the XWiki model for usecases where REST is too verbose or produces too much back and forward between the client and the server.

## Usage

Once installed you can request XWiki using curl:
```
curl -X POST -H "Content-Type: application/json" -d '{"query": "{ document(documentReference: \"Main.WebHome\") { title, content }}"}' http://localhost:8080/xwiki/graphql
```

You can also check the whole schema by going on:
```
http://localhost:8080/xwiki/graphql/graphql.schema
```